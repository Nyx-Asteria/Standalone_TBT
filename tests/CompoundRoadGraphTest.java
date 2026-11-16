package hmgapi.mods.projection;
import java.util.*;
public final class CompoundRoadGraphTest {
 public static final long X=127*360000L,Y=37*360000L;
 static long[] row(int id,long ax,long ay,long bx,long by,int length){return new long[]{1,id,0,length,2,X+ax,Y+ay,X+bx,Y+by,X+ax,Y+ay,X+bx,Y+by,0,0};}
 public static final class Fixture implements StockMapLookahead.Reader {
  final List<long[]> roads=new ArrayList<long[]>();boolean incomplete;int reads;
  public long[] read(long p,long l,int d){reads++;if(incomplete)return null;long[] in=null;for(long[] r:roads)if(r[0]==p&&r[1]==l)in=r;if(in==null)return null;return node(in,d);}
  long[] node(long[] in,int d){long x=in[d==1?11:5],y=in[d==1?12:6];List<long[]> incident=new ArrayList<long[]>();
   for(long[] r:roads)if(r[5]==x&&r[6]==y||r[11]==x&&r[12]==y)incident.add(r);
   long[] a=new long[5+15*(incident.size()+1)];a[0]=4;a[2]=incident.size()+1;a[3]=incident.size();a[4]=1;
   System.arraycopy(in,0,a,5,15);int at=20;for(long[] r:incident){System.arraycopy(r,0,a,at,15);at+=15;}return a;
  }
 }
 public static Fixture fixture(int kind){
  Fixture f=new Fixture();f.roads.add(row(1,0,-400,0,0,124));
  if(kind==1){
   f.roads.add(row(2,0,0,100,100,42));f.roads.add(row(3,100,100,0,200,42));f.roads.add(row(4,0,200,-100,100,42));f.roads.add(row(5,-100,100,0,0,42));
   f.roads.add(row(6,100,100,1000,100,250));f.roads.add(row(7,0,200,0,1200,300));f.roads.add(row(8,-100,100,-1000,100,250));
  }else{
   int sign=kind==2?-1:1;
   f.roads.add(row(2,0,0,0,50,15));f.roads.add(row(3,0,50,sign*50,0,22));f.roads.add(row(4,sign*50,0,0,0,12));
   f.roads.add(row(5,0,50,0,1000,290));f.roads.add(row(6,sign*50,0,sign*1000,0,240));
  }
  if(kind==3){
   f.roads.set(0,row(1,0,-50,0,0,15));
   f.roads.add(row(7,50,0,0,-50,22));
   f.roads.add(row(8,0,-400,0,-50,110));
  }
  return f;
 }
 public static long[] sample(int kind){Fixture f=fixture(kind);return CompoundRoadGraph.expand(StockMapLookahead.frame(f.node(f.roads.get(0),1),120,77),kind==1?1:0,f);}
 static void ok(boolean b){if(!b)throw new AssertionError(CompoundRoadGraph.lastReason);}
 public static void main(String[] args)throws Exception{
  for(int kind:new int[]{0,1,2}){long[] a=sample(kind);ok(a!=null&&a[0]==6&&a[6]==77);CompoundRoadGraph.Graph g=CompoundRoadGraph.decode(a);ok(g!=null&&CompoundRoadGraph.cycle(g)!=null);ok(StockRoadGeometry.decode(a)!=null);
   for(int n=0;n<a.length;n++)ok(CompoundRoadGraph.decode(Arrays.copyOf(a,n))==null);
  }
  Fixture entry=fixture(1);
  long[] entrance=StockMapLookahead.find(entry.node(entry.roads.get(0),1),1,127,37-.001,238,77,1,entry);
  ok(entrance!=null&&entrance[0]==6&&entrance[10]==1&&entry.reads<28);
  Fixture block=fixture(1);ok(CompoundRoadGraph.expand(StockMapLookahead.frame(block.node(block.roads.get(0),1),120,77),0,block)==null);
  for(int k:new int[]{0,1,2}){Fixture bounded=fixture(k);ok(CompoundRoadGraph.expand(StockMapLookahead.frame(bounded.node(bounded.roads.get(0),1),120,77),k==1?1:0,bounded)!=null);ok(bounded.reads<28);}
  Fixture tied=fixture(0);
  long[] atEntry=StockMapLookahead.frame(tied.node(tied.roads.get(0),1),120,77);
  long[] atIsland=StockMapLookahead.frame(tied.node(tied.roads.get(1),1),124,77);
  ok(StockMapLookahead.finishCompound(atEntry,atIsland,4,6,0,tied)!=null);
  long[] outside=StockMapLookahead.frame(tied.node(tied.roads.get(4),1),124,77);
  ok(StockMapLookahead.finishCompound(atEntry,outside,4,6,0,tied)==null);
  Fixture f=fixture(0);f.incomplete=true;ok(CompoundRoadGraph.expand(StockMapLookahead.frame(f.node(f.roads.get(0),1),120,77),0,f)==null);
  f=fixture(0);f.roads.remove(2);ok(CompoundRoadGraph.expand(StockMapLookahead.frame(f.node(f.roads.get(0),1),120,77),0,f)==null);
  f=fixture(0);f.roads.get(2)[14]=1;ok(CompoundRoadGraph.expand(StockMapLookahead.frame(f.node(f.roads.get(0),1),120,77),0,f)==null);
  f=fixture(0);f.roads.add(row(10,0,0,-40,0,12));f.roads.add(row(11,-40,0,-20,30,12));f.roads.add(row(12,-20,30,0,0,12));
  ok(CompoundRoadGraph.expand(StockMapLookahead.frame(f.node(f.roads.get(0),1),120,77),0,f)==null);
  if(args.length>0){
   final Map<String,long[]> saved=new HashMap<String,long[]>();
   for(String line:java.nio.file.Files.readAllLines(java.nio.file.Paths.get(args[0]))){String[] flds=line.split("\t");String[] words=flds[1].split(",");long[] a=new long[words.length];for(int i=0;i<a.length;i++)a[i]=Long.parseLong(words[i]);saved.put(flds[0],a);}
   StockMapLookahead.Reader reader=new StockMapLookahead.Reader(){public long[] read(long p,long l,int d){
    for(long[] source:saved.values())for(int at=5;at<source.length;at+=15)if(source[at]==p&&source[at+1]==l){
     String end=CompoundRoadGraph.node(source[at+(d==1?11:5)],source[at+(d==1?12:6)]);long[] result=saved.get(end);
     if(result!=null){long[] copy=result.clone();System.arraycopy(source,at,copy,5,15);return copy;}
    }return null;
   }};
   int accepted=0,refused=0;Map<String,Integer> reasons=new TreeMap<String,Integer>();
   for(long[] source:saved.values())for(int kind=0;kind<=1;kind++){
    long[] out=CompoundRoadGraph.expand(StockMapLookahead.frame(source,100,77),kind,reader);
    if(out!=null){accepted++;ok(CompoundRoadGraph.decode(out)!=null);}else{refused++;String why=CompoundRoadGraph.lastReason;reasons.put(why,reasons.containsKey(why)?reasons.get(why)+1:1);}
   }
   System.out.println("Saved physical nodes="+saved.size()+" expanded="+accepted+" refused="+refused+" reasons="+reasons);
  }
  System.out.println("Physical compound graphs: left/right island and full ring, bounded reads, missing/open/multiple/grade-separated cycles and serialized truncation refusal passed");
 }
}
