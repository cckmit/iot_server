package com.aliyun.iotx.haas.tdserver.common.security.impl;

import java.util.Map;

import com.alibaba.keycenter.client.properties.KeyCenterProperties;

import com.aliyun.iotx.haas.tdserver.common.security.GeneralCryptograph;
import com.taobao.common.keycenter.keystore.KeyStoreImpl;
import com.taobao.common.keycenter.security.Cryptograph;
import com.taobao.common.keycenter.security.CryptographImpl;

/**
 * @author benxiliu
 * @date 2019/12/06
 */

public class KeyCenterGeneralCryptographImpl implements GeneralCryptograph {
    private Cryptograph keyCenterCryptograph;

    public KeyCenterGeneralCryptographImpl(Map<String, String> arguments) {
        String httpServiceAddress = (String)arguments.get("keycenter.httpServiceAddress");
        String appPublishNum = (String)arguments.get("keycenter.appPublishNum");
        String preferProtocal = (String)arguments.get("keycenter.preferProtocal");
        String encryptCommunication = (String)arguments.get("keycenter.encryptCommunication");
        if (httpServiceAddress != null && appPublishNum != null) {
            KeyCenterProperties properties = new KeyCenterProperties();
            properties.setHttpServiceAddress(httpServiceAddress);
            properties.setAppPublishNum(appPublishNum);
            properties.setPreferProtocal(preferProtocal);
            properties.setEncryptCommunication(Boolean.getBoolean(encryptCommunication));
            KeyStoreImpl keyStore = new KeyStoreImpl();
            keyStore.setKeyCenterProperties(properties);
            keyStore.init();
            CryptographImpl cryptograph = new CryptographImpl();
            cryptograph.setKeyStore(keyStore);
            this.keyCenterCryptograph = cryptograph;
        } else {
            throw new RuntimeException("httpServiceAddress/appPublishNum is null");
        }
    }

    @Override
    public String encrypt(String plainText, String keyName) {
        return this.keyCenterCryptograph.encrypt(plainText, keyName);
    }

    @Override
    public String decrypt(String encryptedText, String keyName) {
        return this.keyCenterCryptograph.decrypt(encryptedText, keyName);
    }

    public static void main(String[] args) {
        KeyCenterProperties properties = new KeyCenterProperties();
        properties.setHttpServiceAddress("http://daily.keycenter.alibaba.net/keycenter");
        properties.setAppPublishNum("8380226603aa4de59563e4032aeff193");
        properties.setPreferProtocal("http");
        properties.setEncryptCommunication(Boolean.getBoolean("true"));
        KeyStoreImpl keyStore = new KeyStoreImpl();
        keyStore.setKeyCenterProperties(properties);
        keyStore.init();
        CryptographImpl cryptograph = new CryptographImpl();
        cryptograph.setKeyStore(keyStore);
        String ename = cryptograph.encrypt("name","iotx-smart-service-key");
        String epasswd = cryptograph.encrypt("passwd","iotx-smart-service-key");
        System.out.println(ename);
        System.out.println(epasswd);
    }
}

