"""Run the actual gauge receiver/hooks/replay queue with a fake clock and stock API."""
from pathlib import Path
import subprocess,tempfile
root=Path(__file__).resolve().parents[1]
sources={
'android/os/SystemClock.java':'''package android.os;public class SystemClock {public static long now;public static long elapsedRealtime(){return now;}}''',
'android/os/Looper.java':'''package android.os;public class Looper {}''',
'android/os/HandlerThread.java':'''package android.os;public class HandlerThread {public HandlerThread(String n){}public void start(){}public Looper getLooper(){return new Looper();}}''',
'android/os/Handler.java':'''package android.os;import java.util.*;public class Handler {
 static class Task {long t,id;Runnable r;Task(long a,long b,Runnable c){t=a;id=b;r=c;}}
 static long id;static PriorityQueue<Task> q=new PriorityQueue<Task>((a,b)->a.t==b.t?Long.compare(a.id,b.id):Long.compare(a.t,b.t));
 public Handler(Looper l){}public boolean post(Runnable r){return postDelayed(r,0);}public boolean postDelayed(Runnable r,long delay){q.add(new Task(SystemClock.now+delay,id++,r));return true;}public void removeCallbacks(Runnable r){q.removeIf(t->t.r==r);}
 public static void until(long t){int n=0;while(!q.isEmpty()&&q.peek().t<=t){if(++n>10000)throw new AssertionError("loop");Task a=q.remove();SystemClock.now=a.t;a.r.run();}SystemClock.now=t;}
 }''',
'android/content/ContentResolver.java':'''package android.content;public class ContentResolver {}''',
'android/content/Intent.java':'''package android.content;import java.util.*;public class Intent {public static final int FLAG_RECEIVER_FOREGROUND=0x10000000;public String action,pkg;Map<String,Integer> a=new HashMap<>();Map<String,String> texts=new HashMap<>();public Intent(String s){action=s;}public Intent addFlags(int v){return this;}public Intent setPackage(String p){pkg=p;return this;}public Intent putExtra(String k,int v){a.put(k,v);return this;}public Intent putExtra(String k,String v){texts.put(k,v);return this;}public String getStringExtra(String k){return texts.get(k);}public int getIntExtra(String k,int d){return a.containsKey(k)?a.get(k):d;}}''',
'android/content/IntentFilter.java':'''package android.content;public class IntentFilter {public IntentFilter(String s){}}''',
'android/content/BroadcastReceiver.java':'''package android.content;public abstract class BroadcastReceiver {public abstract void onReceive(Context c,Intent i);}''',
'android/content/Context.java':'''package android.content;public class Context {public BroadcastReceiver receiver;public Intent last;public Intent registerReceiver(BroadcastReceiver r,IntentFilter f){receiver=r;return null;}public void sendBroadcast(Intent i){last=i;if(receiver!=null)receiver.onReceive(this,i);}public ContentResolver getContentResolver(){return new ContentResolver();}}''',
'android/provider/Settings.java':'''package android.provider;public class Settings {public static class Global {public static long lease;public static long getLong(android.content.ContentResolver r,String k,long d){return lease;}}}''',
'hmgapi/hook/Cluster.java':'''package hmgapi.hook;public class Cluster {public static int names;public static String lastName;public static Cluster of(android.content.Context c){return new Cluster();}public void crossroadName(String n){if(hmgapi.mods.projection.ProjectionTrapBarTestAccess.active()==false)throw new AssertionError();names++;lastName=n;}}''',
'hmgapi/mods/projection/ProjectionTrapBarTestAccess.java':'''package hmgapi.mods.projection;public class ProjectionTrapBarTestAccess {public static boolean active(){return ProjectionTrapBar.REPLAYING.get()!=null;}}''',
'hmgapi/hook/Car.java':'''package hmgapi.hook;public class Car {public static boolean phone=true;public static boolean phoneGuiding(android.content.Context c){return phone;}}''',
'hmgapi/hook/ModContext.java':'''package hmgapi.hook;public class ModContext {public void log(String s){}}''',
'hmgapi/hook/LoadedApp.java':'''package hmgapi.hook;public class LoadedApp {public android.content.Context context;public ClassLoader classLoader=LoadedApp.class.getClassLoader();public LoadedApp(android.content.Context c){context=c;}}''',
'hmgapi/hook/Points.java':'''package hmgapi.hook;public class Points {}''',
'hmgapi/hook/MethodHook.java':'''package hmgapi.hook;public abstract class MethodHook {protected void beforeHookedMethod(Param p)throws Throwable{}protected void afterHookedMethod(Param p)throws Throwable{}public static class Param {public final java.lang.reflect.Member method;public final Object thisObject;public final Object[] args;Throwable error;public Param(java.lang.reflect.Method m,Object o,Object[] a){method=m;thisObject=o;args=a;}public Throwable getThrowable(){return error;}}}''',
'hmgapi/hook/Hooks.java':'''package hmgapi.hook;import java.util.*;import java.lang.reflect.*;public class Hooks {public static Map<Method,MethodHook> hooks=new HashMap<>();public static Class<?> findClass(String n,ClassLoader l){return hmgapi.mods.projection.GaugeReplay.Fake.class;}
 public static Method findAndHookMethod(Class<?> c,String n,Object... a){try{Class<?>[] p=new Class<?>[a.length-1];for(int i=0;i<p.length;i++)p[i]=(Class<?>)a[i];Method m=c.getMethod(n,p);hooks.put(m,(MethodHook)a[a.length-1]);return m;}catch(Exception e){throw new RuntimeException(e);}}
 public static void invoke(Object o,Method m,Object[] a)throws Throwable {MethodHook h=hooks.get(m);MethodHook.Param p=new MethodHook.Param(m,o,a);if(h!=null)h.beforeHookedMethod(p);try{m.invoke(o,p.args);}catch(InvocationTargetException e){p.error=e.getCause();}if(h!=null)h.afterHookedMethod(p);if(p.error!=null)throw p.error;}
 }''',
'hmgapi/mods/projection/AaInputTrace.java':'''package hmgapi.mods.projection;public class AaInputTrace {static void record(android.content.Context c,String k,Object[] a){}}''',
'hmgapi/mods/projection/GaugeReplay.java':'''package hmgapi.mods.projection;import java.util.*;import java.lang.reflect.*;import android.os.*;import android.content.*;import hmgapi.hook.*;
 public class GaugeReplay {
 static void ok(boolean b){if(!b)throw new AssertionError("at "+SystemClock.now);}
 public static class Info {public int camera,speed,barGraphLevel,displayTG;public Info(){}}
 public static class Fake {
  static List<int[]> sent=new ArrayList<>();static Runnable race;
  public void updateTBTTrapInfo(int c,int a1,int a2,int a3,int a4,int a5,int a6,int a7,int a8,int a9,int a10,int bar,int a12,int a13,int a14,int a15,int a16){rec(new int[]{0,c,a12,bar,a10});}
  public void updateTBTTrapInfoNew(Info i){rec(new int[]{1,i.camera,i.speed,i.barGraphLevel,i.displayTG});}
  void rec(int[] f){sent.add(f);if(ProjectionTrapBar.REPLAYING.get()!=null&&race!=null){Runnable r=race;race=null;Thread t=new Thread(r);t.start();try{t.join();}catch(InterruptedException e){throw new RuntimeException(e);}}}
 }
 static Method integer,object;static Fake fake=new Fake();static Context context=new Context();
 static void stockInt(int camera,int bar)throws Throwable{Object[] a=new Object[17];Arrays.fill(a,0);a[0]=camera;a[11]=bar;a[12]=80;Hooks.invoke(fake,integer,a);}
 static Info stockObject(int camera,int bar)throws Throwable{Info i=new Info();i.camera=camera;i.speed=110;i.barGraphLevel=bar;Hooks.invoke(fake,object,new Object[]{i});return i;}
 static void level(int value){context.sendBroadcast(new Intent(ProjectionBar.ACTION).putExtra(ProjectionBar.EXTRA_BAR,value));Handler.until(SystemClock.now);}
 static int[] last(int kind){for(int n=Fake.sent.size()-1;n>=0;n--)if(Fake.sent.get(n)[0]==kind)return Fake.sent.get(n);throw new AssertionError();}
 public static void main(String[] args)throws Throwable{
  ProjectionTrapBar gauge=ProjectionTrapBar.install(new ModContext(),new LoadedApp(context));ok(gauge!=null&&Hooks.hooks.size()==2);
  for(Method m:Hooks.hooks.keySet())if(m.getParameterTypes().length==17)integer=m;else object=m;
  level(70);ok(Fake.sent.isEmpty());stockInt(123,50);ok(last(0)[3]==70&&last(0)[1]==123&&last(0)[2]==80);
  Info original=stockObject(456,40);ok(original.barGraphLevel==40&&last(1)[3]==70&&last(1)[1]==456&&last(1)[2]==110);
  level(-1);Handler.until(250);ok(last(0)[3]==50&&last(1)[3]==40); // restore each API's original value
  Handler.until(1000);level(80);ok(last(0)[3]==80&&last(1)[3]==80);
  Handler.until(1050);level(60);ok(last(0)[3]==80);Handler.until(1200);ok(last(0)[3]==60&&last(1)[3]==60);
  Fake.race=()->{try{stockInt(999,30);}catch(Throwable e){throw new RuntimeException(e);}};
  Handler.until(2000);level(50);Handler.until(2300);ok(last(0)[1]==999&&last(0)[3]==50&&last(0)[2]==80);
  level(-1);Handler.until(2600);ok(last(0)[3]==30&&last(1)[3]==40&&last(0)[1]==999);
  level(90);Handler.until(3000);Car.phone=false;gauge.check(false);Handler.until(3300);ok(last(0)[3]==30&&last(1)[3]==40);
  Car.phone=true;level(70);Handler.until(3600);Handler.until(184000);ok(last(0)[3]==30&&last(1)[3]==40); // expiry restores without new stock calls
  stockInt(555,101);int before=Fake.sent.size();level(80);Handler.until(184500);
  for(int n=before;n<Fake.sent.size();n++)ok(Fake.sent.get(n)[0]!=0); // invalid layout invalidates the old integer slot
  ProjectionBar.Sender sender=new ProjectionBar.Sender();ProjectionBar.send(context,sender,40,184501);ok("com.mnsoft.navi".equals(context.last.pkg));
  Car.phone=false;android.provider.Settings.Global.lease=System.currentTimeMillis()+2000;ok(ProjectionBar.guiding(context));android.provider.Settings.Global.lease=0;ok(!ProjectionBar.guiding(context));
  // Same bar, different maneuver: toll display must update and then restore both API slots.
  Car.phone=true;Handler.until(185000);stockInt(777,30);Info baseline=stockObject(888,40);
  context.sendBroadcast(new Intent(ProjectionBar.ACTION).putExtra(ProjectionBar.EXTRA_BAR,40).putExtra(ProjectionBar.EXTRA_TOLL,1));Handler.until(185300);
  ok(last(0)[4]==1&&last(1)[4]==1&&last(0)[3]==40&&last(1)[3]==40&&baseline.displayTG==0);
  stockInt(778,40);ok(last(0)[4]==1); // equal stock bar must still set the toll flag
  context.sendBroadcast(new Intent(ProjectionBar.ACTION).putExtra(ProjectionBar.EXTRA_BAR,40).putExtra(ProjectionBar.EXTRA_TOLL,0));Handler.until(185600);
  ok(last(0)[4]==0&&last(1)[4]==0&&last(0)[1]==778&&last(1)[1]==888);
  context.sendBroadcast(new Intent(ProjectionBar.ACTION).putExtra(ProjectionBar.EXTRA_BAR,40).putExtra(ProjectionBar.EXTRA_TOLL,1));Handler.until(185900);
  Car.phone=false;gauge.check(false);Handler.until(186200);ok(last(0)[4]==0&&last(1)[4]==0);
  Car.phone=true;context.sendBroadcast(new Intent(ProjectionBar.ACTION).putExtra(ProjectionBar.EXTRA_BAR,40).putExtra(ProjectionBar.EXTRA_TOLL,1));Handler.until(186500);
  Handler.until(367000);ok(last(0)[4]==0&&last(1)[4]==0);
  Car.phone=true;Handler.until(368000);
  context.sendBroadcast(new Intent(ProjectionBar.ACTION).putExtra(ProjectionBar.EXTRA_BAR,40).putExtra(ProjectionBar.EXTRA_TOLL,1).putExtra(ProjectionBar.EXTRA_NAME,"공주톨게이트"));Handler.until(368300);
  ok(Cluster.names>0&&"공주톨게이트".equals(Cluster.lastName)&&last(0)[4]==1&&last(1)[4]==1);
  int names=Cluster.names;
  context.sendBroadcast(new Intent(ProjectionBar.ACTION).putExtra(ProjectionBar.EXTRA_BAR,40).putExtra(ProjectionBar.EXTRA_TOLL,1).putExtra(ProjectionBar.EXTRA_NAME,"2,200원"));Handler.until(368600);
  ok(Cluster.names>names&&"2,200원".equals(Cluster.lastName));
  // A same-value heartbeat must reapply the full toll state after an icon update.
  names=Cluster.names;int frames=Fake.sent.size();
  context.sendBroadcast(new Intent(ProjectionBar.ACTION).putExtra(ProjectionBar.EXTRA_BAR,40).putExtra(ProjectionBar.EXTRA_TOLL,1).putExtra(ProjectionBar.EXTRA_NAME,"2,200원"));Handler.until(368900);
  ok(Cluster.names>names&&Fake.sent.size()>=frames+2&&last(0)[4]==1&&last(1)[4]==1);
 // timeout restores toll state as well
  System.out.println("Actual trap gauge integration: both APIs, original fields/objects, independent restoration, throttle/burst, concurrent camera update, expiry, missing/invalid stock and package-targeted relay passed");
 }
 }'''
}
with tempfile.TemporaryDirectory(prefix='tbt-gauge-') as tmp:
 p=Path(tmp)
 for name,code in sources.items():
  f=p/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(code)
 cp=str(root/'build-105/classes')
 subprocess.run(['javac','--release','8','-Xlint:-options','-cp',cp,'-d',str(p),*[str(p/n) for n in sources]],check=True)
 subprocess.run(['java','-cp',str(p)+':'+cp,'hmgapi.mods.projection.GaugeReplay'],check=True)
