// Adapted from StopDragon/HMG-API 6b7211051f9d5edddf1b627fc92d71719bb6aa98 (AGPL-3.0); see NOTICE.txt.
package hmgapi.mods.projection;
// Korea Projection (StopDragon/HMG-API, branch korea-build), 2026-11-09, 모드 2.3.3.

import android.content.Context;
import android.content.Intent;

/**
 * 폰 안내의 곧 회전 막대를 계기판에 채운다(1.7.2 시험판 3).
 *
 * 순정 내비는 막대가 바뀔 때마다 회전 프레임(updateTBTInfo)과 함께 단속 정보 프레임(updateTBTTrapInfo[New])의
 * barGraphLevel에도 같은 값(10..90)을 넣어 보낸다(차 로그 2026-11-09 15:32). 계기판은 이 칸으로 막대를 그리는 것으로
 * 보인다(회전 프레임 막대만 보낸 시험판 2에서는 막대가 늘 비어 있었다).
 *
 * 단속 정보 프레임은 순정 내비만 보내고, 단속 카메라·제한속도가 함께 들어 있다. 그래서 막대 칸만 바꾼다:
 *   - 카플레이(DiPO)·AA(GPM) 프로세스가 막대 값을 방송(ACTION)으로 알린다. 바뀔 때와 RESEND_MS마다.
 *   - 순정 내비 프로세스(Wireless.navigation)가 순정 단속 정보 호출의 막대 칸을 폰 값으로 바꾼다. 폰 값이 바뀌면
 *     순정이 마지막으로 보낸 단속 정보에 막대만 바꿔 다시 보낸다.
 *   - 폰 안내가 끝나거나(NONE, 폰 안내 아님) 방송이 EXPIRE_MS 동안 없으면 순정의 마지막 막대 값으로 되돌린다.
 * 이 클래스는 판단만 한다(안드로이드 호출은 send와 Wireless 쪽). 같은 보내기 코드가 dipo 꾸러미에도 있다
 * (DiPO 앱 파트는 클래스 로더가 달라 이 클래스를 쓰지 못한다, Mac과 같은 이유).
 */
public final class ProjectionBar {
    private ProjectionBar() {}

    /** 방송 이름과 값(int, 0..90 또는 NONE). 값만 담는다: 이름·주소 없음 */
    public static final String ACTION = "hmgapi.mods.projection.TBT_PROGRESS_BAR";
    public static final String EXTRA_BAR = "bar";
    public static final String EXTRA_TOLL = "displayTG";
    public static final String EXTRA_NAME = "tollName";
    /** 폰이 막대를 정하지 않음(안내 끝): 순정 막대로 되돌린다 */
    public static final int NONE = -1;
    /** 값이 같아도 다시 알리는 간격(순정 내비가 다시 켜져도 되찾게) */
    public static final long RESEND_MS = 10000;
    /**
     * 이만큼 방송이 없으면 폰 값을 버린다. 폰 프로세스가 죽으면 폰 안내 여부(Car.phoneGuiding)가 먼저 거짓이 되어 되돌아가므로,
     * 이것은 그 밖에 방송이 끊긴 경우의 마지막 안전장치다. AA는 이벤트가 올 때만 알리므로(신호 대기 중에는 조용함)
     * 짧게 두면 정차 중 막대가 꺼진다(리뷰 6). 그래서 3분이다.
     */
    public static final long EXPIRE_MS = 180000;
    /** 순정 단속 정보 막대 칸의 범위(NavigationCluster: 0..100 밖이면 0) */
    public static final int MAX = 100;

    /** 보내는 쪽(AA 프로세스): 값이 바뀌었거나, 막대가 있는 동안 RESEND_MS가 지났을 때만 보낸다 */
    public static final class Sender {
        private int last = Integer.MIN_VALUE;
        private long at;
        private int lastToll;
        private String name;private String sentName;
        public void name(String value){name=value;}
        public int level(){return last;}public int toll(){return lastToll;}
        public boolean due(int bar,int toll,long now){
            boolean changed=toll!=lastToll||(toll==1&&!java.util.Objects.equals(name,sentName))||(toll==1&&now-at>=1000);lastToll=toll;sentName=name;
            boolean due=due(bar,now);
            if(changed){last=bar;at=now;}return changed||due;
        }

        /** 이번 값을 보낼지. 보낸다고 하면 그 값을 기억한다 */
        public boolean due(int bar, long now) {
            if (bar == last && (bar == NONE || now - at < RESEND_MS)) return false;
            last = bar;
            at = now;
            return true;
        }
    }

