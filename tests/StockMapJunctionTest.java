package hmgapi.mods.projection.carplay;
public final class StockMapJunctionTest {
 static final long X=127*360000L,Y=37*360000L;
 static void ok(boolean x){if(!x)throw new AssertionError();}
 static long[] row(long id,long ax,long ay,long bx,long by){return new long[]{1,id,0,100,2,ax,ay,bx,by,ax,ay,bx,by};}
 static String encoded(long at,long[]... rows){StringBuilder b=new StringBuilder().append(at).append(",1,0,").append(rows.length);for(long[] r:rows)for(long n:r)b.append(',').append(n);return b.toString();}
 static float[] draw(String data,long now,String app,int type,boolean hasExit,int exit,float distance,double heading){return StockMapJunction.draw(data,now,127,37-.0005,heading,app,type,0,hasExit,exit,distance,400);}
 public static void main(String[] args)throws Exception{
  long[] current=row(1,X,Y-324,X,Y),west=row(2,X,Y,X-405,Y),east=row(3,X,Y,X+405,Y),north=row(4,X,Y,X,Y+324);
  String four=encoded(1000,current,west,current,east,north);
  float[] left=draw(four,1100,"네이버 지도",1,false,0,56,0);
  ok(left!=null && left[0]==13 && left[1]==2065 && left[2]==270 && left[3]==56);
  ok(draw(four,1100,"TMAP",2,false,0,56,0)[2]==90);
  ok(draw(four,1100,"TMAP",3,false,0,56,0)[2]==0);
  for(double heading:new double[]{0,15,345}) {
   float[] stable=draw(four,1100,"TMAP",2,false,0,56,heading);
   ok(stable!=null && stable[1]==2065 && stable[2]==90);
  }
  float[] near=StockMapJunction.draw(four,1100,127,37-.000036,0,"TMAP",1,0,false,0,4,400);
  ok(near!=null && near[3]==4);
  ok(draw(four,1100,"카카오내비",1,false,0,56,0)!=null);
  ok(draw(four,4000,"TMAP",1,false,0,56,0)==null);
  ok(draw(four,900,"TMAP",1,false,0,56,0)==null);
  ok(draw(four,1100,"TMAP",1,false,0,200,0)==null);
  ok(draw(four,1100,"TMAP",1,false,0,56,180)==null);
  ok(draw(four,1100,"TMAP",1,true,90,56,0)==null);
  ok(draw(four,1100,"TMAP",49,false,0,56,0)==null);
  ok(draw(encoded(1000,current,current,north),1100,"TMAP",3,false,0,56,0)==null);
  ok(draw(encoded(1000,current,east,north),1100,"TMAP",1,false,0,56,0)==null);
  ok(StockMapJunction.requestedRange("TMAP",1050,1050));
  ok(!StockMapJunction.requestedRange("TMAP",1201,1050));
  long[][] physical={current,west,current,east,north};
  StringBuilder forward=new StringBuilder("1000,3,0,5,4,1,400,77");
  for(long[] row:physical)for(long v:row)forward.append(',').append(v);
  float[] at400=StockMapJunction.drawTarget(forward.toString(),1100,127,37-.0036,0,"TMAP",1,0,false,0,400,400,77);
  ok(at400!=null&&at400[0]==13&&at400[1]==2065&&at400[2]==270);
  ok(StockMapJunction.drawTarget(forward.toString(),1100,127,37-.0036,0,"TMAP",1,0,false,0,400,399,77)==null);
  ok(StockMapJunction.drawTarget(forward.toString(),1100,127,37-.0036,0,"TMAP",1,0,false,0,400,400,78)==null);
  String highway=forward.toString().replace(",400,77",",1050,77");
  float[] at1050=StockMapJunction.drawTarget(highway,1100,127,37-.00944,0,"TMAP",23,0,false,0,1050,1050,77);
  ok(at1050!=null&&at1050[0]==13&&at1050[2]==90);
  for(float[] frame:new float[][]{left,at400,at1050})ok((((int)frame[1])&PhoneJunction.roadBit(Math.round(frame[2])))!=0);
  ok(StockMapJunction.requestedRange("TMAP",1000));
  ok(!StockMapJunction.requestedRange("TMAP",1001));
  ok(StockMapJunction.requestedRange("Apple Maps",56));
  ok(StockMapJunction.requestedRange(null,56));
  ok(StockMapJunction.requestedRange("Android Auto",56));
  ok(!StockMapJunction.requestedRange("TMAP",Float.NaN));
  ok(TollEnlargement.draw(178,999)[7]==90);
  ok(TollEnlargement.draw(184,40)[7]==10);
  ok(TollEnlargement.draw(184,1000)==null);
  ok(TollEnlargement.draw(181,40)==null);
  ok(TollEnlargement.draw(178,Float.NaN)==null);
  if(args.length>0) {
   String raw=null;
   for(String line:java.nio.file.Files.readAllLines(java.nio.file.Paths.get(args[0])))if(line.startsWith("geometry="))raw=line.substring(9);
   ok(raw!=null);String[] words=raw.replace("[", "").replace("]", "").replace(" ", "").split(",");
   long[] v=new long[words.length];for(int i=0;i<v.length;i++)v[i]=Long.parseLong(words[i]);
   StringBuilder encoded=new StringBuilder("1000");for(long n:v)encoded.append(',').append(n);
   double lon=v[14]/360000.0,lat=v[15]/360000.0;
   double heading=StockMapJunction.bearing(v[14]-v[12],v[15]-v[13],lat);
   heading=(heading+360)%360;
   double r=Math.toRadians(heading),delta=Math.toDegrees(60/6371000.0);
   double positionLat=lat-delta*Math.cos(r),positionLon=lon-delta*Math.sin(r)/Math.cos(Math.toRadians(lat));
   float[] actual=StockMapJunction.draw(encoded.toString(),1100,positionLon,positionLat,heading,"네이버지도",1,0,false,0,60,400);
   ok(actual!=null && actual[0]==13);
   System.out.println("Actual captured geometry: deduplicated branch match and left-turn mask passed (simulated approach position)");
  }
  long[] diagonal=row(8,X,Y,X+405,Y+324),diagonal2=row(9,X,Y,X+405,Y+50);
  String island=encoded(1000,current,north,diagonal);
  ok(draw(island,1100,"TMAP",2,false,0,56,0)!=null);
  ok(draw(island,1100,"TMAP",2,true,90,56,0)==null); // true supplied angle stays strict
  ok(draw(encoded(1000,current,north,diagonal,diagonal2),1100,"TMAP",2,false,0,56,0)==null);
  System.out.println("Stock map junction: four arms/duplicate, phone/GPS distance, heading, stale/future, app/type/branch guards; 1km request boundary and toll approach passed");
 }
}
