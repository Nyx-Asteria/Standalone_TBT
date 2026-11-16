from pathlib import Path
import tempfile,subprocess
r=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp)
 sources={
 'android/os/SystemClock.java':'''package android.os; public class SystemClock {public static long now=1000;public static long elapsedRealtime(){return now;}}''',
 'android/os/Handler.java':'''package android.os;import java.util.*;
 public class Handler {static class E {Runnable r;long at;E(Runnable r,long a){this.r=r;at=a;}}
 public ArrayList<E> q=new ArrayList<E>(); public boolean postDelayed(Runnable r,long d){q.add(new E(r,SystemClock.now+d));return true;}
 public void removeCallbacks(Runnable r){for(Iterator<E> it=q.iterator();it.hasNext();)if(it.next().r==r)it.remove();}
 public void advance(long t){while(true){E next=null;for(E e:q)if(e.at<=t&&(next==null||e.at<next.at))next=e;if(next==null)break;q.remove(next);SystemClock.now=next.at;next.r.run();}SystemClock.now=t;}
 }''',
 'hmgapi/hook/Cluster.java':'''package hmgapi.hook;public class Cluster {
 public int clear;public void destinationName(int a,String s){}
 public void tbtInfo(int a,int b,float c,float d,int e,int f,int g,int h,int i){if(a==8)clear++;}
 public void tbtSubInfo(float a,int b,int c,int d,int e,int f,int g,float h,int i,int j){}
 public void tbtSubInfo2C(int a,int b,int c,int d,int e,int f,int g,int h,int i){}
 public void laneInfo(byte[] a){}public void turnListNew(byte[] a){}
 }''',
 'hmgapi/mods/projection/carplay/Dipo.java':'''package hmgapi.mods.projection.carplay;
 import android.os.Handler; public class Dipo {public static Handler turnsHandler=new Handler();public static int resumes;
 public static hmgapi.hook.ModContext mod;
 public static void log(String s){}public static void error(String s,Throwable e){}
 public static void resumeRouteGuidance(){resumes++;}
 }''',
 'hmgapi/mods/projection/carplay/CpRecoveryReplay.java':'''package hmgapi.mods.projection.carplay;
 import android.os.*;import hmgapi.hook.Cluster;import java.lang.reflect.*;
 public class CpRecoveryReplay {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 static CpTurns active(Cluster c)throws Exception{CpTurns t=new CpTurns(null,c,null);Field f=CpTurns.class.getDeclaredField("showingGuidance");f.setAccessible(true);f.setBoolean(t,true);t.lastPhoneTurnAt=SystemClock.now;return t;}
 public static void main(String[] args)throws Exception{
 Cluster c=new Cluster();CpTurns t=active(c);t.subscriptionStopped();ok(c.clear==0);
 Dipo.turnsHandler.advance(1249);ok(Dipo.resumes==0&&c.clear==0);
 Dipo.turnsHandler.advance(1250);ok(Dipo.resumes==1&&c.clear==0);
 Dipo.turnsHandler.advance(5999);ok(c.clear==0);Dipo.turnsHandler.advance(6000);ok(c.clear==1);
 // A real phone state0 still ends the route promptly and cancels resubscribe.
 Dipo.turnsHandler=new Handler();SystemClock.now=20000;Dipo.resumes=0;c=new Cluster();t=active(c);t.subscriptionStopped();
 CpTurns.Update u=new CpTurns.Update(5,null);u.state=0;t.turn(5,u);Dipo.turnsHandler.advance(20500);ok(c.clear==1&&Dipo.resumes==0);
 Dipo.turnsHandler.advance(26000);ok(c.clear==1);
 // Link/session disconnect remains immediate and cannot be resurrected by queued recovery.
 Dipo.turnsHandler=new Handler();SystemClock.now=30000;c=new Cluster();t=active(c);t.subscriptionStopped();t.disconnected();ok(c.clear==1);
 Dipo.turnsHandler.advance(36000);ok(c.clear==1&&Dipo.resumes==0);
 // Fresh received guidance cancels the timeout; pure policy is shared by the actual callback.
 Dipo.turnsHandler=new Handler();SystemClock.now=40000;c=new Cluster();t=active(c);t.subscriptionStopped();t.subscriptionRecovery.received();
 Dipo.turnsHandler.advance(46000);ok(c.clear==0);
 System.out.println("Actual CarPlay recovery: unsubscribe retains TBT, bounded Start-only retry/expiry, explicit state0 and disconnect clear, fresh guidance cancels timeout passed");
 }}'''}
 for name,s in sources.items():
  f=p/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s)
 cp=':'.join(str(r/x) for x in ['build-100/classes','android.jar','runtime','stubs'])
 subprocess.run(['javac','--release','8','-Xlint:-options','-cp',cp,'-d',str(p),*[str(p/x) for x in sources]],check=True)
 subprocess.run(['java','-cp',str(p)+':'+cp,'hmgapi.mods.projection.carplay.CpRecoveryReplay'],check=True)
