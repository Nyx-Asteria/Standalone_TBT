package hmgapi.mods.projection;
public final class StockRoadGeometryTest {
 public static void main(String[] args) {
  long[] v=new long[16];v[0]=1;v[2]=1;v[3]=123;v[4]=456;v[6]=200;v[7]=4;v[8]=-100;v[15]=123;
  StockRoadGeometry g=StockRoadGeometry.decode(v);
  if(g==null || g.valid!=1 || g.rows!=1)throw new AssertionError();
  for(int i=0;i<v.length;i++)if(StockRoadGeometry.decode(java.util.Arrays.copyOf(v,i))!=null)throw new AssertionError();
  v[7]=4097;if(StockRoadGeometry.decode(v)!=null)throw new AssertionError();
  v[7]=4;v[8]=Long.MAX_VALUE;if(StockRoadGeometry.decode(v)!=null)throw new AssertionError();
  v[5]=-30;if(StockRoadGeometry.decode(v).valid!=0)throw new AssertionError();
  if(StockRoadGeometry.decode(new long[]{1,-21,0}).status!=-21)throw new AssertionError();
  long[] attrs=new long[20];attrs[0]=4;attrs[2]=1;attrs[3]=1;attrs[4]=1;attrs[9]=2;attrs[19]=1;
  if(StockRoadGeometry.decode(attrs)==null)throw new AssertionError();
  attrs[19]=2;if(StockRoadGeometry.decode(attrs)!=null)throw new AssertionError();
  attrs[19]=1;
  long[] forward=new long[22];forward[0]=5;System.arraycopy(attrs,1,forward,1,4);System.arraycopy(attrs,5,forward,7,15);
  if(StockRoadGeometry.decode(forward)==null)throw new AssertionError();
  System.out.println("Geometry snapshot: version/shape, partial failures, truncation and coordinate/point bounds passed");
 }
}
