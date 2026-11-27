package hmgapi.mods.projection.carplay;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;

import com.tcc.iap2.Iap2NotiMsg;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.LinkedHashMap;

import hmgapi.hook.Car;
import hmgapi.hook.Cluster;
import hmgapi.hook.Points;
import hmgapi.mods.projection.ProjectionTbt;

/**
 * CarPlay turn-by-turn on the cluster and HUD: a port of the turn-by-turn build's DiPO
 * route guidance manager (e/u, with its route update e/c and maneuver e/v), fed by
 * HMG-API's own iAP2 listener instead of DiPO's, and sending through HMG-API's Cluster API.
 * DiPO's own manager only exists when stepofspec is 999 or more, and stays off.
 *
 * The iPhone sends RouteGuidanceUpdate (120: state, distances, the current maneuver
 * list), RouteGuidanceManeuverUpdate (121: one maneuver by index) and LaneGuidance
 * (122), each for a display component, often field by field. Kept per component as in
 * DiPO. What it sends, as the build did:
 *   - the turn (updateTBTInfo: icon, distance in the phone's units), crossroad name and,
 *     when state is 6, the destination name
 *   - the HUD list (0xF2 frame: icon, next icon, road) and the HUD distance (2C)
 *   - remaining distance and arrival time (updateTBTSubInfo, in the owner's units)
 *   - up to 3 upcoming turns (updateTPTurnList, owner's units) and lanes (0xF1 frame)
 * Also from the build: when the source app changes (Apple Maps to Waze, say), what the
 * old app left is cleared and route guidance restarted, so the phone resends all of it.
 * Not ported: DiPO's per-region icon filters (China, Europe, Middle East, Australia,
 * Brazil); on US and Canada cars the build sends every icon.
 * One thread: every call comes from the same handler.
 */
final class CpTurns {
    final Context context;
    final Cluster cluster;
    /** restarts the phone's route guidance updates (Stop, then Start) on a thread of its own */
    final Runnable restart;
    /** component id -> its route guidance state (DiPO e/u.Al) */
    final LinkedHashMap<Integer, Update> updates = new LinkedHashMap<Integer, Update>();
    /** component id -> maneuver index -> maneuver (DiPO e/u.Am) */
    final LinkedHashMap<Integer, LinkedHashMap<Integer, Maneuver>> maneuvers = new LinkedHashMap<Integer, LinkedHashMap<Integer, Maneuver>>();
    String source;
    final JunctionSpeed junctionSpeed = new JunctionSpeed();
    String lastJunctionTrial;
    boolean enlargementLinkageFailed;
    private final TollDisplay tollDisplay=new TollDisplay();
    private final FacilityContext facilityContext=new FacilityContext();
    private final JunctionTarget junctionTarget=new JunctionTarget();
    final MapJunctionDisplay mapDisplay=new MapJunctionDisplay();
    long lastMapRequest;
    float mapRequestedMeters;
    int mapRequestedId,lastPublishedTarget;
    boolean mapLeaseLive, mapTickQueued, mapTickRunning;
    long lastPhoneTurnAt;
    private final Runnable refreshMap=new Runnable(){public void run(){
        mapTickQueued=false;
        if(!mapLeaseLive || !showingGuidance || SystemClock.elapsedRealtime()-lastPhoneTurnAt>10000) {
            requestMap(false);return;
        }
        mapTickRunning=true;
        try {for(Integer id:new java.util.ArrayList<Integer>(updates.keySet()))turn(id,updates.get(id));}
        finally {mapTickRunning=false;}
    }};
    /** last HUD list sent (type, next, road), so an unchanged one isn't resent */
    int hudType, hudNext;
    String hudRoad;

    CpTurns(Context context, Cluster cluster, Runnable restart) {
        this.context = context;
        this.cluster = cluster;
        this.restart = restart;
    }

    /** RouteGuidanceUpdate (DiPO e/u.c) */
    void routeGuidance(Iap2NotiMsg msg) {
        if (msg == null) return;
        sourceCheck(msg);
        int[] ids = componentIds(msg);
        if (ids == null) {
            for (Integer id : updates.keySet()) {
                Update u = updates.get(id);
                if (u != null) u.read(id, msg);
                turn(id, u);
                updateLaneDisplay(id, u);
            }
            return;
        }
        for (int k = 0; k < ids.length; k++) {
            Integer id = ids[k];
            Update u = updates.get(id);
            if (u != null) {
                u.read(id, msg);
            } else {
                u = new Update(id, msg);
                updates.put(id, u);
            }
            turn(id, u);
            updateLaneDisplay(id, u);
        }
    }

    /** the components a RouteGuidanceUpdate is for (item 0); null if it doesn't say */
    static int[] componentIds(Iap2NotiMsg msg) {
        Iap2NotiMsg.Iap2ObjGroup g = msg.getItemGroup();
        if (g == null) return null;
        int[] ids = null;
        for (int k = 0; k < g.getItemCnt(); k++) {
            Iap2NotiMsg.Iap2ObjItem it = g.getObjItem(k);
            if (it == null) return null;
            if (it.getItemParamType() == 5 && it.getItemId() == 0 && it.getItemObjCount() > 0) ids = it.getItemIntArray();
        }
        return ids;
    }

    /**
     * RouteGuidanceManeuverUpdate (DiPO e/u.d): item 0 the components, item 1 the index.
     * Stored under the index the message carries (the build's fix, by Isaac Wisdom: stock
     * wrote the index into a copy and stored every maneuver under -1, so none was found).
     */
    void maneuver(Iap2NotiMsg msg) {
        if (msg == null) return;
        int index = -1;
        int[] ids = null;
        Iap2NotiMsg.Iap2ObjGroup g = msg.getItemGroup();
        if (g != null) {
            int[] found = null;
            boolean whole = true;
            for (int k = 0; k < g.getItemCnt(); k++) {
                Iap2NotiMsg.Iap2ObjItem it = g.getObjItem(k);
                if (it == null) {
                    whole = false;
                    break;
                }
                int id = it.getItemId();
                if (it.getItemObjCount() <= 0 || (id != 0 && id != 1)) continue;
                int[] v = it.getItemIntArray();
                if (v == null || v.length == 0) continue;
                if (id == 0) found = v;
                else index = v[0];
            }
            if (whole) ids = found;
        }
        if (index < 0) return;
        if (ids == null) {
            for (Integer id : maneuvers.keySet()) {
                LinkedHashMap<Integer, Maneuver> m = maneuvers.get(id);
                if (m == null) continue;
                Maneuver x = m.get(index);
                if (x != null) x.read(index, id, msg);
                else m.put(index, new Maneuver(index, id, msg));
                retryTurn(id, index);
            }
            return;
        }
        for (int k = 0; k < ids.length; k++) {
            int id = ids[k];
            LinkedHashMap<Integer, Maneuver> m = maneuvers.get(id);
            if (m != null) {
                Maneuver x = m.get(index);
                if (x != null) x.read(index, id, msg);
                else m.put(index, new Maneuver(index, id, msg));
            } else {
                m = new LinkedHashMap<Integer, Maneuver>();
                m.put(index, new Maneuver(index, id, msg));
                maneuvers.put(id, m);
            }
            retryTurn(id, index);
        }
    }

    private void retryTurn(int id, int index) {
        Update u = updates.get(id);
        if (u == null || u.state <= 0 || u.list == null) return;
        if (u.list.length == 1 && index == adjacentNext(u)) {
            turn(id, u);
            return;
        }
        for (int listed : u.list) {
            if (listed == index) {
                turn(id, u);
                return;
            }
        }
    }

    /** Naver sends contiguous maneuver IDs in each route snapshot. */
    private int adjacentNext(Update u) {
        return adjacentIndex(source, u.state, u.list, u.listCountAtSelection);
    }

