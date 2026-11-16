package hmgapi.mods.projection.carplay;
public final class DisplayFormatTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] a){ok(DisplayFormat.distance(90.9f,1)==90);ok(DisplayFormat.distance(9.9f,1)==9.9f);ok(DisplayFormat.distance(10,1)==10);ok(DisplayFormat.distance(90.9f,2)==90.9f);ok(DisplayFormat.distance(350,0)==350);ok(Float.isNaN(DisplayFormat.distance(Float.NaN,1)));System.out.println("Metric numeric formatting: below10km tenths, at/above10km integer, other units retained passed");}
}
