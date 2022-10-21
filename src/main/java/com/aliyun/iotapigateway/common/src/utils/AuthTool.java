package com.aliyun.iotx.haas.tdserver.common.utils;

import com.aliyun.iotx.haas.tdserver.common.exception.HaasServerInternalException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.zip.CRC32;

public class AuthTool {

    public static boolean verifyCheckCode(String productKey, String deviceName, byte[] hexDs, String checkCode) throws HaasServerInternalException {
        if (checkCode == null) {
            return false;
        }
        String calcCheckCode = getHmacSha256CheckCode(productKey, deviceName, hexDs);
        return checkCode.equals(calcCheckCode);
    }

    public static String getHmacSha256CheckCode(String productKey, String deviceName, byte[] hexDs) throws HaasServerInternalException {
        String text = productKey + deviceName;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hexDs, "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] hexCode = sha256_HMAC.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().encodeToString(hexCode);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new HaasServerInternalException(e.getMessage());
        }
    }

    private static int reverseEndian(int value) {
        int transValue = 0;
        transValue += value >>> 24;
        transValue += ((value & 0x00ff0000) >>> 16) << 8;
        transValue += ((value & 0x0000ff00) >>> 8) << 16;
        transValue += (value & 0x000000ff) << 24;
        return transValue;
    }

    private static long getCrc32(byte[] content) throws HaasServerInternalException {
        if(content == null){
            throw new HaasServerInternalException("crc32 wrong param");
        }
        CRC32 crc = new CRC32();
        crc.update(content);
        return crc.getValue();
    }

    public static String appendCrc(String content) throws HaasServerInternalException {
        String result = content;
        int crcCode = (int) getCrc32(HexStrTool.hexStrToByteArray(content));
        int littleEndianValue = AuthTool.reverseEndian(crcCode);
        result += String.format("%08x", littleEndianValue);
        return result;
    }

    public static String genRequestId(String haasUserId, String productKey, String deviceName, String r1)
            throws NoSuchAlgorithmException {
        // 构造RequestId的原始信息:
        String requestIdText = haasUserId + productKey + deviceName + r1 + System.currentTimeMillis();
        // 对RequestId原始信息做SHA256
        MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
        messageDigest.update(requestIdText.getBytes());
        return HexStrTool.byteArrayToHexStr(messageDigest.digest());
    }

//    public static void main(String[] args) {
//        byte[] content = {(byte) 0x80, (byte) 0x00, (byte) 0x32, (byte) 0x00, (byte) 0x61, (byte) 0x31, (byte) 0x79,
//                (byte) 0x6c, (byte) 0x56, (byte) 0x38, (byte) 0x4f, (byte) 0x72, (byte) 0x30, (byte) 0x59,
//                (byte) 0x4a, (byte) 0x48, (byte) 0x6e, (byte) 0x70, (byte) 0xda, (byte) 0x71, (byte) 0x8c,
//                (byte) 0xfa, (byte) 0x59, (byte) 0x22, (byte) 0xcc, (byte) 0xbf, (byte) 0xb2, (byte) 0x85,
//                (byte) 0x25, (byte) 0x37, (byte) 0x30, (byte) 0x56, (byte) 0x92, (byte) 0x2d, (byte) 0xb3,
//                (byte) 0x15, (byte) 0x00, (byte) 0xbc, (byte) 0x55, (byte) 0xcc, (byte) 0xd2, (byte) 0xd4,
//                (byte) 0xf0, (byte) 0x10, (byte) 0xfa, (byte) 0x71, (byte) 0xa4, (byte) 0xde, (byte) 0x1d,
//                (byte) 0x80, (byte) 0x0f, (byte) 0xf4, (byte) 0x0b};
//        System.out.println(Long.valueOf(AuthTool.getCrc32(content)));
//    }
}