    static int adjacentIndex(String source, int state, int[] list, int count) {
        // SourceName can arrive only when the first route ends. Do not block a
        // received adjacent maneuver solely because that optional name is absent.
        if (source != null && !source.isEmpty()
                && !"네이버지도".equals(source) && !"TMAP".equals(source)
                && !"카카오내비".equals(source)) return -1;
        if (state != 1 || list == null || list.length != 1 || list[0] < 0) return -1;
        int next = list[0] + 1;
        return next > 0 && next < count ? next : -1;
    }

    static int startupMeters(Update route, Maneuver current) {
        if (route.hasNextDistance && route.distNext >= 0) return route.distNext;
        // The maneuver packet carries its own explicit distance before the
        // route-level countdown arrives. An absent field must never become 0 m.
        if (current.hasDistance && current.distance >= 0) return current.distance;
        return -1;
    }

    private static final class LaneFrame {
        final int component, index;
        final byte[] bytes;
        final long receivedAt = SystemClock.elapsedRealtime();

        LaneFrame(int component, int index, byte[] bytes) {
            this.component = component;
            this.index = index;
            this.bytes = bytes;
        }
    }

    private LaneFrame pendingLanes, activeLanes;
    private long lastLaneSentAt;
    private final Runnable expireLanes = new Runnable() {
        @Override
        public void run() {
            clearLaneDisplay();
        }
    };

    private void setLaneLease(long until) {
        try {
            Settings.Global.putLong(context.getContentResolver(), ProjectionTbt.LANE_LEASE, until);
        } catch (RuntimeException e) {
            Dipo.error("CarPlay lane lease", e);
        }
    }

    private void clearLaneDisplay() {
        if (activeLanes == null) return;
        Dipo.turnsHandler.removeCallbacks(expireLanes);
        byte[] empty = new byte[81];
        empty[0] = (byte) 0xF1;
        cluster.laneInfo(empty);
        setLaneLease(0);
        Dipo.log("CarPlay: lane display released index=" + activeLanes.index);
        activeLanes = null;
        lastLaneSentAt = 0;
    }

    private void updateLaneDisplay(int component, Update route) {
        if (route == null) return;
        boolean visible = route.state > 0 && route.lanesShown;
        if (activeLanes != null && activeLanes.component == component
                && (!visible || activeLanes.index != route.laneIndex)) clearLaneDisplay();
        LaneFrame pending = pendingLanes;
        long now = SystemClock.elapsedRealtime();
        if (pending != null && now - pending.receivedAt > 2000) {
            pendingLanes = null;
        } else if (pending != null && pending.component == component && pending.index == route.laneIndex && visible) {
            activeLanes = pending;
            pendingLanes = null;
            lastLaneSentAt = 0;
            Dipo.log("CarPlay: lane display active index=" + pending.index);
        }
        if (activeLanes != null && activeLanes.component == component
                && activeLanes.index == route.laneIndex && visible && now - lastLaneSentAt >= 1000) {
            setLaneLease(System.currentTimeMillis() + ProjectionTbt.LANE_LEASE_MS);
            byte[] displayed = activeLanes.bytes;
            try {
                displayed = StockLaneHints.merge(displayed,
                        Settings.Global.getString(context.getContentResolver(), StockLaneHints.KEY), now);
            } catch (RuntimeException ignored) {
                // Fall back to the phone's unchanged frame if factory hints are unavailable.
            }
            if (displayed[2] != activeLanes.bytes[2])
                Dipo.log("CarPlay: pocket markers merged index=" + activeLanes.index
                        + " markers=" + (displayed[2] & 255));
            cluster.laneInfo(displayed);
            Dipo.turnsHandler.removeCallbacks(expireLanes);
            Dipo.turnsHandler.postDelayed(expireLanes, ProjectionTbt.LANE_LEASE_MS);
            lastLaneSentAt = now;
        }
    }

    /** LaneGuidance (the build's applyLanes): up to 8 lanes as the cluster's 0xF1 frame */
    void lanes(Iap2NotiMsg msg) {
        Iap2NotiMsg.Iap2ObjGroup g = msg == null ? null : msg.getItemGroup();
        if (g == null) return;
        int[] components = componentIds(msg);
        if (components == null || components.length == 0) return;
        Dipo.log("CarPlay: lane input root " + inputFields(g));
        int laneIndex = -1;
        int[] status = new int[8], a = new int[8], b = new int[8], c = new int[8];
        int possibleMask = 0, preferredMask = 0;
        int n = 0;
        for (int k = 0; k < g.getItemCnt(); k++) {
            Iap2NotiMsg.Iap2ObjItem it = g.getObjItem(k);
            if (it != null && it.getItemParamType() == 5 && it.getItemId() == 1) {
                int[] values = it.getItemIntArray();
                if (values != null && values.length > 0) laneIndex = values[0];
            }
            if (n == 8 || it == null || it.getItemParamType() != 13 || it.getItemId() != 2) continue;
            Iap2NotiMsg.Iap2ObjGroup[] lanes = it.getItemGroupArray();
            if (lanes == null) continue;
            for (Iap2NotiMsg.Iap2ObjGroup lane : lanes) {
                if (lane == null) continue;
                int[] one = laneOne(lane);
                status[n] = one[0];
                a[n] = one[1];
                b[n] = one[2];
                c[n] = one[3];
                if (one[4] != 0) possibleMask |= 1 << n;
                if (one[5] != 0) preferredMask |= 1 << n;
                n++;
                if (n == 8) break;
            }
        }
        byte[] f = new byte[81];
        f[0] = (byte) 0xF1;
        f[1] = (byte) n;
        for (int k = 0; k < n; k++) {
            f[k + 3] = (byte) status[k];
            f[k * 3 + 19] = (byte) a[k];
            f[k * 3 + 20] = (byte) b[k];
            f[k * 3 + 21] = (byte) c[k];
        }
        // 0x4B/0x4D are stock hipass/nonHipass masks, NOT usable/preferred lanes.
        // Per-direction possibility is already packed into f[3..10].
        int passable = Integer.bitCount(possibleMask);
        Dipo.log("CarPlay: lane frame index=" + laneIndex
                + " lanes=" + n + " passable=" + passable
                + " statuses=" + java.util.Arrays.toString(java.util.Arrays.copyOf(status, n)));
        if (laneIndex < 0) return;
        int component = components[0];
        pendingLanes = new LaneFrame(component, laneIndex, f);
        updateLaneDisplay(component, updates.get(component));
    }

    /** Lane glyph type, three glyph parts, and usable/preferred flags. */
    static int[] laneOne(Iap2NotiMsg.Iap2ObjGroup g) {
        if (g == null) return new int[6];
        int receivedStatus = -1;
        int[] angles = null, highlight = null;
        for (int k = 0; k < g.getItemCnt(); k++) {
            Iap2NotiMsg.Iap2ObjItem it = g.getObjItem(k);
            if (it == null) continue;
            int t = it.getItemParamType();
            if (t != 5 && t != 8) continue;
            int[] v = it.getItemIntArray();
            if (v == null || v.length == 0) continue;
            int id = it.getItemId();
            if (id == 1) {
                receivedStatus = v[0];
            } else if (id == 2) {
                angles = v;
            } else if (id == 3) {
                highlight = v;
            }
        }
        int[] encoded = LaneGlyphs.encode(receivedStatus, angles, highlight);
        Dipo.log("CarPlay: lane detail inputStatus=" + receivedStatus
                + " angles=" + java.util.Arrays.toString(angles)
                + " highlightedAngles=" + java.util.Arrays.toString(highlight)
                + " inputFields=" + inputFields(g)
                + " partColors=" + encoded[0]
                + " glyphParts=" + java.util.Arrays.toString(java.util.Arrays.copyOfRange(encoded, 1, 4)));
        return encoded;
    }

    /** Preserve lane ID / optional fields too, without guessing pocket-lane semantics. */
    static String packetSource(Iap2NotiMsg.Iap2ObjGroup group) {
        for (int k = 0; group != null && k < group.getItemCnt(); k++) {
            Iap2NotiMsg.Iap2ObjItem item = group.getObjItem(k);
            if (item == null || item.getItemId() != 19 || item.getItemParamType() != 10
                    || item.getItemObjCount() <= 0) continue;
            String[] v = item.getItemStringArray();
            if (v != null && v.length > 0 && v[0] != null && !v[0].trim().isEmpty()) return v[0].trim();
        }
        return null;
    }

