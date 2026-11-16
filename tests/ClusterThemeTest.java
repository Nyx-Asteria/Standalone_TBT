package hmgapi.mods.projection;
public final class ClusterThemeTest {
 static void ok(boolean x){if(!x)throw new AssertionError();}
 public static void main(String[] args){
  String boot="11111111-2222-3333-4444-555555555555";
  Object[] values={0,1,2,3,5,15};String encoded=ClusterTheme.encode(boot,values);
  ok(java.util.Arrays.equals(values,ClusterTheme.decode(encoded,boot)));
  ok(ClusterTheme.decode(encoded,"aaaaaaaa-2222-3333-4444-555555555555")==null);
  ok(ClusterTheme.decode(encoded+",1",boot)==null);ok(ClusterTheme.encode(boot,new Object[]{0,1,2,3,4,16})==null);
  ok(ClusterTheme.encode(boot,new Object[]{0,1,2,3,4,"blue"})==null);
  ok(ClusterTheme.enlarged(new Object[]{13,0,0,100,0,0,0,100,0}));
  ok(!ClusterTheme.enlarged(new Object[]{67,0,0,100,0,0,0,100,0}));
  System.out.println("Factory theme: exact six-value preservation, current-boot binding, invalid bounds and enlargement-only replay passed");
 }
}
