package hmgapi.mods.projection.carplay;

public final class CpStartupTest {
    static int checks;
    static void eq(int expected, int actual) {
        checks++;
        if (expected != actual) throw new AssertionError(expected + " != " + actual);
    }
    public static void main(String[] args) {
        // First TMAP snapshot 08:16:29: name absent, list=[0], count=3.
        eq(1, CpTurns.adjacentIndex(null, 1, new int[]{0}, 3));
        eq(1, CpTurns.adjacentIndex("", 1, new int[]{0}, 3));
        eq(1, CpTurns.adjacentIndex("네이버지도", 1, new int[]{0}, 3));
        eq(1, CpTurns.adjacentIndex("TMAP", 1, new int[]{0}, 3));
        // Kakao 08:32:07 sends current 0 and received upcoming 1 (1600 m), count 3.
        eq(1, CpTurns.adjacentIndex("카카오내비", 1, new int[]{0}, 3));
        eq(-1, CpTurns.adjacentIndex("Apple Maps", 1, new int[]{0}, 3));
        eq(-1, CpTurns.adjacentIndex(null, 0, new int[]{0}, 3));
        eq(-1, CpTurns.adjacentIndex(null, 1, new int[]{0,1}, 3));
        eq(-1, CpTurns.adjacentIndex(null, 1, new int[]{2}, 3));
        eq(-1, CpTurns.adjacentIndex(null, 1, new int[]{-1}, 3));
        eq(-1, CpTurns.adjacentIndex(null, 1, new int[]{Integer.MAX_VALUE}, 3));
        CpTurns.Update route = new CpTurns.Update(5, null);
        CpTurns.Maneuver current = new CpTurns.Maneuver(0, 5, null);
        eq(-1, CpTurns.startupMeters(route, current));
        // Naver 08:18:01.810 supplies 65 m before route distance at 08:18:01.830.
        current.distance=65; current.hasDistance=true;
        eq(65, CpTurns.startupMeters(route, current));
        route.distNext=60; route.hasNextDistance=true;
        eq(60, CpTurns.startupMeters(route, current));
        route.distNext=-1;
        eq(65, CpTurns.startupMeters(route, current));
        route.hasNextDistance=false; current.distance=0;
        eq(0, CpTurns.startupMeters(route, current));
        current.clear();
        eq(-1, CpTurns.startupMeters(route, current));
        System.out.println("CpStartup: " + checks + " checks passed");
    }
}
