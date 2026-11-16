package hmgapi.mods.projection.carplay;
/** Factory legacy rotary type32, confirmed on the owner's right-hand-traffic cluster.
 * Missing topology stays missing: never reuse the entrance node as the whole ring.
 */
final class RoundaboutEnlargement {
 static float[] draw(String app,int type,int junction,int side,boolean hasExit,int exit,
         int[] angles,float meters,int threshold) {
  if(!StockMapJunction.allowedApp(app) || (junction!=1 && type!=6 && type!=7)
      || side!=0 || !hasExit || exit< -180 || exit>180 || threshold<=0
      || Float.isNaN(meters) || Float.isInfinite(meters) || meters<=0 || meters>threshold)return null;
  int roads=0;
  // Native SetCaseRotary also accepts a missing mask and uses its direction fallback.
  // If the phone does supply topology, include only its real, representable arms.
  if(angles!=null && angles.length>0 && angles.length<=16) {
   for(int angle:angles) {
    if(angle< -180 || angle>180)return null;
    roads|=PhoneJunction.roadBit(angle);
   }
   roads|=PhoneJunction.roadBit(exit);
  }
  return new float[]{32,roads,(exit+360)%360,meters,0,0,0,
      PhoneJunction.barLevel(meters,threshold),0};
 }
}
