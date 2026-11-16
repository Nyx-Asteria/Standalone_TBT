package hmgapi.mods.projection.carplay;
public final class TurnPresentationTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args) {
  ok(TurnPresentation.direction(70,false,0)==270);
  ok(TurnPresentation.direction(67,false,0)==90);
  ok(TurnPresentation.direction(70,true,-105)==255);
  ok(TurnPresentation.direction(70,true,999)==270);
  ok(TurnPresentation.bar(67,2,80)[0]==20);
  ok(TurnPresentation.bar(67,2,0)[0]==10);
  ok(TurnPresentation.bar(8,2,0)[1]==-1);
  ok(TurnPresentation.bar(0,2,0)[1]==-1);
  ok(TurnPresentation.bar(67,2,70)[0]==20);
  ok(TurnPresentation.bar(67,2,1700)[1]==-1);
  ok(TurnPresentation.bar(91,3,1700)[1]==-1);
  ok(TurnPresentation.bar(164,23,1000)[1]==0);
  ok(TurnPresentation.bar(183,3,80)[1]==-1);
  ok(TurnPresentation.bar(67,2,Float.NaN)[1]==-1);
  System.out.println("Fallback TBT: known direction, captured80/70m bar20, absent/invalid/far hiding, no invented side roads passed");
 }
}
