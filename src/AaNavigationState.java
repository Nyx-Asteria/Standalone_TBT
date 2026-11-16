package hmgapi.mods.projection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import hmgapi.mods.projection.carplay.LaneGlyphs;

/** Bounded semantic AA state decoder. Unknown fields are skipped, never guessed.
 * Field layout checked against open-android-auto navigation notification schema.
 * Distances and pocket-lane origin flags are not present in this state message.
 */
final class AaNavigationState {
    final int steps, lanes, recommendedDirections, nextIcon, currentIcon;
    final String laneHex;
    private AaNavigationState(int s, int l, int r, int n, String h, int current) {
        steps=s; lanes=l; recommendedDirections=r; nextIcon=n; laneHex=h;currentIcon=current;
    }
    static AaNavigationState decode(String dump) {
        if (dump == null || !dump.startsWith("S ") || dump.length()>131100) return null;
        try {
            int split=dump.indexOf(' ',2);
            if (split<0) return null;
            int size=Integer.parseInt(dump.substring(2,split));
            if (size<0 || size>65536 || dump.length()-split-1!=size*2) return null;
            byte[] bytes=new byte[size];
            for(int i=0;i<size;i++) {
                int hi=Character.digit(dump.charAt(split+1+i*2),16);
                int lo=Character.digit(dump.charAt(split+2+i*2),16);
                if(hi<0 || lo<0) return null;
                bytes[i]=(byte)(hi*16+lo);
            }
            List<Field> top=fields(bytes), stepList=new ArrayList<Field>();
            for(Field f:top) if(f.number==1 && f.data!=null) stepList.add(f);
            if(stepList.size()>64) return null;
            int current=0;
            if(!stepList.isEmpty())for(Field f:fields(stepList.get(0).data))if(f.number==1&&f.data!=null)
                for(Field m:fields(f.data))if(m.number==1&&m.data==null)current=icon(m.value);
            int next=0;
            if(stepList.size()>1) {
                for(Field f:fields(stepList.get(1).data)) if(f.number==1 && f.data!=null)
                    for(Field m:fields(f.data)) if(m.number==1 && m.data==null) next=icon(m.value);
            }
            List<Field> laneList=new ArrayList<Field>();
            if(!stepList.isEmpty()) for(Field f:fields(stepList.get(0).data))
                if(f.number==3 && f.data!=null) laneList.add(f);
            if(laneList.size()>8) return new AaNavigationState(stepList.size(),laneList.size(),0,next,null,current);
            StringBuilder hex=new StringBuilder(); append(hex,laneList.size());
            int recommended=0;
            for(Field lane:laneList) {
                List<Integer> all=new ArrayList<Integer>(), selected=new ArrayList<Integer>();
                boolean unknown=false;
                for(Field d:fields(lane.data)) if(d.number==1 && d.data!=null) {
                    long shape=0, rec=0;
                    for(Field a:fields(d.data)) if(a.data==null) {
                        if(a.number==1) shape=a.value;
                        if(a.number==2) rec=a.value;
                    }
                    int angle=angle(shape);
                    if(angle==999) { unknown=true; continue; }
                    all.add(angle);
                    if(rec==1) { selected.add(angle); recommended++; }
                }
                // Preserve lane positions: omit the whole frame rather than shift unknown lanes.
                if(unknown || all.isEmpty() || all.size()>3) return new AaNavigationState(stepList.size(),laneList.size(),recommended,next,null,current);
                int[] glyph=LaneGlyphs.encode(2,ints(all),ints(selected));
                // Boolean false means recommendation absent, not proven off-route.
                int colors=glyph[0];
                for(int j=0;j<3;j++) if(glyph[j+1]!=0 && ((colors>>(j*2))&3)==3)
                    colors=(colors & ~(3<<(j*2))) | (1<<(j*2));
                append(hex,colors); for(int j=1;j<=3;j++) append(hex,glyph[j]);
            }
            return new AaNavigationState(stepList.size(),laneList.size(),recommended,next,hex.toString(),current);
        } catch(IllegalArgumentException error) { return null; }
    }
    static int[] ints(List<Integer> a) { int[] r=new int[a.size()]; for(int i=0;i<r.length;i++)r[i]=a.get(i); return r; }
    static void append(StringBuilder b,int v) { b.append(Character.forDigit((v>>4)&15,16)).append(Character.forDigit(v&15,16)); }
    static int angle(long s) { if(s<1 || s>9)return 999; switch((int)s) {
        case 1:return 0; case 2:return -45; case 3:return 45; case 4:return -90;
        case 5:return 90; case 6:return -135; case 7:return 135; case 8:return -180; case 9:return 180; default:return 999;
    }}
    static int direction(int icon){switch(icon){case 71:return 330;case 66:return 30;
      case 70:return 270;case 67:return 90;case 69:return 225;case 68:return 135;
      case 72:case 73:return 180;default:return 0;}}
    static int icon(long type) { if(type<0 || type>50)return 0; switch((int)type) {
        case 3:case 5:return 71; case 4:case 6:return 66;
        case 7:return 70; case 8:return 67; case 9:return 69; case 10:return 68;
        case 11:return 72; case 12:return 73; case 36:return 65; case 39:return 112; default:return 0;
    }}
    private static final class Field {
        int number; long value; byte[] data;
    }
    private static List<Field> fields(byte[] b) {
        List<Field> out=new ArrayList<Field>(); int[] pos={0};
        while(pos[0]<b.length) {
            if(out.size()>=4096) throw new IllegalArgumentException();
            long tag=varint(b,pos); if(tag<=0 || tag>>>3>0x1fffffff) throw new IllegalArgumentException();
            Field f=new Field(); f.number=(int)(tag>>>3); int wire=(int)(tag&7);
            if(wire==0) f.value=varint(b,pos);
            else {
                long length=wire==2?varint(b,pos):wire==1?8:wire==5?4:-1;
                if(length<0 || length>b.length-pos[0]) throw new IllegalArgumentException();
                if(wire==2) f.data=Arrays.copyOfRange(b,pos[0],pos[0]+(int)length);
                pos[0]+=(int)length;
                // Fixed-size unknown fields must not be mistaken for enum varints.
                if(wire!=2) continue;
            }
            out.add(f);
        }
        return out;
    }
    private static long varint(byte[] b,int[] p) {
        long n=0;
        for(int s=0;s<64;s+=7) {
            if(p[0]>=b.length) throw new IllegalArgumentException();
            int v=b[p[0]++]&255; if(s==63 && (v&254)!=0)throw new IllegalArgumentException();
            n|=(long)(v&127)<<s; if((v&128)==0)return n;
        }
        throw new IllegalArgumentException();
    }
}