    static String focusedFields(Iap2NotiMsg.Iap2ObjGroup group) {
        return inputFields(group, false);
    }

    static String inputFields(Iap2NotiMsg.Iap2ObjGroup group) {
        return inputFields(group, true);
    }

    private static String inputFields(Iap2NotiMsg.Iap2ObjGroup group, boolean includeText) {
        StringBuilder out = new StringBuilder();
        for (int k = 0; group != null && k < group.getItemCnt() && k < 24; k++) {
            Iap2NotiMsg.Iap2ObjItem item = group.getObjItem(k);
            if (item == null) continue;
            int type = item.getItemParamType();
            out.append(" id=").append(item.getItemId()).append("/type=").append(type)
                    .append("/count=").append(item.getItemObjCount());
            if (type == 1 || type == 2 || type == 5 || type == 6 || type == 8 || type == 9) {
                int[] values = item.getItemIntArray();
                if (values != null) out.append("/values=").append(java.util.Arrays.toString(
                        java.util.Arrays.copyOf(values, Math.min(values.length, 16))));
            } else if (type == 3 || type == 7) {
                long[] values = item.getItemLongArray();
                if (values != null) out.append("/values=").append(java.util.Arrays.toString(
                        java.util.Arrays.copyOf(values, Math.min(values.length, 16))));
            } else if (type == 10 && includeText) {
                String[] values = item.getItemStringArray();
                if (values != null && values.length > 0 && values[0] != null) {
                    String value = values[0];
                    out.append("/text=").append(value.substring(0, Math.min(value.length(), 240)));
                }
            }
        }
        return out.toString();
    }

    static int laneIcon(int angle) {
        return LaneGlyphs.angleIcon(angle);
    }

    // Route setup/recalculation can briefly report inactive before the next active frame.
    private boolean showingGuidance;
    private String lastTurnTrace;
    private boolean tripRefreshLogged;
    private final Runnable clearInactiveRoute = new Runnable() {
        @Override
        public void run() {
            stop("confirmed inactive");
        }
    };

    final CpSubscriptionRecovery subscriptionRecovery=new CpSubscriptionRecovery();
    private final Runnable resumeSubscription=new Runnable(){public void run(){
        if(subscriptionRecovery.resume(SystemClock.elapsedRealtime()))Dipo.resumeRouteGuidance();
    }};
    private final Runnable expireSubscription=new Runnable(){public void run(){
        long now=SystemClock.elapsedRealtime();
        if(subscriptionRecovery.expired(now))stop("subscription recovery timed out without fresh guidance");
        else if(subscriptionRecovery.pending()){
            setTurnLease(System.currentTimeMillis()+2500);
            Dipo.turnsHandler.postDelayed(this,Math.min(1000,subscriptionRecovery.remaining(now)));
        }
    }};
    private void cancelSubscriptionRecovery(){
        subscriptionRecovery.clear();
        Dipo.turnsHandler.removeCallbacks(resumeSubscription);
        Dipo.turnsHandler.removeCallbacks(expireSubscription);
    }
    void subscriptionStopped(){
        long now=SystemClock.elapsedRealtime();
        if(!subscriptionRecovery.begin(now,showingGuidance,lastPhoneTurnAt)){
            stop("subscription stopped without fresh route");return;
        }
        Dipo.log("CarPlay: subscription stopped; retaining recent route for bounded recovery");
        Dipo.turnsHandler.removeCallbacks(resumeSubscription);
        Dipo.turnsHandler.postDelayed(resumeSubscription,250);
        // Do not extend the first deadline if unsubscribe callbacks repeat.
        Dipo.turnsHandler.removeCallbacks(expireSubscription);
        setTurnLease(System.currentTimeMillis()+2500);
        Dipo.turnsHandler.postDelayed(expireSubscription,Math.min(1000,subscriptionRecovery.remaining(now)));
    }

    /** The iAP2 session ended. */
    void disconnected() {
        stop("iAP2 disconnected");
        source = null;
        Dipo.experimentalSourceChanged(null);
    }

    void stop(String reason) {
        Dipo.log("CarPlay: route stop reason="+reason);
        cancelSubscriptionRecovery();
        tollDisplay.clear();facilityContext.clear();junctionTarget.clear();mapDisplay.clear();
        requestMap(false);
        Dipo.turnsHandler.removeCallbacks(refreshMap);mapTickQueued=false;
        Dipo.turnsHandler.removeCallbacks(clearInactiveRoute);
        pendingLanes = null;
        clearLaneDisplay();
        lastTrip = null;
        lastAheadDistance = null;
        setTurnLease(0);
        tripOutput = null;
        if (distanceLeaseRenewAt != 0) {
            setDistanceLease(0);
            distanceLeaseRenewAt = 0;
        }
        tripRefreshLogged = false;
        lastTurnTrace = null;
        lastRouteStateTrace = null;
        Dipo.turnsHandler.removeCallbacks(refreshTrip);
        reset();
        if (!showingGuidance) return;
        showingGuidance = false;
        // The LX2 PE native navigation ends its display sequence with TBT type 8.
        cluster.destinationName(0, "");
        cluster.tbtInfo(8, 0, 0f, 0f, -1, 0, 0, 0, -1);
        cluster.tbtSubInfo(0f, -1, 0, 0, 0, 0, 0, 0f, -1, 0);
        hudBlank();
        outputState("clear sent type=8 (" + reason + ")");
    }

    /** forget every component's state and maneuvers (DiPO e/u.nT) */
    void reset() {
        for (LinkedHashMap<Integer, Maneuver> m : maneuvers.values()) {
            if (m != null) m.clear();
        }
        for (Update u : updates.values()) {
            if (u != null) u.clear();
        }
    }

    void mapSettingsChanged() {
        mapDisplay.clear();lastJunctionTrial=null;requestMap(false);
        for(Integer id:new java.util.ArrayList<Integer>(updates.keySet()))turn(id,updates.get(id));
    }
    private int mapRequestedKind;
    private void requestMap(boolean wanted) {
        long now=SystemClock.elapsedRealtime();
        if(!wanted && !mapLeaseLive)return;
        if(wanted && mapLeaseLive && mapRequestedId==lastPublishedTarget && now-lastMapRequest<1000) {
            if(!mapTickQueued){mapTickQueued=true;Dipo.turnsHandler.postDelayed(refreshMap,1000);}
            return;
        }
        try {
            Settings.Global.putString(context.getContentResolver(),StockMapJunction.DETAILS,
                    wanted?now+","+mapRequestedMeters+","+mapRequestedId+","+mapRequestedKind:null);
            Settings.Global.putLong(context.getContentResolver(),StockMapJunction.REQUEST,wanted?now+5000:0);
            lastPublishedTarget=mapRequestedId;
            mapLeaseLive=wanted;lastMapRequest=now;
            if(wanted && !mapTickQueued) {
                mapTickQueued=true;Dipo.turnsHandler.postDelayed(refreshMap,1000);
            }
            if(!wanted){Dipo.turnsHandler.removeCallbacks(refreshMap);mapTickQueued=false;}
        } catch(RuntimeException ignored) { /* Optional map refresh cannot stop phone guidance. */ }
    }

