// Adapted from StopDragon/HMG-API 6b7211051f9d5edddf1b627fc92d71719bb6aa98 (AGPL-3.0); see NOTICE.txt.
package hmgapi.mods.projection;
// Korea Projection (StopDragon/HMG-API, branch korea-build), 2026-11-09, 모드 2.3.3.

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 단속 정보 프레임 인자에서 막대 칸만 바꾼 사본을 만든다(ProjectionTrapBar가 쓴다, 안드로이드·HMG-API 없이 PC에서 시험한다).
 *   - 17 x int 판: 인자 11(barGraphLevel)만 바꾼다. 그 자리 값이 0..100 밖이면 순서가 다른 펌웨어로 보고 바꾸지 않는다.
 *   - 객체 판(NaviTbtTrapInfo): 인스턴스 칸이 모두 public int일 때만 쓴다(그래야 사본이 칸을 빠짐없이 옮긴다).
 *     새 객체에 칸을 모두 옮기고 barGraphLevel만 바꾼다. 순정이 넘긴 객체는 건드리지 않는다.
 */
final class TrapArgs {
    private TrapArgs() {}

    /** updateTBTTrapInfo(17 x int)에서 barGraphLevel 자리(0부터, 순정 서명 순서) */
    static final int BAR_ARG = 11;
    static final int INT_ARGS = 17;
    static final int TOLL_ARG = 10;
    /** Only the display flag changes; toll fee/currency and safety fields stay stock. */
    static Object[] withPresentation(Object[] a,boolean isNew,Field bar,int value,int toll)throws Exception {
        Object[] copy=withBar(a,isNew,bar,value);
        if(toll!=1)return copy;
        if(isNew){
            try{Field f=copy[0].getClass().getField("displayTG");
                if(f.getType()==int.class&&!Modifier.isFinal(f.getModifiers())&&!Modifier.isStatic(f.getModifiers())){
                    int old=f.getInt(copy[0]);if(old>=0&&old<=3)f.setInt(copy[0],1);
                }
            }catch(NoSuchFieldException ignored){}
        }else if(copy[TOLL_ARG] instanceof Integer){int old=(Integer)copy[TOLL_ARG];if(old>=0&&old<=3)copy[TOLL_ARG]=1;}
        return copy;
    }
    static final String BAR_FIELD = "barGraphLevel";

    /** 객체 판을 써도 되는지: static이 아닌 칸이 모두 public int이고 barGraphLevel이 있다 */
    static Field barField(Class<?> info) {
        Field bar = null;
        for (Class<?> c = info; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                int m = f.getModifiers();
                if (Modifier.isStatic(m) || f.isSynthetic()) continue;
                // final 칸은 사본에 옮길 수 없다(2차 리뷰 3)
                if (!Modifier.isPublic(m) || Modifier.isFinal(m) || f.getType() != int.class) return null;
                if (BAR_FIELD.equals(f.getName()) && c == info) bar = f;
            }
        }
        return bar;
    }

    /** 17 x int 판의 막대 값, 쓸 수 없으면 -1 */
    static int intBar(Object[] a) {
        if (a == null || a.length != INT_ARGS || !(a[BAR_ARG] instanceof Integer)) return -1;
        int v = (Integer) a[BAR_ARG];
        return v < 0 || v > ProjectionBar.MAX ? -1 : v;
    }

    /** 객체 사본: 새 객체에 인스턴스 int 칸을 모두 옮긴다 */
    static Object copy(Object o) throws Exception {
        Class<?> c = o.getClass();
        Object n = c.newInstance();
        for (Field f : c.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.getType() != int.class) continue;
            f.setInt(n, f.getInt(o));
        }
        return n;
    }

    /** 인자 사본에 막대만 바꾼 것. isNew면 [0]이 객체 사본이다 */
    static Object[] withBar(Object[] a, boolean isNew, Field bar, int value) throws Exception {
        Object[] r = a.clone();
        if (isNew) {
            Object c = copy(a[0]);
            bar.setInt(c, value);
            r[0] = c;
        } else {
            r[BAR_ARG] = Integer.valueOf(value);
        }
        return r;
    }
}