    /** 방송 보내기(실패해도 회전 보내기에는 영향 없음). 보낸 값이면 true */
    public static boolean send(Context c, Sender s, int bar, long now) {
        return send(c,s,bar,0,now);
    }
    public static boolean send(Context c,Sender s,int bar,int toll,long now){
        if (c == null || s == null || !s.due(bar,toll,now)) return false;
        try {
            Intent i = new Intent(ACTION);
            i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            i.setPackage("com.mnsoft.navi");
            i.putExtra(EXTRA_BAR, bar);
            i.putExtra(EXTRA_TOLL,toll);
            if(toll==1&&s.name!=null)i.putExtra(EXTRA_NAME,s.name);
            c.sendBroadcast(i);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 순정 내비 프로세스 쪽 판단. 순정 단속 정보 호출(onStock), 폰 방송(onBar), 순정의 다른 계기판 호출 때 확인(check)에서
     * "이 막대로 다시 보내라"(Integer) 또는 null을 돌려준다. 순정 단속 정보를 한 번도 못 봤으면 다시 보내지 않는다
     * (단속 카메라·제한속도 칸을 우리가 지어내지 않는다).
     */
    public static final class Navi {
        int phoneBar = NONE;
        private boolean phoneToll;
        int toll(){return overriding&&phoneToll?1:-1;}
        public Integer onBar(int bar,int toll,boolean phone,long now){
            int before=toll();phoneToll=toll==1&&bar>0&&bar<=MAX;
            Integer result=onBar(bar,phone,now);
            return result==null&&seen&&before!=toll()?Integer.valueOf(current()):result;
        }
        long barAt;
        /** 마지막으로 계기판에 나간 단속 정보 막대가 폰 값인지 */
        boolean overriding;
        /** 순정이 마지막으로 보낸 막대(그대로 되돌릴 값) */
        int stockBar;
        boolean seen;

        boolean active(boolean phone, long now) {
            return phone && phoneBar != NONE && now - barAt < EXPIRE_MS;
        }

        /** 순정 단속 정보 호출: 실제로 보낼 막대(폰 값 또는 순정 값) */
        public int onStock(int stock, boolean phone, long now) {
            seen = true;
            stockBar = stock;
            overriding = active(phone, now);
            return overriding ? phoneBar : stock;
        }

        /** 폰 방송: 다시 보낼 막대 또는 null */
        public Integer onBar(int bar, boolean phone, long now) {
            if (bar != NONE && (bar < 0 || bar > MAX)) bar = NONE;   // 범위 밖 값은 무시(순정 값으로)
            boolean changed = bar != phoneBar;
            phoneBar = bar;
            barAt = now;
            if (!seen) return null;
            if (active(phone, now)) {
                if (!changed && overriding) return null;
                overriding = true;
                return bar;
            }
            return restore();
        }

        /** 순정 내비의 다른 계기판 호출 때: 폰 안내가 끝났거나 방송이 끊겼으면 순정 막대로 되돌린다 */
        public Integer check(boolean phone, long now) {
            if (!overriding || active(phone, now)) return null;
            return restore();
        }

        /** 지금 계기판에 있어야 할 막대(폰 값 또는 순정 값) */
        public int current() {
            return overriding ? phoneBar : stockBar;
        }

        private Integer restore() {
            if (!overriding) return null;
            overriding = false;
            return stockBar;
        }
    }
    static android.os.Handler replayHandler(){
        android.os.HandlerThread thread=new android.os.HandlerThread("projection-trap-gauge");thread.start();
        return new android.os.Handler(thread.getLooper());
    }
    static boolean guiding(Context context){
        if(hmgapi.hook.Car.phoneGuiding(context))return true;
        try{long remaining=android.provider.Settings.Global.getLong(context.getContentResolver(),"hmgapi_lx2pe_carplay_turn_until",0)-System.currentTimeMillis();return remaining>0&&remaining<=2500;}
        catch(RuntimeException error){return false;}
    }
    static int tollFromTbt(Object[] args){
        if(fromTbt(args)<=0||!(args[3] instanceof Number)||!(args[6] instanceof Integer))return 0;
        int icon=(Integer)args[0],unit=(Integer)args[6];float meters=((Number)args[3]).floatValue();
        return (icon==178||icon==184)&&Integer.valueOf(0).equals(args[1])&&unit==0&&meters>=0&&meters<1100?1:0;
    }
    static int fromTbt(Object[] args){
        if(args==null||args.length!=9||!(args[0] instanceof Integer)||!(args[7] instanceof Integer)||!(args[8] instanceof Integer))return NONE;
        int type=(Integer)args[0],bar=(Integer)args[7],error=(Integer)args[8];
        if(type==0||type==8||bar<0||bar>100)return NONE;
        return error==0?bar:0;
    }

}

