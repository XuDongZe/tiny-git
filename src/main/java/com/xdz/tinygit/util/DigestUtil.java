package com.xdz.tinygit.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Description: data digest util functions<br/>
 * Author: dongze.xu<br/>
 * Date: 2022/6/17 13:19<br/>
 * Version: 1.0<br/>
 */
public class DigestUtil {
    public static String sha1Hex(String source) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] array = source.getBytes(StandardCharsets.UTF_8);
        byte[] digest = sha.digest(array);
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : digest) {
            int val = Byte.toUnsignedInt(b);
            if (val < 16) {
                hexBuilder.append("0");
            }
            hexBuilder.append(Integer.toHexString(val));
        }
        return hexBuilder.toString();
    }
}
