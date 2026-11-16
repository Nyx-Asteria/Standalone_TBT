package hmgapi.mods.projection;
import java.net.*;
import java.lang.reflect.*;
/** Mirrors AppPartLoader's separate package ownership; the renderer must not request acquisition classes. */
public final class CompoundLoaderTest {
 static final String PREFIX="hmgapi.mods.projection.carplay.";
 static final class Part extends URLClassLoader {
  Part(URL[] urls){super(urls,CompoundLoaderTest.class.getClassLoader());}
  protected synchronized Class<?> loadClass(String n,boolean resolve)throws ClassNotFoundException {
   if(n.equals("hmgapi.mods.projection.CompoundRoadGraph")||n.startsWith("hmgapi.mods.projection.CompoundRoadGraph$")||n.equals("hmgapi.mods.projection.StockMapLookahead"))
    throw new AssertionError("Renderer requested acquisition class: "+n);
   if(n.startsWith(PREFIX)) {Class<?> c=findLoadedClass(n);if(c==null)c=findClass(n);if(resolve)resolveClass(c);return c;}
   return super.loadClass(n,resolve);
  }
 }
 public static void main(String[] args)throws Exception {
  // Populate the main loader first, just as HMG-API initializes the main mod before the app part.
  Class<?> shared=Class.forName(PREFIX+"CompoundGeometry");
  URL root=CompoundLoaderTest.class.getProtectionDomain().getCodeSource().getLocation();
  URL classes=new java.io.File(new java.io.File(root.toURI()).getParentFile(),"classes").toURI().toURL();
  try(Part part=new Part(new URL[]{classes})) {
   Class<?> local=part.loadClass(PREFIX+"CompoundGeometry");
   if(local==shared)throw new AssertionError("App-part decoder was shared");
   Class<?> renderer=part.loadClass(PREFIX+"StockMapJunction");
   Method draw=renderer.getMethod("drawTarget",String.class,long.class,double.class,double.class,double.class,String.class,int.class,int.class,boolean.class,int.class,float.class,int.class,int.class);
   int[] fixtures={0,2,1,1},types={2,1,6,6},exits={90,-90,-90,180};
   for(int i=0;i<fixtures.length;i++) {
    StringBuilder s=new StringBuilder("1000");for(long v:CompoundRoadGraphTest.sample(fixtures[i]))s.append(',').append(v);
    float[] result=(float[])draw.invoke(null,s.toString(),1100L,127.0,37-.001,0f,"CarPlay",types[i],i>=2?1:0,true,exits[i],120f,400,77);
    if(result==null || result[0]!=(i>=2?32:13))throw new AssertionError("Split-loader fixture failed "+i);
   }
  }
  System.out.println("Split loader: physical left/right island and rotary/360 rendering without acquisition-class references passed");
 }
}
