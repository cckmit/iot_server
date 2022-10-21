package com.aliyun.iotx.haas.tdserver.common.utils;

import com.aliyun.iotx.haas.tdserver.common.exception.HaasServerInternalException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class UserTool {

    private static final String HMAC_KEY_B64 =
            "BMJ4zRyb5ZRrt/sc/6jw32+irIRGLgVuANcWWPSIUf6dEuI+sK1B1bBmjBtQ/JMkH991aGCKW0AWejzIcoJF7Q==";
    private static final SecretKeySpec secretKey =
            new SecretKeySpec(Base64.getDecoder().decode(HMAC_KEY_B64), "HmacSHA256");

    public static String genHaasUserId(String platformName, String rawUserId)
            throws HaasServerInternalException {

        String userDigest =
                "plat_" + platformName
                        + "_uid_" + rawUserId
                        + "_time_" + System.currentTimeMillis();

        //不再使用 SHA1
//        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
//        messageDigest.update(userDigest.getBytes());
//        return HexStrTool.byteArrayToHexStr(messageDigest.digest());

        //  BASE64URL(
        //      HMAC_SHA256(
        //          plat_{platformName}_uid_{rawUserId}_time_{currentTimeMillis}
        //      )
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            sha256_HMAC.init(secretKey);
            return Base64.getUrlEncoder().encodeToString(
                    sha256_HMAC.doFinal(
                            userDigest.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new HaasServerInternalException(e.getMessage());
        }
    }
}

