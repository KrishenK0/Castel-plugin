package fr.krishenk.castel.utils.internal;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class FastUUID {
    private static final boolean USE_JDK_UUID_TO_STRING;
    private static final int UUID_STRING_LENGTH = 36;
    private static final char[] HEX_DIGITS;
    private static final long[] NIBBLES;
    public static final UUID ZERO;

    private FastUUID() {
    }

    public static UUID randomUUID(Random random) {
        byte[] randomBytes = new byte[16];
        random.nextBytes(randomBytes);
        randomBytes[6] = (byte)(randomBytes[6] & 0xF);
        randomBytes[6] = (byte)(randomBytes[6] | 0x40);
        randomBytes[8] = (byte)(randomBytes[8] & 0x3F);
        randomBytes[8] = (byte)(randomBytes[8] | 0x80);
        return FastUUID.bytesToUUID(randomBytes);
    }

    public static UUID bytesToUUID(byte[] data) {
        int i;
        long msb = 0L;
        long lsb = 0L;
        for (i = 0; i < 8; ++i) {
            msb = msb << 8 | (long)(data[i] & 0xFF);
        }
        for (i = 8; i < 16; ++i) {
            lsb = lsb << 8 | (long)(data[i] & 0xFF);
        }
        return new UUID(msb, lsb);
    }

    public static boolean equals(UUID first, UUID other) {
        return first.getMostSignificantBits() == other.getMostSignificantBits() && first.getLeastSignificantBits() == other.getLeastSignificantBits();
    }

    public static UUID fromString(CharSequence uuid) {
        Objects.requireNonNull(uuid);
        long mostSignificantBits = FastUUID.getHexValueForChar(uuid.charAt(0)) << 60 | FastUUID.getHexValueForChar(uuid.charAt(1)) << 56 | FastUUID.getHexValueForChar(uuid.charAt(2)) << 52 | FastUUID.getHexValueForChar(uuid.charAt(3)) << 48 | FastUUID.getHexValueForChar(uuid.charAt(4)) << 44 | FastUUID.getHexValueForChar(uuid.charAt(5)) << 40 | FastUUID.getHexValueForChar(uuid.charAt(6)) << 36 | FastUUID.getHexValueForChar(uuid.charAt(7)) << 32 | FastUUID.getHexValueForChar(uuid.charAt(9)) << 28 | FastUUID.getHexValueForChar(uuid.charAt(10)) << 24 | FastUUID.getHexValueForChar(uuid.charAt(11)) << 20 | FastUUID.getHexValueForChar(uuid.charAt(12)) << 16 | FastUUID.getHexValueForChar(uuid.charAt(14)) << 12 | FastUUID.getHexValueForChar(uuid.charAt(15)) << 8 | FastUUID.getHexValueForChar(uuid.charAt(16)) << 4 | FastUUID.getHexValueForChar(uuid.charAt(17));
        long leastSignificantBits = FastUUID.getHexValueForChar(uuid.charAt(19)) << 60 | FastUUID.getHexValueForChar(uuid.charAt(20)) << 56 | FastUUID.getHexValueForChar(uuid.charAt(21)) << 52 | FastUUID.getHexValueForChar(uuid.charAt(22)) << 48 | FastUUID.getHexValueForChar(uuid.charAt(24)) << 44 | FastUUID.getHexValueForChar(uuid.charAt(25)) << 40 | FastUUID.getHexValueForChar(uuid.charAt(26)) << 36 | FastUUID.getHexValueForChar(uuid.charAt(27)) << 32 | FastUUID.getHexValueForChar(uuid.charAt(28)) << 28 | FastUUID.getHexValueForChar(uuid.charAt(29)) << 24 | FastUUID.getHexValueForChar(uuid.charAt(30)) << 20 | FastUUID.getHexValueForChar(uuid.charAt(31)) << 16 | FastUUID.getHexValueForChar(uuid.charAt(32)) << 12 | FastUUID.getHexValueForChar(uuid.charAt(33)) << 8 | FastUUID.getHexValueForChar(uuid.charAt(34)) << 4 | FastUUID.getHexValueForChar(uuid.charAt(35));
        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    public static String toString(UUID uuid) {
        if (USE_JDK_UUID_TO_STRING) {
            return uuid.toString();
        }
        long mostSignificantBits = uuid.getMostSignificantBits();
        long leastSignificantBits = uuid.getLeastSignificantBits();
        char[] uuidChars = new char[]{HEX_DIGITS[(int)((mostSignificantBits & 0xF000000000000000L) >>> 60)], HEX_DIGITS[(int)((mostSignificantBits & 0xF00000000000000L) >>> 56)], HEX_DIGITS[(int)((mostSignificantBits & 0xF0000000000000L) >>> 52)], HEX_DIGITS[(int)((mostSignificantBits & 0xF000000000000L) >>> 48)], HEX_DIGITS[(int)((mostSignificantBits & 0xF00000000000L) >>> 44)], HEX_DIGITS[(int)((mostSignificantBits & 0xF0000000000L) >>> 40)], HEX_DIGITS[(int)((mostSignificantBits & 0xF000000000L) >>> 36)], HEX_DIGITS[(int)((mostSignificantBits & 0xF00000000L) >>> 32)], '-', HEX_DIGITS[(int)((mostSignificantBits & 0xF0000000L) >>> 28)], HEX_DIGITS[(int)((mostSignificantBits & 0xF000000L) >>> 24)], HEX_DIGITS[(int)((mostSignificantBits & 0xF00000L) >>> 20)], HEX_DIGITS[(int)((mostSignificantBits & 0xF0000L) >>> 16)], '-', HEX_DIGITS[(int)((mostSignificantBits & 0xF000L) >>> 12)], HEX_DIGITS[(int)((mostSignificantBits & 0xF00L) >>> 8)], HEX_DIGITS[(int)((mostSignificantBits & 0xF0L) >>> 4)], HEX_DIGITS[(int)(mostSignificantBits & 0xFL)], '-', HEX_DIGITS[(int)((leastSignificantBits & 0xF000000000000000L) >>> 60)], HEX_DIGITS[(int)((leastSignificantBits & 0xF00000000000000L) >>> 56)], HEX_DIGITS[(int)((leastSignificantBits & 0xF0000000000000L) >>> 52)], HEX_DIGITS[(int)((leastSignificantBits & 0xF000000000000L) >>> 48)], '-', HEX_DIGITS[(int)((leastSignificantBits & 0xF00000000000L) >>> 44)], HEX_DIGITS[(int)((leastSignificantBits & 0xF0000000000L) >>> 40)], HEX_DIGITS[(int)((leastSignificantBits & 0xF000000000L) >>> 36)], HEX_DIGITS[(int)((leastSignificantBits & 0xF00000000L) >>> 32)], HEX_DIGITS[(int)((leastSignificantBits & 0xF0000000L) >>> 28)], HEX_DIGITS[(int)((leastSignificantBits & 0xF000000L) >>> 24)], HEX_DIGITS[(int)((leastSignificantBits & 0xF00000L) >>> 20)], HEX_DIGITS[(int)((leastSignificantBits & 0xF0000L) >>> 16)], HEX_DIGITS[(int)((leastSignificantBits & 0xF000L) >>> 12)], HEX_DIGITS[(int)((leastSignificantBits & 0xF00L) >>> 8)], HEX_DIGITS[(int)((leastSignificantBits & 0xF0L) >>> 4)], HEX_DIGITS[(int)(leastSignificantBits & 0xFL)]};
        return new String(uuidChars);
    }

    private static long getHexValueForChar(char ch) {
        try {
            long hex = NIBBLES[ch];
            if (hex == -1L) {
                throw new IllegalArgumentException("Illegal hexadecimal digit: " + ch);
            }
            return hex;
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("Illegal hexadecimal digit: " + ch);
        }
    }

    static {
        int version;
        HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        NIBBLES = new long[128];
        ZERO = new UUID(0L, 0L);
        String java = System.getProperty("java.specification.version");
        try {
            version = Integer.parseInt(java);
        }
        catch (NumberFormatException ex) {
            version = 0;
        }
        USE_JDK_UUID_TO_STRING = version >= 11;
        Arrays.fill(NIBBLES, -1L);
        FastUUID.NIBBLES[48] = 0L;
        FastUUID.NIBBLES[49] = 1L;
        FastUUID.NIBBLES[50] = 2L;
        FastUUID.NIBBLES[51] = 3L;
        FastUUID.NIBBLES[52] = 4L;
        FastUUID.NIBBLES[53] = 5L;
        FastUUID.NIBBLES[54] = 6L;
        FastUUID.NIBBLES[55] = 7L;
        FastUUID.NIBBLES[56] = 8L;
        FastUUID.NIBBLES[57] = 9L;
        FastUUID.NIBBLES[97] = 10L;
        FastUUID.NIBBLES[98] = 11L;
        FastUUID.NIBBLES[99] = 12L;
        FastUUID.NIBBLES[100] = 13L;
        FastUUID.NIBBLES[101] = 14L;
        FastUUID.NIBBLES[102] = 15L;
        FastUUID.NIBBLES[65] = 10L;
        FastUUID.NIBBLES[66] = 11L;
        FastUUID.NIBBLES[67] = 12L;
        FastUUID.NIBBLES[68] = 13L;
        FastUUID.NIBBLES[69] = 14L;
        FastUUID.NIBBLES[70] = 15L;
    }
}
