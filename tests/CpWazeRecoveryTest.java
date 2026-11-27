package hmgapi.mods.projection.carplay;

public final class CpWazeRecoveryTest {
    private static void check(boolean value) {
        if (!value) throw new AssertionError("Waze recovery policy");
    }

    public static void main(String[] args) {
        CpWazeRecovery p = new CpWazeRecovery();
        check(p.observe(1, "Google Maps", true, false) == -1);
        check(p.observe(1, "Waze", true, true) == -1);
        int first = p.observe(1, "Waze", true, false);
        check(first >= 0 && p.delay() == 3000);
        check(p.observe(1, "Waze", true, false) == -1);
        check(p.fire(1, first) == 1);
        check(p.fire(1, first) == 0);
        int second = p.observe(1, "Waze", true, false);
        check(second >= 0 && p.delay() == 7000);
        check(p.fire(1, second) == 2);
        check(p.observe(1, "Waze", true, false) == -1);
        p.sourceChanged(1, "Apple Maps");
        int switched = p.observe(1, "Waze", true, false);
        check(switched >= 0 && p.delay() == 3000);
        check(p.observe(1, "Waze", false, false) == -1);
        check(p.fire(1, switched) == 0);
        int unsupported = p.observe(1, "Waze", true, false);
        p.observe(1, "Waze", true, true);
        check(p.fire(1, unsupported) == 0);
        int old = p.observe(1, "Waze", true, false);
        int fresh = p.observe(2, "Waze", true, false);
        check(p.fire(1, old) == 0 && p.fire(2, old) == 0);
        check(p.fire(2, fresh) == 1);
        p.cancel();
        System.out.println("CpWazeRecoveryTest passed");
    }
}
