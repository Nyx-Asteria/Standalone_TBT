package hmgapi.mods.projection;
public final class AaDistanceGuardTest {
 static void ok(boolean v){if(!v)throw new AssertionError();}
 public static void main(String[] args){
  AaDistanceGuard g=new AaDistanceGuard();
  ok(g.accept(890,0));g.camera(1000);ok(g.accept(813,3000));ok(g.accept(788,4000));ok(g.accept(763,5000));
  ok(!g.accept(198,6000));ok(!g.accept(174,7000));ok(g.accept(671,10000));
  g.reset();ok(g.accept(400,11000));g.camera(11500);ok(!g.accept(193,12000));ok(g.accept(380,13000));
  g.reset();ok(g.accept(30,14000)); // actual maneuver change/recalculation
  g.camera(15000);g.reset();ok(g.accept(5,15001)); // explicit reset cannot retain an overlay lease
  g.reset();ok(g.accept(900,16000));g.camera(16001);ok(g.accept(100,27000)); // bounded to ten seconds
  g.reset();ok(g.accept(400,30000));ok(!g.accept(193,31000));ok(g.accept(193,33000)); // no camera evidence: bounded settle only
  g.reset();ok(g.accept(1000,40000));ok(!g.accept(452,40900));g.camera(41800);ok(!g.accept(427,41810));ok(g.accept(941,42800)); // distance before camera state
  System.out.println("AA camera distance: late 763/198/671 and 400/193/recovery, resets, expiry and no-camera path passed");
 }
}
