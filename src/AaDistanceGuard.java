package hmgapi.mods.projection;
/** Keep a recent camera overlay's late distance packets out of the current maneuver. */
final class AaDistanceGuard {
 private long cameraAt=-1,acceptedAt=-1,suspectAt=-1;
 private int acceptedMeters;
 void camera(long now){cameraAt=now;}
 void reset(){cameraAt=-1;acceptedAt=-1;suspectAt=-1;}
 boolean implausibleDrop(int meters,long now){
  long age=now-acceptedAt;
  return acceptedAt>=0&&age>=0&&age<=10000&&acceptedMeters-meters>50+age/10;
 }
 boolean accept(int meters,long now){
  if(meters<0)return false;
  // Bound the guard to an observed camera and a fresh real distance. 100 m/s plus
  // 50 m quantization tolerance allows more movement than normal road driving.
  if(implausibleDrop(meters,now)) {
   // P(distance) can precede S(camera). Briefly quarantine an implausible jump
   // so a following semantic packet can classify it; never infer a camera from distance alone.
   if(suspectAt<0||now<suspectAt)suspectAt=now;
   if((cameraAt>=0&&now>=cameraAt&&now-cameraAt<=10000)||now-suspectAt<1500)return false;
  }
  suspectAt=-1;
  acceptedAt=now;acceptedMeters=meters;return true;
 }
}
