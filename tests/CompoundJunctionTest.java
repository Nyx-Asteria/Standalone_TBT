package hmgapi.mods.projection.carplay;
import hmgapi.mods.projection.CompoundRoadGraphTest;
public final class CompoundJunctionTest {
 static void ok(boolean b){if(!b)throw new AssertionError(StockMapJunction.diagnostic());}
 static String encoded(long[] data){StringBuilder b=new StringBuilder("1000");for(long v:data)b.append(',').append(v);return b.toString();}
 static float[] draw(int fixture,int type,int junction,int exit,int target){return StockMapJunction.drawTarget(encoded(CompoundRoadGraphTest.sample(fixture)),1100,127,37-.001,0,"Android Auto",type,junction,true,exit,120,400,target);}
 public static void main(String[] args){
  float[] coarse=StockMapJunction.drawTarget(encoded(CompoundRoadGraphTest.sample(0)),1100,127,37-.001,0,"Android Auto",2,0,false,0,120,400,77);
  ok(coarse!=null&&coarse[2]==90);
  float[] coarseLeft=StockMapJunction.drawTarget(encoded(CompoundRoadGraphTest.sample(2)),1100,127,37-.001,0,"Android Auto",1,0,false,0,120,400,77);
  ok(coarseLeft!=null&&coarseLeft[2]==270);
  float[] joined=draw(3,2,0,90,77);ok(joined!=null&&joined[0]==13&&joined[1]==17); // paired approach is not a fourth arm
  float[] r=draw(0,2,0,90,77);ok(r!=null&&r[0]==13&&r[2]==90&&r[1]==17);
  float[] l=draw(2,1,0,-90,77);ok(l!=null&&l[0]==13&&l[2]==270&&l[1]==2049);
  float[] rotary=draw(1,6,1,-90,77);ok(rotary!=null&&rotary[0]==32&&rotary[1]==2065&&rotary[2]==270);
  float[] sameRoad=draw(1,6,1,180,77);ok(sameRoad!=null&&sameRoad[0]==32&&sameRoad[2]==180&&sameRoad[1]==2065);
  String ringData=encoded(CompoundRoadGraphTest.sample(1));
  float[] arc=StockMapJunction.drawTarget(ringData,1100,127,37-.001,0,"Android Auto",6,1,true,-90,246,400,77);
  ok(arc!=null&&arc[0]==32&&arc[1]==2065&&arc[3]==246);
  ok(StockMapJunction.drawTarget(ringData,1100,127,37-.001,0,"Android Auto",6,1,true,-90,350,400,77)==null);
  ok(draw(1,6,1,45,77)==null);ok(draw(1,6,1,-90,78)==null);ok(draw(0,6,1,-90,77)==null);
  String stale=encoded(CompoundRoadGraphTest.sample(0)).replaceFirst("1000,","-3000,");
  ok(StockMapJunction.drawTarget(stale,1100,127,37-.001,0,"CarPlay",2,0,true,90,120,400,77)==null);
  for(float[] f:new float[][]{r,l,rotary,sameRoad})ok(f[7]==30&&f[3]==120);
  System.out.println("Compound rendering: mirrored left/right island arms, physical rotary branches,360-degree return, unmatched exit/stale/target/kind guards and progress passed");
 }
}
