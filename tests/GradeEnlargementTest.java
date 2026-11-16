package hmgapi.mods.projection.carplay;
public final class GradeEnlargementTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args) {
  int[][] pairs={{88,44,270},{87,43,90},{82,43,270},{81,44,90},
    {91,46,270},{90,45,90},{85,45,270},{84,46,90}};
  for(int[] p:pairs){float[] f=GradeEnlargement.draw(p[0],100,400);ok(f!=null&&f[0]==p[1]&&f[1]==0&&f[2]==p[2]&&f[3]==100);}
  ok(GradeEnlargement.draw(65,100,400)==null);
  ok(GradeEnlargement.draw(91,401,400)==null);
  ok(GradeEnlargement.draw(91,Float.NaN,400)==null);
  System.out.println("Factory grade selectors: eight exact over/under entry/side variants and invalid range guards passed");
 }
}
