package hmgapi.mods.projection.carplay;

import java.util.ArrayList;
import java.util.HashSet;

/** Phone/stock target agreement. Single-node or verified compound-cycle junction, no invented road branches. */
public final class StockMapJunction {
 public static final String KEY="hmgapi_stock_geometry_live_v1";
 public static final String DETAILS="hmgapi_stock_geometry_request_details";
 public static final String REQUEST="hmgapi_stock_geometry_request_until";
 private static final double SCALE=360000.0, EARTH=6371000.0;
 private StockMapJunction() {}
 private static final ThreadLocal<String> DIAGNOSTIC=new ThreadLocal<String>();
 private static final ThreadLocal<String> NODE=new ThreadLocal<String>();
 public static String diagnostic(){return DIAGNOSTIC.get();}
 public static String node(){return NODE.get();}
 private static float[] reject(String reason){DIAGNOSTIC.set(reason);return null;}
 static boolean allowedApp(String app){
  return true; // All projection apps; geometry and maneuver validation still apply.
 }
 static boolean requestedRange(String app,float meters){return requestedRange(app,meters,400);}
 static boolean requestedRange(String app,float meters,int threshold){
  return allowedApp(app) && !Float.isNaN(meters) && !Float.isInfinite(meters) && meters>0 && meters<=Math.max(1000,threshold+150);
 }
 public static boolean ownsRequest(String value,int id){
  if(value==null||value.length()>100)return false;String[] f=value.split(",");
  return (f.length==3||f.length==4)&&f[2].equals(Integer.toString(id));
 }
 static int targetId(String token){int h=token.hashCode();return h==0?1:h;}
 static double angle(double a){a%=360;if(a>180)a-=360;if(a< -180)a+=360;return a;}
 static double distance(double lon1,double lat1,double lon2,double lat2) {
  double x=Math.toRadians(lon2-lon1)*Math.cos(Math.toRadians((lat1+lat2)/2));
  double y=Math.toRadians(lat2-lat1);return Math.hypot(x,y)*EARTH;
 }
 static double bearing(double dx,double dy,double latitude){return Math.toDegrees(Math.atan2(dx*Math.cos(Math.toRadians(latitude)),dy));}
 static boolean same(long ax,long ay,long bx,long by){return ax==bx&&ay==by;}
 public static float[] draw(String encoded,long now,double lon,double lat,double heading,
         String app,int type,int junction,boolean hasExit,int exit,float meters,int threshold) {
  return drawTarget(encoded,now,lon,lat,heading,app,type,junction,hasExit,exit,meters,threshold,0);
 }
 public static float[] drawTarget(String encoded,long now,double lon,double lat,double heading,
         String app,int type,int junction,boolean hasExit,int exit,float meters,int threshold,int targetId) {
  DIAGNOSTIC.set("no-node-match");NODE.remove();
  if(!allowedApp(app)|| threshold<=0 || (Float.isNaN(meters)||Float.isInfinite(meters)) || meters<0 || meters>threshold
      || (Double.isNaN(lon)||Double.isInfinite(lon))||(Double.isNaN(lat)||Double.isInfinite(lat))||(Double.isNaN(heading)||Double.isInfinite(heading))
      || Math.abs(lon)>180 || Math.abs(lat)>90 || heading<0 || heading>=360
      || encoded==null || encoded.length()>30000)return reject("input-app-distance-or-missing-geometry");
  if(encoded!=null&&encoded.startsWith("6,0,",encoded.indexOf(',')+1)){
   try{
    String[] f=encoded.split(",");long age=now-Long.parseLong(f[0]);
    if(age<0||age>2500)return reject("snapshot-stale");
    long[] a=new long[f.length-1];for(int i=0;i<a.length;i++)a[i]=Long.parseLong(f[i+1]);
    float[] frame=CompoundJunction.draw(a,targetId,lon,lat,type,junction,hasExit,exit,meters,threshold);
    DIAGNOSTIC.set(CompoundJunction.reason);if(frame!=null)NODE.set(CompoundJunction.node);return frame;
   }catch(IllegalArgumentException error){return reject("malformed-compound-snapshot");}
  }
  if(junction!=0)return reject("rotary-physical-graph-unavailable");
  // Normal intersection maneuvers only. Grade changes, ramps and roundabouts retain their own icons.
  boolean ramp=type==8||type==9||type==22||type==23||type==52||type==53;
  boolean left=type==1||type==20||type==47||type==22||type==52||(ramp&&hasExit&&exit<0);
  boolean right=type==2||type==21||type==48||type==23||type==53||(ramp&&hasExit&&exit>0);
  if(!left&&!right&&type!=3)return reject("maneuver-type");
  double requested=hasExit?exit:left?(ramp?-45:-90):right?(ramp?45:90):0;
  if(requested< -180 || requested>180 || (left&&requested>=0)||(right&&requested<=0)||(type==3&&Math.abs(requested)>22))return reject("phone-angle-conflict");
  try {
   String[] fields=encoded.split(",");if(fields.length<4)return reject("snapshot-format");
   long age=now-Long.parseLong(fields[0]);if(age<0||age>2500)return reject("snapshot-stale");
   long[] a=new long[fields.length-1];for(int i=0;i<a.length;i++)a[i]=Long.parseLong(fields[i+1]);
   if(a.length<3||(a[0]<1||a[0]>5)||a[1]!=0||a[2]<1||a[2]>9)return reject("snapshot-format");
   int header=a[0]==1?3:a[0]==2||a[0]==4?5:7;
   int stride=a[0]>=4?15:13;
   boolean forward=a[0]==3||a[0]==5;
   if(a.length!=header+stride*a[2])return reject("snapshot-format");
   if(a[0]>=2 && a[4]!=1)return reject("incomplete-physical-node");
   if(targetId!=0 && !forward)return reject("forward-node-unavailable");
   if(forward && a[6]!=targetId)return reject("previous-maneuver-snapshot");
   ArrayList<long[]> rows=new ArrayList<long[]>();HashSet<String> seen=new HashSet<String>();
   for(int i=0;i<a[2];i++) {
    int at=header+i*stride;if(a[at+2]!=0)continue;
    if(a[at+4]<2||a[at+4]>4096)return null;
    long[] row=new long[stride];System.arraycopy(a,at,row,0,stride);
    if(stride==15&&(row[13]<0||row[13]>1||row[14]<0||row[14]>1))return reject("invalid-grade-attributes");
    for(int j=5;j<13;j++)if(row[j]<Integer.MIN_VALUE||row[j]>Integer.MAX_VALUE)return null;
    if(seen.add(row[0]+":"+row[1]))rows.add(row);
   }
   if(a[header+2]!=0||rows.size()<2||(a[0]>=2 && rows.size()!=a[3]))return reject("fewer-than-three-roads");
   long[] current=rows.get(0);float[] result=null;
   for(int end=0;end<2;end++) {
    int p=end==0?5:11;long x=current[p],y=current[p+1];
    double nodeLon=x/SCALE,nodeLat=y/SCALE;
    if(Math.abs(nodeLon)>180||Math.abs(nodeLat)>90)continue;
    double d=distance(lon,lat,nodeLon,nodeLat);
    if(d>threshold+50||Math.abs((forward?a[5]:d)-meters)>Math.max(15,Math.min(40,meters*.10))) {
     DIAGNOSTIC.set("node-distance-mismatch");continue;
    }
    double towards=bearing(nodeLon-lon,nodeLat-lat,lat);
    if(!forward && d>10 && Math.abs(angle(towards-heading))>65){DIAGNOSTIC.set("node-behind-heading");continue;}
    int approachPoint=end==0?7:9;
    if(same(current[approachPoint],current[approachPoint+1],x,y))continue;
    double approach=bearing(x-current[approachPoint],y-current[approachPoint+1],nodeLat);
    if(!forward && Math.abs(angle(heading-approach))>75){DIAGNOSTIC.set("incoming-road-heading-mismatch");continue;}
    float[] grade=gradeFrame(rows,current,x,y,approach,requested,hasExit,left,right,meters,threshold);
    if(grade!=null){
     if(result!=null)return reject("ambiguous-endpoints");
     result=grade;NODE.set(current[0]+":"+x+":"+y);continue;
    }
    if(rows.size()<3){DIAGNOSTIC.set("no-grade-boundary-or-junction");continue;}
    int roads=0,count=0;boolean overlapping=false;double best=999,second=999,selected=0;int coarseChoices=0;
    for(long[] r:rows) {
     int near=-1;
     if(same(r[5],r[6],x,y))near=7;
     else if(same(r[11],r[12],x,y))near=9;
     if(near<0 || same(r[near],r[near+1],x,y))continue;
     double relative=angle(bearing(r[near]-x,r[near+1]-y,nodeLat)-approach);
     count++;
     // Current road is the incoming arm, already implied by the cluster layout.
     if(r==current)continue;
     int bit=PhoneJunction.roadBit((int)Math.round(relative));
     if(bit==0 || (roads&bit)!=0){overlapping=true;continue;}
     roads|=bit;
     if(!hasExit && ((left&&relative< -12&&relative>=-157.5)||(right&&relative>12&&relative<=157.5)))coarseChoices++;
     double error=ramp&&!hasExit?((left&&relative< -12)||(right&&relative>12)?0:999):Math.abs(angle(relative-requested));
     if(error<best){second=best;best=error;selected=PhoneJunction.displayAngle(bit);}else if(error<second)second=error;
    }
    if(overlapping){DIAGNOSTIC.set("road-arms-overlap-display-sector");continue;}
    if(count<3||roads==0||best>(hasExit?30:60)||second-best<15||(!hasExit&&(left||right)&&coarseChoices!=1)){
     DIAGNOSTIC.set("no-unambiguous-turn-branch");continue;
    }
    int bar=PhoneJunction.barLevel(meters,threshold);
    float[] candidate={13,roads,(float)((selected+360)%360),meters,0,0,0,bar,0};
    if(result!=null)return reject("ambiguous-endpoints"); // Ambiguous endpoints must not draw a guessed junction.
    result=candidate;NODE.set(current[0]+":"+x+":"+y);
   }
   if(result!=null)DIAGNOSTIC.set("matched");
   return result;
  } catch(IllegalArgumentException e){return reject("malformed-snapshot");}
 }
 /** Only a matched future node may classify a grade. Current-position flags are never borrowed. */
 private static float[] gradeFrame(ArrayList<long[]> rows,long[] incoming,long x,long y,double approach,
         double requested,boolean exact,boolean left,boolean right,float meters,int threshold) {
  if(incoming.length<15||incoming[13]!=0||incoming[14]!=0)return null;
  long[] selected=null,structure=null;double selectedAngle=0,best=999,second=999;
  int structures=0,choices=0;
  for(long[] r:rows){
   if(r==incoming||r.length<15)continue;
   int near=same(r[5],r[6],x,y)?7:same(r[11],r[12],x,y)?9:-1;
   if(near<0||same(r[near],r[near+1],x,y))continue;
   double relative=angle(bearing(r[near]-x,r[near+1]-y,y/SCALE)-approach);
   if(!exact&&((left&&relative< -12&&relative>=-157.5)||(right&&relative>12&&relative<=157.5)))choices++;
   double error=Math.abs(angle(relative-requested));
   if(error<best){second=best;best=error;selected=r;selectedAngle=relative;}else if(error<second)second=error;
   if(r[13]+r[14]==1&&Math.abs(relative)<=45){structures++;structure=r;}
  }
  if(selected==null||best>(exact?30:60)||second-best<15||(!exact&&(left||right)&&choices!=1))return null;
  boolean enter=selected[13]+selected[14]==1;
  int icon;
  if(enter){
   if(Math.abs(selectedAngle)>45)return null;
   icon=selected[14]==1?(selectedAngle>12?90:91):(selectedAngle>12?87:88);
  }else{
   if(selected[13]!=0||selected[14]!=0||structures!=1||Math.abs(selectedAngle)<12||Math.abs(selectedAngle)>60)return null;
   icon=structure[14]==1?(selectedAngle<0?85:84):(selectedAngle<0?82:81);
  }
  return GradeEnlargement.draw(icon,meters,threshold);
 }

}
