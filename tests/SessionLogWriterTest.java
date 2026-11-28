package hmgapi.mods.projection.carplay;
import java.io.*;
import static hmgapi.mods.projection.carplay.SessionLogPolicy.Level.*;
import static hmgapi.mods.projection.carplay.SessionLogPolicy.Category.*;
public final class SessionLogWriterTest {
    static void check(boolean b){if(!b)throw new AssertionError();}
    static String read(File f)throws Exception{
        BufferedReader r=new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        StringBuilder b=new StringBuilder();try{String s;while((s=r.readLine())!=null)b.append(s).append('\n');}finally{r.close();}return b.toString();
    }
    public static void main(String[] args)throws Exception{
        File root=File.createTempFile("stbt-log-test","");check(root.delete());check(root.mkdir());
        try{
            File unfinished=new File(root,"old.open");check(unfinished.mkdir());
            SessionLogWriter.writeText(new File(unfinished,"session.log"),"old data");
            SessionLogWriter w=new SessionLogWriter(root,null,java.util.EnumSet.of(NAVIGATION));
            w.start(7,"firmware=test");
            w.record(7,INFO,CORE,"connected");
            long nativeAt=System.currentTimeMillis()-2000;
            w.recordAt(7,INFO,CORE,"native event",nativeAt);
            w.record(6,INFO,CORE,"wrong generation");
            w.record(7,ERROR,NAVIGATION,"failed");
            w.record(7,INFO,SUBSCRIPTIONS,"[milestone] guidance_requested");
            w.record(7,INFO,SUBSCRIPTIONS,"[milestone] guidance_eligible");
            w.record(7,INFO,SUBSCRIPTIONS,"[milestone] guidance_received");
            long observedEnd=System.currentTimeMillis();
            w.record(7,INFO,CORE,"late teardown closed");
            w.finishAt(7,"unknown",3,observedEnd);
            w.shutdown();check(w.await(10000));check(w.failure()==null);
            check(read(new File(root,"old/summary.txt")).contains("recovered unfinished session"));
            File session=null;for(File f:root.listFiles())if(f.getName().contains("session-7_"))session=f;
            check(session!=null&&!session.getName().endsWith(".open"));
            check(read(new File(session,"session.log")).contains("connected"));
            check(read(new File(session,"session.log")).contains(nativeAt+" INFO CORE native event"));
            check(!read(new File(session,"session.log")).contains("wrong generation"));
            check(read(new File(session,"errors.log")).contains("failed"));
            check(read(new File(session,"diagnostics/navigation.log")).contains("failed"));
            check(!new File(session,"diagnostics/subscriptions.log").exists());
            check(read(new File(session,"summary.txt")).contains("guidance_requested="));
            check(read(new File(session,"summary.txt")).contains("policy_suppressed=3"));
            check(read(new File(session,"summary.txt")).contains("end_ms="+observedEnd));
            check(read(new File(session,"session.log")).contains("late teardown closed"));
            String summary=read(new File(session,"summary.txt"));
            check(summary.contains("guidance_eligible="));
            check(summary.contains("guidance_received="));
            check(summary.contains("events_subscriptions=3"));
            check(summary.contains("error_count=1"));
            check(summary.contains("first_event_ms="+nativeAt));
            File legacy=new File(root,"legacy");check(legacy.mkdir());
            String base="session-123-12345678-1234-1234-1234-123456789abc";
            File oldActive=new File(legacy,base+".active");
            SessionLogWriter.writeText(oldActive,"original interrupted bytes");
            SessionLogWriter.recoverLegacy(legacy);
            File recovered=new File(legacy,base+"-interrupted.log");
            check(!oldActive.exists()&&read(recovered).equals("original interrupted bytes\n"));
            SessionLogWriter.recoverLegacy(legacy);
            check(read(recovered).equals("original interrupted bytes\n"));
            SessionLogWriter.writeText(oldActive,"conflicting original");
            try {SessionLogWriter.recoverLegacy(legacy);throw new AssertionError("conflict accepted");}
            catch(IOException expected){}
            check(oldActive.isFile()&&read(recovered).equals("original interrupted bytes\n"));
            System.out.println("SessionLogWriterTest passed");
        }finally{SessionLogWriter.delete(root);}
    }
}
