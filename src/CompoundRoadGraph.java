package hmgapi.mods.projection;
import java.util.*;
import hmgapi.mods.projection.carplay.CompoundGeometry;
/** Bounded physical graph acquisition; only serialized arrays cross the renderer boundary. */
public final class CompoundRoadGraph extends CompoundGeometry {
 static double meters(long x,long y,long xx,long yy){double lat=(y+yy)/720000.0;return Math.hypot(StockMapLookahead.dx(x,xx,lat),StockMapLookahead.dy(y,yy));}
 static double bearing(long[] a,long[] b){return Math.toDegrees(Math.atan2(StockMapLookahead.dx(a[0],b[0],(a[1]+b[1])/720000.0),StockMapLookahead.dy(a[1],b[1])));}
 static long[] expand(long[] frame,int kind,StockMapLookahead.Reader reader){
  lastReason="searching";
  if(frame==null||frame.length<7||(frame[0]!=3&&frame[0]!=5))return fail("no-target-frame");
  int stride=frame[0]==5?15:13;
  if(frame[2]<2||frame[2]>9||frame.length!=7+stride*frame[2]||frame[4]!=1)return fail("incomplete-target-frame");
  Graph g=new Graph();g.kind=kind;g.distance=frame[5];g.target=(int)frame[6];
  for(int i=0;i<frame[2];i++){long[] r=row(frame,7+i*stride,stride);if(!valid(r))return fail("invalid-link");if(!g.add(new Edge(r)))return fail("conflicting-link");}
  Edge in=g.edges.values().iterator().next();g.incoming=in.id;
  String root=null;
  for(String n:new String[]{in.a,in.b}){boolean all=true;for(Edge e:g.edges.values())if(e.other(n)==null)all=false;if(all){if(root!=null)return fail("ambiguous-root");root=n;}}
  if(root==null)return fail("no-common-target-node");g.root=root;long[] xy=in.point(root);g.x=xy[0];g.y=xy[1];g.approach=bearing(in.near(root),xy);
  g.queried.add(root);ArrayDeque<String> queue=new ArrayDeque<String>();queue.add(root);Set<String> scheduled=new HashSet<String>();scheduled.add(root);
  long deadline=System.nanoTime()+500000000L;double radius=kind==1?180:65,maxLength=kind==1?220:85;
  while(!queue.isEmpty()){
   String n=queue.remove();
   for(Edge e:new ArrayList<Edge>(g.at(n))){
    if(e.id.equals(in.id)||e.raw[3]>maxLength)continue;
    String to=e.other(n);long[] pt=e.point(to);
    if(meters(g.x,g.y,pt[0],pt[1])>radius||scheduled.contains(to))continue;
    if(g.queried.size()>=MAX_NODES||System.nanoTime()>deadline)return fail("compound-query-budget");
    scheduled.add(to);
    long[] found=reader.read(e.raw[0],e.raw[1],e.directionFrom(n));List<StockMapLookahead.Row> checked=StockMapLookahead.rows(found);
    if(checked==null)return fail("compound-incomplete-node");
    int step=found[0]==4?15:13;
    for(int j=0;j<found[2];j++){Edge add=new Edge(row(found,5+j*step,step));if(!valid(add.raw)||add.other(to)==null||!g.add(add))return fail("compound-inconsistent-node");}
    g.queried.add(to);queue.add(to);
   }
  }
  if(cycle(g)==null)return fail(lastReason);
  lastReason=kind==1?"physical-rotary-cycle":"physical-island-cycle";return encode(g);
 }
}
