package hmgapi.mods.projection;

import hmgapi.hook.Car;
import hmgapi.hook.LoadedApp;
import hmgapi.hook.MethodHook;
import hmgapi.hook.ModContext;
import hmgapi.hook.Points;

/** Android Auto callback integration adapted from HMG-API Wireless. */
final class AndroidAuto {
    static void install(final ModContext mod, final LoadedApp app) {
        final ClassLoader cl = app.classLoader;
        String[] required = {
            "gpm.navigation_status.init", "gpm.navigation_status.next_turn",
            "gpm.navigation_status.next_turn_distance", "gpm.navigation_status.status",
            "gpm.navigation_status.receiver", "gpm.cluster_type.image", "gpm.cluster_type.enum",
            "gpm.receiver.send_navigation_focus", "gpm.receiver.focus_native", "gpm.receiver.destroy"
        };
        for (String point : required) {
            if (!Points.has(point)) {
                mod.log("Android Auto TBT unavailable: missing map point " + point);
                return;
            }
        }
        if (Car.stepOfSpec() >= 999) {
            mod.log("Android Auto: stepofspec is 999 or more, GPM's own turn-by-turn runs (HMG-API's stays off)");
            return;
        }
        // the AA library: protocol 1.6 and the turn-by-turn extras packed into its callbacks, in memory (file stays stock)
        if (AaTbtSupport.supports(new java.io.File("/system/lib/libautoreceiver_infobank.so"))) {
            mod.nativePatch(app, "libautoreceiver_infobank.so");
        } else {
            // Keep the stock callback path available for basic guidance. Never force the expanded native parser.
            mod.log("Android Auto: expanded native parser skipped (unrecognized stock library); retaining basic callback guidance");
        }
        mod.hook(cl, "gpm.navigation_status.init", new MethodHook() {
            @Override
            protected void beforeHookedMethod(Param p) {
                if (p.args[3] == Points.get(cl, "gpm.cluster_type.image", null)) p.args[3] = Points.get(cl, "gpm.cluster_type.enum", null);
                AaInputTrace.record(app.context,"config",new Object[]{p.args[0],p.args[2],String.valueOf(p.args[3]),p.args[4],p.args[5],p.args[6]});
            }
        });
        mod.log("Android Auto TBT callback hooks enabled");
        final java.util.Map<Object, AaTurns> turns = new java.util.WeakHashMap<Object, AaTurns>();
        final android.os.HandlerThread thread = new android.os.HandlerThread("hmgapi-aa-tbt");
        thread.start();
        final android.os.Handler handler = new android.os.Handler(thread.getLooper());
        abstract class OnTurns extends MethodHook {
            abstract void run(AaTurns t, Object[] a);

            /** receiver: the GalReceiver; create: make its AaTurns if there's none yet */
            void post(final Object receiver, final boolean create, final Object[] args) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            AaTurns t;
                            synchronized (turns) {
                                t = turns.get(receiver);
                                if (t == null && create) {
                                    hmgapi.hook.Cluster c = hmgapi.hook.Cluster.of(app.context);
                                    if (c == null) return;
                                    turns.put(receiver, t = new AaTurns(app.context, c, handler, mod));
                                    final AaTurns active = t;
                                    t.renewDisplay = new Runnable() {
                                        @Override public void run() {
                                            long now = System.currentTimeMillis();
                                            if (active.displayType >= 64)
                                                lease("turn", now + 2500);
                                            if (active.tripMeters >= 0)
                                                lease("distance", now + 5000);
                                            if (active.laneKey != null && !active.laneKey.isEmpty())
                                                lease("lane", now + 12000);
                                        }
                                        private void lease(String name, long until) {
                                            try {
                                                android.provider.Settings.Global.putLong(app.context.getContentResolver(),
                                                    "hmgapi_lx2pe_carplay_" + name + "_until", until);
                                            } catch (RuntimeException error) {
                                                mod.error("Android Auto display lease", error);
                                            }
                                        }
                                    };
                                    t.releaseDisplay = new Runnable() {
                                        @Override public void run() {
                                            for (String name : new String[] {"turn", "distance", "lane"}) {
                                                try {
                                                    android.provider.Settings.Global.putLong(app.context.getContentResolver(),
                                                        "hmgapi_lx2pe_carplay_" + name + "_until", 0);
                                                } catch (RuntimeException error) {
                                                    mod.error("Android Auto display release", error);
                                                }
                                            }
                                        }
                                    };
                                    mod.log("Android Auto: turn-by-turn to the cluster for this connection");
                                }
                            }
                            if (t != null) OnTurns.this.run(t, args);
                        } catch (Throwable x) {
                            mod.error("Android Auto turn-by-turn", x);
                        }
                    }
                });
            }

            @Override
            protected void afterHookedMethod(Param p) {
                post(Points.get(cl, "gpm.navigation_status.receiver", p.thisObject), true, p.args.clone());
            }
        }
        mod.hook(cl, "gpm.navigation_status.next_turn", new OnTurns() {
            @Override
            void run(AaTurns t, Object[] a) {
                AaInputTrace.record(app.context,"turn",a);
                mod.log("Android Auto TBT turn event=" + a[2] + " side=" + a[1] + " angle=" + a[4]);
                mod.log("Android Auto TBT payload event=" + a[2] + " text=" + a[0]);
                String previousState = t.rawStateSummary;
                t.nextTurn((String) a[0], (Integer) a[1], (Integer) a[2], (Integer) a[4], (Integer) a[5]);
                if (t.rawStateSummary != null && !t.rawStateSummary.equals(previousState))
                    mod.log("Android Auto TBT raw state " + t.rawStateSummary);
                if ((Integer) a[2] < 32)
                    mod.log("Android Auto TBT output road=" + t.road);
            }
        });
        mod.hook(cl, "gpm.navigation_status.next_turn_distance", new OnTurns() {
            @Override
            void run(AaTurns t, Object[] a) {
                AaInputTrace.record(app.context,"distance",a);
                mod.log("Android Auto TBT distance meters=" + a[0] + " flags=" + a[3]);
                t.nextTurnDistance((Integer) a[0], (Integer) a[1], (Integer) a[2], (Integer) a[3]);
            }
        });
        mod.hook(cl, "gpm.navigation_status.status", new OnTurns() {
            @Override
            void run(AaTurns t, Object[] a) {
                AaInputTrace.record(app.context,"status",a);
                mod.log("Android Auto TBT status=" + a[0]);
                t.status((Integer) a[0]);
            }
        });
        mod.hook(cl, "gpm.receiver.send_navigation_focus", new OnTurns() {
            @Override
            void run(AaTurns t, Object[] a) {
                if (a[0].equals(Points.get(cl, "gpm.receiver.focus_native", null))) t.status(2);
            }

            @Override
            protected void afterHookedMethod(Param p) {
                post(p.thisObject, false, p.args.clone());
            }
        });
        mod.hook(cl, "gpm.receiver.destroy", new OnTurns() {
            @Override
            void run(AaTurns t, Object[] a) {
                t.end();
                synchronized (turns) {
                    turns.values().remove(t);
                }
            }

            @Override
            protected void beforeHookedMethod(Param p) {
                post(p.thisObject, false, new Object[0]);
            }

            @Override
            protected void afterHookedMethod(Param p) {}
        });
    }

}
