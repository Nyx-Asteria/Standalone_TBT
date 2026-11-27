package hmgapi.mods.projection;

import android.os.SystemClock;
import android.provider.Settings;
import hmgapi.hook.*;
import java.io.FileInputStream;
import java.security.MessageDigest;

/** One-shot parked experiment. Native engine ABI is pinned to the owning vehicle dump. */
public final class StockGraphNative {
    public static final String KEY="hmgapi_stock_road_graph_probe_v1";
    private static boolean scheduled;
    private static long retryAfter;
    private static String lastResult;
    private static byte[] helperPayload;
    static void prepare(ModContext mod) {
        try {
            helperPayload=NativePayload.read(new java.io.File(mod.dir,"libstockgraph.so"));
            mod.log("Stock graph probe helper cached before app UID switch; bytes="+helperPayload.length);
        } catch(Exception error) {mod.error("Stock graph probe helper preload",error);}
    }
    private static native long[] query(long[] executableRanges);
    private static native String resolutionDiagnostic();
    private static native long[] queryGeometry(long[] road);
    static void captureInitialized(LoadedApp app) {writeCapture(app,"waiting_for_position",null,null);}
    private static void writeCapture(LoadedApp app,String phase,long[] road,long[] geometry) {
        try {
            java.io.BufferedReader boot=new java.io.BufferedReader(new java.io.FileReader("/proc/sys/kernel/random/boot_id"));
            String bootId;try {bootId=boot.readLine();}finally {boot.close();}
            java.io.File target=new java.io.File(app.context.getFilesDir(),"hmgapi-stock-road-api.txt");
            java.io.File temp=new java.io.File(app.context.getFilesDir(),"hmgapi-stock-road-api.txt.part");
            java.io.FileOutputStream out=new java.io.FileOutputStream(temp);
            try {
                StringBuilder text=new StringBuilder("runtime=1.0.0-rc.1\nboot=").append(bootId)
                        .append("\npid=").append(android.os.Process.myPid()).append("\nphase=").append(phase)
                        .append("\nelapsed=").append(SystemClock.elapsedRealtime()).append('\n');
                if(road!=null)text.append("road=").append(java.util.Arrays.toString(road)).append('\n');
                if(geometry!=null)text.append("geometry=").append(java.util.Arrays.toString(geometry)).append('\n');
                out.write(text.toString().getBytes("UTF-8"));
            } finally {out.close();}
            if(!temp.renameTo(target))temp.delete();
        } catch(Exception ignored) { /* Private diagnostics must not affect stock engine. */ }
    }
    private static int lastGraphHash;
    private static long lastGraphAt=-10000;
    private static void graphJournal(LoadedApp app,long at,String request,long[] root,String queries,long[] result,String summary){
        int hash=31*java.util.Arrays.hashCode(root)+queries.hashCode();
        if(hash==lastGraphHash&&at-lastGraphAt<10000)return;
        try{
            java.io.File file=new java.io.File(app.context.getFilesDir(),"hmgapi-stock-road-graph.log");
            if(file.length()>4194304){java.io.File old=new java.io.File(app.context.getFilesDir(),"hmgapi-stock-road-graph.log.1");if(old.exists()&&!old.delete())return;if(!file.renameTo(old))return;}
            String text="runtime=1.0.0-rc.1 elapsed="+at+" wall="+System.currentTimeMillis()+" request="+request+"\n"
                +"summary="+summary+"\nroot="+java.util.Arrays.toString(root)+"\n"+queries
                +"result="+java.util.Arrays.toString(result)+"\nend\n";
            java.io.FileOutputStream out=new java.io.FileOutputStream(file,true);
            try{out.write(text.getBytes("UTF-8"));}finally{out.close();}
            lastGraphHash=hash;lastGraphAt=at;
        }catch(Exception ignored){ /* Private bounded diagnostics cannot interrupt navigation. */ }
    }
    static synchronized void schedule(final ModContext mod,final LoadedApp app) {
        if(scheduled || SystemClock.elapsedRealtime()<retryAfter)return;
        scheduled=true;
        new Thread(new Runnable(){public void run(){
            try {
                Thread.sleep(250);
                boolean diagnosticStart=false;
                while(true) {
                    boolean liveEnabled=true;
                    boolean probeEnabled=false;
                    if(!mod.enabled()) {
                        synchronized(StockGraphNative.class){scheduled=false;}
                        return;
                    }
                    long now=SystemClock.elapsedRealtime();
                    long lease=Settings.Global.getLong(app.context.getContentResolver(),
                            hmgapi.mods.projection.carplay.StockMapJunction.REQUEST,0);
                    if(MapQueryPolicy.live(liveEnabled,now,lease)) {
                        mod.log("Stock map initialization mode=live-guidance-request");break;
                    }
                    if(probeEnabled) {
                        Class<?> callback=Hooks.findClass("com.mnsoft.navi.NativeCall",app.classLoader);
                        Object value=callback.getMethod("getCanSpeedKMH").invoke(null);
                        float speed=value instanceof Number?((Number)value).floatValue():Float.NaN;
                        if(MapQueryPolicy.diagnostic(speed)) {diagnosticStart=true;break;}
                        if(!liveEnabled) {
                            writeCapture(app,"skipped_not_stationary",null,null);
                            mod.log("Stock graph probe skipped: stationary speed not confirmed");
                            synchronized(StockGraphNative.class){scheduled=false;retryAfter=now+10000;}
                            return;
                        }
                    }
                    writeCapture(app,"waiting_for_supported_guidance_request",null,null);
                    Thread.sleep(1000);
                }
                String path="/data/data/com.mnsoft.navi/BIN/libExSLNavi.so";
                MessageDigest digest=MessageDigest.getInstance("SHA-256");
                FileInputStream in=new FileInputStream(path);
                try {byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)digest.update(b,0,n);}
                finally {in.close();}
                StringBuilder hex=new StringBuilder();for(byte b:digest.digest())hex.append(String.format("%02x",b&255));
                String engineProfile=EngineProfiles.identify(hex.toString());
                if(engineProfile==null) {
                    writeCapture(app,"skipped_engine_hash",null,null);
                    mod.log("Stock graph probe skipped: unverified engine hash="+hex);return;
                }
                mod.log("Stock graph engine profile="+engineProfile+" sha256="+hex);
                writeCapture(app,"verified_engine_"+engineProfile,null,null);
                long[] ranges=LoadedEngineRanges.read(path);
                if(ranges.length==0) {
                    writeCapture(app,"skipped_engine_mapping",null,null);
                    mod.log("Stock graph probe skipped: no verified engine executable mapping");return;
                }
                mod.log("Stock graph probe verified executable ranges="+(ranges.length/2));
                java.io.File helper=NativePayload.stage(
                        app.context.getDir("hmgapi_tbt_native",0),helperPayload);
                mod.log("Stock graph probe helper staged in stock app private directory; hash verified");
                System.load(helper.getAbsolutePath());
                if(diagnosticStart)collect(mod,app,ranges);
                mod.log("Stock map live ready: requested range<=1000m interval=1000ms non-overlapping");
                Thread.sleep(1000);
                while(mod.enabled()) {
                    long started=SystemClock.elapsedRealtime();
                    long lease=Settings.Global.getLong(app.context.getContentResolver(),
                            hmgapi.mods.projection.carplay.StockMapJunction.REQUEST,0);
                    if(MapQueryPolicy.live(true,started,lease)) {
                        long[] currentRanges=LoadedEngineRanges.read(path);
                        if(currentRanges.length>0)collect(mod,app,currentRanges);
                    }
                    Thread.sleep(Math.max(50,1000-(SystemClock.elapsedRealtime()-started)));
                }
            } catch(Throwable error){writeCapture(app,"error_"+error.getClass().getSimpleName(),null,null);mod.error("Stock graph probe unavailable",error);}
        }},"stock-graph-probe").start();
    }
    private static final StockGeometryCache topologyCache=new StockGeometryCache();
    private static void collect(ModContext mod,final LoadedApp app,long[] ranges)throws Exception {
        long at=SystemClock.elapsedRealtime();
        long[] road=query(ranges);
        if(road==null || road.length==0) {
            Settings.Global.putString(app.context.getContentResolver(),hmgapi.mods.projection.carplay.StockMapJunction.KEY,null);
            writeCapture(app,"missing_road_result",null,null);return;
        }
        StringBuilder snapshot=new StringBuilder().append(at);
        for(long number:road)snapshot.append(',').append(number);
        Settings.Global.putString(app.context.getContentResolver(),KEY,snapshot.toString());
        long[] geometry=road[0]==0?queryGeometry(road):null;
        final long[] rootSample=geometry;
        final StringBuilder surveyed=new StringBuilder();
        StockRoadGeometry rootGeometry=StockRoadGeometry.decode(geometry);
        String forwardReason="not-requested";
        MapTargetRequest target=MapTargetRequest.decode(Settings.Global.getString(app.context.getContentResolver(),
                hmgapi.mods.projection.carplay.StockMapJunction.DETAILS),SystemClock.elapsedRealtime());
        if(target!=null && road[0]==0) {
            StockNavigationApi.Position position=StockNavigationApi.currentPosition(app.context);
            if(position!=null) {
                StockMapLookahead.Reader reader=new StockMapLookahead.Reader(){
                    private final java.util.Map<String,long[]> cache=new java.util.HashMap<String,long[]>();
                    public long[] read(long p,long l,int d){
                        String key=p+":"+l+":"+d;
                        if(!cache.containsKey(key)){
                            long[] found=topologyCache.get(p,l,d,SystemClock.elapsedRealtime());
                            boolean hit=found!=null;
                            if(!hit){found=queryGeometry(new long[]{0,p,l,d,0,0,0});
                                topologyCache.put(p,l,d,SystemClock.elapsedRealtime(),found);}
                            cache.put(key,found);
                            surveyed.append("topology_cache=").append(hit).append('\n');
                            surveyed.append("query=").append(key).append("\nnode=").append(java.util.Arrays.toString(found)).append('\n');
                        }
                        return cache.get(key);
                    }
                };
                geometry=StockMapLookahead.find(geometry,(int)road[3],position.longitude,position.latitude,
                        target.meters,target.id,target.kind,reader);
                forwardReason=StockMapLookahead.lastReason;
                if(geometry!=null&&geometry[0]!=6){
                    long[] compound=CompoundRoadGraph.expand(geometry,target.kind,reader);
                    forwardReason+="/"+CompoundRoadGraph.lastReason;
                    if(compound!=null)geometry=compound;
                }
            } else {geometry=null;forwardReason="position-not-ready";}
        }
        StockRoadGeometry decoded=StockRoadGeometry.decode(geometry);
        String summary="road="+road[0]+" connections="+(road.length>=7?road[6]:-1)
                +" geometry="+(decoded==null?"invalid_or_absent":decoded.summary())
                +" root="+(rootGeometry==null?"invalid":rootGeometry.summary())
                +" forward="+forwardReason+" resolver="+resolutionDiagnostic();
        if(!summary.equals(lastResult)){mod.log("Stock map sample "+summary);lastResult=summary;}
        String encoded=null;
        if(decoded!=null && decoded.status==0 && decoded.valid>0) {
            StringBuilder value=new StringBuilder().append(at);
            for(long number:geometry)value.append(',').append(number);
            encoded=value.toString();
        }
        Settings.Global.putString(app.context.getContentResolver(),
                hmgapi.mods.projection.carplay.StockMapJunction.KEY,encoded);
        writeCapture(app,"complete",road,geometry);
        if(target!=null)graphJournal(app,at,target.id+","+target.meters+","+target.kind,rootSample,surveyed.toString(),geometry,summary);
    }

}
