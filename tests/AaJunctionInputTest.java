package hmgapi.mods.projection;
import hmgapi.mods.projection.carplay.AaEnlargement;
public final class AaJunctionInputTest {
 static void ok(boolean x){if(!x)throw new AssertionError();}
 public static void main(String[] args) {
  AaJunctionInput l=AaJunctionInput.from(4,1,0),r=AaJunctionInput.from(4,2,0);
  ok(l.type==1&&l.exit==-90&&r.type==2&&r.exit==90);
  ok(AaJunctionInput.from(3,1,0).exit==-30);
  ok(AaJunctionInput.from(5,2,0).exit==135);
  ok(AaJunctionInput.from(14,3,0).type==3);
  ok(AaJunctionInput.from(7,1,0).type==52);
  ok(AaJunctionInput.from(8,2,0).type==23);
  ok(AaJunctionInput.from(13,2,270).exit==-90);
  ok(AaJunctionInput.from(13,2,180).exit==0);
  ok(AaJunctionInput.from(13,2,90).exit==90);
  ok(AaJunctionInput.from(13,2,0).exit==180);
  ok(AaJunctionInput.from(13,1,90).exit==90);
  ok(AaJunctionInput.from(11,1,150).exit==30);
  ok(!AaJunctionInput.from(4,2,-1).hasExit);
  ok(AaJunctionInput.from(9,2,-1)!=null);
  ok(AaJunctionInput.from(13,2,361)==null);
  ok(AaJunctionInput.from(8,3,0)==null);
  ok(AaJunctionInput.from(6,1,0)==null);
  ok(AaJunctionInput.from(4,3,0)==null);
  ok(AaEnlargement.facility(3,0,0,"천안 요금소")==178);
  ok(AaEnlargement.facility(3,0,0,"정안알밤휴게소")==183);
  System.out.println("AA normalization: left/right, ramps, rotary angle convention, incomplete/unsupported rejection and facilities passed");
 }
}
