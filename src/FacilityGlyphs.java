package hmgapi.mods.projection.carplay;

/** Modern cluster display types recovered from RGIconIDToClusterDispType. */
final class FacilityGlyphs {
    private FacilityGlyphs() {}

    static boolean under(String text){return text!=null && (text.replace(" ","").contains("지하차도")||text.replace(" ","").contains("지하도로"));}
    static boolean over(String text){return text!=null && (text.replace(" ","").contains("고가차도")||text.replace(" ","").contains("고가도로"));}
    static boolean grade(String text){return under(text)||over(text)||text!=null&&text.contains("터널");}
    static int highway(int type, int junction, int exit) {
        if (junction != 0) return 0;
        switch (type) {
            case 9: return exit < 0 ? 162 : exit > 0 ? 161 : 160;
            case 52: return 162;
            case 53: return 161;
            case 8: return exit < 0 ? 165 : exit > 0 ? 164 : 163;
            case 22: return 165;
            case 23: return 164;
            default: return 0;
        }
    }

    static int road(int type, int junction, int exit, String description, String afterRoad) {
        if (junction != 0) return 0;
        String text = ((description == null ? "" : description) + " "
                + (afterRoad == null ? "" : afterRoad)).replaceAll("\\s+", "");
        if (text.contains("금지") || text.contains("진입하지") || text.contains("이용하지")) return 0;
        boolean rest = text.contains("휴게소") || text.contains("졸음쉼터");
        if (rest) {
            // Owner-selected service-area display code; no legacy saved override.
            if (text.contains("요금소") || text.contains("톨게이트") || text.contains("자동징수")) return 0;
            return type == 3 || type == 13 || type == 14 || highway(type, junction, exit) != 0 ? 183 : 0;
        }
        boolean toll = text.contains("요금소") || text.contains("톨게이트")
                || text.contains("자동징수") || text.toUpperCase(java.util.Locale.ROOT).matches(".*TG(?:$|[\\s()/,·].*)");
        if (toll && (type == 3 || type == 13 || type == 14)) {
            return text.toUpperCase(java.util.Locale.ROOT).contains("TG") ? 184 : 178;
        }
        // Source 86/87 is ferry boarding/alighting (visually verified), not a tunnel.
        // Keep the named tunnel and straight guidance until a supported tunnel type is verified.
        if (type == 3 && text.contains("터널")) return 65;
        boolean under = under(text), over = over(text);
        if (under == over) return 0;
        boolean beside = text.contains("옆길") || text.contains("측도") || type == 49 || type == 50;
        if (beside) {
            boolean left = type == 1 || type == 13 || type == 49 || exit < 0;
            boolean right = type == 2 || type == 14 || type == 50 || exit > 0;
            if (left == right) return 0;
            // Source 37/38 -> 82/81; source 42/43 -> 85/84.
            return under ? (left ? 85 : 84) : (left ? 82 : 81);
        }
        boolean action=text.contains("진입") || text.contains("이용") || text.contains("통과");
        boolean namedStructure=text.endsWith("지하차도")||text.endsWith("지하도로")
                ||text.endsWith("고가차도")||text.endsWith("고가도로");
        if (action || type == 3 && namedStructure) {
            boolean right = type == 2 || type == 14 || type == 50 || exit > 0;
            // Source 34/35/36 -> 88/88/87; source 39/40/41 -> 91/91/90.
            return under ? (right ? 90 : 91) : (right ? 87 : 88);
        }
        return 0;
    }
}
