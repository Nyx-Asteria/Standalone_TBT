package hmgapi.mods.projection.carplay;

/** Worker-confined policy; callbacks carry a generation and a cancellation token. */
final class CpWazeRecovery {
    private int generation = -1, epoch, attempts;
    private String source;
    private boolean pending;

    void sourceChanged(int generation, String source) {
        this.generation = generation;
        this.source = source;
        epoch++;
        attempts = 0;
        pending = false;
    }

    int observe(int generation, String source, boolean missing, boolean unsupported) {
        if (this.generation != generation ||
                !(source == null ? this.source == null : source.equals(this.source))) {
            sourceChanged(generation, source);
        }
        if (source == null || !source.toLowerCase(java.util.Locale.ROOT).contains("waze")
                || !missing || unsupported) {
            cancel();
            return -1;
        }
        if (pending || attempts >= 2) return -1;
        pending = true;
        return ++epoch;
    }

    void cancel() {
        if (pending) epoch++;
        pending = false;
    }

    long delay() { return attempts == 0 ? 3000L : 7000L; }

    int fire(int generation, int token) {
        if (!pending || this.generation != generation || epoch != token) return 0;
        pending = false;
        return ++attempts;
    }

    String source() { return source; }
}
