package hmgapi.mods.projection.carplay;

import android.content.ComponentName;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.tcc.iap2.IIap2Listener;
import com.tcc.iap2.IIap2Service;
import com.tcc.iap2.Iap2ListenerInfo;
import com.tcc.iap2.Iap2NotiMsg;

import hmgapi.hook.Car;
import hmgapi.hook.Cluster;
import hmgapi.hook.LoadedApp;
import hmgapi.hook.MethodHook;
import hmgapi.hook.ModContext;
import hmgapi.hook.Points;

public final class Dipo {
    private Dipo() {}

    static final String IAP2_ACTION = "com.remote.service.IAP2SERVICE";
    /** iAP2 listener type for route guidance (notifications 120, 121, 122) */
    static final int LISTENER_ROUTE_GUIDANCE = 4096;
    static final int RG_UPDATE = 120, RG_MANEUVER = 121, RG_LANES = 122;
    static final String[] RG_COMPONENTS = { "RouteGuidanceComponent0" };

    private static SessionJournal journal;
    static ModContext mod;
    static LoadedApp app;
    static volatile IIap2Service iap2;
    static boolean tbt = true;
    static Handler turnsHandler;
    static CpTurns turns;
    static RgListener rg;
    private static final CpSessionLifecycle session = new CpSessionLifecycle();
    private static long lastSubscriptionAt = -10000;
    private static int lastSubscriptionGeneration = -1;
    private static int experimentalStartupGeneration = -1;
    private static int receivedPacketGeneration = -1;
    private static final CpWazeRecovery wazeRecovery = new CpWazeRecovery();
    /** set while HMG-API itself calls the iAP2 server (its own route guidance restart) */
    static final ThreadLocal<Boolean> OWN = new ThreadLocal<Boolean>();

    static void log(String m) {
        mod.log(m);
        if (journal != null) journal.record(m);
        if(app!=null)CpInputTrace.record(app.context,m);
    }

    static void error(String m, Throwable t) {
        mod.error(m, t);
        if (journal != null) journal.record(m + " error=" + t.getClass().getSimpleName());
        if(app!=null)CpInputTrace.record(app.context,m+" error="+t);
    }

