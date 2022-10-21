package com.aliyun.iotx.haas.tdserver.common.security;

/**
 * @author benxiliu
 * @date 2019/09/19
 */
public interface GeneralCryptograph {
    String encrypt(String var1, String var2);

    String decrypt(String var1, String var2);
}

