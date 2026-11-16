package hmgapi.mods.projection.carplay;
/** A confirmed physical target survives transient lookup failures, never a route change. */
final class MapJunctionDisplay {
 private String token,node;
 private float[] accepted;
 private long candidateAt,lastAccepted;
 private float lastDistance,minDistance;
 String state="none";
 void clear(){token=null;node=null;accepted=null;candidateAt=0;lastAccepted=0;state="none";}
 float[] apply(String turnToken,String candidateNode,float[] candidate,long now,float distance,
         int threshold,double longitude,double latitude) {
  if(turnToken==null || !turnToken.equals(token) || distance>lastDistance+40
      || (Float.isNaN(distance)||Float.isInfinite(distance)) || distance<0 || distance>threshold) {
   clear();token=turnToken;
  }
  boolean valid=accepted!=null && now>=lastAccepted && now-lastAccepted<=120000
      && distance<=minDistance+30 && plausible(node,longitude,latitude,distance);
  // A different lookup result must not replace a still plausible confirmed target.
  if(valid && (candidate==null || !node.equals(candidateNode) || ((int)accepted[0]==32&&candidate[0]==32))) {
   lastDistance=distance;minDistance=Math.min(minDistance,distance);
   state="confirmed-target-hold";return frame(distance,threshold);
  }
  if(candidate!=null && candidateNode!=null && plausible(candidateNode,longitude,latitude,distance)) {
   if(!candidateNode.equals(node)) {node=candidateNode;candidateAt=now;accepted=null;}
   lastDistance=distance;
   if(now-candidateAt<350){state="confirming-node";return null;}
   accepted=candidate.clone();lastAccepted=now;minDistance=distance;
   state="matched-stable";return frame(distance,threshold);
  }
  state="no-match";accepted=null;node=null;lastDistance=distance;return null;
 }
 private float[] frame(float distance,int threshold) {
  float[] result=accepted.clone();result[3]=distance;
  result[7]=PhoneJunction.barLevel(distance,threshold);return result;
 }
 private static boolean plausible(String key,double longitude,double latitude,float distance) {
  if(key==null || Double.isNaN(longitude) || Double.isInfinite(longitude)
      || Double.isNaN(latitude) || Double.isInfinite(latitude)
      || Math.abs(longitude)>180 || Math.abs(latitude)>90)return false;
  try {
   String[] parts=key.split(":");if(parts.length!=3&&parts.length!=4)return false;
   int radius=parts.length==4?Integer.parseInt(parts[3]):0;if(radius<0||radius>180)return false;
   double lon=Long.parseLong(parts[1])/360000.0,lat=Long.parseLong(parts[2])/360000.0;
   return Math.abs(lon)<=180 && Math.abs(lat)<=90
       && StockMapJunction.distance(longitude,latitude,lon,lat)<=distance+radius+40;
  }catch(NumberFormatException e){return false;}
 }
}
