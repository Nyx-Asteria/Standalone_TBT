package hmgapi.mods.projection;

public final class AaTbtSupportTest {
    private static void ok(boolean b) { if (!b) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        ok(AaTbtSupport.supportsDigest("48ee793af8b86052503b8c952b034731"));
        ok(AaTbtSupport.supportsDigest("a0bea1c4992ea983a8190c4e032d7190"));
        ok(AaTbtSupport.supportsDigest("dbfe7bd2e36152a50f97fed612cf6326"));
        ok(!AaTbtSupport.supportsDigest(null));
        ok(!AaTbtSupport.supportsDigest("48ee793af8b86052503b8c952b034730"));
        ok(!AaTbtSupport.supports(null));
        java.io.File f=java.io.File.createTempFile("aa-stock-test", ".so");
        try { ok(!AaTbtSupport.supports(f)); }
        finally { f.delete(); }
        System.out.println("AA native compatibility: three known fingerprints, unknown and missing library rejected");
    }
}
