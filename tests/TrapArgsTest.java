package hmgapi.mods.projection;
import java.lang.reflect.*;
public final class TrapArgsTest {
 public static class Base {public int inherited=99;}
 public static class Info extends Base {public int barGraphLevel,displayTG=2,camera=123,speed=80,warning=7;public static final int CONSTANT=1;public Info(){}}
 public static class Bad {public int barGraphLevel;private int hidden;public Bad(){}}
 public static class BadType {public int barGraphLevel;public String text;public BadType(){}}
 public static class BadFinal {public final int barGraphLevel=10;}
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args)throws Exception {
  Object[] a=new Object[17];for(int i=0;i<17;i++)a[i]=i+1000;a[11]=40;
  Object[] changed=TrapArgs.withBar(a,false,null,70);ok((Integer)a[11]==40&&(Integer)changed[11]==70);
  for(int i=0;i<17;i++)if(i!=11)ok(a[i].equals(changed[i]));
  Info original=new Info();original.barGraphLevel=30;Field bar=TrapArgs.barField(Info.class);ok(bar!=null);
  Info copy=(Info)TrapArgs.withBar(new Object[]{original},true,bar,90)[0];ok(copy!=original&&original.barGraphLevel==30&&copy.barGraphLevel==90&&copy.camera==123&&copy.speed==80&&copy.warning==7&&copy.inherited==99);
  ok(TrapArgs.barField(Bad.class)==null&&TrapArgs.barField(BadType.class)==null&&TrapArgs.barField(BadFinal.class)==null);
  a[10]=2;Object[] toll=TrapArgs.withPresentation(a,false,null,40,1);ok((Integer)toll[10]==1&&(Integer)a[10]==2);
  Object[] restored=TrapArgs.withPresentation(a,false,null,40,-1);ok((Integer)restored[10]==2);
  Info flagged=(Info)TrapArgs.withPresentation(new Object[]{original},true,bar,40,1)[0];ok(flagged.displayTG==1&&original.displayTG==2&&flagged.camera==123);
  a[10]=99;toll=TrapArgs.withPresentation(a,false,null,40,1);ok((Integer)toll[10]==99);
  a[11]=101;ok(TrapArgs.intBar(a)==-1);ok(TrapArgs.intBar(new Object[16])==-1);
  System.out.println("Trap frame copies: legacy16 other values, object/inherited fields, original immutability and unknown layout refusal passed");
 }
}
