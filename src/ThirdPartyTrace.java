package hmgapi.mods.projection.carplay;

import java.util.Locale;

/** Unknown sources are not attributed; known Apple and Google sources are excluded. */
final class ThirdPartyTrace {
    static boolean isThirdParty(String source) {
        if (source == null) return false;
        String n = source.trim().toLowerCase(Locale.ROOT);
        return !n.isEmpty() && !n.equals("apple maps") && !n.equals("apple 지도")
                && !n.equals("maps") && !n.equals("com.apple.maps")
                && !n.equals("google maps") && !n.equals("com.google.maps");
    }
}
