package hmgapi.mods.projection;
import java.util.*;
/** Bounded forward walk. Never chooses a fork with an ambiguous continuation. */
final class StockMapLookahead {
 static String lastReason="none";
 static long[] finish(long[] candidate,double best,double second) {
  if(candidate!=null && second-best>=8){lastReason="matched-nearest-node";return candidate;}
  return fail(candidate==null?"target-distance-not-found":"multiple-nearby-target-nodes");
 }
 static long[] finishCompound(long[] candidate,long[] alternate,double best,double second,int kind,Reader reader){
  if(candidate!=null&&alternate!=null&&second-best<8){
   long[] compound=CompoundRoadGraph.expand(candidate,kind,reader);
   CompoundRoadGraph.Graph graph=CompoundRoadGraph.decode(compound);
   if(graph!=null){
    Set<String> cycle=CompoundRoadGraph.cycle(graph);List<Row> otherRows=frameRows(alternate);
    if(cycle!=null&&otherRows!=null){
     HashSet<String> roots=new HashSet<String>();Row first=otherRows.get(0);
     roots.add(CompoundRoadGraph.node(first.points[0][0],first.points[0][1]));
     long[] end=first.points[first.points.length-1];roots.add(CompoundRoadGraph.node(end[0],end[1]));
     for(Row row:otherRows){HashSet<String> ends=new HashSet<String>();ends.add(CompoundRoadGraph.node(row.points[0][0],row.points[0][1]));long[] ep=row.points[row.points.length-1];ends.add(CompoundRoadGraph.node(ep[0],ep[1]));roots.retainAll(ends);}
     if(roots.size()==1){String root=roots.iterator().next();for(String edge:cycle){CompoundRoadGraph.Edge e=graph.edges.get(edge);if(e.a.equals(root)||e.b.equals(root)){lastReason="matched-same-compound-cycle";return compound;}}}
    }
   }
  }
  return finish(candidate,best,second);
 }
 static List<Row> frameRows(long[] frame){
  if(frame==null||frame.length<7)return null;long[] node=new long[frame.length-2];node[0]=frame[0]==5?4:2;System.arraycopy(frame,1,node,1,4);System.arraycopy(frame,7,node,5,frame.length-7);return rows(node);
 }
 static long[] fail(String reason){lastReason=reason;return null;}
 interface Reader {long[] read(long parcel,long link,int direction);}
 static final double SCALE=360000.0,R=6371000;
 static final class Row {
  long parcel,link;int over,under;double length;long[][] points;
  Row(long[] a,int at) {
   if(a[0]==4){over=(int)a[at+13];under=(int)a[at+14];}
   parcel=a[at];link=a[at+1];length=a[at+3];int n=(int)a[at+4];
   ArrayList<long[]> p=new ArrayList<long[]>();
   for(int j=0;j<(n==2?2:4);j++) {
    long[] q={a[at+5+j*2],a[at+6+j*2]};
    if(p.isEmpty()||!Arrays.equals(q,p.get(p.size()-1)))p.add(q);
   }
   points=p.toArray(new long[p.size()][]);
  }
 }
 static List<Row> rows(long[] a) {
  if(a==null||a.length<5||(a[0]!=2&&a[0]!=4)||a[1]!=0||a[2]<1||a[2]>9||a[4]!=1||a.length!=5+(a[0]==4?15:13)*a[2])return null;
  List<Row> out=new ArrayList<Row>();Set<String> seen=new HashSet<String>();
  for(int i=0;i<a[2];i++) {
   int at=5+(a[0]==4?15:13)*i;
   if(a[0]==4&&(a[at+13]<0||a[at+13]>1||a[at+14]<0||a[at+14]>1))return null;
   if(a[at+2]!=0||a[at+4]<2||a[at+4]>4096)return null;
   if(seen.add(a[at]+":"+a[at+1]))out.add(new Row(a,at));
  }
  return out.size()==a[3]?out:null;
 }
 static double dx(long a,long b,double lat){return (b-a)/SCALE*Math.PI/180*R*Math.cos(Math.toRadians(lat));}
 static double dy(long a,long b){return (b-a)/SCALE*Math.PI/180*R;}
 static double remaining(Row row,int direction,double lon,double lat) {
  if(row.points.length<2||row.length<=0)return -1;
  double total=0,best=Double.MAX_VALUE,along=0;
  for(int i=1;i<row.points.length;i++) {
   long[] a=row.points[i-1],b=row.points[i];double x=dx(a[0],b[0],lat),y=dy(a[1],b[1]);
   double len=Math.hypot(x,y);if(len==0)continue;
   double vx=(lon-a[0]/SCALE)*Math.PI/180*R*Math.cos(Math.toRadians(lat));
   double vy=(lat-a[1]/SCALE)*Math.PI/180*R;
   double fraction=Math.max(0,Math.min(1,(vx*x+vy*y)/(len*len)));
   double off=Math.hypot(vx-fraction*x,vy-fraction*y);
   if(off<best){best=off;along=total+fraction*len;}
   total+=len;
  }
  if(best>25||total<1)return -1;
  return row.length*(direction==1?1-along/total:along/total);
 }
 static long[] frame(long[] node,double distance,int token) {
  long[] out=new long[node.length+2];out[0]=node[0]==4?5:3;System.arraycopy(node,1,out,1,4);
  out[5]=Math.round(distance);out[6]=token;System.arraycopy(node,5,out,7,node.length-5);return out;
 }
 static long[] find(long[] initial,int direction,double lon,double lat,float target,int token,Reader reader) {
  return find(initial,direction,lon,lat,target,token,0,reader);
 }
 static long[] find(long[] initial,int direction,double lon,double lat,float target,int token,int kind,Reader reader) {
  lastReason="searching";
  List<Row> first=rows(initial);if(first==null||direction<0||direction>1||target<=0||target>1200)return fail("incomplete-current-node-or-request");
  double distance=remaining(first.get(0),direction,lon,lat);if(distance<0)return fail("vehicle-off-current-polyline");
  long[] current=initial;Set<String> visited=new HashSet<String>();long deadline=System.nanoTime()+800000000L;
  double tolerance=Math.max(15,Math.min(40,target*.1));
  long[] candidate=null,alternate=null;double candidateError=999,secondError=999;
  for(int hop=0;hop<32 && System.nanoTime()<deadline;hop++) {
   List<Row> data=rows(current);if(data==null)return fail("incomplete-forward-node");Row in=data.get(0);
   if(!visited.add(in.parcel+":"+in.link+":"+direction))return fail("cycle");
   boolean gradeBoundary=false;
   for(Row r:data)if(r.over!=in.over||r.under!=in.under)gradeBoundary=true;
   if((data.size()>=3||gradeBoundary) && Math.abs(distance-target)<=tolerance){
    double error=Math.abs(distance-target);
    if(error<2){lastReason="matched-hop-"+hop;return frame(current,distance,token);}
    if(error<candidateError){alternate=candidate;secondError=candidateError;candidateError=error;candidate=frame(current,distance,token);}
    else if(error<secondError){secondError=error;alternate=frame(current,distance,token);}
   }
   if(distance>target+tolerance)return finishCompound(candidate,alternate,candidateError,secondError,kind,reader);
   int end=direction==1?in.points.length-1:0,previous=direction==1?end-1:1;
   long[] node=in.points[end],before=in.points[previous];double nodeLat=node[1]/SCALE;
   double ix=dx(before[0],node[0],nodeLat),iy=dy(before[1],node[1]);
   double il=Math.hypot(ix,iy);if(il==0)return fail("zero-incoming-vector");
   Row bestRow=null;int bestDir=0;double best=999,second=999;
   for(int i=1;i<data.size();i++) {
    Row r=data.get(i);int from=-1,near=-1,dir=0;
    if(Arrays.equals(r.points[0],node)){from=0;near=1;dir=1;}
    else if(Arrays.equals(r.points[r.points.length-1],node)){from=r.points.length-1;near=from-1;dir=0;}
    if(from<0)continue;
    double ox=dx(r.points[from][0],r.points[near][0],nodeLat),oy=dy(r.points[from][1],r.points[near][1]);
    double ol=Math.hypot(ox,oy);if(ol==0)continue;
    double angle=Math.toDegrees(Math.acos(Math.max(-1,Math.min(1,(ix*ox+iy*oy)/(il*ol)))));
    if(angle<best){second=best;best=angle;bestRow=r;bestDir=dir;}else if(angle<second)second=angle;
   }
   if(bestRow==null||best>30||second-best<15||bestRow.length<=0){
    if(kind==1&&distance>0&&distance<=target+tolerance&&target-distance<=1200){
     long[] ring=CompoundRoadGraph.expand(frame(current,distance,token),kind,reader);
     if(ring!=null){lastReason="rotary-entrance-physical-cycle";return ring;}
    }
    return candidate==null?fail("ambiguous-forward-continuation"):finishCompound(candidate,alternate,candidateError,secondError,kind,reader);
   }
   distance+=bestRow.length;direction=bestDir;
   current=reader.read(bestRow.parcel,bestRow.link,direction);
  }
  return fail("search-budget-or-hop-limit");
 }
}
