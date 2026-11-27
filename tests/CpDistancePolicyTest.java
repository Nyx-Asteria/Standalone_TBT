package hmgapi.mods.projection.carplay;
public final class CpDistancePolicyTest {
    static void check(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) {
        check(CpDistancePolicy.retainNumeric(0,0,false));
        check(!CpDistancePolicy.retainNumeric(0,1,false));
        check(CpDistancePolicy.retainNumeric(0,1,true));
        check(!CpDistancePolicy.retainNumeric(0,-1,false));
        check(!CpDistancePolicy.retainFormatted(0,0,true,false,false));
        check(CpDistancePolicy.retainFormatted(0,0,false,false,false));
        check(CpDistancePolicy.retainFormatted(0,0,true,true,true));
        check(!CpDistancePolicy.retainFormatted(0,1,false,false,false));
        check(!CpDistancePolicy.retainFormatted(0,1,true,true,false));
        check(CpDistancePolicy.retainFormatted(0,1,true,true,true));
        check(CpDistancePolicy.retainFormatted(-1,0,false,true,true));
        System.out.println("CpDistancePolicy: 11 checks passed");
    }
}
