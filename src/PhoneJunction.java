package hmgapi.mods.projection.carplay;

/** Phone-supplied geometry only. Factory type 13 and 16-bit road directions.
 * Sector algorithm adapted from BeegSchmooze/Lambda-Carplay-AA-Bridge,
 * src/java/com/daudio/app/dipo/LeafiaCpTurn.java (GPL-3.0).
 * Verified against the factory 0x811 crossroad mask in this project's captures.
 */
final class PhoneJunction {
 static int roadBit(int angle) {
  int cw=((angle%360)+360)%360;
  if(cw<12 || cw>348)return 1;
  int lo=12,bit=1;
  for(int k=0;k<15;k++) {
   int hi=lo+(k%2==0?22:23);
   if(k!=7) { if(cw>=lo && cw<hi)return 1<<bit; bit++; }
   lo=hi;
  }
  return 0;
 }
 static float displayAngle(int bit) {
  float[] centres={0,22.5f,45,67.5f,90,112.5f,135,157.5f,202.5f,225,247.5f,270,292.5f,315,337.5f};
  for(int i=0;i<centres.length;i++)if(bit==(1<<i))return centres[i];
  return Float.NaN;
 }
 static int barLevel(float meters,int threshold) {
  if(threshold<=0 || Float.isNaN(meters) || Float.isInfinite(meters) || meters<0)return 0;
  // Stock paired-drive captures retain one segment through 0m; route clear is separate.
  return Math.max(10,Math.min(100,Math.round(meters*10/threshold)*10));
 }
 static int roadThreshold(int type) {
  return type==8 || type==9 || type==22 || type==23 || type==51 || type==52 || type==53 ? 1050 : 400;
 }
 static float[] draw(int type,int junction,boolean hasExit,int exit,int[] angles,
                     float meters,float speed) {
  return drawAt(type,junction,hasExit,exit,angles,meters,JunctionHints.threshold(speed));
 }
 static float[] drawAt(int type,int junction,boolean hasExit,int exit,int[] angles,
                     float meters,int threshold) {
  if(threshold<0 || Float.isNaN(meters) || Float.isInfinite(meters)
      || meters<=0 || meters>threshold || junction!=0 || !hasExit
      || exit < -180 || exit > 180 || angles==null || angles.length==0)return null;
  boolean ramp=type==8 || type==22 || type==23 || type==51;
  boolean left=type==1 || type==20 || type==47 || type==49 || type==22 || (ramp && exit<0);
  boolean right=type==2 || type==21 || type==48 || type==50 || type==23 || (ramp && exit>0);
  if(!left && !right && type!=3)return null;
  if((left && exit>=0)||(right && exit<=0)||(type==3 && Math.abs(exit)>22))return null;
  int roads=roadBit(exit);
  for(int angle:angles) {
   if(angle < -180 || angle > 180)return null;
   roads|=roadBit(angle);
  }
  int bar=barLevel(meters,threshold);
  return new float[]{13,roads,displayAngle(roadBit(exit)),meters,0,0,0,bar,0};
 }
}
