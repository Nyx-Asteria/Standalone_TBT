package hmgapi.mods.projection;
import java.io.*;
import java.util.*;
public final class LoadedEngineRangesTest {
 public static void main(String[] args)throws Exception {
  String path="/data/data/com.mnsoft.navi/BIN/libExSLNavi.so";
  String maps="70000000-70100000 r-xp 00000000 00:00 123 "+path+"\n"
   +"70100000-70101000 rw-p 00100000 00:00 123 "+path+"\n"
   +"70200000-70300000 r-xp 00000000 00:00 124 /other/libExSLNavi.so\n"
   +"70400000-70500000 r-xp 00000000 00:00 125 "+path+" (deleted)\n";
  long[] r=LoadedEngineRanges.parse(new BufferedReader(new StringReader(maps)),path);
  if(!Arrays.equals(r,new long[]{0x70000000L,0x70100000L}))throw new AssertionError();
  if(LoadedEngineRanges.parse(new BufferedReader(new StringReader("")),path).length!=0)throw new AssertionError();
  try {LoadedEngineRanges.parse(new BufferedReader(new StringReader("bad-xx r-xp 0 0 0 "+path)),path);throw new AssertionError();}catch(IOException expected){}
  System.out.println("Engine mappings: exact file/executable filter, other-path/deleted rejection and malformed bounds passed");
 }
}
