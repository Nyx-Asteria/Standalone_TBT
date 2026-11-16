"""Native navigation hex payload survives verbatim in its separate bounded private journal."""
from pathlib import Path
import tempfile,subprocess
r=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory() as tmp:
 p=Path(tmp);data=p/'capture';data.mkdir()
 src={
 'android/content/Context.java':'''package android.content;public class Context {private java.io.File f;public Context(java.io.File f){this.f=f;}public java.io.File getFilesDir(){return f;}}''',
 'android/os/SystemClock.java':'''package android.os;public class SystemClock {public static long elapsedRealtime(){return 1234;}}''',
 'hmgapi/mods/projection/AaInputTraceTest.java':'''package hmgapi.mods.projection;import java.nio.file.*;import android.content.Context;
 public class AaInputTraceTest {public static void main(String[] args)throws Exception {
 Path dir=Paths.get(args[0]);Context c=new Context(dir.toFile());String raw="S 6 0a040a020824";
 AaInputTrace.record(c,"turn",new Object[]{raw,0,33,0,-1,0});
 String line=new String(Files.readAllBytes(dir.resolve("hmgapi-aa-raw-packets.log")),"UTF-8");
 if(!line.endsWith("\\t"+raw+"\\n"))throw new AssertionError(line);
 AaInputTrace.output(c,new Object[]{13,2065,270f,115f,0,0,0,30,0},null);
 String sent=new String(Files.readAllBytes(dir.resolve("hmgapi-tbt-output.log")),"UTF-8");
 if(!sent.contains("30, 0]\tcompleted"))throw new AssertionError(sent);
 AaInputTrace.record(c,"distance",new Object[]{1300,90,1300,3});
 String inputs=new String(Files.readAllBytes(dir.resolve("hmgapi-aa-input.log")),"UTF-8");
 if(!inputs.contains(raw)||!inputs.contains("distance\\t1300\\t90\\t1300\\t3"))throw new AssertionError(inputs);
 Files.write(dir.resolve("hmgapi-aa-raw-packets.log"),new byte[2097153]);
 AaInputTrace.record(c,"turn",new Object[]{raw,0,33,0,-1,0});
 if(Files.size(dir.resolve("hmgapi-aa-raw-packets.log.1"))!=2097153)throw new AssertionError();
 if(Files.size(dir.resolve("hmgapi-aa-raw-packets.log"))>500)throw new AssertionError();
 System.out.println("AA input trace: exact original hex, independent callback values and bounded rotation passed");
 }}'''}
 for path,code in src.items():
  f=p/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(code)
 cp=str(r/'build-100/classes')
 subprocess.run(['javac','--release','8','-Xlint:-options','-cp',cp,'-d',str(p),*[str(p/x) for x in src]],check=True)
 subprocess.run(['java','-cp',str(p)+':'+cp,'hmgapi.mods.projection.AaInputTraceTest',str(data)],check=True)
