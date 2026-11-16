package hmgapi.mods.projection;
/** Current and following turn distances; unknown following distance uses protocol sentinels. */
final class AaHudDistance {
 static int[] encode(float distance,int unit,int nextMeters) {
  if(Float.isNaN(distance)||Float.isInfinite(distance)||distance<0||unit<0||unit>4)return null;
  distance=hmgapi.mods.projection.carplay.DisplayFormat.distance(distance,unit);
  int whole=Math.min((int)distance,4095),tenths=0;
  if(unit==1||unit==2)tenths=Math.max(0,Math.min(9,(int)((distance-(int)distance)*10+.5f)));
  int nextUnit=15,nextTenths=15,nextWhole=65535;
  if(nextMeters>=0) {
   boolean imperial=unit==2||unit==3||unit==4;
   nextUnit=imperial?(nextMeters<161?3:2):(nextMeters<1000?0:1);
   float next=nextUnit==3?nextMeters*3.28084f:nextUnit==2?nextMeters/1609.344f:nextUnit==1?nextMeters/1000f:nextMeters;
   next=hmgapi.mods.projection.carplay.DisplayFormat.distance(next,nextUnit);
   nextWhole=Math.min((int)next,4095);
   nextTenths=nextUnit==1||nextUnit==2?Math.max(0,Math.min(9,(int)((next-(int)next)*10+.5f))):0;
  }
  return new int[]{unit,tenths,whole,nextUnit,nextTenths,nextWhole,15,15,65535};
 }
}
