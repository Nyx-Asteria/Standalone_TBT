package hmgapi.mods.projection.carplay;
public final class JunctionTargetTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){JunctionTarget t=new JunctionTarget();
 String a=t.select("CP/5/23",1000,1000);ok(a.equals(t.select("CP/5/23",14000,701))==false); // gap expires
 a=t.select("CP/5/23",15000,701);ok(a.equals(t.select("CP/5/23",16000,680)));
 ok(!a.equals(t.select("CP/5/23",17000,1000))); // route recalculation
 a=t.select("AA/left",18000,45);for(int n=1;n<25;n++)ok(a.equals(t.select("AA/left",18000+n*1000,45))); // alternating labels cannot reset identity
 ok(!a.equals(t.select("AA/right",43000,40)));
 a=t.select("AA/right",44000,5);ok(!a.equals(t.select("AA/right",45000,15))); // next close same-direction maneuver
 a=t.select("AA/right",46000,10);t.clear();ok(!a.equals(t.select("AA/right",46001,10)));
 System.out.println("Target identity: same-maneuver renumber/name changes, distance reset, direction, expiry and route clear passed");}
}
