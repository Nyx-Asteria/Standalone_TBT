package hmgapi.mods.projection.carplay;
/** Known maneuver direction and distance countdown, without inventing road-arm geometry. */
final class TurnPresentation {
 static float direction(int icon,boolean hasExit,int exit) {
  if(hasExit && exit>=-180 && exit<=180)return (exit+360)%360;
  switch(icon) {
   case 66:return 30;case 67:return 90;case 68:return 135;
   case 69:return 225;case 70:return 270;case 71:return 330;
   case 72:case 73:return 180;
   case 161:case 164:return 30;case 162:case 165:return 330;
   default:return 0;
  }
 }
 static int[] bar(int icon,int type,float meters) {
  int threshold=PhoneJunction.roadThreshold(type);
  boolean turn=icon>=65&&icon<=73 || icon>=160&&icon<=165
      || icon==81||icon==82||icon==84||icon==85||icon==87||icon==88||icon==90||icon==91;
  if(!turn || Float.isNaN(meters)||Float.isInfinite(meters)||meters<0||meters>threshold)
   return new int[]{0,-1};
  return new int[]{PhoneJunction.barLevel(meters,threshold),0};
 }
}
