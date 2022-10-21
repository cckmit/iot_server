package com.aliyun.iotx.haas.tdserver.sal.dingtalk;

import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.dal.domain.odm.OdmInfoDO;
import com.dingtalk.api.request.OapiChatSendRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author imost.lwf
 * @date 2021/03/08
 */
@Component
public class DingTalkNotificationComponent {
    @Value("${aliyun.haas.tdserver.dingtalk.sign.url}")
    private String signUrl;

    @Value("${aliyun.haas.tdserver.dingtalk.triad.url}")
    private String triadUrl;

    @Value("${aliyun.haas.tdserver.dingtalk.sign.chatId}")
    private String signChatId;

    @Value("${aliyun.haas.tdserver.dingtalk.triad.chatId}")
    private String triadChatId;

    private static final String OA_HEAD_TEXT = "智能出行服务";

    private static final String OA_HEAD_BGCOLOR = "FFBBBBBBB";

    private static final String NOTIFICATION_ADMIN = "Admin";

    private static final String OA_SIGN_NOTIFICATION_CONTRACT = "【入驻申请】";

    private static final String OA_TRIAD_NOTIFICATION_CONTRACT = "【三元组申请】";

    private static final String OA_TENANT_ID = "租户ID: ";

    private static final String OA_CONTRACT_NAME = "厂商名称: ";

    private static final String OA_CONTACT_NAME = "厂商联系人: ";

    private static final String OA_CONTRACT_MOBILE = "联系人电话: ";

    private static final String OA_PRODUCT_ID = "产品ID: ";

    private static final String OA_TRIAD_APPLY_ID = "申请批次ID: ";

    private static final String OA_TRIAD_COUNT = "申请三元组数量: ";

    private static final String DING_TALK_URL_PREFIX = "dingtalk://dingtalkclient/page/link?url=";

    private static final String DING_TALK_URL_SUFFIX = "&pc_slide=false";

    @Resource
    private DingTalkSericeClient dingTalkSericeClient;

    public IoTxResult<Void> sendContractNotification(OdmInfoDO odmInfoDO) {
        OapiChatSendRequest.Oa oa = new OapiChatSendRequest.Oa();
        String encodedSignUrl = null;

        // 设置跳转URL在浏览器打开
        try {
            encodedSignUrl = DING_TALK_URL_PREFIX + URLEncoder.encode(signUrl, "UTF-8") + DING_TALK_URL_SUFFIX;
        } catch (Exception e) {
            return new IoTxResult<>(IoTxCodes.SERVER_ERROR.getCode(), "URL Encoder Error");
        }

        oa.setPcMessageUrl(encodedSignUrl);
        oa.setMessageUrl(encodedSignUrl);

        OapiChatSendRequest.Head head = new OapiChatSendRequest.Head();
        head.setText(OA_HEAD_TEXT);
        head.setBgcolor(OA_HEAD_BGCOLOR);
        oa.setHead(head);

        OapiChatSendRequest.Body body = new OapiChatSendRequest.Body();
        body.setAuthor(NOTIFICATION_ADMIN);
        body.setTitle(OA_SIGN_NOTIFICATION_CONTRACT);

        List<OapiChatSendRequest.Form> formList = new ArrayList<>();

        OapiChatSendRequest.Form formTenantId = new OapiChatSendRequest.Form();
        formTenantId.setKey(OA_TENANT_ID);
        formTenantId.setValue(odmInfoDO.getOdmTenantId());
        formList.add(formTenantId);

        OapiChatSendRequest.Form formComName = new OapiChatSendRequest.Form();
        formComName.setKey(OA_CONTRACT_NAME);
        formComName.setValue(odmInfoDO.getName());
        formList.add(formComName);

        OapiChatSendRequest.Form formName = new OapiChatSendRequest.Form();
        formName.setKey(OA_CONTACT_NAME);
        formName.setValue(odmInfoDO.getContact());
        formList.add(formName);

        OapiChatSendRequest.Form mobile = new OapiChatSendRequest.Form();
        mobile.setKey(OA_CONTRACT_MOBILE);
        mobile.setValue(odmInfoDO.getMobile());
        formList.add(mobile);

        body.setForm(formList);
        oa.setBody(body);

        return dingTalkSericeClient.sendOaNotification(signChatId, oa);
    }

    public IoTxResult<Void> sendTriadNotification(OdmInfoDO odmInfoDO, String productName, Long count, Long applyId) {
        OapiChatSendRequest.Oa oa = new OapiChatSendRequest.Oa();
        String encodedSignUrl = null;

        // 设置跳转URL在浏览器打开
        try {
            encodedSignUrl = DING_TALK_URL_PREFIX + URLEncoder.encode(triadUrl, "UTF-8") + DING_TALK_URL_SUFFIX;
        } catch (Exception e) {
            return new IoTxResult<>(IoTxCodes.SERVER_ERROR.getCode(), "URL Encoder Error");
        }

        oa.setPcMessageUrl(encodedSignUrl);
        oa.setMessageUrl(encodedSignUrl);

        OapiChatSendRequest.Head head = new OapiChatSendRequest.Head();
        head.setText(OA_HEAD_TEXT);
        head.setBgcolor(OA_HEAD_BGCOLOR);
        oa.setHead(head);

        OapiChatSendRequest.Body body = new OapiChatSendRequest.Body();
        body.setAuthor(NOTIFICATION_ADMIN);
        body.setTitle(OA_TRIAD_NOTIFICATION_CONTRACT);

        List<OapiChatSendRequest.Form> formList = new ArrayList<>();

        OapiChatSendRequest.Form formTenantId = new OapiChatSendRequest.Form();
        formTenantId.setKey(OA_TENANT_ID);
        formTenantId.setValue(odmInfoDO.getOdmTenantId());
        formList.add(formTenantId);

        OapiChatSendRequest.Form formComName = new OapiChatSendRequest.Form();
        formComName.setKey(OA_CONTRACT_NAME);
        formComName.setValue(odmInfoDO.getName());
        formList.add(formComName);

        OapiChatSendRequest.Form formName = new OapiChatSendRequest.Form();
        formName.setKey(OA_CONTACT_NAME);
        formName.setValue(odmInfoDO.getContact());
        formList.add(formName);

        OapiChatSendRequest.Form mobile = new OapiChatSendRequest.Form();
        mobile.setKey(OA_CONTRACT_MOBILE);
        mobile.setValue(odmInfoDO.getMobile());
        formList.add(mobile);

        OapiChatSendRequest.Form product = new OapiChatSendRequest.Form();
        product.setKey(OA_PRODUCT_ID);
        product.setValue(productName);
        formList.add(product);

        OapiChatSendRequest.Form triadCount = new OapiChatSendRequest.Form();
        triadCount.setKey(OA_TRIAD_COUNT);
        triadCount.setValue(count.toString());
        formList.add(triadCount);

        OapiChatSendRequest.Form batchApplyId = new OapiChatSendRequest.Form();
        batchApplyId.setKey(OA_TRIAD_APPLY_ID);
        batchApplyId.setValue(applyId.toString());
        formList.add(batchApplyId);

        body.setForm(formList);
        oa.setBody(body);

        return dingTalkSericeClient.sendOaNotification(triadChatId, oa);
    }

}

