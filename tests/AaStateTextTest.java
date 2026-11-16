package hmgapi.mods.projection;

public final class AaStateTextTest {
    public static void main(String[] args) {
        String packet = "S 32 0a1e0a020807120b0a09ebacb4eba0b9eba19c220b0a09ebacb4eba0b9eba19c";
        // Real packet with an intact Korean road string, unlike the legacy callback.
        String road = "무령로";
        byte[] text = road.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] message = new byte[text.length + 6];
        message[0] = 10; message[1] = (byte) (text.length + 4);
        message[2] = 18; message[3] = (byte) (text.length + 2);
        message[4] = 10; message[5] = (byte) text.length;
        System.arraycopy(text, 0, message, 6, text.length);
        StringBuilder hex = new StringBuilder();
        for (byte value : message) hex.append(String.format("%02x", value & 255));
        String valid = "S " + message.length + " " + hex;
        require(road.equals(AaStateText.currentRoad(valid)));
        require(AaStateText.currentRoad(valid + "ff") == null);
        require(AaStateText.currentRoad("P " + message.length + " " + hex) == null);
        require(AaStateText.currentRoad("S 3 0a08ff") == null);
        require(AaStateText.currentRoad("S 1 00") == null);
        require(road.equals(AaStateText.currentRoad(packet)));
        for (int end = 0; end < valid.length(); end++)
            require(AaStateText.currentRoad(valid.substring(0, end)) == null);
        System.out.println("AaStateText: Korean UTF-8 and malformed/truncated packets passed");
    }

    private static void require(boolean condition) {
        if (!condition) throw new AssertionError();
    }
}
