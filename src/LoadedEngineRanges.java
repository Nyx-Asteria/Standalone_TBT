package hmgapi.mods.projection;
import java.io.*;
import java.util.*;
/** Executable mappings belonging to the exact engine file verified by SHA-256. */
final class LoadedEngineRanges {
 static long[] read(String path)throws IOException {
  try(BufferedReader in=new BufferedReader(new FileReader("/proc/self/maps"))) {return parse(in,path);}
 }
 static long[] parse(BufferedReader in,String path)throws IOException {
  ArrayList<Long> values=new ArrayList<Long>();String line;
  while((line=in.readLine())!=null) {
   String[] f=line.trim().split("\\s+",6);
   if(f.length!=6 || !f[5].equals(path) || f[1].length()!=4 || f[1].charAt(2)!='x')continue;
   String[] range=f[0].split("-");
   try {
    if(range.length!=2)throw new NumberFormatException();
    long a=Long.parseLong(range[0],16),b=Long.parseLong(range[1],16);
    if(a<=0 || b<=a || b>0x100000000L || values.size()>=32)throw new NumberFormatException();
    values.add(a);values.add(b);
   }catch(NumberFormatException e){throw new IOException("invalid engine mapping",e);}
  }
  long[] result=new long[values.size()];for(int i=0;i<result.length;i++)result[i]=values.get(i);return result;
 }
}
