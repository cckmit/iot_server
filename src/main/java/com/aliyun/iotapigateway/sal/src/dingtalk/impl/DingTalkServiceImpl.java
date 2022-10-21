package com.aliyun.iotx.haas.tdserver.sal.dingtalk.impl;

import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.sal.dingtalk.DingTalkSericeClient;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiChatCreateRequest;
import com.dingtalk.api.request.OapiChatSendRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiChatCreateResponse;
import com.dingtalk.api.response.OapiChatSendResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author imost.lwf
 * @date 2021/03/08
 */
@Component
public class DingTalkSericeClientImpl implements DingTalkSericeClient {

    private static final String TOKEN_REQUEST_URL = "https://oapi.dingtalk.com/gettoken";

    private static final String CHAT_SEND_URL = "https://oapi.dingtalk.com/chat/send";

    private static final String CHAT_CREATE_URL = "https://oapi.dingtalk.com/chat/create";

    @Resource
    CryptographUtils cryptographUtils;

    @Value("${aliyun.haas.tdserver.dingtalk.encrypted.accessKey}")
    private String accessKeyIdEncryption;

    @Value("${aliyun.haas.tdserver.dingtalk.encrypted.accessSecret}")
    private String accessKeySecretEncryption;

    private String getToken() {
        DefaultDingTalkClient client = new DefaultDingTalkClient(TOKEN_REQUEST_URL);
        OapiGettokenRequest request = new OapiGettokenRequest();

        request.setAppkey(cryptographUtils.decrypt(accessKeyIdEncryption));
        request.setAppsecret(cryptographUtils.decrypt(accessKeySecretEncryption));
        request.setHttpMethod("GET");

        try {
            OapiGettokenResponse response = client.execute(request);
            return response.getAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public IoTxResult<Void> sendOaNotification(String chatId, OapiChatSendRequest.Oa oa) {
        String accessToken = getToken();

        DefaultDingTalkClient client = new DefaultDingTalkClient(CHAT_SEND_URL);
        OapiChatSendRequest request = new OapiChatSendRequest();

        request.setChatid(chatId);

        OapiChatSendRequest.Msg msg = new OapiChatSendRequest.Msg();
        msg.setMsgtype("oa");
        msg.setOa(oa);

        request.setMsg(msg);

        try {
            OapiChatSendResponse response = client.execute(request, accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new IoTxResult<>();
    }

    @Override
    public IoTxResult<String> createChat() {
        String accessToken = getToken();

        DefaultDingTalkClient client = new DefaultDingTalkClient(CHAT_CREATE_URL);
        OapiChatCreateRequest req = new OapiChatCreateRequest();
        req.setName("出行入驻审核群(日常)");
        req.setOwner("223189");
        req.setUseridlist(Arrays.asList("175505","139681","179100"));
//        req.setUseridlist(Arrays.asList("171875","179937","163916","225375","139681","165249","WB927438","WB497480"));

        try {
            OapiChatCreateResponse response = client.execute(req, accessToken);
            System.out.println(response.getChatid());
            return new IoTxResult<>(response.getChatid());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new IoTxResult<>();
    }
}

