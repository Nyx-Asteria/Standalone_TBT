package hmgapi.mods.projection.carplay;
import java.util.*;
/** Render only physical external arms of a verified connected cycle. */
final class CompoundJunction {
 static String reason;
 static String node;
 static double angle(double a){return StockMapJunction.angle(a);}
 static double bearing(long[] a,long[] b){return StockMapJunction.bearing(b[0]-a[0],b[1]-a[1],(a[1]+b[1])/720000.0);}
 static final class Arm {String id;double road,display;int bit;Arm(String i,double r,double d){id=i;road=r;display=d;bit=PhoneJunction.roadBit((int)Math.round(d));}}
 static float[] fail(String why){reason=why;return null;}
 /** A second lane reconnecting to the same fully observed approach is not another junction arm. */
 static boolean approachReturn(CompoundGeometry.Graph g,Set<String> cycle,CompoundGeometry.Edge edge,String at) {
  CompoundGeometry.Edge incoming=g.edges.get(g.incoming);
  String upstream=incoming.other(g.root);
  if(upstream==null||!upstream.equals(edge.other(at))||!g.queried.contains(upstream))return false;
  int independent=0;
  for(CompoundGeometry.Edge other:g.at(upstream)){
   if(other.id.equals(incoming.id)||other.id.equals(edge.id))continue;
   if(cycle.contains(other.id))return false;
   independent++;
  }
  return independent==1 && incoming.raw[13]==0 && incoming.raw[14]==0 && edge.raw[13]==0 && edge.raw[14]==0;
 }
 /** Follow only the unique observed right-hand-traffic ring, never an invented shortcut. */
 static double rotaryArc(CompoundGeometry.Graph g,Set<String> cycle,String target,boolean full){
  CompoundGeometry.Edge first=null;
  for(CompoundGeometry.Edge e:g.at(g.root))if(cycle.contains(e.id)){
   double turn=angle(bearing(e.point(g.root),e.near(g.root))-g.approach);
   if(turn>=0&&turn<180){if(first!=null)return Double.NaN;first=e;}
  }
  if(first==null)return Double.NaN;String at=g.root,previous=null;double length=0;
  for(int n=0;n<=cycle.size();n++){
   if(at.equals(target)&&(length>0||!full))return length;
   CompoundGeometry.Edge next=previous==null?first:null;
   if(next==null)for(CompoundGeometry.Edge e:g.at(at))if(cycle.contains(e.id)&&!e.id.equals(previous)){
    if(next!=null)return Double.NaN;next=e;
   }
   if(next==null)return Double.NaN;length+=next.raw[3];at=next.other(at);previous=next.id;
  }
  return Double.NaN;
 }
 static float[] draw(long[] data,int target,double lon,double lat,int type,int junction,boolean hasExit,int exit,float meters,int limit){
  reason="compound-invalid";node=null;CompoundGeometry.Graph g=CompoundGeometry.decode(data);
  if(g==null||g.target!=target)return fail("compound-target-or-format");
  boolean rotary=junction==1||type==6||type==7;
  double tolerance=Math.max(15,Math.min(40,meters*.10));
  if(!rotary&&Math.abs(g.distance-meters)>tolerance)return fail("compound-distance-mismatch");
  if(StockMapJunction.distance(lon,lat,g.x/360000.0,g.y/360000.0)>meters+40)return fail("compound-node-too-far");
  Set<String> cycle=CompoundGeometry.cycle(g);if(cycle==null)return fail(CompoundGeometry.lastReason);
  if(rotary!=(g.kind==1))return fail("compound-maneuver-kind");
  if(rotary&&!hasExit)return fail("rotary-route-exit-unknown");
  HashSet<String> vertices=new HashSet<String>();double cx=0,cy=0;
  for(String id:cycle){CompoundGeometry.Edge e=g.edges.get(id);vertices.add(e.a);vertices.add(e.b);}
  for(String key:vertices){String[] xy=key.split(":");cx+=Long.parseLong(xy[0]);cy+=Long.parseLong(xy[1]);}
  long[] centre={Math.round(cx/vertices.size()),Math.round(cy/vertices.size())};
  double axis=rotary?bearing(new long[]{g.x,g.y},centre):g.approach;
  ArrayList<Arm> arms=new ArrayList<Arm>();int roads=0;
  for(CompoundGeometry.Edge e:g.edges.values()){
   if(cycle.contains(e.id))continue;
   boolean a=vertices.contains(e.a),b=vertices.contains(e.b);
   if(a==b){if(a)return fail("cycle-has-unresolved-chord");continue;}
   String at=a?e.a:e.b;
   if(!rotary&&!e.id.equals(g.incoming)&&approachReturn(g,cycle,e,at))continue;
   long[] pt=e.point(at),near=e.near(at);
   if(Arrays.equals(pt,near))return fail("compound-zero-arm-vector");
   double direction=angle(bearing(pt,near)-g.approach);
   double display=rotary?angle(bearing(centre,near)-axis):direction;
   Arm arm=new Arm(e.id,direction,display);arms.add(arm);
   if(!e.id.equals(g.incoming)){
    if(arm.bit==0&&Math.abs(display)<157.5)return fail("compound-unrepresentable-arm");
    roads|=arm.bit;
   }
  }
  boolean ramp=type==8||type==9||type==22||type==23||type==52||type==53;
  boolean left=type==1||type==20||type==47||type==22||type==52||(ramp&&hasExit&&exit<0);
  boolean right=type==2||type==21||type==48||type==23||type==53||(ramp&&hasExit&&exit>0);
  if(!rotary&&!left&&!right&&type!=3)return fail("compound-maneuver-type");
  double requested=hasExit?exit:left?-90:right?90:0;
  double best=999,second=999;Arm selected=null;Set<Integer> choices=new HashSet<Integer>();
  for(Arm arm:arms){
   if(!rotary&&arm.id.equals(g.incoming))continue;
   if(!hasExit&&((left&&arm.road< -12&&arm.road>=-157.5)||(right&&arm.road>12&&arm.road<=157.5)))choices.add(arm.bit);
   double err=Math.abs(angle(arm.road-requested));
   if(err<best){second=best;best=err;selected=arm;}else if(err<second)second=err;
  }
  // A divided physical road may have a paired entrance/exit in the same sector.
  if(selected==null||best>(hasExit?35:60)||(!hasExit&&(left||right)&&choices.size()!=1))return fail("compound-no-route-arm");
  if(second-best<15){
   for(Arm arm:arms)if(arm!=selected&&Math.abs(angle(arm.road-requested))-best<15
       &&arm.bit!=selected.bit)return fail("compound-ambiguous-route-arm");
  }
  if(roads==0)return fail("compound-no-external-arms");
  if(rotary&&Math.abs(g.distance-meters)>tolerance){
   CompoundGeometry.Edge exitEdge=g.edges.get(selected.id);
   String exitNode=vertices.contains(exitEdge.a)?exitEdge.a:exitEdge.b;
   double arc=rotaryArc(g,cycle,exitNode,selected.id.equals(g.incoming));
   if(Double.isNaN(arc)||Math.abs(g.distance+arc-meters)>tolerance)return fail("rotary-arc-distance-mismatch");
  }
  float selectedDirection;
  if(rotary&&(selected.id.equals(g.incoming)||selected.bit==0))selectedDirection=180;
  else {if(selected.bit==0)return fail("compound-route-sector");selectedDirection=PhoneJunction.displayAngle(selected.bit);}
  double radius=0;
  for(String key:vertices){String[] xy=key.split(":");radius=Math.max(radius,StockMapJunction.distance(centre[0]/360000.0,centre[1]/360000.0,Long.parseLong(xy[0])/360000.0,Long.parseLong(xy[1])/360000.0));}
  node=g.edges.get(g.incoming).raw[0]+":"+centre[0]+":"+centre[1]+":"+(int)Math.ceil(radius);
  reason=rotary?"physical-rotary-arms":"physical-island-arms";
  return new float[]{rotary?32:13,roads,selectedDirection,meters,0,0,0,PhoneJunction.barLevel(meters,limit),0};
 }
}
