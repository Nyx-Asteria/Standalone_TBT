package hmgapi.mods.projection.carplay;

public final class ThirdPartyTraceTest {
    private static void check(String name, boolean expected) {
        if (ThirdPartyTrace.isThirdParty(name) != expected)
            throw new AssertionError("classification: " + name);
    }
    private static void attribution(java.util.Map<Integer,String> known, int[] ids,
            String active, String explicit, String expected) {
        String actual = ThirdPartyTrace.attribution(known, ids, active, explicit);
        if (!expected.equals(actual)) throw new AssertionError(expected + " != " + actual);
    }
    public static void main(String[] args) {
        check(null, false);
        check("", false);
        check("   ", false);
        check("Apple Maps", false);
        check("Apple 지도", false);
        check("Maps", false);
        check("com.apple.Maps", false);
        check("Google Maps", false);
        check(" GOOGLE MAPS ", false);
        check("com.google.Maps", false);
        check("Waze", true);
        check("MapQuest", true);
        check("TomTom GO", true);
        check("HERE WeGo", true);
        java.util.Map<Integer,String> known = new java.util.HashMap<Integer,String>();
        known.put(5, "Waze");
        known.put(6, "Google Maps");
        attribution(known, new int[]{5}, "Waze", "Waze", "explicit-source");
        attribution(known, new int[]{5}, "Waze", null, "component-associated");
        attribution(known, new int[]{6}, "Waze", null, "different-source");
        attribution(known, new int[]{5,6}, "Waze", null, "different-source");
        attribution(known, new int[]{5,7}, "Waze", null, "unattributed");
        attribution(known, null, "Waze", null, "unattributed");
        attribution(known, new int[]{}, "Waze", null, "unattributed");
        attribution(known, new int[]{5}, "Waze", "MapQuest", "different-source");
        known.clear();
        attribution(known, new int[]{5}, "Waze", null, "unattributed");
        known.put(5, "MapQuest");
        attribution(known, new int[]{5}, "MapQuest", null, "component-associated");
        System.out.println("ThirdPartyTrace: 24 classification and attribution checks passed");
    }
}
