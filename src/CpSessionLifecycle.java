package hmgapi.mods.projection.carplay;

/** Distinguishes wireless bridge transport churn from actual CarPlay finalization. */
final class CpSessionLifecycle {
    private boolean active, wireless, tracked;
    private int generation;
    synchronized void start(boolean wireless, boolean tracked) {
        generation++;
        active = true;
        this.wireless = wireless;
        this.tracked = tracked;
    }
    synchronized void end() { active = false; wireless = false; generation++; }
    synchronized boolean active() { return active; }
    synchronized boolean wireless() { return active && tracked && wireless; }
    synchronized int generation() { return generation; }
    synchronized boolean current(int token) { return active && generation == token; }
    synchronized boolean ignoreDisconnect(int component) {
        return component == 2 || (wireless() && (component == 4 || component == 5));
    }
}
