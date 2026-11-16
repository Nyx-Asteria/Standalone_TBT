package hmgapi.mods.projection;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import hmgapi.hook.*;
import java.lang.reflect.Method;

/** Read-only snapshot of the stock engine's verified Java GIS callback.
 * Does not turn on stock guidance, issue native commands or manufacture topology.
 */
public final class StockNavigationApi {
    private static final String KEY="hmgapi_stock_navigation_position_v1";
    public static final class Position {
        public final long elapsed;
        public final double latitude, longitude, heading;
        Position(long at,double lat,double lon,double direction) {
            elapsed=at; latitude=lat; longitude=lon; heading=direction;
        }
    }
    public static Position decode(String value,long now) {
        if(value==null || value.length()>200)return null;
        try {
            String[] f=value.split(","); if(f.length!=4)return null;
            long at=Long.parseLong(f[0]);
            double lat=Double.parseDouble(f[1]),lon=Double.parseDouble(f[2]),dir=Double.parseDouble(f[3]);
            if(now<at || now-at>2500 || Double.isNaN(lat)||Double.isNaN(lon)||Double.isNaN(dir)
                    || Math.abs(lat)>90 || Math.abs(lon)>180 || dir<0 || dir>=360)return null;
            return new Position(at,lat,lon,dir);
        } catch(RuntimeException e) {return null;}
    }
    public static Position currentPosition(Context context) {
        try {return decode(Settings.Global.getString(context.getContentResolver(),KEY),SystemClock.elapsedRealtime());}
        catch(RuntimeException e){return null;}
    }
    /** Fresh one-shot native result; contains link IDs, not an upcoming-junction model. */
    public static long[] currentRoad(Context context) {
        try {
            String value=Settings.Global.getString(context.getContentResolver(),StockGraphNative.KEY);
            if(value==null || value.length()>2400)return null;
            String[] fields=value.split(",");
            if(fields.length<8)return null;
            long age=SystemClock.elapsedRealtime()-Long.parseLong(fields[0]);
            if(age<0 || age>2500 || Long.parseLong(fields[1])!=0)return null;
            int count=Integer.parseInt(fields[7]);
            if(count<0 || count>32 || fields.length!=8+2*count)return null;
            long[] data=new long[fields.length-1];
            for(int i=0;i<data.length;i++)data[i]=Long.parseLong(fields[i+1]);
            return data;
        } catch(RuntimeException error){return null;}
    }
    private static void publishStatus(LoadedApp app,String state) {
        try {Settings.Global.putString(app.context.getContentResolver(),
                "hmgapi_stock_navigation_status_v1", "1.0.0,"+android.os.Process.myPid()+","
                +SystemClock.elapsedRealtime()+","+state);}
        catch(RuntimeException ignored) { /* Diagnostics cannot block navigation. */ }
    }
    static void install(final ModContext mod,final LoadedApp app) {
        StockGraphNative.captureInitialized(app);
        mod.log("Stock navigation API runtime=1.0.0 install begin; probeEnabled="
                + false);
        publishStatus(app,"install_begin");
        try {
            int matched=0;
            Class<?> c=Hooks.findClass("com.mnsoft.navi.NativeCall",app.classLoader);
            for(Method m:c.getDeclaredMethods()) {
                if(!m.getName().equals("sendGISInfo"))continue;
                Class<?>[] t=m.getParameterTypes();
                if(t.length<8 || t[0]!=double.class || t[1]!=double.class || t[2]!=double.class
                        || t[3]!=int.class || t[4]!=int.class || t[5]!=int.class || t[6]!=int.class
                        || t[7]!=String.class)continue;
                matched++;
                Bridge.hookMethod(m,new MethodHook(){
                    long last=-10000; boolean announced, rejected;
                    @Override protected synchronized void afterHookedMethod(Param p) {
                        if(p.getThrowable()!=null)return;
                        long now=SystemClock.elapsedRealtime();if(now-last<500)return;last=now;
                        String value=now+","+p.args[0]+","+p.args[1]+","+p.args[5];
                        if(decode(value,now)==null) {
                            if(!rejected) {
                                rejected=true;
                                mod.log("Stock navigation API callback received, position validation rejected (coordinates omitted)");
                                publishStatus(app,"callback_rejected");
                            }
                            return;
                        }
                        if(!announced)publishStatus(app,"callback_valid");
                        StockGraphNative.schedule(mod, app);
                        try {
                            boolean published=Settings.Global.putString(app.context.getContentResolver(),KEY,value);
                            if(!announced && published) {
                                announced=true;
                                mod.log("Stock navigation API position callback active; topology=unavailable");
                                publishStatus(app,"position_published");
                            }
                        } catch(RuntimeException ignored) { /* Optional position feed. */ }
                    }
                });
                mod.log("Stock navigation API observing sendGISInfo/"+t.length);
            }
            mod.log("Stock navigation API installed GIS hooks="+matched);
            publishStatus(app,"hooks_"+matched);
        } catch(RuntimeException e) {
            publishStatus(app,"install_error_"+e.getClass().getSimpleName());
            mod.error("Stock navigation API unavailable",e);
        }
    }
}
