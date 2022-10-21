package com.aliyun.iotx.haas.tdserver.common.security.impl;

import java.util.Map;

import com.aliyun.iotx.haas.tdserver.common.security.GeneralCryptograph;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.InstanceProfileCredentialsProvider;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.kms.model.v20160120.DecryptRequest;
import com.aliyuncs.kms.model.v20160120.DecryptResponse;
import com.aliyuncs.kms.model.v20160120.EncryptRequest;
import com.aliyuncs.kms.model.v20160120.EncryptResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

/**
 * @author benxiliu
 * @date 2019/12/06
 */

public class KmsGeneralCryptographImpl implements GeneralCryptograph {

    private DefaultAcsClient kmsClient;
    private String regionId;
    private String accessKeyId;
    private String accessKeySecret;
    private String roleName;

    private static String ENV_ONLINE = "online";

    private static final Logger errorLog = LoggerFactory.getLogger("error");

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public KmsGeneralCryptographImpl(Map<String, String> arguments) throws ClientException {
        String regionId = (String)arguments.get("kms.regionId");
        String accessKeyId = (String)arguments.get("kms.accessKeyId");
        String accessKeySecret = (String)arguments.get("kms.accessKeySecret");
        String roleName = (String)arguments.get("kms.roleName");
        if (regionId != null && accessKeyId != null && accessKeySecret != null) {
            this.setRegionId(regionId);
            this.setAccessKeyId(accessKeyId);
            this.setAccessKeySecret(accessKeySecret);
            this.setRoleName(roleName);
            this.initClient();
        } else {
            throw new RuntimeException("createKmsImp regionId or accessKeyId or accesskeySecret or domain is null");
        }
    }

    private void initClient() throws ClientException {
        DefaultProfile.addEndpoint( regionId + "-vpc", regionId + "-vpc", "Kms", "kms-vpc." + regionId + ".aliyuncs.com");
        DefaultProfile profile = DefaultProfile.getProfile(regionId);
        InstanceProfileCredentialsProvider provider = new InstanceProfileCredentialsProvider(roleName);
        kmsClient = new DefaultAcsClient(profile, provider);
    }

    @Override
    public String encrypt(String plainText, String keyName) {
        final EncryptRequest encReq = new EncryptRequest();
        encReq.setProtocol(ProtocolType.HTTPS);
        encReq.setAcceptFormat(FormatType.JSON);
        encReq.setMethod(MethodType.POST);
        encReq.setKeyId(keyName);
        encReq.setPlaintext(plainText);
        try {
            final EncryptResponse encResponse = kmsClient.getAcsResponse(encReq);
            return encResponse.getCiphertextBlob();
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
            throw new RuntimeException("failed to encrypt");
        }
    }

    @Override
    public String decrypt(String encryptedText, String keyName) {
        final DecryptRequest decReq = new DecryptRequest();
        decReq.setProtocol(ProtocolType.HTTPS);
        decReq.setAcceptFormat(FormatType.JSON);
        decReq.setMethod(MethodType.POST);
        decReq.setCiphertextBlob(encryptedText);
        try {
            final DecryptResponse decResponse = kmsClient.getAcsResponse(decReq);
            return getFromBASE64(decResponse.getPlaintext());
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
            throw new RuntimeException("failed to decrypt");
        }
    }

    private static String getFromBASE64(String s) {
        if (s == null) {
            return null;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] b = decoder.decodeBuffer(s);
            return new String(b);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", "ak", "as");
        DefaultAcsClient kmsClient= new DefaultAcsClient(profile);
        final EncryptRequest encReq = new EncryptRequest();
        encReq.setProtocol(ProtocolType.HTTPS);
        encReq.setAcceptFormat(FormatType.JSON);
        encReq.setMethod(MethodType.POST);
        encReq.setKeyId("keyId");
        encReq.setPlaintext("plaintext");
        String temp;
        try {
            final EncryptResponse encResponse = kmsClient.getAcsResponse(encReq);
            temp = encResponse.getCiphertextBlob();
            System.out.println(encResponse.getCiphertextBlob());
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
            throw new RuntimeException("failed to encrypt");
        }
        final DecryptRequest decReq = new DecryptRequest();
        decReq.setProtocol(ProtocolType.HTTPS);
        decReq.setAcceptFormat(FormatType.JSON);
        decReq.setMethod(MethodType.POST);
        decReq.setCiphertextBlob(temp);
        try {
            final DecryptResponse decResponse = kmsClient.getAcsResponse(decReq);
            System.out.println(decResponse.getPlaintext());
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
            throw new RuntimeException("failed to decrypt");
        }
    }
}