    public static void mapSettingsChanged(final ModContext context) {
        if(mod==null || turnsHandler==null || turns==null)return;
        mod=context;
        log("Projection enlargement settings enabled="+mod.settingBoolean("stock_map_junction",true));
        turnsHandler.post(new Runnable(){public void run(){turns.mapSettingsChanged();}});
    }
    /** Wireless.appLoaded, in DiPO's process */
    public static void start(ModContext m, LoadedApp a) {
        mod = m;
        app = a;
        if (a.context != null) journal = new SessionJournal(a.context, "stbt-carplay-sessions");
        turnByTurn();
        if (!tbt) return;
        // Use lifecycle tracking only when the matching finalization hook is available.
        // Otherwise retain the upstream disconnect behavior on older maps.
        final boolean lifecycle = Points.has("dipo.carplay.session_finalized");
        if (lifecycle) {
            mod.hook(app.classLoader, "dipo.carplay.session_finalized", new MethodHook() {
                @Override protected void beforeHookedMethod(Param p) {
                    session.end();
                    log("CarPlay: session finalized generation=" + session.generation());
                    if (journal != null) journal.end("session finalized");
                    post(new Runnable() { public void run() {
                        if (!session.active()) turns.disconnected();
                    }});
                }
            });
            try {
                a.context.registerReceiver(new BroadcastReceiver() {
                    @Override public void onReceive(Context c, Intent i) {
                        if ("com.tcc.iap2.connectedsuccess".equals(i.getAction())
                                && i.getIntExtra("com.telechips.android.iap2.component", -1) == 5
                                && session.wireless()) requestSubscription("component 5 authenticated", 1000);
                    }
                }, new IntentFilter("com.tcc.iap2.connectedsuccess"));
            } catch (RuntimeException e) {
                error("CarPlay: component 5 observer unavailable", e);
            }
        }
        mod.hook(app.classLoader, "dipo.carplay.session_started", new MethodHook() {
            @Override protected void afterHookedMethod(Param p) {
                if (p.getThrowable() != null) return;
                session.start(p.args.length > 0 && Integer.valueOf(1).equals(p.args[0]), lifecycle);
                final int generation = session.generation();
                post(new Runnable() { public void run() {
                    if (session.current(generation)) turns.disconnected();
                }});
                if (journal != null) journal.start("transport=" + (p.args.length > 0 ? p.args[0] : "unknown"));
                log("CarPlay: session started generation=" + session.generation() + " wireless=" + session.wireless());
                requestSubscription("session started", 1000);
            }
        });
        Intent i = new Intent(IAP2_ACTION);
        boolean bound = a.context.bindService(i, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder b) {
                IIap2Service s = IIap2Service.Stub.asInterface(b);
                iap2 = s;
                if (tbt) {
                    registerRouteGuidance(s);
                    if (session.active()) requestSubscription("iAP2 bound", 0);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                iap2 = null;
                final int generation = session.generation();
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (session.generation() == generation && iap2 == null) turns.disconnected();
                    }
                });
                log("DiPO: iAP2 server gone");
            }
        }, Context.BIND_AUTO_CREATE);
        log("DiPO: iAP2 server " + (bound ? "binding" : "can't be bound"));
    }

    // Verified against the US DL322 com.tcc.iap2.odex Binder signatures.
    // Reflection keeps older compile-time stubs and firmware variants compatible.
    private static void traceComponents(IIap2Service service, String reason) {
        try {
            Class<?> api = IIap2Service.class;
            int count = ((Number)api.getMethod("Iap2SvcGetRouteGuidanceRegisteredComponentCount").invoke(service)).intValue();
            int selected = ((Number)api.getMethod("Iap2SvcGetRouteGuidanceComponentIdByName", String.class)
                    .invoke(service, RG_COMPONENTS[0])).intValue();
            log("CarPlay registered guidance components reason=" + reason + " count=" + count
                    + " requestedName=" + RG_COMPONENTS[0] + " resolvedId=" + selected);
            if (count <= 0 || count > 32) return;
            int[] ids = new int[count];
            java.util.Arrays.fill(ids, -1);
            String[] names = new String[count];
            Object result = api.getMethod("Iap2SvcGetRouteGuidanceComponentNameIdList", int[].class, String[].class)
                    .invoke(service, ids, names);
            log("CarPlay guidance component list result=" + result + " ids="
                    + java.util.Arrays.toString(ids) + " names=" + java.util.Arrays.toString(names));
        } catch (Exception e) {
            log("CarPlay guidance component enumeration unavailable: " + e);
        }
    }

    // Serialized on the guidance worker; never perform a Binder request in a stock callback.
    private static void requestSubscription(final String reason, long delay) {
        final int generation = session.generation();
        turnsHandler.postDelayed(new Runnable() {
            @Override public void run() {
                if (!mod.enabled() || !session.current(generation)) return;
                IIap2Service service = iap2;
                if (service == null) return; // onServiceConnected handles a late bind
                long now = android.os.SystemClock.elapsedRealtime();
                if (lastSubscriptionGeneration == generation && now - lastSubscriptionAt < 750) return;
                lastSubscriptionGeneration = generation;
                lastSubscriptionAt = now;
                experimentalStartup(generation);
                traceComponents(service, reason);
                if (!session.current(generation) || iap2 != service) return;
                try {
                    log("CarPlay: " + reason + " route update request=" +
                            service.Iap2SvcStartRouteGuidanceUpdate(RG_COMPONENTS, true, true));
                } catch (Throwable e) { error("CarPlay: " + reason + " route update request", e); }
            }
        }, delay);
    }

    // Opt-in only. A silent session gets one Start-only refresh; an identified Waze
    // source with incomplete guidance gets at most two refreshes per source activation.
    // Never invent a maneuver or override an explicit unsupported-source flag.
    private static boolean experimentalEnabled() {
        return mod.enabled() && mod.settingBoolean("experimental_waze_recovery", false);
    }

    private static void experimentalStartup(final int generation) {
        if (!experimentalEnabled() || experimentalStartupGeneration == generation) return;
        experimentalStartupGeneration = generation;
        turnsHandler.postDelayed(new Runnable() { public void run() {
            if (!experimentalEnabled() || !session.current(generation)
                    || receivedPacketGeneration == generation) return;
            requestSubscription("experimental silent-session refresh", 0);
        }}, 5000);
    }

    static void experimentalSourceChanged(String source) {
        wazeRecovery.sourceChanged(session.generation(), source);
    }

    static void experimentalIncompleteWaze(String source, boolean missing, boolean explicitlyUnsupported) {
        if (!experimentalEnabled()) { wazeRecovery.cancel(); return; }
        final int generation = session.generation();
        if (!session.current(generation)) { wazeRecovery.cancel(); return; }
        final int token = wazeRecovery.observe(generation, source, missing, explicitlyUnsupported);
        if (token < 0) return;
        turnsHandler.postDelayed(new Runnable() { public void run() {
            if (!session.current(generation) || !experimentalEnabled()) return;
            int attempt = wazeRecovery.fire(generation, token);
            if (attempt == 0) return;
            requestSubscription("experimental Waze incomplete guidance attempt=" + attempt, 0);
            experimentalIncompleteWaze(wazeRecovery.source(), true, false);
        }}, wazeRecovery.delay());
    }

    private static void turnByTurn() {
        if (Car.stepOfSpec() >= 999) {
            log("CarPlay: stepofspec is 999 or more, DiPO's own turn-by-turn runs (HMG-API's stays off)");
            tbt = false;
            return;
        }
        Cluster c = Cluster.of(app.context);
        if (c == null) {
            log("CarPlay: cluster service unavailable");
            tbt = false;
            return;
        }
        HandlerThread t = new HandlerThread("hmgapi-cp-tbt");
        t.start();
        turnsHandler = new Handler(t.getLooper());
        turns = new CpTurns(app.context, c, new Runnable() {
            @Override
            public void run() {
                restartRouteGuidance();
            }
        });
        rg = new RgListener();
        mod.hook(app.classLoader, "dipo.iap2_proxy.stop_route_guidance", new MethodHook() {
            @Override
            protected void beforeHookedMethod(Param p) {
                if (OWN.get() != null) return;
                post(new Runnable() {
                    @Override
                    public void run() {
                        turns.subscriptionStopped();
                    }
                });
            }
        });
        log("Standalone TBT 1.0.0-rc.1: route guidance listener enabled");
    }

    static void post(Runnable r) {
        final Runnable run = r;
        turnsHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    run.run();
                } catch (Throwable x) {
                    error("CarPlay turn-by-turn", x);
                }
            }
        });
    }

    static void registerRouteGuidance(IIap2Service s) {
        try {
            Iap2ListenerInfo info = new Iap2ListenerInfo();
            info.onSetSupportType(LISTENER_ROUTE_GUIDANCE);
            info.onSetListenerName("HMGAPI_RG");
            log("CarPlay: Iap2SvcInit(HMGAPI_RG, 0x1000) = " + s.Iap2SvcInit(rg, info));
        } catch (Throwable t) {
            error("CarPlay: route guidance listener", t);
        }
    }

    static void resumeRouteGuidance() {
        IIap2Service service=iap2;
        if(service==null){log("CarPlay: subscription recovery unavailable; iAP2 gone");return;}
        try {log("CarPlay: subscription recovery Start-only result="+
                service.Iap2SvcStartRouteGuidanceUpdate(RG_COMPONENTS,true,true));}
        catch(Throwable error){error("CarPlay subscription recovery",error);}
    }

    /** after a source change: Stop, then Start, so the phone sends its whole current state (the build's HmgApiRgRestart) */
    static void restartRouteGuidance() {
        IIap2Service s = iap2;
        if (s == null || !mod.enabled()) return;
        final int generation = session.generation();
        OWN.set(Boolean.TRUE);
        try {
            s.Iap2SvcStopRouteGuidanceUpdate(RG_COMPONENTS);
            Thread.sleep(200);
            if (session.generation() != generation || iap2 != s || !mod.enabled()) return;
            s.Iap2SvcStartRouteGuidanceUpdate(RG_COMPONENTS, true, true);
        } catch (Throwable t) {
            // the session went away meanwhile
        } finally {
            OWN.remove();
        }
    }

    /** HMG-API's iAP2 listener for route guidance: everything goes to CpTurns on its thread */
    static final class RgListener extends IIap2Listener.Stub {
        private int seenTypes;
        private final String[] lastFields = new String[3];
        private final long[] lastTraceAt = new long[3];
        private int traceGeneration = -1;
        @Override
        public void connected(int component, String serial) {
            log("CarPlay: iAP2 listener connected component=" + component);
        }

        @Override
        public void disconnected(final int component) {
            // as DiPO's listener: component 2 isn't a session, and the bridge's own sessions end while wireless CarPlay runs
            log("CarPlay: iAP2 listener disconnected component=" + component + " preserved=" + session.ignoreDisconnect(component));
            if (session.ignoreDisconnect(component)) return;
            final int generation = session.generation();
            post(new Runnable() {
                @Override
                public void run() {
                    if (session.generation() == generation && !session.ignoreDisconnect(component)) turns.disconnected();
                }
            });
        }

        @Override
        public void notify(final int what, int a1, int a2, final Iap2NotiMsg msg) {
            if ((what != RG_UPDATE && what != RG_MANEUVER && what != RG_LANES) || msg == null) return;
            final int receivedGeneration = session.generation();
            post(new Runnable() {
                @Override
                public void run() {
                    if (session.generation() != receivedGeneration || !mod.enabled()) return;
                    if (receivedGeneration != 0 && !session.active()) return;
                    receivedPacketGeneration = receivedGeneration;
                    int typeBit = 1 << (what - RG_UPDATE);
                    if ((seenTypes & typeBit) == 0) {
                        seenTypes |= typeBit;
                        log("CarPlay: first route guidance event type=" + what);
                    }
                    // Trace changed decoded fields, with a periodic heartbeat for unchanged packets.
                    // The logger is independent of the decoder: diagnostics must not block navigation.
                    try {
                        if (mod.settingBoolean("verbose_diagnostics", false)) {
                        int generation = session.generation();
                        if (traceGeneration != generation) {
                            java.util.Arrays.fill(lastFields, null);
                            java.util.Arrays.fill(lastTraceAt, 0L);
                            traceGeneration = generation;
                        }
                        int slot = what - RG_UPDATE;
                        String fields = CpTurns.inputFields(msg.getItemGroup());
                        long now = android.os.SystemClock.elapsedRealtime();
                        if (!fields.equals(lastFields[slot])) {
                            log("CarPlay decoded packet=" + what + " generation=" + generation + fields);
                            lastFields[slot] = fields;
                            lastTraceAt[slot] = now;
                        }
                        }
                    } catch (Throwable diagnosticError) {
                        error("CarPlay packet trace", diagnosticError);
                    }
                    if (what == RG_UPDATE) turns.routeGuidance(msg);
                    else if (what == RG_MANEUVER) turns.maneuver(msg);
                    else turns.lanes(msg);
                }
            });
        }
    }

}
