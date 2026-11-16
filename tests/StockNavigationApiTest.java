package hmgapi.mods.projection;
public final class StockNavigationApiTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  StockNavigationApi.Position p=StockNavigationApi.decode("1000,36.5,127.1,270",1200);
  ok(p!=null && p.latitude==36.5 && p.longitude==127.1 && p.heading==270);
  ok(StockNavigationApi.decode("1000,36.5,127.1,270",3501)==null);
  ok(StockNavigationApi.decode("1000,36.5,127.1,270",999)==null);
  ok(StockNavigationApi.decode("1000,NaN,127.1,270",1200)==null);
  ok(StockNavigationApi.decode("1000,36.5,Infinity,270",1200)==null);
  ok(StockNavigationApi.decode("1000,36.5,127.1,360",1200)==null);
  ok(StockNavigationApi.decode("1000,91,127.1,270",1200)==null);
  ok(StockNavigationApi.decode("unknown",1200)==null);
  System.out.println("Stock navigation API: position fields, stale/future and malformed readings passed");
 }
}
