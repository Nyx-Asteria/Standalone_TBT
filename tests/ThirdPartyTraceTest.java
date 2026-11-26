package hmgapi.mods.projection.carplay;

public final class ThirdPartyTraceTest {
    private static void check(String name, boolean expected) {
        if (ThirdPartyTrace.isThirdParty(name) != expected)
            throw new AssertionError("classification: " + name);
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
        System.out.println("ThirdPartyTrace: 14 classification checks passed");
    }
}