    /** SourceName (item 19) changed: clear the old app's leftovers and have the phone resend */
    void sourceCheck(Iap2NotiMsg msg) {
        Iap2NotiMsg.Iap2ObjGroup g = msg.getItemGroup();
        if (g == null) return;
        for (int k = 0; k < g.getItemCnt(); k++) {
            Iap2NotiMsg.Iap2ObjItem it = g.getObjItem(k);
            if (it == null || it.getItemId() != 19 || it.getItemParamType() != 10 || it.getItemObjCount() <= 0) continue;
            String[] v = it.getItemStringArray();
            String s = v == null || v.length == 0 ? null : v[0];
            if (s == null || s.length() == 0) continue;
            if ("Apple 지도".equals(s)) s = "Apple Maps";
            String was = source;
            source = s;
            if (!s.equals(was)) Dipo.experimentalSourceChanged(s);
            if (was == null) {
                Dipo.log("CarPlay: guidance source=" + s);
                return;
            }
            if (was.equals(s)) return;
            Dipo.log("CarPlay: route guidance source changed from " + was + " to " + s + ", clearing turn-by-turn");
            stop("guidance source changed");
            if (explicitUnsupportedSource(g)) {
                Dipo.log("CarPlay: source explicitly unsupported; source-switch refresh skipped");
            } else if (restart != null) new Thread(restart, "hmgapi-cp-rg-restart").start();
            return;
        }
    }

    /** Only an explicit false in this source-change packet vetoes refresh. */
    private static boolean explicitUnsupportedSource(Iap2NotiMsg.Iap2ObjGroup group) {
        for (int k = 0; k < group.getItemCnt(); k++) {
            Iap2NotiMsg.Iap2ObjItem item = group.getObjItem(k);
            if (item == null || item.getItemId() != 20 || item.getItemParamType() != 9
                    || item.getItemObjCount() <= 0) continue;
            int[] value = item.getItemIntArray();
            if (value != null && value.length > 0) return value[0] == 0;
        }
        return false;
    }

    private String lastOutputState;
    private String lastRouteStateTrace;
    private void outputState(String state) {
        if (state.equals(lastOutputState)) return;
        lastOutputState = state;
        Dipo.log("CarPlay: cluster path " + state);
    }

    /** one component's update to the cluster (DiPO e/u.a(Integer, Object), with the build's changes) */
    void turn(Integer id, Update u) {
        if (id < 0 || u == null) return;
        String routeTrace = "CarPlay route state source=" + u.sourceName + " component=" + id + " state=" + u.state
                + " sourceSupportsGuidance=" + (u.hasSupports ? String.valueOf(u.supports) : "absent")
                + " maneuverIndexes=" + java.util.Arrays.toString(u.list);
        if (!routeTrace.equals(lastRouteStateTrace)) {
            Dipo.log(routeTrace);
            lastRouteStateTrace = routeTrace;
        }
        LinkedHashMap<Integer, Maneuver> available = maneuvers.get(id);
        boolean missing = u.state > 0 && (u.list == null || u.list.length == 0
                || available == null || available.get(u.list[0]) == null);
        Dipo.experimentalIncompleteWaze(source, missing, u.hasSupports && !u.supports);
        if (u.state < 0) {
            outputState("waiting for route state component=" + id);
            return;
        }
        if (u.state == 0) {
            cancelSubscriptionRecovery();
            outputState("inactive component=" + id + " state=" + u.state +
                    " turnCount=" + (u.list == null ? 0 : u.list.length));
            Dipo.turnsHandler.removeCallbacks(clearInactiveRoute);
            Dipo.turnsHandler.postDelayed(clearInactiveRoute, 500);
            return;
        }
        Dipo.turnsHandler.removeCallbacks(clearInactiveRoute);
        int[] list = u.list == null ? null : u.list.clone();
        float dist = Float.NaN;
        if (u.nextStr != null && !u.nextStr.equals("")) {
            try {
                dist = Float.parseFloat(u.nextStr);
                if (Float.isNaN(dist) || Float.isInfinite(dist) || dist < 0f) dist = Float.NaN;
            } catch (NumberFormatException e) {
                dist = Float.NaN;
            }
        }
        showingGuidance = true;
        if (u.state == 6) cluster.destinationName(0, u.destination);
        sendTrip(u);
        LinkedHashMap<Integer, Maneuver> m = maneuvers.get(id);
        if (list == null || list.length == 0) {
            outputState("waiting for turn list");
            return;
        }
        Maneuver x = m == null ? null : m.get(list[0]);
        if (x == null) {
            outputState("waiting for maneuver");
            return;
        }
        int unit;
        switch (u.nextUnits) {
            case 0: unit = Cluster.UNIT_KM; break;
            case 1: unit = Cluster.UNIT_MI; break;
            case 3: unit = Cluster.UNIT_YD; break;
            case 4: unit = Cluster.UNIT_FT; break;
            default: unit = Cluster.UNIT_M; break;
        }
        if (Float.isNaN(dist)) {
            int meters = startupMeters(u, x);
            if (meters < 0) {
                outputState("waiting for current maneuver distance");
                return;
            }
            dist = meters;
            unit = Cluster.UNIT_M;
        }
        int icon = maneuverIcon(x, u.state);
        String facilityHint=facilityContext.resolve(x.type,x.junction,x.exit,x.description,x.afterRoad,JunctionHints.meters(dist,unit),SystemClock.elapsedRealtime());
        int facilityOverride=FacilityGlyphs.road(x.type,x.junction,x.exit,facilityHint,null);
        if(facilityOverride!=0)icon=facilityOverride;
        int adjacent = adjacentNext(u);
        if (adjacent >= 0 && x.type != 10 && x.type != 12 && x.type != 27 && m.containsKey(adjacent)) {
            list = new int[] { list[0], adjacent };
        }
        Maneuver next = list.length > 1 ? m.get(list[1]) : null;
        String trace = "index=" + list[0] + " type=" + x.type + " side=" + x.side + " junction=" + x.junction
                + " exit=" + (x.hasExit ? String.valueOf(x.exit) : "missing") + " icon=" + icon
                + " selectedList=" + java.util.Arrays.toString(list) + " count=" + list.length
                + x.facilityTrace() + " next=" + (next == null ? "missing"
                        : next.type + "/" + next.exit + "/" + next.distance + next.facilityTrace());
        if (!trace.equals(lastTurnTrace)) {
            Dipo.log("CarPlay 1.0.0-rc.1 turn " + trace);
            lastTurnTrace = trace;
        }
        // Publish the name after the display frame below; some clusters gate names by display type.
        if (icon == 0) {
            outputState("no icon for maneuver type " + x.type + " junction=" + x.junction
                    + " side=" + x.side + " exit=" + x.exit);
            return;
        }
        outputState("sending turn to NavigationCluster");
        setTurnLease(System.currentTimeMillis() + 2500);
        if(!mapTickRunning){lastPhoneTurnAt=SystemClock.elapsedRealtime();cancelSubscriptionRecovery();}
        String junctionApp=source;
        if(junctionApp==null || junctionApp.isEmpty())junctionApp="CarPlay";
        boolean supported=StockMapJunction.allowedApp(junctionApp);
        float phoneMeters=JunctionHints.meters(dist,unit);
        boolean mapEnabled=!enlargementLinkageFailed && Dipo.mod.settingBoolean("stock_map_junction",true);
        String mapTurnToken=junctionTarget.select(junctionApp+"/"+id+"/"+x.type+"/"+x.junction+"/"+x.side+"/"+x.hasExit+"/"+x.exit,SystemClock.elapsedRealtime(),phoneMeters);
        mapRequestedKind=(x.junction==1||x.type==6||x.type==7)&&x.side==0?1:0;
        mapRequestedMeters=phoneMeters;mapRequestedId=StockMapJunction.targetId(mapTurnToken);
        requestMap(mapEnabled && StockMapJunction.requestedRange(junctionApp,phoneMeters,PhoneJunction.roadThreshold(x.type)));
        float[] enlarged = mapEnabled && supported && Dipo.mod.settingBoolean("toll_enlargement",true)
                ? tollDisplay.draw(icon,phoneMeters,SystemClock.elapsedRealtime()):null;
        if(icon!=178&&icon!=184)tollDisplay.clear();
        String mapState=enlarged!=null?"toll": "none";
        String mapReason="not-requested";
        try {
        if (mapEnabled) {
            // Source name is diagnostic only; every projection app uses the same validation.
            String app = junctionApp;
            boolean allowed = supported;
            boolean stockDistance = "road".equals(Dipo.mod.settingString("phone_junction_distance", "road"));
            float speed = allowed && !stockDistance ? junctionSpeed.read() : Float.NaN;
            int limit = stockDistance ? PhoneJunction.roadThreshold(x.type) : JunctionHints.threshold(speed);
            float meters = JunctionHints.meters(dist, unit);
            if(enlarged==null){enlarged=RampEnlargement.draw(x.type,x.junction,x.hasExit,x.exit,meters,limit);
                if(enlarged!=null){mapDisplay.clear();mapState="factory-exit";mapReason="phone-exit-kind-and-side";}}
            if(enlarged==null) {
                enlarged=GradeEnlargement.draw(icon,meters,limit);
                if(enlarged!=null){mapDisplay.clear();mapState="factory-grade";mapReason="phone-facility-text";}
            }
            if(enlarged==null && mapEnabled && allowed && ((icon>=65 && icon<=71)||(icon>=160 && icon<=165)||mapRequestedKind==1)
                    && !FacilityGlyphs.grade(String.valueOf(x.description)+" "+String.valueOf(x.afterRoad))) {
                try {
                    hmgapi.mods.projection.StockNavigationApi.Position position=
                            hmgapi.mods.projection.StockNavigationApi.currentPosition(context);
                    String geometry=Settings.Global.getString(context.getContentResolver(),StockMapJunction.KEY);
                    if(position!=null)enlarged=StockMapJunction.drawTarget(geometry,SystemClock.elapsedRealtime(),
                            position.longitude,position.latitude,position.heading,app,x.type,x.junction,
                            x.hasExit,x.exit,meters,limit,mapRequestedId);
                    mapReason=position==null?"position-stale-or-missing":StockMapJunction.diagnostic();
                    String turnToken=mapTurnToken;
                    enlarged=mapDisplay.apply(turnToken,StockMapJunction.node(),enlarged,
                            SystemClock.elapsedRealtime(),meters,limit,
                            position==null?Double.NaN:position.longitude,
                            position==null?Double.NaN:position.latitude);
                    mapState="stock-map-"+mapDisplay.state;
                    JunctionMatchTrace.record(context,turnToken,SystemClock.elapsedRealtime(),meters,
                            mapReason,mapDisplay.state,enlarged,position,geometry);
                } catch(RuntimeException ignored) { /* Original TBT remains available. */ }
            }
            if(enlarged==null&&mapRequestedKind==1){
                enlarged=RoundaboutEnlargement.draw(app,x.type,x.junction,x.side,x.hasExit,x.exit,null,meters,limit);
                if(enlarged!=null){mapState="rotary-direction-only";mapReason+="/physical-arms-unavailable";}
            }
            if (enlarged==null && allowed && ((icon >= 65 && icon <= 71) || (icon >= 160 && icon <= 165)))
                enlarged = PhoneJunction.drawAt(x.type, x.junction, x.hasExit, x.exit,
                        x.angles, meters, limit);
            String trial = "app=" + app + " source=" + source + " speed=" + speed
                    + " threshold=" + limit + " policy=" + (stockDistance ? "road" : "speed") + " distance=" + meters
                    + " hasExit=" + x.hasExit + " roads=" + (x.angles == null ? 0 : x.angles.length)
                    + " matched=" + (enlarged != null)+" method="+mapState+" reason="+mapReason
                    +" frame="+(enlarged==null?"none":java.util.Arrays.toString(enlarged));
            if (!trial.equals(lastJunctionTrial)) { Dipo.log("Phone junction " + trial); lastJunctionTrial=trial; }
        }
        } catch (RuntimeException | LinkageError error) {
            enlargementLinkageFailed=true;enlarged=null;mapDisplay.clear();
            try {requestMap(false);} catch(RuntimeException | LinkageError ignored) {}
            Dipo.error("CarPlay enlargement disabled after render failure; basic TBT retained",error);
        }
        if (enlarged != null) cluster.tbtInfo((int)enlarged[0], (int)enlarged[1], enlarged[2],
                enlarged[3], 0, (int)enlarged[5], 0, (int)enlarged[7], 0);
        else {
            int[] progress=TurnPresentation.bar(icon,x.type,phoneMeters);
            cluster.tbtInfo(icon,0,TurnPresentation.direction(icon,x.hasExit,x.exit),DisplayFormat.distance(dist,unit),
                    0,0,unit,progress[0],progress[1]);
        }
        cluster.crossroadName(x.displayRoad());
        sendHud(icon, id, list);
        sendHudDist(dist, unit, next);
        sendAhead(id, list);
    }

