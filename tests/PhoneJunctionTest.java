package hmgapi.mods.projection.carplay;
public final class PhoneJunctionTest {
 static void ok(boolean x){if(!x)throw new AssertionError();}
 public static void main(String[] args){
  // Matches the independently captured factory crossroad mask 0x811.
  ok((PhoneJunction.roadBit(0)|PhoneJunction.roadBit(90)|PhoneJunction.roadBit(-90))==2065);
  ok(PhoneJunction.roadBit(180)==0);
  float[] a=PhoneJunction.draw(1,0,true,-90,new int[]{0,90,-90},90,19);
  ok(a!=null && a[0]==13 && a[1]==2065 && a[2]==270 && a[3]==90);
  ok(PhoneJunction.draw(1,0,true,-90,new int[]{0,90},101,19)==null);
  ok(PhoneJunction.draw(1,0,true,-90,new int[]{0,90},400,20)!=null);
  ok(PhoneJunction.draw(1,0,true,-90,new int[]{0,90},500,60)!=null);
  ok(PhoneJunction.draw(1,0,true,-90,new int[]{0,90},501,60)==null);
  ok(PhoneJunction.draw(1,0,false,0,new int[]{0,90},50,0)==null);
  ok(PhoneJunction.draw(1,0,true,-90,null,50,0)==null);
  ok(PhoneJunction.draw(1,0,true,90,new int[]{0,90},50,0)==null);
  ok(PhoneJunction.draw(1,0,true,-90,new int[]{0,90},50,Float.NaN)==null);
  ok(PhoneJunction.draw(6,1,true,-90,new int[]{0,90},50,0)==null);
  ok(PhoneJunction.roadThreshold(1)==400);
  ok(PhoneJunction.roadThreshold(23)==1050);
  ok(PhoneJunction.drawAt(1,0,true,-90,new int[]{0,90},400,400)!=null);
  ok(PhoneJunction.drawAt(1,0,true,-90,new int[]{0,90},401,400)==null);
  float[] ramp=PhoneJunction.drawAt(23,0,true,90,new int[]{0,90},1000,1050);
  ok(ramp!=null && ramp[0]==13);
  System.out.println("PhoneJunction: native crossroad mask, thresholds and absent/contradictory geometry passed");
 }
}
