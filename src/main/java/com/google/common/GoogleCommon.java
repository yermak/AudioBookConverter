package com.google.common;

import java.net.*;
import java.util.Arrays;

/**
 * Created by yermak on 16-Nov-18.
 */
public class GoogleCommon {
    private static final int IPV6_PART_COUNT = 8;

    /**
     * Creates a URL to parse to FFmpeg based on the scheme, address and port.
     *
     * <p>TODO Move this method to somewhere better.
     *
     * @param scheme
     * @param address
     * @param port
     * @return
     * @throws URISyntaxException
     */
    public static URI createUri(String scheme, InetAddress address, int port) throws URISyntaxException {
        return new URI(
                scheme,
                null /* userInfo */,
                toUriString(address),
                port,
                null /* path */,
                null /* query */,
                null /* fragment */);
    }

    public static String toUriString(InetAddress ip) {
        if (ip instanceof Inet6Address) {
            return "[" + toAddrString(ip) + "]";
        }
        return toAddrString(ip);
    }

    public static String toAddrString(InetAddress ip) {
        if (ip instanceof Inet4Address) {
            // For IPv4, Java's formatting is good enough.
            return ip.getHostAddress();
        }
        //        checkArgument(ip instanceof Inet6Address);
        byte[] bytes = ip.getAddress();
        int[] hextets = new int[IPV6_PART_COUNT];
        for (int i = 0; i < hextets.length; i++) {
            hextets[i] = fromBytes((byte) 0, (byte) 0, bytes[2 * i], bytes[2 * i + 1]);
        }
        compressLongestRunOfZeroes(hextets);
        return hextetsToIPv6String(hextets);
    }

    public static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
        return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
    }

    private static String hextetsToIPv6String(int[] hextets) {
        // While scanning the array, handle these state transitions:
        //   start->num => "num"     start->gap => "::"
        //   num->num   => ":num"    num->gap   => "::"
        //   gap->num   => "num"     gap->gap   => ""
        StringBuilder buf = new StringBuilder(39);
        boolean lastWasNumber = false;
        for (int i = 0; i < hextets.length; i++) {
            boolean thisIsNumber = hextets[i] >= 0;
            if (thisIsNumber) {
                if (lastWasNumber) {
                    buf.append(':');
                }
                buf.append(Integer.toHexString(hextets[i]));
            } else {
                if (i == 0 || lastWasNumber) {
                    buf.append("::");
                }
            }
            lastWasNumber = thisIsNumber;
        }
        return buf.toString();
    }

    private static void compressLongestRunOfZeroes(int[] hextets) {
        int bestRunStart = -1;
        int bestRunLength = -1;
        int runStart = -1;
        for (int i = 0; i < hextets.length + 1; i++) {
            if (i < hextets.length && hextets[i] == 0) {
                if (runStart < 0) {
                    runStart = i;
                }
            } else if (runStart >= 0) {
                int runLength = i - runStart;
                if (runLength > bestRunLength) {
                    bestRunStart = runStart;
                    bestRunLength = runLength;
                }
                runStart = -1;
            }
        }
        if (bestRunLength >= 2) {
            Arrays.fill(hextets, bestRunStart, bestRunStart + bestRunLength, -1);
        }
    }
}
