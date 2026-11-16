package hmgapi.mods.projection.carplay;
public final class MapJunctionDisplayTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 static final String A="1:45720000:13320000",B="1:45720010:13320000";
 static float[] f={13,2065,270,300,0,0,0,80,0};
 static float[] apply(MapJunctionDisplay d,String t,String n,float[] c,long now,float m){
  return d.apply(t,n,c,now,m,400,127,37);
 }
 public static void main(String[] args) {
  MapJunctionDisplay d=new MapJunctionDisplay();
  ok(apply(d,"turnA",A,f,1000,300)==null);
  ok(apply(d,"turnA",A,f,1450,290)!=null);
  ok(apply(d,"turnA",null,null,2000,280)[3]==280);
  ok(apply(d,"turnA",null,null,8000,115)[7]==30); // same target, fresh distance and bar
  ok(apply(d,"turnA",B,f,9000,110)[1]==2065); // lookup cannot switch the confirmed target
  ok(apply(d,"turnA",null,null,10000,4)[3]==4);
  ok(apply(d,"turnB",null,null,10100,4)==null);
  ok(apply(d,"turnB",A,f,11000,30)==null);
  ok(apply(d,"turnB",A,f,11400,29)!=null);
  ok(apply(d,"turnB",null,null,11500,150)==null); // recalculation
  ok(apply(d,"turnC",A,f,12000,200)==null);
  ok(apply(d,"turnC",A,f,12500,190)!=null);
  ok(apply(d,"turnC",null,null,133000,180)==null); // bounded lifetime
  ok(apply(d,"turnD",A,f,140000,200)==null);
  ok(apply(d,"turnD",A,f,140400,190)!=null);
  ok(d.apply("turnD",null,null,141000,180,400,128,37)==null); // wrong position
  ok(apply(d,"turnE",A,f,150000,200)==null);
  ok(apply(d,"turnE",A,f,150400,190)!=null);
  ok(d.apply("turnE",null,null,151000,180,400,Double.NaN,37)==null); // stale GPS
  ok(PhoneJunction.barLevel(400,400)==100);
  ok(PhoneJunction.barLevel(180,400)==50);
  ok(PhoneJunction.barLevel(90,400)==20);
  ok(PhoneJunction.barLevel(0,400)==10);
  ok(PhoneJunction.barLevel(4,400)==10);
  ok(PhoneJunction.barLevel(19,400)==10);
  ok(PhoneJunction.barLevel(-1,400)==0);
  ok(PhoneJunction.barLevel(Float.NaN,400)==0);
  ok(PhoneJunction.barLevel(Float.POSITIVE_INFINITY,400)==0);
  ok(PhoneJunction.barLevel(10,0)==0);
  MapJunctionDisplay near=new MapJunctionDisplay();
  ok(apply(near,"near",A,f,1000,10)==null);
  ok(apply(near,"near",A,f,1400,4)[7]==10);
  ok(apply(near,"near",null,null,1500,0)[7]==10);
  near.clear();
  ok(apply(near,"near",null,null,1600,0)==null); // route end clears retained segment
  ok(apply(near,"next",null,null,1700,300)==null); // never carry gauge to next turn
  MapJunctionDisplay ring=new MapJunctionDisplay();float[] rotary={32,2065,270,200,0,0,0,50,0};
  String ringNode="1:45720000:13320250:80";
  ok(ring.apply("ring",ringNode,rotary,1000,100,400,127,37)==null);
  ok(ring.apply("ring",ringNode,rotary,1400,90,400,127,37)!=null);
  float[] rotated=rotary.clone();rotated[2]=90;
  ok(ring.apply("ring",ringNode,rotated,2000,10,400,127,37)[2]==270);
  ok(ring.apply("ring",null,null,2500,5,400,127,37)[3]==5);
  ok(ring.apply("new-exit",null,null,2600,5,400,127,37)==null);
  System.out.println("Map display: transient gaps, target lock, distance/bar refresh, route reset, age and GPS guards passed");
 }
}
