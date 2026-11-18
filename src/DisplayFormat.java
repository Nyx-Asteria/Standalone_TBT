package hmgapi.mods.projection.carplay;
/** Numeric policy only: the cluster controls whether an integer still shows a trailing .0. */
public final class DisplayFormat {
 public static int timeFormat(String preference,boolean system24){return "12".equals(preference)?1:"system".equals(preference)?(system24?2:1):2;}
 public static int arrivalFormat(String preference,android.content.Context context){
  boolean system24=true;
  if("system".equals(preference)&&context!=null)try{system24=android.text.format.DateFormat.is24HourFormat(context);}catch(RuntimeException ignored){}
  return timeFormat(preference,system24);
 }
 public static float distance(float value,int unit){return unit==1&&value>=10&&!Float.isNaN(value)&&!Float.isInfinite(value)?(float)Math.floor(value):value;}
}
