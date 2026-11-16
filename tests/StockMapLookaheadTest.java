package hmgapi.mods.projection;
import java.util.*;
public final class StockMapLookaheadTest {
 static final long X=127*360000L,Y=37*360000L;
 static long[] row(int id,long ax,long ay,long bx,long by,int length){return new long[]{1,id,0,length,2,ax,ay,bx,by,ax,ay,bx,by};}
 static long[] node(long[] current,long[]... roads){long[] a=new long[5+13*(1+roads.length)];a[0]=2;a[2]=1+roads.length;a[3]=roads.length;a[4]=1;System.arraycopy(current,0,a,5,13);for(int i=0;i<roads.length;i++)System.arraycopy(roads[i],0,a,18+13*i,13);return a;}
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  final long[][] links=new long[12][];for(int i=0;i<12;i++)links[i]=row(i+1,X,Y+(i-1)*324,X,Y+i*324,100);
  final Map<Long,long[]> graph=new HashMap<Long,long[]>();
  for(int i=0;i<11;i++)graph.put((long)i+1,node(links[i],links[i],links[i+1]));
  long[] left=row(50,X,Y+3*324,X-405,Y+3*324,100),right=row(51,X,Y+3*324,X+405,Y+3*324,100);
  graph.put(4L,node(links[3],links[3],links[4],left,right));
  StockMapLookahead.Reader reader=new StockMapLookahead.Reader(){public long[] read(long p,long l,int d){return graph.get(l);}};
  long[] match=StockMapLookahead.find(graph.get(1L),1,127,37-.0009,400,123,reader);
  ok(match!=null && match[0]==3 && match[5]==400 && match[6]==123);
  // The current-link node is100m away; the fourth forward node must be used at400m.
  ok(match[8]==4);
  ok(StockMapLookahead.find(graph.get(1L),1,128,37,400,123,reader)==null);
  ok(StockMapLookahead.find(graph.get(1L),1,127,37-.0009,1201,123,reader)==null);
  long[] highway=row(80,X,Y-3402,X,Y,1050),east=row(81,X,Y,X+405,Y,100),north=row(82,X,Y,X,Y+324,100);
  long[] highwayNode=node(highway,highway,east,north);
  long[] far=StockMapLookahead.find(highwayNode,1,127,37-3402/360000.0,1050,88,reader);
  ok(far!=null && far[5]==1050);
  long[] graded=Arrays.copyOf(north,15);graded[14]=1;
  long[] plain=Arrays.copyOf(highway,15);
  long[] gradeNode=new long[5+3*15];gradeNode[0]=4;gradeNode[2]=3;gradeNode[3]=2;gradeNode[4]=1;
  System.arraycopy(plain,0,gradeNode,5,15);System.arraycopy(plain,0,gradeNode,20,15);System.arraycopy(graded,0,gradeNode,35,15);
  long[] gradeMatch=StockMapLookahead.find(gradeNode,1,127,37-3402/360000.0,1050,88,reader);
  ok(gradeMatch!=null&&gradeMatch[0]==5&&gradeMatch[5]==1050&&gradeMatch[6]==88&&gradeMatch[gradeMatch.length-1]==1);
  System.out.println("Forward lookup: skips intermediate100m nodes, reaches400m target, handles1050m, rejects off-road/out-of-range passed");
 }
}
