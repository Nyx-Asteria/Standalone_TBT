package hmgapi.mods.projection.carplay;
/** Synthetic physical-link transitions; no phone facility label required. */
public final class MapGradeAttributesTest {
 static final long X=127*360000L,Y=37*360000L;
 static void ok(boolean b){if(!b)throw new AssertionError(StockMapJunction.diagnostic());}
 static long[] road(int id,long ax,long ay,long bx,long by,int over,int under){return new long[]{1,id,0,100,2,ax,ay,bx,by,ax,ay,bx,by,over,under};}
 static String data(int version,int target,long[]... rows){StringBuilder b=new StringBuilder("1000,").append(version).append(",0,").append(rows.length).append(',').append(rows.length).append(",1");if(version==5)b.append(",56,").append(target);for(long[] r:rows)for(long v:r)b.append(',').append(v);return b.toString();}
 static float[] draw(String s,int type,int exit){return StockMapJunction.drawTarget(s,1100,127,37-.0005,0,"Android Auto",type,0,true,exit,56,400,77);}
 public static void main(String[] args){
  long[] incoming=road(1,X,Y-324,X,Y,0,0),under=road(2,X,Y,X,Y+324,0,1),over=road(2,X,Y,X,Y+324,1,0);
  long[] right=road(3,X,Y,X+405,Y+324,0,0),left=road(4,X,Y,X-405,Y+324,0,0);
  float[] f=draw(data(5,77,incoming,under),3,0);ok(f!=null&&f[0]==46&&f[2]==270);
  f=draw(data(5,77,incoming,over),3,0);ok(f!=null&&f[0]==44);
  f=draw(data(5,77,incoming,under,right),2,45);ok(f!=null&&f[0]==46&&f[2]==90);
  f=draw(data(5,77,incoming,over,left),1,-45);ok(f!=null&&f[0]==43&&f[2]==270);
  ok(draw(data(5,78,incoming,under),3,0)==null); // previous maneuver's map result
  ok(draw(data(4,77,incoming,under),3,0)==null); // current-position snapshot is insufficient
  ok(draw(data(5,77,incoming,road(2,X,Y,X,Y+324,0,0)),3,0)==null);
  ok(draw(data(5,77,incoming,road(2,X,Y,X,Y+324,1,1)),3,0)==null);
  long[] alreadyUnder=road(1,X,Y-324,X,Y,0,1);
  ok(draw(data(5,77,alreadyUnder,under),3,0)==null);
  ok(draw(data(5,77,incoming,under).replace("1000,","-3000,"),3,0)==null);
  System.out.println("Future map grade attributes: under/over entry and side roads without phone text; stale/target/current-only/conflicting flag guards passed");
 }
}
