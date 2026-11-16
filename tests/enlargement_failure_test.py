from pathlib import Path
import tempfile, subprocess
r=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp)
 sources={
 'hmgapi/hook/ModContext.java':'''package hmgapi.hook; public class ModContext {
 public boolean settingBoolean(String k,boolean d){return d;}
 public String settingString(String k,String d){return d;}
 public void log(String s){}
 }''',
 'android/os/SystemClock.java':'''package android.os;public class SystemClock {public static long elapsedRealtime(){return 1000;}}''',
 'hmgapi/mods/projection/carplay/StockMapJunction.java':'''package hmgapi.mods.projection.carplay;public class StockMapJunction {
 public static int calls;public static int targetId(String s){calls++;throw new IllegalAccessError("injected cross-loader failure");}
 }''',
 'hmgapi/mods/projection/carplay/FailureReplay.java':'''package hmgapi.mods.projection.carplay;
 import hmgapi.hook.ModContext;
 public class FailureReplay {public static void main(String[] args){
 AaEnlargement e=new AaEnlargement(null,new ModContext());
 for(int i=0;i<2;i++)if(e.draw("turn",2,0,0,false,0,"right",80)!=null)throw new AssertionError();
 if(StockMapJunction.calls!=1)throw new AssertionError("Failed enlargement retried");
 System.out.println("Actual AA enlargement: injected IllegalAccessError returns fallback and disables repeat rendering");
 }}'''}
 for name,source in sources.items():
  f=p/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(source)
 cp=':'.join(str(r/x) for x in ['build-100/classes','android.jar','runtime','stubs'])
 subprocess.run(['javac','--release','8','-Xlint:-options','-cp',cp,'-d',str(p),*[str(p/x) for x in sources]],check=True)
 subprocess.run(['java','-cp',str(p)+':'+cp,'hmgapi.mods.projection.carplay.FailureReplay'],check=True)
