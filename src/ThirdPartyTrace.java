package hmgapi.mods.projection.carplay;

import java.util.Locale;

/** Unknown sources are not attributed; known Apple and Google sources are excluded. */
final class ThirdPartyTrace {
    /** A component association is not proof of packet origin after an app switch. */
    static String attribution(java.util.Map<Integer,String> known, int[] ids, String active, String explicit) {
        if (explicit != null) return explicit.equals(active) ? "explicit-source" : "different-source";
        if (ids == null || ids.length == 0) return "unattributed";
        boolean unknown = false;
        for (int id : ids) {
            String owner = known.get(id);
            if (owner == null) unknown = true;
            else if (!owner.equals(active)) return "different-source";
        }
        return unknown ? "unattributed" : "component-associated";
    }

    static boolean isThirdParty(String source) {
        if (source == null) return false;
        String n = source.trim().toLowerCase(Locale.ROOT);
        return !n.isEmpty() && !n.equals("apple maps") && !n.equals("apple 지도")
                && !n.equals("maps") && !n.equals("com.apple.maps")
                && !n.equals("google maps") && !n.equals("com.google.maps");
    }
}