    static int maneuverIcon(Maneuver x, int state) {
        // Some apps send the roundabout maneuver type but omit junction/angle.
        // Keep text and distance live; an absent exit must not imply straight.
        if (x.type == 6 || x.type == 7) {
            return x.hasExit ? roundaboutAngleIcon(x.exit, x.side) : 64;
        }
        if (x.type == 11) return 64; // route start, no directional instruction
        int facility = facilityIcon(x);
        if (facility != 0) return facility;
        int icon = icon(x.type, x.side, x.junction, x.exit, state);
        if (icon != 0 || !x.hasExit || x.junction != 1 || x.side != 0) return icon;
        // Some apps describe the roundabout with the junction and angle alone.
        if (!(x.type == 0 || x.type == 1 || x.type == 2 || x.type == 3 || x.type == 6 || x.type == 7
                || x.type == 19 || (x.type >= 28 && x.type <= 46))) return 0;
        return roundaboutAngleIcon(x.exit, x.side);
    }

    static int roundaboutAngleIcon(int angle, int side) {
        if (angle < -180 || angle > 180 || side != 0) return 64;
        if (Math.abs(angle) >= 158) return 102;
        if (angle >= 113) return 100;
        if (angle >= 68) return 99;
        if (angle >= 23) return 97;
        if (angle >= -22) return 96;
        if (angle >= -67) return 106;
        if (angle >= -112) return 105;
        return 103;
    }

    private static int facilityIcon(Maneuver x) {
        if (x.junction != 0) return 0;
        int icon = x.roadFacilityIcon();
        return icon;
    }

