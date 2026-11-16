package hmgapi.mods.projection;
public final class AaHudDistanceTest {
 static void ok(boolean x){if(!x)throw new AssertionError();}
 public static void main(String[] args) {
  int[] a=AaHudDistance.encode(85,0,471);ok(a[2]==85&&a[3]==0&&a[4]==0&&a[5]==471);
  a=AaHudDistance.encode(1.2f,1,1350);ok(a[1]==2&&a[2]==1&&a[3]==1&&a[4]==4&&a[5]==1);
  a=AaHudDistance.encode(85,0,-1);ok(a[3]==15&&a[4]==15&&a[5]==65535);
  a=AaHudDistance.encode(.5f,2,1609);ok(a[3]==2&&a[5]==0&&a[4]==9);
  a=AaHudDistance.encode(50,3,30);ok(a[3]==3&&a[5]==98);
  ok(AaHudDistance.encode(Float.NaN,0,30)==null);
  ok(AaHudDistance.encode(-1,0,30)==null);
  System.out.println("AA current/next HUD distances: actual metric/imperial values and unknown-next sentinels passed");
 }
}
