package hmgapi.mods.projection;
/** Versioned diagnostic only: raw coordinate units must be validated on vehicle. */
final class StockRoadGeometry {
 final long status;final int rows,valid;
 private StockRoadGeometry(long s,int r,int v){status=s;rows=r;valid=v;}
 static StockRoadGeometry decode(long[] data) {
  if(data!=null&&data.length>=3&&data[0]==6){CompoundRoadGraph.Graph g=CompoundRoadGraph.decode(data);return g==null?null:new StockRoadGeometry(0,g.edges.size(),g.edges.size());}
  if(data==null || data.length<3 || (data[0]<1||data[0]>5) || data[2]<0 || data[2]>9)return null;
  int header=data[0]==1?3:data[0]==2||data[0]==4?5:7;
  int stride=data[0]>=4?15:13;
  if(data.length!=header+stride*data[2])return null;
  int valid=0;
  for(int i=0;i<data[2];i++) {
   int at=header+i*stride;
   if(stride==15 && (data[at+13]<0||data[at+13]>1||data[at+14]<0||data[at+14]>1))return null;
   if(data[at+2]==0) {
    if(data[at+4]<2 || data[at+4]>4096 || data[at+3]<0 || data[at+3]>0xffffffffL)return null;
    for(int j=5;j<13;j++)if(data[at+j]<Integer.MIN_VALUE || data[at+j]>Integer.MAX_VALUE)return null;
    valid++;
   }
  }
  return new StockRoadGeometry(data[1],(int)data[2],valid);
 }
 String summary(){return "status="+status+" roads="+rows+" valid="+valid;}
}
