package hmgapi.mods.projection.carplay;
/** Numeric policy only: the cluster controls whether an integer still shows a trailing .0. */
public final class DisplayFormat {
 public static float distance(float value,int unit){return unit==1&&value>=10&&!Float.isNaN(value)&&!Float.isInfinite(value)?(float)Math.floor(value):value;}
}
