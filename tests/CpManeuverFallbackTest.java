package hmgapi.mods.projection.carplay;

public final class CpManeuverFallbackTest {
    static int checks;
    static void eq(int e,int a){checks++;if(e!=a)throw new AssertionError(e+" != "+a);}
    static CpTurns.Maneuver m(int type) {
        CpTurns.Maneuver m=new CpTurns.Maneuver(0,5,null);m.clear();m.type=type;return m;
    }
    public static void main(String[] args) {
        // Kakao 08:59:26: type 6, junction absent/default 0, no angle.
        CpTurns.Maneuver enter=m(6);
        enter.afterRoad="공주IC 방면";
        eq(64,CpTurns.maneuverIcon(enter,1));
        if(!enter.displayRoad().contains("회전교차로 진입"))throw new AssertionError("missing roundabout text");
        // An explicit angle is different from an absent field defaulting to zero.
        enter.hasExit=true;enter.exit=0;
        eq(96,CpTurns.maneuverIcon(enter,1));
        enter.exit=-90;eq(105,CpTurns.maneuverIcon(enter,1));
        enter.exit=90;eq(99,CpTurns.maneuverIcon(enter,1));
        enter.side=1;eq(64,CpTurns.maneuverIcon(enter,1));
        enter.side=0;enter.exit=999;eq(64,CpTurns.maneuverIcon(enter,1));
        eq(64,CpTurns.maneuverIcon(m(7),1));
        eq(64,CpTurns.maneuverIcon(m(11),1));
        // Kakao 08:53:46/08:57:14: next type 50 has no underpass text.
        eq(66,CpTurns.aheadIcon(m(50)));
        eq(71,CpTurns.aheadIcon(m(49)));
        eq(69,CpTurns.aheadIcon(m(47)));
        eq(68,CpTurns.aheadIcon(m(48)));
        eq(70,CpTurns.aheadIcon(m(20)));
        eq(67,CpTurns.aheadIcon(m(21)));
        CpTurns.Maneuver side=m(50);side.afterRoad="지하차도 옆길";
        eq(84,CpTurns.aheadIcon(side));
        eq(84,CpTurns.maneuverIcon(side,1));
        eq(64,CpTurns.aheadIcon(m(999)));
        System.out.println("CpManeuverFallback: "+checks+" checks passed");
    }
}
