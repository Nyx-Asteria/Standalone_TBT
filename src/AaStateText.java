package hmgapi.mods.projection;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Reads the first NavigationState step's road from the native packet dump. */
final class AaStateText {
    private AaStateText() {}

    static String currentRoad(String dump) {return stepText(dump,2);}
    static String currentInstruction(String dump) {return stepText(dump,4);}
    private static String stepText(String dump,int fieldNumber) {
        if (dump == null || !dump.startsWith("S ") || dump.length() > 131100) return null;
        try {
            int split = dump.indexOf(' ', 2);
            if (split < 0) return null;
            int length = Integer.parseInt(dump.substring(2, split));
            String hex = dump.substring(split + 1);
            if (length < 0 || length > 65536 || hex.length() != length * 2) return null;
            byte[] message = new byte[length];
            for (int i = 0; i < length; i++) {
                int high = Character.digit(hex.charAt(i * 2), 16);
                int low = Character.digit(hex.charAt(i * 2 + 1), 16);
                if (high < 0 || low < 0) return null;
                message[i] = (byte) (high * 16 + low);
            }
            byte[] step = field(message, 1);
            byte[] road = field(step, fieldNumber);
            byte[] text = field(road, 1);
            if (text == null) return null;
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(text)).toString();
        } catch (IllegalArgumentException | CharacterCodingException error) {
            return null;
        }
    }

    private static byte[] field(byte[] message, int wanted) {
        if (message == null) return null;
        int[] position = {0};
        while (position[0] < message.length) {
            long tag = varint(message, position);
            if (tag >>> 3 == 0) throw new IllegalArgumentException("invalid protobuf field");
            int wire = (int) (tag & 7);
            if (wire == 0) {
                varint(message, position);
                continue;
            }
            long length;
            if (wire == 1) length = 8;
            else if (wire == 5) length = 4;
            else if (wire == 2) length = varint(message, position);
            else throw new IllegalArgumentException("unsupported protobuf wire");
            if (length < 0 || length > message.length - position[0])
                throw new IllegalArgumentException("truncated protobuf field");
            int start = position[0];
            position[0] += (int) length;
            if (wire == 2 && tag >>> 3 == wanted)
                return Arrays.copyOfRange(message, start, position[0]);
        }
        return null;
    }

    private static long varint(byte[] message, int[] position) {
        long value = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            if (position[0] == message.length) throw new IllegalArgumentException("truncated varint");
            int next = message[position[0]++] & 255;
            if (shift == 63 && (next & 254) != 0)
                throw new IllegalArgumentException("overflowing varint");
            value |= (long) (next & 127) << shift;
            if ((next & 128) == 0) return value;
        }
        throw new IllegalArgumentException("overflowing varint");
    }
}
