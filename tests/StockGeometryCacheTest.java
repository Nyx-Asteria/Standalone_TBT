package hmgapi.mods.projection;
public final class StockGeometryCacheTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  StockGeometryCache c=new StockGeometryCache();
  CompoundRoadGraphTest.Fixture fixture=CompoundRoadGraphTest.fixture(0);
  long[] a=fixture.node(fixture.roads.get(0),1);a[5]=42;
  c.put(1,2,0,1000,a);a[5]=99;ok(c.get(1,2,0,1000)[5]==42);
  long[] b=c.get(1,2,0,1100);b[5]=100;ok(c.get(1,2,0,1200)[5]==42);
  ok(c.get(1,2,1,1200)==null&&c.get(2,2,0,1200)==null);
  ok(c.get(1,2,0,6000)!=null&&c.get(1,2,0,6001)==null);
  c.put(1,2,0,9000,a);ok(c.get(1,2,0,8999)==null);
  c.put(1,2,0,10000,new long[]{4,-1,1,1,1});ok(c.get(1,2,0,10000)==null);
  for(int i=0;i<129;i++)c.put(1,i,0,20000,fixture.node(fixture.roads.get(0),1));
  ok(c.get(1,0,0,20000)==null&&c.get(1,128,0,20000)!=null);
  System.out.println("Road topology cache: immutable copies, parcel/link/direction isolation, bounded5s lifetime/capacity and failed-result refusal passed");
 }
}
