package hmgapi.mods.projection;
import java.nio.file.*;
import java.io.*;
public final class NativePayloadTest {
 public static void main(String[] args)throws Exception {
  byte[] payload=NativePayload.read(new File(args[0]));
  Path dir=Files.createTempDirectory("tbt-helper-");
  try {
   File staged=NativePayload.stage(dir.toFile(),payload);
   if(!java.util.Arrays.equals(payload,Files.readAllBytes(staged.toPath())))throw new AssertionError();
   if(!NativePayload.stage(dir.toFile(),payload).equals(staged))throw new AssertionError();
   Files.write(staged.toPath(),new byte[]{1,2,3});
   NativePayload.stage(dir.toFile(),payload);
   if(!java.util.Arrays.equals(payload,Files.readAllBytes(staged.toPath())))throw new AssertionError();
   byte[] corrupt=payload.clone();corrupt[50]^=1;
   try {NativePayload.stage(dir.toFile(),corrupt);throw new AssertionError();}catch(IOException expected){}
   try {NativePayload.stage(dir.toFile(),null);throw new AssertionError();}catch(IOException expected){}
   if(dir.toFile().listFiles().length!=1)throw new AssertionError("temporary file leaked");
   System.out.println("Native helper: pinned hash, app-private stage, reuse, corruption repair/refusal and cleanup passed");
  }finally{for(File f:dir.toFile().listFiles())f.delete();Files.delete(dir);}
 }
}
