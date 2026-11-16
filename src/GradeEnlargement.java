package hmgapi.mods.projection.carplay;
/** Exact legacy grade diagram/direction pairs from the pinned factory SetCaseOver/UnderPass.
 * Direction here is the factory diagram selector; it is not a compass bearing.
 */
final class GradeEnlargement {
 static float[] draw(int icon,float meters,int threshold) {
  if(threshold<=0||Float.isNaN(meters)||Float.isInfinite(meters)||meters<=0||meters>threshold)return null;
  int type,angle;
  switch(icon){
   case 88:type=44;angle=270;break;case 87:type=43;angle=90;break;
   case 82:type=43;angle=270;break;case 81:type=44;angle=90;break;
   case 91:type=46;angle=270;break;case 90:type=45;angle=90;break;
   case 85:type=45;angle=270;break;case 84:type=46;angle=90;break;
   default:return null;
  }
  return new float[]{type,0,angle,meters,0,0,0,PhoneJunction.barLevel(meters,threshold),0};
 }
}
