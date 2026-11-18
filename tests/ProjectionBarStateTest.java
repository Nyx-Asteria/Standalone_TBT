package hmgapi.mods.projection;
public final class ProjectionBarStateTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  ProjectionBar.Sender s=new ProjectionBar.Sender();ok(s.due(70,0));ok(!s.due(70,9999));ok(s.due(70,10000));ok(s.due(-1,10001));ok(!s.due(-1,500000));
  ProjectionBar.Navi n=new ProjectionBar.Navi();ok(n.onBar(70,true,0)==null);ok(n.onStock(40,true,1)==70);ok(n.check(true,10000)==null);
  ok(n.onBar(70,true,10000)==null);ok(n.onBar(50,true,11000)==50);ok(n.onStock(30,true,11001)==50);ok(n.onBar(-1,true,12000)==30);ok(n.check(false,12001)==null);
  ok(n.onBar(90,true,13000)==90);ok(n.check(false,13001)==30);ok(n.onBar(80,true,14000)==80);ok(n.check(true,194001)==30);
  ok(n.onBar(200,true,195000)==null);ok(!n.overriding);
  Object[] a={13,2065,270f,100f,0,0,0,60,0};ok(ProjectionBar.fromTbt(a)==60);a[0]=32;ok(ProjectionBar.fromTbt(a)==60);a[0]=8;ok(ProjectionBar.fromTbt(a)==-1);a[0]=67;a[8]=-1;ok(ProjectionBar.fromTbt(a)==0);a[7]=254;ok(ProjectionBar.fromTbt(a)==-1);
  Object[] toll={178,0,0f,400f,0,0,0,40,0};ok(ProjectionBar.tollFromTbt(toll)==1);
  toll[0]=184;ok(ProjectionBar.tollFromTbt(toll)==1);toll[6]=1;ok(ProjectionBar.tollFromTbt(toll)==0);
  toll[6]=0;toll[3]=1100f;ok(ProjectionBar.tollFromTbt(toll)==0);toll[3]=400f;toll[7]=0;ok(ProjectionBar.tollFromTbt(toll)==0);
  ProjectionBar.Sender ts=new ProjectionBar.Sender();ok(ts.due(40,0,0));ok(ts.due(40,1,1));ok(!ts.due(40,1,2));ok(ts.due(40,0,3));
  ok(ts.due(40,1,1000));ok(!ts.due(40,1,1199));ok(ts.due(40,1,1200));
  System.out.println("Projection gauge: dual-frame level extraction, change/heartbeat, missing stock, expiry, clear and restoration passed");
 }
}
