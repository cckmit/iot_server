package com.aliyun.iotx.haas.tdserver.sal.dingtalk;

import com.aliyun.iotx.common.base.service.IoTxResult;
import com.dingtalk.api.request.OapiChatSendRequest;

import java.util.List;

/**
 * @author imost.lwf
 * @date 2021/03/08
 */
public interface DingTalkSericeClient {
    /**
     * 发送OA消息到钉钉群
     * @param chatId 发送群ID
     * @param oa    请求消息体OA
     * @return
     */
    IoTxResult<Void> sendOaNotification(String chatId, OapiChatSendRequest.Oa oa);

    /**
     * 创建钉钉群
     * @return
     */
    IoTxResult<String> createChat();

}