    /**
     * The cluster icon for a maneuver: iAP2 ManeuverType, driving side (0 right-hand
     * traffic, 1 left), junction type (0 intersection, 1 roundabout) and exit angle, as
     * the turn-by-turn build maps them (0: nothing to show).
     */
    static int icon(int type, int side, int junction, int exit, int state) {
        int highway = FacilityGlyphs.highway(type, junction, exit);
        if (highway != 0) return highway;
        switch (type) {
            case 20:  // left at end of road
            case 1:   // left turn
                if (junction == 0 && exit == -120) return 69;
                if (side == 0 && junction == 0 && exit == -90) return 70;
                if (junction == 0 && exit == -60) return 71;
                if (junction == 0 && exit == -45) return 145;
                if (side == 1 && junction == 1 && exit == -120) return 104;
                if (side == 1 && junction == 1 && exit == -90) return 105;
                if (side == 1 && junction == 1 && exit == -30) return 107;
                return junction == 0 ? 70 : 0;
            case 21:  // right at end of road
            case 2:   // right turn
                if (junction == 0 && exit == 60) return 66;
                if (side == 0 && junction == 0 && exit == 90) return 67;
                if (junction == 0 && exit == 120) return 68;
                if (side == 0 && junction == 1 && exit == 0) return 96;
                if (side == 1 && junction == 1 && exit == 0) return 97;
                if (side == 1 && junction == 1 && exit == 90) return 99;
                if (side == 1 && junction == 1 && exit == 120) return 100;
                if (side == 1 && junction == 1 && exit == 180) return 102;
                if (side == 0 && junction == 0 && exit == 45) return 144;
                return junction == 0 ? 67 : 0;
            case 3:   // straight
                return junction == 0 ? 65 : 0;
            case 13:  // keep left
                return junction == 0 ? 71 : 0;
            case 14:  // keep right
                return junction == 0 ? 66 : 0;
            case 18:  // U-turn to rejoin route
            case 26:  // U-turn when possible
            case 4:   // U-turn
                if (side == 0 && junction == 0) return 72;
                if (side == 1 && junction == 0) return 73;
                return 0;
            case 5:
                return side == 0 && junction == 0 && exit == 0 ? 172 : 0;
            case 10: // end of navigation
            case 27: // end of directions
                return state == 2 ? 3 : 0;
            case 12: {   // arrive; state 2 (arrived): 3
                int i = side == 0 && junction == 0 ? 112 : 0;
                return state == 2 ? 3 : i;
            }
            case 15: return side == 0 && junction == 0 && exit == 0 ? 176 : 0;
            case 19: return side == 0 && junction == 1 && exit == -180 ? 102 : 0;
            case 24: return side == 0 && junction == 0 && exit == 0 ? 114 : 0;
            case 25: return side == 0 && junction == 0 && exit == 0 ? 113 : 0;
            case 28: return side == 0 && junction == 1 && exit == 135 ? 100 : 0;
            case 29: return side == 0 && junction == 1 && exit == 90 ? 99 : 0;
            case 31:
                if (side == 0 && junction == 1 && exit == 0) return 97;
                if (side == 1 && junction == 1 && exit == 0) return 96;
                return 0;
            case 32: return side == 0 && junction == 1 && exit == -45 ? 104 : 0;
            case 33: return side == 0 && junction == 1 && exit == -90 ? 105 : 0;
            case 34: return side == 0 && junction == 1 && exit == -135 ? 104 : 0;
            case 35: return side == 0 && junction == 1 && exit == -180 ? 102 : 0;
            case 47:
                if (junction == 0 && exit <= -135 && exit >= -150) return 69;
                if (junction == 0 && exit == -30) return 71;
                return junction == 0 ? 69 : 0;
            case 48:
                if (junction == 0 && exit == 30) return 66;
                if (junction == 0 && exit >= 135 && exit <= 150) return 68;
                return junction == 0 ? 68 : 0;
            case 49: return junction == 0 ? 71 : 0;
            case 50: return junction == 0 ? 66 : 0;
            default: return 0;
        }
    }

    /** an upcoming turn's small icon (HUD list and turn list): 64 none, 65 straight, 66-68 right, 69-71 left, 72/73 U-turn */
    static int aheadIcon(Maneuver x) {
        int facility = facilityIcon(x);
        if (facility != 0) return facility;
        if (x.junction == 1) {
            int roundabout = maneuverIcon(x, 1);
            if (roundabout >= 96 && roundabout <= 107) return roundabout;
        }
        int type = x.type, side = x.side, junction = x.junction, exit = x.exit;
        if (type == 1) {
            if (exit == -120 && junction == 0) return 69;
            if (exit == -90 && side == 0 && junction == 0) return 70;
            if (exit == -60 && junction == 0) return 71;
            return 70;
        }
        if (type == 2) {
            if (exit == 60 && junction == 0) return 66;
            if (exit == 90 && side == 0 && junction == 0) return 67;
            if (exit == 120 && junction == 0) return 68;
            return 67;
        }
        if (type == 3) return 65;
        if (type == 4 || type == 18 || type == 26) return side == 0 ? 72 : 73;
        if (junction == 0 && type == 13) return 71;
        if (junction == 0 && type == 14) return 66;
        if (type == 8 || type == 9 || type == 22 || type == 23 || type == 52 || type == 53) {
            int ramp = icon(type, side, junction, exit, 1);
            if (ramp != 0) return ramp;
        }
        int mapped = maneuverIcon(x, 1);
        return mapped >= 64 ? mapped : 64;
    }

    /** the HUD's list frame (0xF2): count, this icon, the next turn's icon, 0, road after this turn (UTF-16LE) */
    void sendHud(int type, Integer id, int[] list) {
        if (type < 64) return;
        int next = 0;
        String road = "";
        LinkedHashMap<Integer, Maneuver> m = maneuvers.get(id);
        if (m != null && list != null && list.length > 0) {
            Maneuver x = m.get(list[0]);
            if (x != null) road = x.displayRoad();
            if (list.length > 1) {
                Maneuver y = m.get(list[1]);
                if (y != null) {
                    int nextIcon = aheadIcon(y);
                    if (nextIcon != 64) next = nextIcon;
                }
            }
        }
        if (hudType == type && hudNext == next && hudRoad != null && hudRoad.equals(road)) return;
        byte[] r;
        try {
            r = road.getBytes("UTF-16LE");
        } catch (java.io.UnsupportedEncodingException e) {
            return;
        }
        int n = Math.min(r.length, 78);
        byte[] f = new byte[n + 5];
        f[0] = (byte) 0xF2;
        f[1] = (byte) (next != 0 ? 2 : 1);
        f[2] = (byte) type;
        f[3] = (byte) next;
        System.arraycopy(r, 0, f, 5, n);
        Dipo.log("CarPlay: hud list type " + type + " next " + next);
        cluster.turnListNew(f);
        hudType = type;
        hudNext = next;
        hudRoad = road;
    }

    /** the HUD's distance (updateTBTSubInfo2C: unit, tenths for km/mi, whole up to 4095) */
    void sendHudDist(float f, int unit, Maneuver next) {
        f=DisplayFormat.distance(f,unit);
        if (f < 0f || unit < 0 || unit > 4) return;
        int whole = (int) f;
        if (whole > 4095) whole = 4095;
        int tenths = 0;
        if (unit == Cluster.UNIT_KM || unit == Cluster.UNIT_MI) {
            tenths = (int) ((f - whole) * 10f + 0.5f);
            if (tenths < 0) tenths = 0;
            if (tenths > 9) tenths = 9;
        }
        int nextUnit = 15, nextTenths = 15, nextWhole = 65535;
        if (hudNext != 0 && next != null && next.distance > 0) {
            int meters = next.distance;
            nextUnit = meters < 1000 ? Cluster.UNIT_M : Cluster.UNIT_KM;
            nextTenths = meters < 1000 || meters>=10000 ? 0 : (meters % 1000) / 100;
            nextWhole = meters < 1000 ? meters : Math.min(4095, meters / 1000);
        }
        cluster.tbtSubInfo2C(unit, tenths, whole, nextUnit, nextTenths, nextWhole, 15, 15, 65535);
    }

    /** HUD list and extras cleared (the build's sendHudBlank) */
    void hudBlank() {
        cluster.tbtSubInfo2C(0, 0, 0, 0, 0, 0, 0, 0, 0);
        byte[] lanes = new byte[81];
        lanes[0] = (byte) 0xF1;
        cluster.laneInfo(lanes);
        hudType = -1;
        hudNext = 0;
        hudRoad = null;
        byte[] empty = { (byte) 0xF2 };
        cluster.turnListNew(empty);
        cluster.turnListNew(empty);
    }

    /**
     * Remaining distance and arrival time (updateTBTSubInfo). Distance in the owner's
     * units (Settings), else the phone's. Arrival: a clock time (seconds since 2001 or
     * 1970) or, failing that, now + the time remaining (up to a week).
     */
    private String lastTrip;
    private Runnable tripOutput;
    private long distanceLeaseRenewAt;

    private void setTurnLease(long until) {
        try {
            Settings.Global.putLong(context.getContentResolver(),
                    "hmgapi_lx2pe_carplay_turn_until", until);
        } catch (RuntimeException e) {
            Dipo.error("CarPlay turn lease", e);
        }
    }

    private void setDistanceLease(long expiresAt) {
        try {
            if (!Settings.Global.putLong(context.getContentResolver(),
                    "hmgapi_lx2pe_carplay_distance_until", expiresAt)) {
                Dipo.log("CarPlay: distance lease write failed");
            }
        } catch (RuntimeException e) {
            Dipo.error("CarPlay: distance lease write", e);
        }
    }

