package hmgapi.mods.projection.carplay;
import android.content.Context;
import hmgapi.mods.projection.StockNavigationApi;
import java.io.*;
/** Private bounded replay journal; not a public mod log or release asset. */
final class JunctionMatchTrace {
 private static long last=-10000;
 static void record(Context context,String token,long now,float meters,String reason,String state,
        float[] frame,StockNavigationApi.Position position,String geometry) {
  if(now-last<900 || meters>1200)return;last=now;
  try {
   File file=new File(context.getFilesDir(),"hmgapi-junction-match.log");
   if(file.length()>1048576) {
    File old=new File(context.getFilesDir(),"hmgapi-junction-match.log.1");
    if(old.exists()&&!old.delete())return;
    if(!file.renameTo(old))return;
   }
   String p=position==null?"none":position.elapsed+","+position.latitude+","+position.longitude+","+position.heading;
   String line="1.0.0\t"+System.currentTimeMillis()+"\t"+android.os.Process.myPid()+"\t"+now
      +"\t"+token.replace('\t',' ').replace('\n',' ')+"\t"+meters+"\t"+reason+"\t"+state+"\t"+java.util.Arrays.toString(frame)
      +"\t"+p+"\t"+(geometry==null?"none":geometry)+"\n";
   try(FileOutputStream out=new FileOutputStream(file,true)){out.write(line.getBytes("UTF-8"));}
  } catch(Exception ignored) { /* Research capture cannot change the guidance result. */ }
 }
}
