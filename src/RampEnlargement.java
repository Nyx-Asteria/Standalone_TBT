package hmgapi.mods.projection.carplay;
/** Factory SetCaseExpExit: right17/selector90, left18/selector270; no invented road arms. */
final class RampEnlargement {
 static float[] draw(int type,int junction,boolean hasExit,int exit,float meters,int threshold){
  if(junction!=0||threshold<=0||Float.isNaN(meters)||Float.isInfinite(meters)||meters<0||meters>threshold)return null;
  boolean left=type==22||(type==8&&hasExit&&exit<0),right=type==23||(type==8&&hasExit&&exit>0);
  if(left==right)return null;
  return new float[]{left?18:17,0,left?270:90,meters,0,0,0,PhoneJunction.barLevel(meters,threshold),0};
 }
}
