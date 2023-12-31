package com.tianli.tool;

import com.tianli.exception.ErrorCodeEnum;
import org.springframework.data.redis.util.ByteUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author wangqiyun
 * @since 2021/2/26 17:19
 */
public class Bech32 {
    /**
     * The Bech32 character set for encoding.
     */
    private static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

    /**
     * The Bech32 character set for decoding.
     */
    private static final byte[] CHARSET_REV = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            15, -1, 10, 17, 21, 20, 26, 30, 7, 5, -1, -1, -1, -1, -1, -1,
            -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1,
            1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1,
            -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1,
            1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1
    };

    public static class Bech32Data {
        public final String hrp;
        public final byte[] data;

        private Bech32Data(final String hrp, final byte[] data) {
            this.hrp = hrp;
            this.data = data;
        }
    }

    /**
     * Find the polynomial with value coefficients mod the generator as 30-bit.
     */
    private static int polymod(final byte[] values) {
        int c = 1;
        for (byte v_i : values) {
            int c0 = (c >>> 25) & 0xff;
            c = ((c & 0x1ffffff) << 5) ^ (v_i & 0xff);
            if ((c0 & 1) != 0) c ^= 0x3b6a57b2;
            if ((c0 & 2) != 0) c ^= 0x26508e6d;
            if ((c0 & 4) != 0) c ^= 0x1ea119fa;
            if ((c0 & 8) != 0) c ^= 0x3d4233dd;
            if ((c0 & 16) != 0) c ^= 0x2a1462b3;
        }
        return c;
    }

    /**
     * Expand a HRP for use in checksum computation.
     */
    private static byte[] expandHrp(final String hrp) {
        int hrpLength = hrp.length();
        byte ret[] = new byte[hrpLength * 2 + 1];
        for (int i = 0; i < hrpLength; ++i) {
            int c = hrp.charAt(i) & 0x7f; // Limit to standard 7-bit ASCII
            ret[i] = (byte) ((c >>> 5) & 0x07);
            ret[i + hrpLength + 1] = (byte) (c & 0x1f);
        }
        ret[hrpLength] = 0;
        return ret;
    }

    /**
     * Verify a checksum.
     */
    private static boolean verifyChecksum(final String hrp, final byte[] values) {
        byte[] hrpExpanded = expandHrp(hrp);
        byte[] combined = new byte[hrpExpanded.length + values.length];
        System.arraycopy(hrpExpanded, 0, combined, 0, hrpExpanded.length);
        System.arraycopy(values, 0, combined, hrpExpanded.length, values.length);
        return polymod(combined) == 1;
    }

    /**
     * Create a checksum.
     */
    private static byte[] createChecksum(final String hrp, final byte[] values) {
        byte[] hrpExpanded = expandHrp(hrp);
        byte[] enc = new byte[hrpExpanded.length + values.length + 6];
        System.arraycopy(hrpExpanded, 0, enc, 0, hrpExpanded.length);
        System.arraycopy(values, 0, enc, hrpExpanded.length, values.length);
        int mod = polymod(enc) ^ 1;
        byte[] ret = new byte[6];
        for (int i = 0; i < 6; ++i) {
            ret[i] = (byte) ((mod >>> (5 * (5 - i))) & 31);
        }
        return ret;
    }

    /**
     * Encode a Bech32 string.
     */
    public static String encode(final Bech32Data bech32) {
        return encode(bech32.hrp, bech32.data);
    }

    /**
     * Encode a Bech32 string.
     */
    public static String encode(String hrp, final byte[] values) {
        if (!(hrp.length() >= 1 && hrp.length() <= 83)) ErrorCodeEnum.ADDRESS_ERROR.throwException();
        hrp = hrp.toLowerCase(Locale.ROOT);
        byte[] checksum = createChecksum(hrp, values);
        byte[] combined = new byte[values.length + checksum.length];
        System.arraycopy(values, 0, combined, 0, values.length);
        System.arraycopy(checksum, 0, combined, values.length, checksum.length);
        StringBuilder sb = new StringBuilder(hrp.length() + 1 + combined.length);
        sb.append(hrp);
        sb.append('1');
        for (byte b : combined) {
            sb.append(CHARSET.charAt(b));
        }
        return sb.toString();
    }

    /**
     * Decode a Bech32 string.
     */
    public static Bech32Data decode(final String str) throws AddressFormatException {
        boolean lower = false, upper = false;
        if (str.length() < 8)
            return null;
        if (str.length() > 90)
            return null;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (c < 33 || c > 126) return null;
            if (c >= 'a' && c <= 'z') {
                if (upper)
                    return null;
                lower = true;
            }
            if (c >= 'A' && c <= 'Z') {
                if (lower)
                    return null;
                upper = true;
            }
        }
        final int pos = str.lastIndexOf('1');
        if (pos < 1) return null;
        final int dataPartLength = str.length() - 1 - pos;
        if (dataPartLength < 6)
            return null;
        byte[] values = new byte[dataPartLength];
        for (int i = 0; i < dataPartLength; ++i) {
            char c = str.charAt(i + pos + 1);
            if (CHARSET_REV[c] == -1) return null;
            values[i] = CHARSET_REV[c];
        }
        String hrp = str.substring(0, pos).toLowerCase(Locale.ROOT);
        if (!verifyChecksum(hrp, values)) return null;
        return new Bech32Data(hrp, Arrays.copyOfRange(values, 0, values.length - 6));
    }

    public static byte[] convertbits(byte[] data, long frombits, long tobits, boolean pad) {
        long acc = 0;
        long bits = 0;
        List<Long> ret = new ArrayList<>();
        long maxv = (1L << tobits) - 1;
        for (byte datum : data) {
            int value = datum & 0xFF;
            if (value >> frombits != 0)
                ErrorCodeEnum.ADDRESS_ERROR.throwException();
            acc = (acc << frombits) | value;
            bits += frombits;
            while (bits >= tobits) {
                bits -= tobits;
                ret.add((acc >> bits) & maxv);
            }
        }
        if (pad) {
            if (bits > 0) {
                ret.add((acc << (tobits - bits)) & maxv);
            }
        } else if (bits >= frombits || ((acc << (tobits - bits)) & maxv) != 0)
            ErrorCodeEnum.ADDRESS_ERROR.throwException();
        byte[] result = new byte[ret.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = ret.get(i).byteValue();
        return result;
    }

    public static String SegwitAddrEncode(String hrp, int version, byte[] program) {
        byte[] convertbits = convertbits(program, 8, 5, true);
        return encode("bc", ByteUtils.concat(new byte[]{(byte) version}, convertbits));
    }

    public static byte[] SegwitAddrDecode(String addr) {
        Bech32Data bech32Data = decode(addr);
        if (bech32Data == null) return null;
        byte[] convertbits = new byte[bech32Data.data.length - 1];
        if (bech32Data.data.length - 1 >= 0)
            System.arraycopy(bech32Data.data, 1, convertbits, 0, bech32Data.data.length - 1);
        return convertbits(convertbits, 5, 8, false);
    }
}
