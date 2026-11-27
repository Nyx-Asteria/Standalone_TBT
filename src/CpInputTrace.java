package hmgapi.mods.projection.carplay;
import android.content.Context;
import android.os.SystemClock;
import java.io.*;
/** Private route/stop evidence survives sparse system logcat captures. */
final class CpInputTrace {
 static synchronized void record(Context c,String text) {
  if(c==null)return;
  try {
   File f=new File(c.getFilesDir(),"hmgapi-carplay-input.log");
   if(f.length()>2097152){File old=new File(f.getPath()+".1");if(old.exists()&&!old.delete())return;if(!f.renameTo(old))return;}
   String row="1.0.0-rc.1\t"+SystemClock.elapsedRealtime()+"\t"+System.currentTimeMillis()+"\t"+text.replace('\t',' ').replace('\n',' ').replace('\r',' ')+"\n";
   try(FileOutputStream out=new FileOutputStream(f,true)){out.write(row.getBytes("UTF-8"));}
  }catch(Exception ignored){}
 }
}
