package hmgapi.mods.projection.carplay;
public final class TollDisplayTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] a){TollDisplay d=new TollDisplay();ok(d.draw(178,1000,1000)==null);ok(d.draw(178,990,2000)!=null);ok(d.draw(178,1020,3000)[3]==1020);ok(d.draw(178,1040,4000)!=null);ok(d.draw(178,100,5000)!=null);ok(d.draw(178,0,6000)[7]==10);ok(d.draw(178,1050,7000)==null);ok(d.draw(178,200,8000)!=null);ok(d.draw(178,190,20001)!=null);ok(d.draw(178,1010,21000)==null);ok(d.draw(178,900,22000)!=null);d.clear();ok(d.draw(178,1020,23000)==null);ok(d.draw(183,100,24000)==null);System.out.println("Toll approach:1000m entry/1100m exit margin, bounded fresh samples, distance resets and clear passed");}
}
