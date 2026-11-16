package hmgapi.mods.projection.carplay;
public final class FacilityContextTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] a){FacilityContext c=new FacilityContext();
 String h=c.resolve(3,0,0,null,"북상주톨게이트",800,1000);ok(h.contains("톨게이트"));
 for(int n=0;n<4;n++){
  ok(c.resolve(3,0,0,null,"2,200원",790-n*20,2000+n*2000).contains("톨게이트"));
  c.resolve(3,0,0,null,"북상주톨게이트",780-n*20,3000+n*2000);
 }
 ok(!c.resolve(3,0,0,null,"서울",600,10000).contains("톨게이트"));
 c.resolve(3,0,0,null,"휴게소",400,11000);ok(c.resolve(3,0,0,null,"휘발유 1,600원/L",390,12000).contains("휴게소"));
 ok(!c.resolve(3,0,0,null,"1,600원",390,22000).contains("휴게소")); // bounded timeout
 c.resolve(3,0,0,null,"요금소",200,23000);ok(!c.resolve(2,0,90,null,"2,200원",190,24000).contains("요금소")); // turn changed
 c.resolve(3,0,0,null,"요금소",20,25000);ok(!c.resolve(3,0,0,null,"2,200원",500,26000).contains("요금소")); // distance reset
 c.clear();ok(!c.resolve(3,0,0,null,"2,200원",10,27000).contains("요금소"));
 ok(!FacilityContext.auxiliary("2200"));ok(!FacilityContext.auxiliary("서울 2,200원 방향"));
 System.out.println("Facility labels: fee/oil text preserves only known same-target facility, bounded expiry/reset/direction/clear passed");}
}
