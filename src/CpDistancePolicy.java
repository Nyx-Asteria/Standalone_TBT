package hmgapi.mods.projection.carplay;

final class CpDistancePolicy {
    private CpDistancePolicy() {}
    static boolean retainNumeric(int previous, int current, boolean received) {
        return previous == current || received;
    }
    static boolean retainFormatted(int previous, int current, boolean numeric,
                                   boolean formatted, boolean units) {
        if (previous != current) return formatted && units;
        return !numeric || formatted;
    }
}
