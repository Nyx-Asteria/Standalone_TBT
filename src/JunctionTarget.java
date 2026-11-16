package hmgapi.mods.projection.carplay;
/** Phone labels and maneuver indices are metadata, not physical junction identifiers. */
final class JunctionTarget {
 private String semantic,token;private long at;private float last,minimum;private int generation;
 void clear(){semantic=null;token=null;}
 String select(String key,long now,float meters){
  boolean reset=token==null||!key.equals(semantic)||now<at||now-at>10000
      ||Float.isNaN(meters)||Float.isInfinite(meters)||meters<0
      ||meters>last+30||(minimum<=10&&meters>minimum+5);
  if(reset){semantic=key;token=key+"/target/"+(++generation);minimum=meters;}
  else minimum=Math.min(minimum,meters);
  at=now;last=meters;return token;
 }
}
