package hmgapi.mods.projection.carplay;
import hmgapi.hook.Points;
import android.os.SystemClock;

final class JunctionSpeed {
    private Object service;
    private long last=-10000;
    private float speed=Float.NaN;
    float read() {
        long now=SystemClock.elapsedRealtime();
        if (now-last<1000) return speed;
        last=now; speed=Float.NaN;
        try {
            if(service==null) {
                ClassLoader cl=new dalvik.system.PathClassLoader("/system/framework/com.hkmc.ext.hcan.jar",getClass().getClassLoader());
                String name=(String)Points.get(cl,"framework.hscan_adapter.service_name",null);
                Object binder=Points.call(null,"framework.service_manager.get_service",null,name);
                if(binder!=null) service=Points.call(cl,"framework.hscan_service_stub.as_interface",null,binder);
            }
            if(service!=null) {
                Object v=Points.call(service.getClass().getClassLoader(),"framework.hscan_service.car_speed",service,(byte)0);
                if(v instanceof Number) speed=((Number)v).floatValue();
            }
        } catch(Throwable e) { service=null; }
        return speed;
    }
}
