package hmgapi.mods.projection;
/** Replays an actually observed factory theme, scoped to the current boot. */
final class ClusterTheme {
 static final String KEY="hmgapi_tbt_factory_theme_v1";
 static String encode(String boot,Object[] values){
  if(boot==null||!boot.matches("[a-fA-F0-9-]{36}")||values==null||values.length!=6)return null;
  StringBuilder out=new StringBuilder(boot);
  for(Object value:values){if(!(value instanceof Integer))return null;int n=(Integer)value;if(n<0||n>15)return null;out.append(',').append(n);}
  return out.toString();
 }
 static Object[] decode(String encoded,String boot){
  if(encoded==null||encoded.length()>100||boot==null)return null;
  String[] f=encoded.split(",");if(f.length!=7||!boot.equals(f[0]))return null;
  Object[] out=new Object[6];
  try{for(int i=0;i<6;i++){int n=Integer.parseInt(f[i+1]);if(n<0||n>15)return null;out[i]=n;}}catch(NumberFormatException e){return null;}
  return out;
 }
 static String boot(){
  try{java.io.BufferedReader r=new java.io.BufferedReader(new java.io.FileReader("/proc/sys/kernel/random/boot_id"));try{return r.readLine();}finally{r.close();}}
  catch(java.io.IOException e){return null;}
 }
 static boolean enlarged(Object[] a){
  if(a==null||a.length!=9||!(a[0] instanceof Integer))return false;
  int t=(Integer)a[0];return t==13||t==17||t==18||t==32||t==33||t==43||t==44||t==45||t==46;
 }
}
