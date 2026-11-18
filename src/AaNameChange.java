package hmgapi.mods.projection;
/** Short-lived directionless notifications must not immediately replace a real maneuver. */
final class AaNameChange {
 static final long DELAY_MS=3500;
 final long due;
 String text;int side,angle,number;boolean camera,conflictingDistance;
 int[] distance;
 AaNameChange(long now,String text,int side,int angle,int number){due=now+DELAY_MS;update(text,side,angle,number);}
 void update(String t,int s,int a,int n){text=t;side=s;angle=a;number=n;camera=cameraText(t);}
 static boolean cameraText(String text) {
  if(text==null)return false;
  String s=text.replaceAll("\\s+","");
  return s.equals("박스형")||s.equals("이동식")||s.contains("단속카메라")
      ||s.contains("과속단속")||s.contains("구간단속")||s.contains("신호위반단속");
 }
 static boolean defer(int ev,int side,boolean current,float meters) {
  return ev==2&&side==3&&current&&!Float.isNaN(meters)&&!Float.isInfinite(meters)&&meters>50;
 }
}
