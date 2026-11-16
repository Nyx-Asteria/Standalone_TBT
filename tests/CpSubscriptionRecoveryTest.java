package hmgapi.mods.projection.carplay;
public final class CpSubscriptionRecoveryTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  CpSubscriptionRecovery p=new CpSubscriptionRecovery();
  ok(!p.begin(1000,false,900));ok(!p.begin(20000,true,900));
  ok(p.begin(1000,true,900));ok(!p.resume(1249));ok(p.resume(1250));ok(!p.resume(1500));
  ok(!p.expired(5999));ok(p.begin(5800,true,900));ok(p.remaining(5800)==200);ok(p.expired(6000));
  p.received();ok(!p.pending()&&!p.expired(9000));
  ok(p.begin(10000,true,9900));ok(!p.resume(10250)); // bounded recovery attempts
  p.clear();ok(!p.pending()&&!p.resume(12000));
  ok(p.begin(22000,true,21900));ok(p.resume(22250));p.clear();ok(!p.expired(30000));
  System.out.println("CarPlay subscription recovery: fresh-route grace, start-only timing/cooldown, fixed timeout and explicit route-end cancellation passed");
 }
}
