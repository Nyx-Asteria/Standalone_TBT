package hmgapi.mods.projection;
import android.content.Context;
import android.os.SystemClock;
import java.io.*;
/** Private bounded native-callback and untouched navigation-payload journals. */
final class AaInputTrace {
 private static void append(Context context,String name,String row)throws IOException {
  File file=new File(context.getFilesDir(),name);
  if(file.length()>2097152){File old=new File(file.getPath()+".1");
   if(old.exists()&&!old.delete())return;if(!file.renameTo(old))return;}
  try(FileOutputStream out=new FileOutputStream(file,true)){out.write(row.getBytes("UTF-8"));}
 }
 static synchronized void output(Context context,Object[] args,Throwable error) {
  try {
   String row="1.0.5\t"+SystemClock.elapsedRealtime()+"\t"+System.currentTimeMillis()+"\t"
       +java.util.Arrays.toString(args)+"\t"+(error==null?"completed":"error_"+error.getClass().getSimpleName())+"\n";
   append(context,"hmgapi-tbt-output.log",row);
  }catch(Exception ignored){}
 }
 static synchronized void record(Context context,String kind,Object[] args) {
  try {
   String prefix="1.0.5\t"+SystemClock.elapsedRealtime()+"\t"+System.currentTimeMillis()+"\t";
   // EV_DUMP supplies the original S/P protobuf bytes as length + hex. Keep that
   // text verbatim, before AaStateText/AaNavigationState interpret its fields.
   if("turn".equals(kind)&&args.length>2&&Integer.valueOf(33).equals(args[2])&&args[0] instanceof String) {
    String raw=(String)args[0];
    if((raw.startsWith("S ")||raw.startsWith("P "))&&raw.length()<=131200)
     append(context,"hmgapi-aa-raw-packets.log",prefix+raw+"\n");
   }
   StringBuilder row=new StringBuilder(prefix).append(kind);
   for(Object a:args){String value=String.valueOf(a);if(value.length()>131200)value=value.substring(0,131200);
    row.append('\t').append(value.replace('\t',' ').replace('\n',' ').replace('\r',' '));}
   row.append('\n');append(context,"hmgapi-aa-input.log",row.toString());
  }catch(Exception ignored){/* Diagnostic failure cannot change guidance. */}
 }
}
