package hmgapi.mods.projection;
import java.io.*;
import java.util.*;
public final class AaNavigationStateTest {
    static byte[] field(int n, byte[] b) { if(b.length>127)throw new AssertionError(); byte[] r=new byte[b.length+2];r[0]=(byte)(n*8+2);r[1]=(byte)b.length;System.arraycopy(b,0,r,2,b.length);return r; }
    static byte[] cat(byte[]... a) {ByteArrayOutputStream b=new ByteArrayOutputStream();for(byte[] v:a)b.write(v,0,v.length);return b.toByteArray();}
    static byte[] direction(int shape,boolean rec) {return field(1,new byte[]{8,(byte)shape,16,(byte)(rec?1:0)});}
    static String packet(byte[] b) {StringBuilder h=new StringBuilder("S "+b.length+" ");for(byte v:b)AaNavigationState.append(h,v&255);return h.toString();}
    static void check(boolean b) {if(!b)throw new AssertionError();}
    public static void main(String[] args) throws Exception {
        byte[] maneuver=field(1,new byte[]{8,7});
        byte[] lane=field(3,cat(direction(1,false),direction(4,true)));
        String two=packet(cat(field(1,cat(maneuver,lane)),field(1,maneuver)));
        AaNavigationState r=AaNavigationState.decode(two);
        check(r!=null && r.currentIcon==70 && r.steps==2 && r.lanes==1 && r.recommendedDirections==1 && r.nextIcon==70);
        // Straight+left retains both shapes, white straight and blue left (2-bit colors).
        check(r.laneHex.equals("0109090c00"));
        check(AaNavigationState.decode(two.toUpperCase()).laneHex.equals(r.laneHex));
        AaNavigationState one=AaNavigationState.decode(packet(field(1,maneuver)));
        check(one.steps==1 && one.nextIcon==0 && one.lanes==0 && one.laneHex.equals("00"));
        check(AaNavigationState.decode(packet(field(1,field(3,direction(99,true))))).laneHex==null);
        check(AaNavigationState.decode(packet(new byte[]{0}))==null);
        check(AaNavigationState.decode("S 3 0a08ff")==null);
        check(AaNavigationState.icon(999)==0);
        for(int end=0;end<two.length();end++)check(AaNavigationState.decode(two.substring(0,end))==null);
        int count=0;
        if(args.length>0)try(BufferedReader in=new BufferedReader(new FileReader(args[0]))) {
            String s;while((s=in.readLine())!=null){AaNavigationState p=AaNavigationState.decode(s);check(p!=null && p.steps==1 && p.lanes==0 && p.nextIcon==0);count++;}
        }
        System.out.println("AA state: multi-step, compound lane/colors, unknown enum, truncation and "+count+" saved packets passed");
    }
}
