package hmgapi.mods.projection.carplay;
public final class JunctionHintsTest {
 static void ok(boolean v){if(!v)throw new AssertionError();}
 public static void main(String[] args){
  ok(JunctionHints.threshold(0)==100);ok(JunctionHints.threshold(19.9f)==100);
  ok(JunctionHints.threshold(20)==400);ok(JunctionHints.threshold(59.9f)==400);
  ok(JunctionHints.threshold(60)==500);ok(JunctionHints.threshold(Float.NaN)==-1);
  String frame="1000,13,2065,270,480,0,0,0,90,0";
  float[] a=JunctionHints.match(frame,1500,60,432,270,70,"left");
  ok(a!=null && a[3]==432 && a[2]==270);
  ok(JunctionHints.match(frame,1500,20,432,270,70,"")==null);
  ok(JunctionHints.match(frame,3100,60,432,270,70,"")==null);
  ok(JunctionHints.match(frame,1500,60,432,90,67,"")==null);
  ok(JunctionHints.match(frame,1500,60,300,270,70,"")==null);
  ok(JunctionHints.match("1000,8,0,0,0,0,0,0,0,-1",1500,60,20,0,65,"")==null);
  ok(JunctionHints.match("1000,46,0,90,90,0,0,0,20,0",1500,0,80,90,84,"지하차도 옆길")!=null);
  ok(JunctionHints.match("1000,46,0,90,90,0,0,0,20,0",1500,0,80,90,84,"지하차도 진입")==null);
  ok(JunctionHints.match("bad",1500,60,432,270,70,"")==null);
  System.out.println("Junction thresholds, freshness, branch, distance and underpass checks passed");
 }
}
