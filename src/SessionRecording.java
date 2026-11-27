package hmgapi.mods.projection.carplay;

import android.content.Context;
import android.os.SystemClock;
import java.io.File;
import java.util.Locale;

/** Directory recording adapter; root service alone publishes to USB. */
final class SessionRecording {
    private final SessionLogWriter writer;
    private final SessionLogPolicy policy;
    private final PreSessionEvents pre = new PreSessionEvents();
    private long generation, previousEnd;
    private boolean active;
    private final boolean focused;

    SessionRecording(Context context, String folder, boolean focused) {
        this.focused=focused;
        policy=new SessionLogPolicy(SessionLogPolicy.diagnosticCategories(true,false));
        writer=new SessionLogWriter(new File(context.getFilesDir(),folder),null,
                SessionLogPolicy.diagnosticCategories(true,false));
    }
    synchronized void beginAttempt(String detail) {
        end("superseded by next attempt");
        start(detail);
    }
    synchronized void start(String detail) {
        if(active)return;
        active=true;generation++;
        policy.reset();
        long now=System.currentTimeMillis();
        writer.start(generation,detail);
        for(PreSessionEvents.Event e:pre.drain(now,previousEnd))
            writer.recordAt(generation,SessionLogPolicy.Level.INFO,SessionLogPolicy.Category.CORE,"pre-session "+e.message,e.at);
    }
    synchronized void record(String message) { record(SessionLogPolicy.Level.INFO,message); }
    synchronized void error(String message) { record(SessionLogPolicy.Level.ERROR,message); }
    private void record(SessionLogPolicy.Level level,String message) {
        SessionLogPolicy.Category category=category(message);
        if(!active){
            if(!focused){
                long now=System.currentTimeMillis();
                pre.add(message,now,now,previousEnd);
            }
            return;
        }
        String safe=policy.accept(level,category,message,SystemClock.elapsedRealtime());
        writer.record(generation,level,category,safe);
    }
    synchronized void end(String reason) {
        if(!active)return;
        active=false;previousEnd=System.currentTimeMillis();
        writer.finishAt(generation,reason,policy.suppressed(),previousEnd);
    }
    private SessionLogPolicy.Category category(String message) {
        if(focused)return SessionLogPolicy.Category.THIRD_PARTY;
        String text=message==null?"":message.toLowerCase(Locale.US);
        if(text.contains("waze"))return SessionLogPolicy.Category.WAZE;
        if(text.contains("subscription")||text.contains("registered guidance"))return SessionLogPolicy.Category.SUBSCRIPTIONS;
        if(text.contains("maneuver"))return SessionLogPolicy.Category.MANEUVERS;
        if(text.contains("junction")||text.contains("toll"))return SessionLogPolicy.Category.JUNCTIONS;
        if(text.contains("lane"))return SessionLogPolicy.Category.LANES;
        if(text.contains("cluster")||text.contains("hud"))return SessionLogPolicy.Category.CLUSTER;
        if(text.contains("android auto"))return SessionLogPolicy.Category.ANDROID_AUTO;
        if(text.contains("guidance")||text.contains("navigation")||text.contains("route"))return SessionLogPolicy.Category.NAVIGATION;
        return SessionLogPolicy.Category.CORE;
    }
}
