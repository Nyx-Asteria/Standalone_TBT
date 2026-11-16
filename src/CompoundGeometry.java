package hmgapi.mods.projection.carplay;
import java.util.*;
/** Pure serialized geometry. Kept with renderer to avoid Dalvik cross-loader preverification conflicts. */
public class CompoundGeometry {
 private static double dx(long a,long b,double lat){return (b-a)/360000.0*Math.PI/180*6371000.0*Math.cos(Math.toRadians(lat));}
 private static double dy(long a,long b){return (b-a)/360000.0*Math.PI/180*6371000.0;}
 public static String lastReason="none";
 public static final int HEADER=11,STRIDE=15,MAX_EDGES=64,MAX_NODES=28;
 public static String node(long x,long y){return x+":"+y;}
 public static final class Edge {
  public final long[] raw;public final String id,a,b;
  public Edge(long[] r){raw=r.clone();id=r[0]+":"+r[1];a=node(r[5],r[6]);b=node(r[11],r[12]);}
  public long[] point(String at){return at.equals(a)?new long[]{raw[5],raw[6]}:at.equals(b)?new long[]{raw[11],raw[12]}:null;}
  public long[] near(String at){return at.equals(a)?new long[]{raw[7],raw[8]}:at.equals(b)?new long[]{raw[9],raw[10]}:null;}
  public String other(String at){return at.equals(a)?b:at.equals(b)?a:null;}
  public int directionFrom(String at){return at.equals(a)?1:0;}
 }
 public static final class Graph {
  public final LinkedHashMap<String,Edge> edges=new LinkedHashMap<String,Edge>();
  public final LinkedHashSet<String> queried=new LinkedHashSet<String>();
  public long x,y;public int target,kind;public double approach,distance;
  public String root,incoming;
  public List<Edge> at(String n){List<Edge> out=new ArrayList<Edge>();for(Edge e:edges.values())if(e.a.equals(n)||e.b.equals(n))out.add(e);return out;}
  public boolean add(Edge e){Edge old=edges.get(e.id);if(old!=null&&!Arrays.equals(old.raw,e.raw))return false;edges.put(e.id,e);return edges.size()<=MAX_EDGES;}
 }
 public static long[] row(long[] a,int at,int stride){long[] r=new long[STRIDE];System.arraycopy(a,at,r,0,stride);return r;}
 public static boolean valid(long[] r){
  if(r.length!=15||r[2]!=0||r[3]<=0||r[3]>100000||r[4]<2||r[4]>4096||r[13]<0||r[13]>1||r[14]<0||r[14]>1)return false;
  for(int j=5;j<13;j++)if(r[j]<Integer.MIN_VALUE||r[j]>Integer.MAX_VALUE)return false;
  return r[5]!=r[11]||r[6]!=r[12];
 }
 public static Graph decode(long[] a){
  if(a==null||a.length<HEADER||a[0]!=6||a[1]!=0||a[2]<3||a[2]>MAX_EDGES||a[3]<3||a[3]>MAX_NODES||a[4]!=1
      ||a.length!=HEADER+STRIDE*a[2]+2*a[3]||a[5]<=0||a[5]>1300||a[6]==0||a[6]!=(int)a[6]||a[9]<0||a[9]>=360000||(a[10]!=0&&a[10]!=1))return null;
  Graph g=new Graph();g.x=a[7];g.y=a[8];g.root=node(g.x,g.y);g.approach=a[9]/1000.0;g.distance=a[5];g.target=(int)a[6];g.kind=(int)a[10];
  if(Math.abs(g.x/360000.0)>180||Math.abs(g.y/360000.0)>90)return null;
  for(int i=0;i<a[2];i++){long[] r=row(a,HEADER+i*STRIDE,STRIDE);if(!valid(r))return null;Edge e=new Edge(r);if(g.edges.containsKey(e.id)||!g.add(e))return null;if(i==0)g.incoming=e.id;}
  int at=HEADER+STRIDE*(int)a[2];for(int i=0;i<a[3];i++){String n=node(a[at++],a[at++]);if(!g.queried.add(n)||g.at(n).isEmpty())return null;}
  if(!g.queried.contains(g.root)||g.edges.get(g.incoming).other(g.root)==null)return null;
  return g;
 }
 public static long[] encode(Graph g){
  long[] a=new long[HEADER+STRIDE*g.edges.size()+2*g.queried.size()];a[0]=6;a[2]=g.edges.size();a[3]=g.queried.size();a[4]=1;a[5]=Math.round(g.distance);a[6]=g.target;a[7]=g.x;a[8]=g.y;a[9]=Math.round((g.approach+360)%360*1000)%360000;a[10]=g.kind;
  int at=HEADER;for(Edge e:g.edges.values()){System.arraycopy(e.raw,0,a,at,STRIDE);at+=STRIDE;}
  for(String n:g.queried){String[] xy=n.split(":");a[at++]=Long.parseLong(xy[0]);a[at++]=Long.parseLong(xy[1]);}return a;
 }
 public static long[] fail(String reason){lastReason=reason;return null;}
 /** A unique small cycle is required. Nearby disconnected roads cannot be combined. */
 public static Set<String> cycle(Graph g){
  List<Set<String>> found=new ArrayList<Set<String>>();Set<String> signatures=new HashSet<String>();
  HashSet<String> nodes=new HashSet<String>();nodes.add(g.root);
  int[] budget={0};walk(g,g.root,null,new ArrayList<String>(),nodes,found,signatures,budget);
  if(budget[0]>2048){lastReason="cycle-search-budget";return null;}
  if(found.size()!=1){lastReason=found.isEmpty()?"no-closed-physical-cycle":"multiple-physical-cycles";return null;}
  Set<String> chosen=found.get(0);double length=0;Set<String> vertices=new HashSet<String>();
  for(String id:chosen){Edge e=g.edges.get(id);length+=e.raw[3];vertices.add(e.a);vertices.add(e.b);if(e.raw[13]!=0||e.raw[14]!=0){lastReason="grade-separated-cycle";return null;}}
  if(length>(g.kind==1?1200:200)||vertices.size()>(g.kind==1?20:4)){lastReason="cycle-too-large";return null;}
  double twiceArea=0;String at=g.root,previous=null;int hops=0;
  do{
   Edge next=null;for(Edge e:g.at(at))if(chosen.contains(e.id)&&!e.id.equals(previous)){next=e;break;}
   if(next==null||++hops>chosen.size()){lastReason="broken-cycle-order";return null;}
   String to=next.other(at);long[] fromPt=next.point(at),toPt=next.point(to);
   double ax=dx(g.x,fromPt[0],g.y/360000.0),ay=dy(g.y,fromPt[1]);
   double bx=dx(g.x,toPt[0],g.y/360000.0),by=dy(g.y,toPt[1]);
   twiceArea+=ax*by-ay*bx;previous=next.id;at=to;
  }while(!at.equals(g.root));
  double area=Math.abs(twiceArea)/2;
  if(area<2||area>(g.kind==1?90000:1200)){lastReason="cycle-area-outside-bound";return null;}
  lastReason="unique-cycle";return chosen;
 }
 static void walk(Graph g,String at,String previous,List<String> path,Set<String> visited,List<Set<String>> found,Set<String> signatures,int[] budget){
  if(++budget[0]>2048||found.size()>1||path.size()>=(g.kind==1?20:4))return;
  for(Edge e:g.at(at)){
   if(e.id.equals(previous)||e.id.equals(g.incoming))continue;String next=e.other(at);
   if(!g.queried.contains(next))continue;
   if(next.equals(g.root)&&path.size()>=2){TreeSet<String> set=new TreeSet<String>(path);set.add(e.id);String key=set.toString();if(signatures.add(key))found.add(set);continue;}
   if(visited.contains(next))continue;visited.add(next);path.add(e.id);walk(g,next,e.id,path,visited,found,signatures,budget);path.remove(path.size()-1);visited.remove(next);
  }
 }
}
