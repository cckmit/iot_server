package com.aliyun.iotx.haas.tdserver.common.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author benxiliu
 * @date 2020/09/01
 */

public class RandomTool {
    public static String getRandomBase64String() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeBase64URLSafeString(bb.array());
    }

    public static String getRandomBase64String(Integer length) {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        String result = Base64.encodeBase64URLSafeString(bb.array());
        if (length <= 1 || length > result.length()) {
            return null;
        } else {
            return result.substring(0, length - 1);
        }
    }

    public static String getRamdomAlphanumeric(Integer length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    public static String getRamdomNumeric(Integer length) {
        return RandomStringUtils.randomNumeric(length);
    }

}

