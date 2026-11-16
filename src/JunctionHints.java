package hmgapi.mods.projection.carplay;

/** Experimental match of a fresh factory guide target, not a map-node search. */
public final class JunctionHints {
    public static final String KEY = "hmgapi_projection_junction_hint";
    private JunctionHints() {}
    public static int threshold(float speed) {
        if (Float.isNaN(speed) || Float.isInfinite(speed) || speed < 0 || speed > 250) return -1;
        return speed < 20 ? 100 : speed < 60 ? 400 : 500;
    }
    public static float meters(float distance, int unit) {
        float[] factors = {1, 1000, 1609.344f, .3048f, .9144f};
        return unit >= 0 && unit < factors.length ? distance * factors[unit] : Float.NaN;
    }
    public static float[] match(String encoded, long now, float speed, float phoneMeters,
            float phoneDirection, int phoneIcon, String road) {
        int limit = threshold(speed);
        if (limit < 0 || Float.isNaN(phoneMeters) || phoneMeters <= 0 || phoneMeters > limit
                || encoded == null || encoded.length() > 300) return null;
        try {
            String[] v = encoded.split(",");
            if (v.length != 10) return null;
            long age = now - Long.parseLong(v[0]);
            if (age < 0 || age > 2000) return null;
            float[] a = new float[9];
            for (int i=0;i<9;i++) {
                a[i]=Float.parseFloat(v[i+1]);
                if (Float.isNaN(a[i]) || Float.isInfinite(a[i])) return null;
            }
            int type=(int)a[0];
            if (!(type==13 || type==32 || (type>=43 && type<=46))) return null;
            float d=meters(a[3],(int)a[6]);
            if (d<=0 || a[4]!=0 || a[8]!=0 || Math.abs(d-phoneMeters)>75) return null;
            if (a[2]<0 || a[2]>360 || a[7]<0 || a[7]>100) return null;
            if (type==32) {
                // Roundabout branch angles cannot be inferred from an ordinary turn.
                if (phoneIcon<96 || phoneIcon>107) return null;
            } else if (type>=43 && type<=46) {
                if (road==null || !(road.contains("고가차도") || road.contains("지하차도"))) return null;
                boolean under=road.contains("지하차도");
                boolean side=road.contains("옆길") || road.contains("옆 길") || road.contains("측도");
                if (under != (type>=45) || side != (type==44 || type==46)) return null;
            }
            float delta=Math.abs(a[2]-phoneDirection)%360;
            if (Math.min(delta,360-delta)>30) return null;
            // Reuse only geometry; phone countdown and selected branch drive the display.
            a[2]=phoneDirection; a[3]=phoneMeters; a[6]=0;
            a[7]=Math.max(1,Math.min(100,Math.round(phoneMeters*100/limit)));
            return a;
        } catch (RuntimeException e) { return null; }
    }
}
