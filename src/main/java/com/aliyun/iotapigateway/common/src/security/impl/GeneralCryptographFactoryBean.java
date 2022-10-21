package com.aliyun.iotx.haas.tdserver.common.security.impl;

import java.util.Map;

import com.aliyun.iotx.haas.tdserver.common.security.GeneralCryptograph;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author benxiliu
 * @date 2019/12/06
 */

public class GeneralCryptographFactoryBean implements FactoryBean<GeneralCryptograph> {

    private static String cryptographType = "kms";
    private Map<String, String> arguments;
    private static String KEY_CENTER = "keycenter";
    private static String KMS = "kms";

    public GeneralCryptographFactoryBean() {
    }

    public Map<String, String> getArguments() {
        return this.arguments;
    }

    public String getCryptographType() {
        return this.cryptographType;
    }

    public void setCryptographType(String cryptographType) {
        this.cryptographType = cryptographType;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public GeneralCryptograph getObject() throws Exception {
        if (KEY_CENTER.equals(this.cryptographType)) {
            return new com.aliyun.iotx.haas.tdserver.common.security.impl.KeyCenterGeneralCryptographImpl(this.arguments);
        } else if (KMS.equals(this.cryptographType)) {
            return new KmsGeneralCryptographImpl(this.arguments);
        } else {
            throw new Exception("Unkown cryptographType " + this.cryptographType);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return GeneralCryptograph.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

