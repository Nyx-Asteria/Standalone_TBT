package hmgapi.mods.projection.carplay;
/** Translated/rekeyed small junction fixture: no vehicle map IDs or original coordinates. */
public final class InternalApproachTest {
 static void ok(boolean b){if(!b)throw new AssertionError(StockMapJunction.diagnostic());}
 static long[] sample(){return new long[]{6,0,8,4,1,417,77,45719943,13319917,208867,0,1,1,0,29,2,45720000,13320000,45719943,13319917,45720000,13320000,45719943,13319917,0,0,1,2,0,26,3,45720036,13319958,45720014,13319948,45720014,13319948,45719943,13319917,0,0,1,3,0,36,5,45719943,13319917,45719904,13319900,45719833,13319870,45719811,13319862,0,0,1,4,0,40,6,45720186,13320010,45720175,13320007,45720048,13319963,45720036,13319958,0,0,1,5,0,16,4,45720036,13319958,45720017,13319970,45720011,13319978,45720000,13320000,0,0,1,6,0,63,7,45720000,13320000,45719967,13319980,45719851,13319902,45719811,13319862,0,0,1,7,0,54,6,45719811,13319862,45719793,13319855,45719646,13319825,45719599,13319822,0,0,1,8,0,351,23,45720000,13320000,45720013,13320018,45720189,13321101,45720190,13321110,0,0,45719943,13319917,45720036,13319958,45719811,13319862,45720000,13320000};}
 static float[] draw(long[] a,int heading,float meters){StringBuilder b=new StringBuilder("1000");for(long n:a)b.append(',').append(n);
 return StockMapJunction.drawTarget(b.toString(),1100,127,37.0034,heading,"Android Auto",1,0,false,-90,meters,400,77);}
 public static void main(String[] args)throws Exception{
 long[] a=sample();float[] f=draw(a,185,392);ok(f!=null&&f[0]==13&&Integer.bitCount((int)f[1])==2);System.out.println(java.util.Arrays.toString(f));
 ok(draw(a,90,392)==null);ok(draw(a,185,300)==null);
 long[] bad=a.clone();bad[11+13]=1;ok(draw(bad,185,392)==null);
 bad=a.clone();bad[11+3]=100;ok(draw(bad,185,392)==null);
 CompoundGeometry.Graph g=CompoundGeometry.decode(a);java.util.Set<String> ring=CompoundGeometry.cycle(g);
 CompoundGeometry.Graph corrected=CompoundJunction.externalApproach(g,ring,185);
 ok(corrected!=null&&corrected.distance==a[5]-29&&corrected.root.equals(CompoundGeometry.node(127*360000,37*360000)));
 ok(g.distance==417); // original snapshot remains immutable
 g=CompoundGeometry.decode(a);long[] duplicate=g.edges.get("1:8").raw.clone();duplicate[1]=99;g.add(new CompoundGeometry.Edge(duplicate));
 ring=CompoundGeometry.cycle(g);ok(CompoundJunction.externalApproach(g,ring,185)==null);
 if(args.length>0){int raw=0,shown=0;MapJunctionDisplay latch=new MapJunctionDisplay();
 for(String file:args)for(String line:java.nio.file.Files.readAllLines(java.nio.file.Paths.get(file))){String[] row=line.split("\\t",-1);
 if(!row[0].equals("1.0.0")||!row[4].equals("AA/4/1/-1/용궁로")||Float.parseFloat(row[5])>400)continue;
 String[] pos=row[9].split(","),geo=row[10].split(",");if(pos.length<4||geo.length<8)continue;
 long now=Long.parseLong(row[3]);float meters=Float.parseFloat(row[5]);double lon=Double.parseDouble(pos[2]),lat=Double.parseDouble(pos[1]);
 float[] candidate=StockMapJunction.drawTarget(row[10],now,lon,lat,Double.parseDouble(pos[3]),"Android Auto",1,0,false,-90,meters,400,Integer.parseInt(geo[7]));
 if(candidate!=null)raw++;
 float[] displayed=latch.apply("same-left",StockMapJunction.node(),candidate,now,meters,400,lon,lat);
 if(displayed!=null){shown++;ok(displayed[0]==13&&Integer.bitCount((int)displayed[1])==2);}
 }
 ok(raw>=20&&shown>=20);System.out.println("Actual 1.0.0 road replay: valid="+raw+", displayed/held="+shown);
 }
 System.out.println("Internal approach: real external entry, single chord contraction, distance correction, three arms, heading/grade/size/ambiguity refusal passed");
 }
}
