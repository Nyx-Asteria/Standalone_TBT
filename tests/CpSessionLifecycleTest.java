package hmgapi.mods.projection.carplay;
public final class CpSessionLifecycleTest {
    private static void ok(boolean b) { if (!b) throw new AssertionError(); }
    public static void main(String[] args) {
        CpSessionLifecycle s = new CpSessionLifecycle();
        ok(s.ignoreDisconnect(2)); ok(!s.ignoreDisconnect(4));
        s.start(true, true); int old = s.generation();
        ok(s.wireless() && s.current(old));
        ok(s.ignoreDisconnect(4) && s.ignoreDisconnect(5) && !s.ignoreDisconnect(1));
        s.end(); ok(!s.current(old) && !s.ignoreDisconnect(5));
        s.start(false, true); ok(!s.wireless() && !s.ignoreDisconnect(4));
        s.start(true, false); ok(!s.wireless() && !s.ignoreDisconnect(5));
        s.end(); ok(!s.active());
        System.out.println("CarPlay lifecycle: wireless bridge churn, finalization, wired and legacy fallback passed");
    }
}
