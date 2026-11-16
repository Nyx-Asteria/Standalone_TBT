package hmgapi.mods.projection.carplay;
public final class RampEnlargementTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
 float[] r=RampEnlargement.draw(23,0,false,0,1050,1050),l=RampEnlargement.draw(22,0,false,0,400,1050);
 ok(r!=null&&r[0]==17&&r[1]==0&&r[2]==90&&r[3]==1050);ok(l!=null&&l[0]==18&&l[2]==270);
 ok(RampEnlargement.draw(8,0,true,-45,40,1050)[0]==18);
 ok(RampEnlargement.draw(23,0,false,0,0,1050)[7]==10);
 ok(RampEnlargement.draw(23,0,false,0,1051,1050)==null);
 ok(RampEnlargement.draw(8,0,false,0,100,1050)==null);
 ok(RampEnlargement.draw(53,0,false,0,100,1050)==null);
 ok(RampEnlargement.draw(2,0,true,90,100,400)==null);
 ok(RampEnlargement.draw(23,1,false,0,100,1050)==null);
 System.out.println("Factory highway exit: canonical17/90 and18/270,1050m boundary, zero-distance segment and unknown/non-exit rejection passed");}
}
