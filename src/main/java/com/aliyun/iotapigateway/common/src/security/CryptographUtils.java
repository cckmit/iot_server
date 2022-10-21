package com.aliyun.iotx.haas.tdserver.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author benxiliu
 * @date 2019/09/18
 */

//@Component
public class CryptographUtils {
    @Autowired
    private GeneralCryptograph generalCryptograph;

    @Value("${security.system.keyName}")
    private String keyName;

    /**
     * 解密
     *x
     * @param encryption 密文
     * @return 明文
     */
    public String decrypt(String encryption) {
        return generalCryptograph.decrypt(encryption, keyName);
    }

    /**
     * 加密
     *
     * @param plainText 明文
     * @return 密文
     */
    public String encrypt(String plainText) {
        return generalCryptograph.encrypt(plainText, keyName);
    }
}

