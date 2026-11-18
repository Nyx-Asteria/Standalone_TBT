from pathlib import Path
import tempfile,subprocess,sys
r=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp)
 sources={
 'hmgapi/mods/projection/carplay/AaEnlargement.java':'''package hmgapi.mods.projection.carplay;
 public class AaEnlargement {public static int draws;public AaEnlargement(android.content.Context c,hmgapi.hook.ModContext m){}public void clear(){}
 public static int facility(int t,int j,int e,String r){return FacilityGlyphs.road(t,j,e,r,null);}
 public float[] draw(String k,int t,int j,int s,boolean h,int e,String r,float d){draws++;return null;}}
''',
 'android/os/SystemClock.java':'''package android.os;public class SystemClock {public static long now;public static long elapsedRealtime(){return now;}}''',
 'android/os/Handler.java':'''package android.os;import java.util.*;public class Handler {
 static class Job {Runnable r;long due;Job(Runnable r,long t){this.r=r;due=t;}}
 ArrayList<Job> jobs=new ArrayList<Job>();
 public boolean postDelayed(Runnable r,long ms){jobs.add(new Job(r,SystemClock.now+ms));return true;}
 public void removeCallbacks(Runnable r){for(int i=jobs.size()-1;i>=0;i--)if(jobs.get(i).r==r)jobs.remove(i);}
 public void advance(long now){while(true){Job first=null;for(Job j:jobs)if(j.due<=now&&(first==null||j.due<first.due))first=j;
 if(first==null)break;jobs.remove(first);SystemClock.now=first.due;first.r.run();}SystemClock.now=now;}
 }''',
 'hmgapi/hook/Car.java':'''package hmgapi.hook;public class Car {public static int distanceUnit(android.content.Context c){return 0;}}''',
 'hmgapi/hook/Cluster.java':'''package hmgapi.hook;import java.util.*;public class Cluster {
 public ArrayList<long[]> frames=new ArrayList<long[]>();public int lastType;public float lastDistance;public String crossroad;
 public static class Turn {public int type,direction,traffic,no,unit;public String name,exitNumber;public float distance;}
 public void tbtInfo(int a,int b,float c,float d,int e,int f,int g,int h,int i){lastType=a;lastDistance=d;frames.add(new long[]{android.os.SystemClock.now,a,(long)(d*1000),g});}
 public void tbtSubInfo2C(int a,int b,int c,int d,int e,int f,int g,int h,int i){}
 public void tbtSubInfo(float a,int b,int c,int d,int e,int f,int g,float h,int i,int j){}
 public void turnListNew(byte[] f){}public void turnList(Turn[] t){}public void crossroadName(String s){crossroad=s;}public void destinationName(int n,String s){}
 }''',
 'hmgapi/mods/projection/AaNotificationReplay.java':'''package hmgapi.mods.projection;
 import android.os.*;import hmgapi.hook.Cluster;import java.nio.file.*;
 public class AaNotificationReplay {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args)throws Exception {
 Handler h=new Handler();Cluster c=new Cluster();AaTurns t=new AaTurns(null,c,h);
 t.navLive=true;t.hasCurrentTurn=true;t.event=4;t.turnSide=1;t.displayType=70;t.distance=500;t.unit=0;t.road="real road";
 h.advance(1000);t.nextTurn("박스형",3,2,-1,0);t.nextTurnDistance(298,0,298000,1);
 ok(c.lastType==0&&t.event==4&&t.distance==500);
 h.advance(3000);t.nextTurn("real road",1,4,-1,0);ok(c.lastType==70);
 h.advance(5000);ok(c.lastType==70);
 h.advance(6000);t.nextTurn("new road",3,2,-1,0);t.nextTurnDistance(400,0,400000,1);
 h.advance(9499);ok(c.lastType==70);h.advance(9500);ok(c.lastType==65&&t.distance==400);
 t.event=4;t.displayType=70;t.distance=20;
 h.advance(10000);t.nextTurn("near change",3,2,-1,0);ok(c.lastType==65);
 t.event=4;t.displayType=70;t.distance=500;
 h.advance(11000);int before=c.frames.size();t.nextTurn("박스형",3,2,-1,0);h.advance(15000);ok(c.frames.size()==before&&t.event==4); // warning timeout never writes a maneuver
 int beforeDestination=c.frames.size();t.nextTurn("도착지 주변",3,19,-1,0);
 ok("도착지 주변".equals(c.crossroad));
 ok(c.frames.size()==beforeDestination&&t.displayType==112&&t.direction==0&&!t.hasCurrentDistance);
 t.nextTurnDistance(1300,0,1300,3);ok(c.lastType==112&&c.lastDistance>1.29f&&c.lastDistance<1.31f);
 t.nextTurn("도착지 주변",3,19,-1,0);ok(c.lastType==112&&t.hasCurrentDistance);
 t.nextTurnDistance(0,0,0,1);ok(c.lastType==112);
 ok(AaTurns.aheadType(19,3)==112&&AaNavigationState.icon(39)==112);
 t.nextTurn("roundabout",1,13,150,0);ok(t.displayType==97);
 t.nextTurn("roundabout enter",1,11,150,0);ok(t.displayType==97);
 t.nextTurn("roundabout no angle",1,12,-1,0);ok(t.displayType==64);
 t.setRampType(9,2);ok(t.displayType==66&&t.direction==30);
 t.rawCurrentIcon=68;t.nextTurn("recovered fork",0,4,-1,0);ok(t.displayType==68&&t.direction==135);
 System.out.println("AA rotary side1/enter/no-angle, fork and raw semantic recovery passed");
 System.out.println("Destination approach: fresh1300m, no prior-turn distance/direction, repeat/zero-distance and next-icon paths passed");
 System.out.println("Directionless notice: camera suppression, distance isolation, real-turn recovery, bounded settle and near-turn bypass passed");
 // Reference-derived Korean selectors must survive subsequent distance callbacks.
 AaTurns k=new AaTurns(null,c,h,null);k.navLive=true;
 int[] sides={1,2,3};int[] ins={162,161,160},outs={165,164,163};
 for(int z=0;z<3;z++){k.nextTurn("진입",sides[z],7,-1,0);k.nextTurnDistance(500,0,500000,1);ok(c.lastType==ins[z]&&AaTurns.aheadType(7,sides[z])==ins[z]);
 k.nextTurn("진출",sides[z],8,-1,0);k.nextTurnDistance(400,0,400000,1);ok(c.lastType==outs[z]&&AaTurns.aheadType(8,sides[z])==outs[z]);}
 k=new AaTurns(null,c,h,null);k.navLive=true;k.nextTurn("북상주톨게이트",3,2,-1,0);k.nextTurnDistance(500,0,500000,1);ok(c.lastType==178);
 int frames=c.frames.size();k.nextTurn("2,200원",3,2,-1,0);ok(c.frames.size()>frames&&c.lastType==178&&"2,200원".equals(c.crossroad));
 k.nextTurnDistance(490,0,490000,1);ok(c.lastType==178);
 k.nextTurn("상주휴게소",3,2,-1,0);ok(c.lastType==183);k.nextTurn("휘발유 1,600원/L",3,2,-1,0);ok(c.lastType==183);
 int drawCount=hmgapi.mods.projection.carplay.AaEnlargement.draws;
 k.nextTurn("회전교차로",1,13,180,0);ok(hmgapi.mods.projection.carplay.AaEnlargement.draws==drawCount);
 k.nextTurnDistance(350,0,350000,1);ok(hmgapi.mods.projection.carplay.AaEnlargement.draws>drawCount);
 drawCount=hmgapi.mods.projection.carplay.AaEnlargement.draws;
 k.nextTurn("바뀐 도로명",1,13,180,0);ok(hmgapi.mods.projection.carplay.AaEnlargement.draws>drawCount);
 // No camera label: a notice carries existing destinations but an unrelated short distance.
 AaTurns unnamed=new AaTurns(null,c,h,null);unnamed.navLive=true;
 h.advance(20000);unnamed.nextTurn("route",2,9,-1,0);unnamed.nextTurnDistance(1600,0,1600,3);
 h.advance(20900);unnamed.nextTurn("same destination",3,2,-1,0);unnamed.nextTurnDistance(134,0,0,1);
 h.advance(22000);unnamed.nextTurnDistance(1500,0,1500,3);
 h.advance(22900);unnamed.nextTurn("same destination",3,2,-1,0);unnamed.nextTurnDistance(89,0,0,1);
 h.advance(23900);unnamed.nextTurnDistance(66,0,0,1);h.advance(24401);
 ok(unnamed.event==9&&c.lastType==66&&unnamed.distance>=1.5f);
 h.advance(25000);unnamed.nextTurn("route",2,9,-1,0);unnamed.nextTurnDistance(1400,0,1400,3);ok(c.lastType==66&&c.lastDistance>1.39f&&c.lastDistance<1.41f);
 System.out.println("Unnamed directionless notice: conflicting short distance never changes maneuver, normal route resumes passed");
 if(args.length==0)return;
 h=new Handler();c=new Cluster();t=new AaTurns(null,c,h,null);t.navLive=true;String rawRoad="";int turns=0;
 for(String line:Files.readAllLines(Paths.get(args[0]))) {
 String[] a=line.split("\\t",-1);long now=Long.parseLong(a[0]);h.advance(now);
 if(a[1].equals("S")){String road=AaStateText.currentRoad(a[2]);if(road!=null)rawRoad=road;}
 if(a[1].equals("T")){t.nextTurn(rawRoad,Integer.parseInt(a[3]),Integer.parseInt(a[2]),Integer.parseInt(a[4]),0);turns++;}
 if(a[1].equals("D")){int m=Integer.parseInt(a[2]),f=Integer.parseInt(a[3]);int e3=(f==1?m*1000:m);t.nextTurnDistance(m,0,e3,f);}
 }
 if(args.length>1&&args[1].equals("unnamed-notice")){
 int held=0,recovered=0;
 for(long[] f:c.frames){
  if(f[0]>=223900&&f[0]<229000){ok(f[1]==66);ok(f[2]>=1000);held++;}
  if(f[0]>=229000&&f[0]<231000&&f[1]==66&&f[2]>=1300&&f[2]<=1500)recovered++;
 }
 ok(held>0&&recovered>0);System.out.println("Actual unnamed notice replay: right fork retained, 134/89/66m isolated, route1400m restored; held="+held+", recovered="+recovered);return;
 }
 if(args.length>1&&args[1].equals("camera-distance")){
 int held=0,recovered=0;
 for(long[] f:c.frames){if(f[0]>=817815&&f[0]<819799)ok(f[2]!=452000&&f[2]!=427000);if(f[0]>=827800&&f[0]<831575){ok(f[2]!=198000&&f[2]!=174000);held++;}
 if(f[0]>=831575&&f[0]<832900&&f[2]>=648000&&f[2]<=671000)recovered++;}
 ok(held>0&&recovered>0);System.out.println("DL3 actual camera replay: late camera198 suppressed, route671/648 restored; held="+held+", recovered="+recovered);return;
 }
 if(args.length>1&&args[1].equals("toll")){
 int count=0;for(long[] f:c.frames)if(f[0]>=Long.parseLong(args[2])&&f[0]<=Long.parseLong(args[3])){count++;ok(f[1]==178);}
 ok(count>10);System.out.println("Actual issue3 toll/fee alternation replay: stable178 frames="+count);return;
 }
 if(args.length>1) {
 int approaching=0;for(long[] f:c.frames){ok(f[1]!=3);if(f[1]==112&&f[3]==1&&f[2]>=1000)approaching++;}
 ok(approaching>0);System.out.println("DN8 actual replay: far destination112 frames="+approaching+", premature-arrival3=0");return;
 }
 long[][] windows={{70548000,70552000},{71195000,71219000},{71405000,71422000}};
 for(long[] w:windows){int count=0,straight=0;for(long[] f:c.frames)if(f[0]>=w[0]&&f[0]<=w[1]){count++;if(f[1]==65)straight++;}
 System.out.println("DL3 window="+w[0]+".."+w[1]+" output="+count+" spurious65="+straight);ok(count>0&&straight==0);}
 System.out.println("DL3 actual callback replay passed; turns="+turns);
 }}'''}
 for path,source in sources.items():
  f=p/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(source)
 cp=':'.join(str(r/x) for x in ['build-105/classes','android.jar','runtime','stubs'])
 subprocess.run(['javac','--release','8','-Xlint:-options','-cp',cp,'-d',str(p),*[str(p/x) for x in sources]],check=True)
 subprocess.run(['java','-cp',str(p)+':'+cp,'hmgapi.mods.projection.AaNotificationReplay']+sys.argv[1:],check=True)