    private void renewDistanceLease() {
        long now = SystemClock.elapsedRealtime();
        if (now < distanceLeaseRenewAt) return;
        distanceLeaseRenewAt = now + 3000;
        setDistanceLease(System.currentTimeMillis() + 5000);
    }
    private final Runnable refreshTrip = new Runnable() {
        @Override
        public void run() {
            if (!showingGuidance || tripOutput == null) return;
            tripOutput.run();
            if (!tripRefreshLogged) {
                Dipo.log("CarPlay 1.0.5 trip refresh running");
                tripRefreshLogged = true;
            }
            Dipo.turnsHandler.postDelayed(this, 750);
        }
    };

    void sendTrip(Update u) {
        int d = u.distLeft, units = u.distLeftUnits;
        int whole = 0, tenths = 0, unit = Cluster.UNIT_M;
        if (d > 0) {
            whole = d;
            int owner = Car.distanceUnit(context);
            if (owner == Car.DISTANCE_KM) units = d < 1000 ? 2 : 0;
            else if (owner == Car.DISTANCE_MILES) units = d < 161 ? 4 : 1;
            switch (units) {
                case 0:
                    whole = d / 1000;
                    tenths = (d % 1000) / 100;
                    unit = Cluster.UNIT_KM;
                    break;
                case 1: {
                    int t = d * 10 / 1609;
                    tenths = t % 10;
                    whole = t / 10;
                    unit = Cluster.UNIT_MI;
                    break;
                }
                case 2:
                    break;
                case 3:
                    whole = d * 1094 / 1000;
                    unit = Cluster.UNIT_YD;
                    break;
                case 4:
                    whole = d * 3281 / 1000;
                    unit = Cluster.UNIT_FT;
                    break;
                default:
                    if (d >= 1000) {
                        whole = d / 1000;
                        tenths = (d % 1000) / 100;
                        unit = Cluster.UNIT_KM;
                    }
                    break;
            }
        }
        int hour = -1, minute = -1, timeType = -1, timeFormat;
        Calendar cal = null;
        long eta = u.eta;
        if (eta < 500000000L) {
            long left = u.timeLeft;
            if (left > 0 && left <= 604800L) {
                cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, (int) left);
            }
        } else {
            if (eta < 1500000000L) eta += 978307200L;   // Apple's 2001 epoch
            cal = Calendar.getInstance();
            cal.setTimeInMillis(eta * 1000L);
        }
        if (cal != null) {
            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);
            timeType = 1;
            timeFormat = DisplayFormat.arrivalFormat(Dipo.mod==null?"24":Dipo.mod.settingString("arrival_time_format","24"),context);
        } else {
            timeFormat = -1;
        }
        if (d <= 0) return;
        String trip = whole + ":" + tenths + ":" + (d > 0) + ":" + unit + ":" +
                hour + ":" + minute + ":" + timeType + ":" + timeFormat;
        if (trip.equals(lastTrip)) return;
        lastTrip = trip;
        final float distance = DisplayFormat.distance(whole + tenths / 10f,unit);
        final int distanceUnit = unit, arrivalHour = hour, arrivalMinute = minute;
        final int arrivalType = timeType, arrivalFormat = timeFormat;
        tripOutput = new Runnable() {
            @Override
            public void run() {
                renewDistanceLease();
                cluster.tbtSubInfo(distance, 0, distanceUnit, arrivalHour, arrivalMinute,
                        arrivalType, arrivalFormat, 0f, -1, 0);
            }
        };
        Dipo.log("CarPlay 1.0.5 trip distance=" + distance + " distanceErr=0 unit=" + distanceUnit);
        tripOutput.run();
        Dipo.turnsHandler.removeCallbacks(refreshTrip);
        Dipo.turnsHandler.postDelayed(refreshTrip, 750);
    }

    private String lastAheadDistance;

    /** up to 3 upcoming turns after this one (updateTPTurnList), distances in the owner's units */
    void sendAhead(Integer id, int[] list) {
        if (id == null || list == null || list.length <= 1) return;
        LinkedHashMap<Integer, Maneuver> m = maneuvers.get(id);
        if (m == null) return;
        int count = 0;
        for (int k = 1; k < list.length && count < 3; k++) {
            Maneuver x = m.get(list[k]);
            if (x != null && x.distance > 0) count++;
        }
        if (count == 0) return;
        int owner = Car.distanceUnit(context);
        Cluster.Turn[] turns = new Cluster.Turn[count];
        int n = 0, firstIndex = -1;
        for (int k = 1; k < list.length && n < 3; k++) {
            Maneuver x = m.get(list[k]);
            if (x == null || x.distance <= 0 || n >= count) continue;
            Cluster.Turn t = new Cluster.Turn();
            t.type = aheadIcon(x);
            t.direction = 0;
            t.traffic = 0;
            t.no = n + 1;
            t.name = x.displayRoad();
            t.exitNumber = "";
            int d = x.distance;
            float f = d;
            int unit = Cluster.UNIT_M;
            if (owner == Car.DISTANCE_KM) {
                if (d >= 1000) {
                    unit = Cluster.UNIT_KM;
                    f = d / 1000f;
                }
            } else if (owner == Car.DISTANCE_MILES) {
                if (d < 161) {
                    unit = Cluster.UNIT_FT;
                    f = d * 3.28084f;
                } else {
                    unit = Cluster.UNIT_MI;
                    f = d / 1609f;
                }
            }
            if (f < 0f) f = 0f;
            if (f > 65535f) f = 65535f;
            t.distance = f;
            t.unit = unit;
            if (n == 0) firstIndex = list[k];
            turns[n++] = t;
        }
        if (n > 0) {
            Cluster.Turn first = turns[0];
            String distance = "current=" + list[0] + " next=" + firstIndex + " distance="
                    + first.distance + " unit=" + first.unit + " position=" + first.no;
            if (!distance.equals(lastAheadDistance)) {
                Dipo.log("TBT ahead distance " + distance);
                lastAheadDistance = distance;
            }
            cluster.turnList(turns);
        }
    }

    /** one display component's route guidance state (DiPO e/c) */
    static final class Update {
        int state = -1, maneuverState, distLeft, distLeftUnits, distNext, nextUnits, count, laneIndex, laneCount;
        String road, destination, distLeftStr, nextStr, sourceName;
        long eta, timeLeft;
        int[] list;
        int listCountAtSelection;
        boolean visible, lanesShown, supports, hasSupports, hasNextDistance;

        /** as DiPO: a component id of 0 or less starts with nothing set (not even the defaults) */
        Update(int id, Iap2NotiMsg msg) {
            clear();
            if (msg == null || id < 0) return;
            read(id, msg);
        }

        void clear() {
            state = -1;
            maneuverState = 1;
            road = "";
            destination = "";
            eta = 0;
            timeLeft = 0;
            distLeft = 0;
            distLeftStr = "";
            distLeftUnits = 0;
            distNext = 0;
            hasNextDistance = false;
            nextStr = "";
            nextUnits = 0;
            list = null;
            listCountAtSelection = 0;
            count = 0;
            visible = false;
            laneIndex = 0;
            laneCount = 0;
            lanesShown = false;
            sourceName = "";
            supports = false;
            hasSupports = false;
        }

        /** the fields this message carries (by iAP2 type: 1/5 uint8/16, 2/6 uint32, 3/7 uint64, 8 enum, 9 bool, 10 string) */
        void read(int id, Iap2NotiMsg msg) {
            Iap2NotiMsg.Iap2ObjGroup g = msg == null ? null : msg.getItemGroup();
            if (g == null) return;
            final int previousIndex = list == null || list.length == 0 ? -1 : list[0];
            boolean numericReceived = false, formattedReceived = false, unitsReceived = false;
            for (int k = 0; k < g.getItemCnt(); k++) {
                Iap2NotiMsg.Iap2ObjItem it = g.getObjItem(k);
                if (it == null) continue;
                int item = it.getItemId(), n = it.getItemObjCount();
                switch (it.getItemParamType()) {
                    case 1:
                    case 5: {
                        int[] v = n > 0 ? it.getItemIntArray() : null;
                        switch (item) {
                            case 13:
                                if (v != null) {
                                    list = java.util.Arrays.copyOf(v, Math.min(n, v.length));
                                } else {
                                    list = null;
                                }
                                break;
                            case 14: count = v != null && v.length > 0 ? v[0] : 0; break;
                            case 16: laneIndex = v != null && v.length > 0 ? v[0] : 0; break;
                            case 17: laneCount = v != null && v.length > 0 ? v[0] : 0; break;
                        }
                        break;
                    }
                    case 2:
                    case 6: {
                        int[] v = n > 0 ? it.getItemIntArray() : null;
                        if (v == null || v.length == 0) break;
                        if (item == 7) distLeft = v[0];
                        else if (item == 10) {
                            distNext = v[0];
                            hasNextDistance = true;
                            numericReceived = true;
                        }
                        break;
                    }
                    case 3:
                    case 7: {
                        long[] v = n > 0 ? it.getItemLongArray() : null;
                        if (v == null) break;
                        if (item == 5) eta = v[0];
                        else if (item == 6) timeLeft = v[0];
                        break;
                    }
                    case 8: {
                        int[] v = it.getItemIntArray();
                        if (v == null || v.length == 0) break;
                        switch (item) {
                            case 1: state = v[0]; break;
                            case 2: maneuverState = v[0]; break;
                            case 9: distLeftUnits = v[0]; break;
                            case 12: nextUnits = v[0]; unitsReceived = true; break;
                        }
                        break;
                    }
                    case 9: {
                        int[] v = n > 0 ? it.getItemIntArray() : null;
                        if (v == null) break;
                        if (item == 15) visible = v[0] != 0;
                        else if (item == 18) lanesShown = v[0] != 0;
                        else if (item == 20) { supports = v[0] != 0; hasSupports = true; }
                        break;
                    }
                    case 10: {
                        String[] v = n > 0 ? it.getItemStringArray() : null;
                        if (v == null) break;
                        switch (item) {
                            case 3: road = v[0]; break;
                            case 4: destination = v[0]; break;
                            case 8: distLeftStr = v[0]; break;
                            case 11: nextStr = v[0]; formattedReceived = true; break;
                            case 19: sourceName = v[0]; break;
                        }
                        break;
                    }
                }
            }
            final int currentIndex = list == null || list.length == 0 ? -1 : list[0];
            if (!CpDistancePolicy.retainNumeric(previousIndex, currentIndex, numericReceived))
                hasNextDistance = false;
            if (!CpDistancePolicy.retainFormatted(previousIndex, currentIndex,
                    numericReceived, formattedReceived, unitsReceived)) nextStr = "";
            if (list != null) {
                listCountAtSelection = list == null ? 0 : count;
            }
        }
    }

    /** one maneuver (DiPO e/v) */
    static final class Maneuver {
        int index, type, distance, distUnits, side, junction, exit, linkedLane;
        String description, afterRoad, distStr;
        int[] angles;
        boolean hasExit, hasDistance;

        /** Log facility hints without exporting route text. */
        String facilityTrace() {
            return " tollText=" + tollText() + " restText=" + restText()
                    + " underpassText=" + FacilityGlyphs.under(String.valueOf(description)+" "+String.valueOf(afterRoad))
                    + " overpassText=" + FacilityGlyphs.over(String.valueOf(description)+" "+String.valueOf(afterRoad))
                    + " roadsideText=" + (hasRoadText("옆길") || hasRoadText("옆 길") || hasRoadText("측도"))
                    + " description=" + description + " afterRoad=" + afterRoad
                    + " angles=" + java.util.Arrays.toString(angles);
        }

        String displayRoad() {
            String road = afterRoad == null ? "" : afterRoad;
            if ((type == 6 || type == 7) && !hasExit) {
                String action = type == 6 ? "회전교차로 진입" : "회전교차로 진출";
                return road.trim().isEmpty() ? action : action + " · " + road;
            }
            if (!road.trim().isEmpty()) return road;
            if (hasRoadText("지하차도") && !hasRoadText("고가차도")) return "지하차도";
            if (hasRoadText("고가차도") && !hasRoadText("지하차도")) return "고가차도";
            if (restText() && !tollText()) return hasRoadText("졸음쉼터") ? "졸음쉼터" : "휴게소";
            if (junction == 0 && (type == 8 || type == 22 || type == 23)) return "고속도로 진출";
            if (junction == 0 && (type == 9 || type == 52 || type == 53)) return "고속도로 진입";
            return road;
        }

        private boolean hasRoadText(String token) {
            return (description != null && description.contains(token))
                    || (afterRoad != null && afterRoad.contains(token));
        }

        int roadFacilityIcon() {
            return FacilityGlyphs.road(type, junction, exit, description, afterRoad);
        }

        private boolean tollText() {
            return hasTollText(description) || hasTollText(afterRoad);
        }

        private boolean restText() {
            return hasRestText(description) || hasRestText(afterRoad);
        }

        private static boolean hasTollText(String text) {
            return text != null && (text.contains("요금소") || text.contains("톨게이트")
                    || text.contains("자동징수") || text.matches("(?is).*\\bTG\\b.*")
                    || text.matches("(?is).*TG(?:\\s|[()/,·].*|$)"));
        }

        private static boolean hasRestText(String text) {
            return text != null && (text.contains("휴게소") || text.contains("졸음쉼터"));
        }

        Maneuver(int index, int id, Iap2NotiMsg msg) {
            if (msg == null) return;
            clear();
            read(index, id, msg);
        }

        void clear() {
            index = -1;
            description = "";
            type = 0;
            afterRoad = "";
            distance = 0;
            hasDistance = false;
            distStr = "";
            distUnits = 2;
            side = 0;
            junction = 0;
            angles = null;
            exit = 0;
            linkedLane = 0;
        }

        /** as DiPO, the exit angle resets with every update (it's only kept if this one carries it) */
        void read(int index, int id, Iap2NotiMsg msg) {
            Iap2NotiMsg.Iap2ObjGroup g = msg.getItemGroup();
            this.index = index;
            exit = 0;
            hasExit = false;
            java.util.ArrayList<Integer> receivedAngles = null;
            for (int k = 0; k < g.getItemCnt(); k++) {
                Iap2NotiMsg.Iap2ObjItem it = g.getObjItem(k);
                int item = it.getItemId(), n = it.getItemObjCount();
                switch (it.getItemParamType()) {
                    case 1: {
                        int[] v = n > 0 ? it.getItemIntArray() : null;
                        if (item == 10) {
                            if (v != null) {
                                if (receivedAngles == null) receivedAngles = new java.util.ArrayList<Integer>();
                                for (int j = 0; j < n && j < v.length; j++) receivedAngles.add(v[j]);
                                angles = new int[receivedAngles.size()];
                                for (int j = 0; j < angles.length; j++) angles[j] = receivedAngles.get(j);
                            } else {
                                angles = null;
                            }
                        } else if (item == 11 && v != null && v.length > 0) {
                            exit = v[0];
                            hasExit = true;
                        }
                        break;
                    }
                    case 5: {
                        int[] v = it.getItemIntArray();
                        if (item == 12 && v != null && v.length > 0) linkedLane = v[0];
                        break;
                    }
                    case 6: {
                        int[] v = n > 0 ? it.getItemIntArray() : null;
                        if (item == 5 && v != null && v.length > 0) {
                            distance = v[0];
                            hasDistance = true;
                        }
                        break;
                    }
                    case 8: {
                        int[] v = n > 0 ? it.getItemIntArray() : null;
                        if (v == null) break;
                        switch (item) {
                            case 3: type = v[0]; break;
                            case 7: distUnits = v[0]; break;
                            case 8: side = v[0]; break;
                            case 9: junction = v[0]; break;
                        }
                        break;
                    }
                    case 10: {
                        String[] v = it.getItemStringArray();
                        if (v == null) break;
                        switch (item) {
                            case 2: description = v[0]; break;
                            case 4: afterRoad = v[0]; break;
                            case 6: distStr = v[0]; break;
                        }
                        break;
                    }
                }
            }
        }
    }
}
