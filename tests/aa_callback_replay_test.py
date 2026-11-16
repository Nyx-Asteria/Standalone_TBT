from pathlib import Path
import tempfile,subprocess
r=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp)
 sources={
 'hmgapi/hook/Car.java':'''package hmgapi.hook; public class Car {public static int distanceUnit(android.content.Context c){return 0;}}''',
 'hmgapi/hook/Cluster.java':'''package hmgapi.hook; public class Cluster {
 public int[] hud; public byte[] list;public int lists; public Turn[] turns;
 public static class Turn {public int type,direction,traffic,no,unit;public String name,exitNumber;public float distance;}
 public void tbtSubInfo2C(int a,int b,int c,int d,int e,int f,int g,int h,int i){hud=new int[]{a,b,c,d,e,f,g,h,i};}
 public void turnListNew(byte[] f){list=f;} public void turnList(Turn[] t){lists++;turns=t;}
 }''',
 'hmgapi/mods/projection/AaCallbackReplay.java':'''package hmgapi.mods.projection;
 import hmgapi.hook.Cluster;
 public class AaCallbackReplay {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
 Cluster c=new Cluster();AaTurns t=new AaTurns(null,c,null);
 t.displayType=70;t.distance=85;t.unit=0;t.road="current";
 // Metadata + end marker arrive before the next distance.
 t.nextTurn("following",2,64,90,4);t.nextTurn("",0,67,0,1);
 ok(c.lists==0&&c.hud[5]==65535);
 t.nextTurnDistance(471,0,471000,0x200|1);
 ok(c.lists==1&&c.hud[3]==0&&c.hud[5]==471&&c.list[3]==67);
 t.sendHudDistance();ok(c.hud[5]==471); // current refresh cannot zero following distance
 // New batch, distance arrives before end marker.
 t.nextTurn("following2",1,64,270,4);t.nextTurnDistance(1350,0,1350,0x200|2);
 t.nextTurn("",0,67,0,1);ok(c.lists==2&&c.hud[3]==1&&c.hud[5]==1);
 t.nextTurn("",0,67,0,0);ok(c.hud[3]==15&&c.hud[5]==65535&&c.list[3]==0);
 System.out.println("Actual AaTurns callback replay: late/early distance, HUD persistence and explicit empty batch passed");
 }}'''}
 for path,source in sources.items():
  f=p/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(source)
 cp=':'.join(str(r/x) for x in ['build-100/classes','android.jar','runtime','stubs'])
 subprocess.run(['javac','--release','8','-Xlint:-options','-cp',cp,'-d',str(p),*[str(p/x) for x in sources]],check=True)
 subprocess.run(['java','-cp',str(p)+':'+cp,'hmgapi.mods.projection.AaCallbackReplay'],check=True)
