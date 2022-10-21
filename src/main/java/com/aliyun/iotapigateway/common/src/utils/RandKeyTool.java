package com.aliyun.iotx.haas.tdserver.common.utils;

import com.aliyun.iotx.haas.tdserver.common.exception.HaasServerInternalException;
import org.apache.commons.lang3.ArrayUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandKeyTool {

    private static final int RAND_BYTES = 16;

    public static byte[] genHexRand() throws HaasServerInternalException {

        byte[] hexRand = new byte[RAND_BYTES];
        try {
            SecureRandom.getInstance("NativePRNGNonBlocking").nextBytes(hexRand);
        } catch (NoSuchAlgorithmException e) {
            throw new HaasServerInternalException("No NativePRNGNonBlocking algorithm.");
        }
        //hex 串转 ascii 串
        return hexRand;
    }

    public static byte[] signHexRand(byte[] hexRand, byte[] hexDs)
            throws HaasServerInternalException {

        //AES-CMAC 算法，使用 DeviceSecret 加密 R1
        // 2020/9/3 算法已更换，CMAC不再使用
//        CipherParameters params = new KeyParameter(hexDs);
//        BlockCipher aes = new AESEngine();
//        CMac mac = new CMac(aes);
//        mac.init(params);
//        mac.update(hexRand, 0, hexRand.length);
//        byte[] hexSignedRand = new byte[mac.getMacSize()];
//        mac.doFinal(hexSignedRand, 0);
//        return hexSignedRand;

        // 2020/9/3 算法已更换为 SHA256(RAND+DS)
        //拼接rand|ds
        byte[] data = ArrayUtils.addAll(hexRand, hexDs);

        //SHA256
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new HaasServerInternalException("No SHA-256 algorithm");
        }
    }

    public static byte[] genHexKey(byte[] hexR1, byte[] hexR2, byte[] hexDs)
            throws HaasServerInternalException {
        //Hex 拼接 R1 R2 DeviceSecret
        byte[] data = ArrayUtils.addAll(hexR1, hexR2);
        data = ArrayUtils.addAll(data, hexDs);

        //SHA256
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new HaasServerInternalException("No SHA-256 algorithm");
        }

    }

}

