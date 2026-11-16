package hmgapi.mods.projection.carplay;
public final class RoundaboutEnlargementTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args) {
  float[] f=RoundaboutEnlargement.draw("네이버지도",0,1,0,true,-90,null,290,400);
  ok(f!=null && f[0]==32 && f[1]==0 && f[2]==270 && f[3]==290 && f[7]==70);
  f=RoundaboutEnlargement.draw("TMAP",6,0,0,true,-90,new int[]{0,90,-90},290,400);
  ok(f!=null && f[1]==2065 && f[2]==270);
  ok(RoundaboutEnlargement.draw("TMAP",7,0,0,true,0,null,100,400)!=null);
  ok(RoundaboutEnlargement.draw("TMAP",0,1,0,false,0,null,100,400)==null);
  ok(RoundaboutEnlargement.draw("TMAP",0,1,1,true,-90,null,100,400)==null);
  ok(RoundaboutEnlargement.draw("카카오내비",0,1,0,true,-90,null,100,400)!=null);
  ok(RoundaboutEnlargement.draw("TMAP",0,0,0,true,-90,null,100,400)==null);
  ok(RoundaboutEnlargement.draw("TMAP",0,1,0,true,-90,null,401,400)==null);
  ok(RoundaboutEnlargement.draw("TMAP",0,1,0,true,-90,new int[]{361},100,400)==null);
  ok(RoundaboutEnlargement.draw("TMAP",0,1,0,true,-90,null,Float.NaN,400)==null);
  System.out.println("Rotary enlargement: factory32/mask2065/direction270, missing topology, source/angle/side/distance guards passed");
 }
}
