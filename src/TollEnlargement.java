package hmgapi.mods.projection.carplay;
/** FrameCluster::CheckDisplatTG: preserve booth icon and set the approach bar <1km. */
final class TollEnlargement {
 static float[] draw(int icon,float meters) {
  if((icon!=178&&icon!=184)||Float.isNaN(meters)||Float.isInfinite(meters)||meters<0||meters>=1000)return null;
  int bar=Math.max(10,Math.min(90,(int)(meters/100)*10));
  return new float[]{icon,0,0,meters,0,0,0,bar,0};
 }
}
