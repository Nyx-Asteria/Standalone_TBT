package hmgapi.mods.projection;
import java.util.LinkedHashMap;
/** Short-lived immutable road topology, not vehicle position or route distance. */
final class StockGeometryCache {
 private static final int CAPACITY=128;
 private static final long TTL=5000;
 private static final class Entry {final long at;final long[] data;Entry(long t,long[] d){at=t;data=d.clone();}}
 private final LinkedHashMap<String,Entry> entries=new LinkedHashMap<String,Entry>();
 private static String key(long p,long l,int d){return p+":"+l+":"+d;}
 synchronized long[] get(long p,long l,int d,long now){
  String k=key(p,l,d);Entry e=entries.get(k);
  if(e==null)return null;
  if(now<e.at||now-e.at>TTL){entries.remove(k);return null;}
  return e.data.clone();
 }
 synchronized void put(long p,long l,int d,long now,long[] data){
  if(data==null||data.length<5||(data[0]!=2&&data[0]!=4)||data[1]!=0||data[4]!=1)return;
  StockRoadGeometry geometry=StockRoadGeometry.decode(data);
  if(geometry==null||geometry.status!=0||geometry.valid==0||geometry.valid!=geometry.rows)return;
  String k=key(p,l,d);entries.remove(k);
  while(entries.size()>=CAPACITY)entries.remove(entries.keySet().iterator().next());
  entries.put(k,new Entry(now,data));
 }
}
