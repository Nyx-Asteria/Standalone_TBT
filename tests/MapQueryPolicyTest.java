package hmgapi.mods.projection;
public final class MapQueryPolicyTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] a){
  ok(MapQueryPolicy.live(true,1000,6000));ok(!MapQueryPolicy.live(true,6000,6000));
  ok(!MapQueryPolicy.live(false,1000,6000));ok(!MapQueryPolicy.live(true,1000,9000));
  ok(MapQueryPolicy.diagnostic(0));ok(!MapQueryPolicy.diagnostic(30));ok(!MapQueryPolicy.diagnostic(Float.NaN));
  ok(MapTargetRequest.decode("1000,1050,123",1100)!=null);
  ok(MapTargetRequest.decode("1000,1050,123",7000)==null);
  ok(MapTargetRequest.decode("1000,1201,123",1100)==null);
  ok(MapTargetRequest.decode("1000,40,0",1100)==null);
  ok(MapTargetRequest.decode("1000,400,123,1",1100).kind==1);
  ok(MapTargetRequest.decode("1000,400,123,0",1100).kind==0);
  ok(MapTargetRequest.decode("1000,400,123,2",1100)==null);
  ok(hmgapi.mods.projection.carplay.StockMapJunction.ownsRequest("1000,400,123,1",123));
  ok(!hmgapi.mods.projection.carplay.StockMapJunction.ownsRequest("1000,400,123,1",400));
  System.out.println("Live request independent of parked diagnostic; monotonic lease and target request boundaries passed");
 }
}
