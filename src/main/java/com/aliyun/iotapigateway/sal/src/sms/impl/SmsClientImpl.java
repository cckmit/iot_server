package com.aliyun.iotx.haas.tdserver.sal.msg.impl;

import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.sal.msg.SmsClient;
import com.aliyun.iotx.msgcenter.core.facade.dto.notify.sms.request.BatchSendRequestDTO;
import com.aliyun.iotx.msgcenter.core.facade.dto.notify.sms.request.SmsAccessCodeRequestDTO;
import com.aliyun.iotx.msgcenter.core.facade.dto.service.access.MessageServiceAccessDTO;
import com.aliyun.iotx.msgcenter.core.facade.dto.service.access.MessageServiceResourceDTO;
import com.aliyun.iotx.msgcenter.core.facade.dto.service.access.SmsServiceMetadataDTO;
import com.aliyun.iotx.msgcenter.core.facade.service.MessageServiceAccessServiceFacade;
import com.aliyun.iotx.msgcenter.core.facade.service.SmsServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author imost.lwf
 * @date 2020/11/24
 */

@Component
public class SmsClientImpl implements SmsClient {
    @Value("${aliyun.haas.linkscreen.msgcenter.encrypted.accessKey}")
    private String accessKeyIdEncryption;

    @Value("${aliyun.haas.linkscreen.msgcenter.encrypted.accessSecret}")
    private String accessKeySecretEncryption;

    @Value("${aliyun.haas.linkscreen.msgcenter.appkey.tenantId}")
    private String tenantId;

    private Long serviceId;

    private String accessCode;

    private final String HLS_MSG_CENTER_NAME = "hassLinkScreenMsgcenter";

    private final Integer MSG_RESOURCE_ACTION_ON = 1;

    private final String MSG_ACCESS_CHANNEL_CODE = "SMS";

    // 告警类型：1-防盗告警
    private final int TDSERVER_ALARM_TYPE_ANTI_THEFT = 1;

    // 告警类型：2-安全告警
    private final int TDSERVER_ALARM_TYPE_SAFT = 2;

    private final String SMS_TEMPLATE_CODE_TDSERVER_ALARM = "SMS_205825057";

    private final String SMS_TEMPLATE_CODE_TDSERVER_ALARM_DETAIL = "SMS_205815694";

    private final String SMS_TEMPLATE_CODE_TDSERVER_VERIFICATION_CODE = "SMS_205880107";

    private final String SMS_TEMPLATE_CODE_TDSERVER_FLOW_EXHAUST = "SMS_226426467";

    private final String SMS_TEMPLATE_TYEP_NAME = "SMS_TEMPLATE_CODE";

    private final String SMS_SIGN_TYEP_NAME = "SMS_SIGN_NAME";

    private final String TDSERVER_ALARM_SIGN_NAME = "闪骑侠";

    private final String SMS_TDSERVER_ALARM_ANTI_THEFT_0X00 = "车辆震动";
    private final String SMS_TDSERVER_ALARM_ANTI_THEFT_0X01 = "车辆推动";
    private final String SMS_TDSERVER_ALARM_ANTI_THEFT_0X02 = "电池偷盗";
    private final String SMS_TDSERVER_ALARM_ANTI_THEFT_0XFF = "其他原因";

    @Resource
    private CryptographUtils cryptographUtils;

    @Resource
    private MessageServiceAccessServiceFacade messageServiceAccessServiceFacade;

    @Resource
    private SmsServiceFacade smsServiceFacade;

    private static final Logger errorLog = LoggerFactory.getLogger("error");

    @PostConstruct
    public void initClient() {
        try {
            String accessKeyId = cryptographUtils.decrypt(accessKeyIdEncryption);
            String accessKeySecret = cryptographUtils.decrypt(accessKeySecretEncryption);
            SmsServiceMetadataDTO smsServiceMetadataDTO = new SmsServiceMetadataDTO();
            smsServiceMetadataDTO.setAccessKey(accessKeyId);
            smsServiceMetadataDTO.setAccessSecret(accessKeySecret);
            smsServiceMetadataDTO.setName(HLS_MSG_CENTER_NAME);
            smsServiceMetadataDTO.setTenantId(tenantId);

            // 注册SMS服务
            IoTxResult<Long> result = messageServiceAccessServiceFacade.registerSMSServiceMetadata(smsServiceMetadataDTO);
            if (result.hasSucceeded()) {
                // 保存serviceId
                serviceId = result.getData();
                smsServiceMetadataDTO.setId(serviceId);

                // 更新SMS服务
                messageServiceAccessServiceFacade.updateSMSServiceMetadata(smsServiceMetadataDTO);

                // SMS授权
                IoTxResult<String> accessCodeResult = authorizeMSGService();

                if (accessCodeResult.hasSucceeded()) {
                    // 授权成功，保存accessCode
                    accessCode = accessCodeResult.getData();
                }
            }
        } catch (Exception e) {
            errorLog.error("[initClient] init iot msgcenter client failed", e);
        }
    }

    private IoTxResult<String> authorizeMSGService() {
        // 授权短信预警模版
        MessageServiceAccessDTO messageServiceAccessDTO = new MessageServiceAccessDTO();
        List<MessageServiceResourceDTO> messageServiceResourceDTOList = new ArrayList<>();
        MessageServiceResourceDTO  messageServiceResourceDTO = new MessageServiceResourceDTO();
        messageServiceResourceDTO.setAction(MSG_RESOURCE_ACTION_ON);
        messageServiceResourceDTO.setTarget(SMS_TEMPLATE_CODE_TDSERVER_ALARM);
        messageServiceResourceDTO.setType(SMS_TEMPLATE_TYEP_NAME);
        messageServiceResourceDTOList.add(messageServiceResourceDTO);

        // 授权详细短信预警模版
        messageServiceResourceDTO = new MessageServiceResourceDTO();
        messageServiceResourceDTO.setAction(MSG_RESOURCE_ACTION_ON);
        messageServiceResourceDTO.setTarget(SMS_TEMPLATE_CODE_TDSERVER_ALARM_DETAIL);
        messageServiceResourceDTO.setType(SMS_TEMPLATE_TYEP_NAME);
        messageServiceResourceDTOList.add(messageServiceResourceDTO);

        // 授权短信验证码模版
        messageServiceResourceDTO = new MessageServiceResourceDTO();
        messageServiceResourceDTO.setAction(MSG_RESOURCE_ACTION_ON);
        messageServiceResourceDTO.setTarget(SMS_TEMPLATE_CODE_TDSERVER_VERIFICATION_CODE);
        messageServiceResourceDTO.setType(SMS_TEMPLATE_TYEP_NAME);
        messageServiceResourceDTOList.add(messageServiceResourceDTO);

        // 授权签名模版
        messageServiceResourceDTO = new MessageServiceResourceDTO();
        messageServiceResourceDTO.setAction(MSG_RESOURCE_ACTION_ON);
        messageServiceResourceDTO.setTarget(TDSERVER_ALARM_SIGN_NAME);
        messageServiceResourceDTO.setType(SMS_SIGN_TYEP_NAME);
        messageServiceResourceDTOList.add(messageServiceResourceDTO);

        // 请求授权
        messageServiceAccessDTO.setMessageServiceResourceList(messageServiceResourceDTOList);
        messageServiceAccessDTO.setChannelCode(MSG_ACCESS_CHANNEL_CODE);
        messageServiceAccessDTO.setServiceId(serviceId);
        messageServiceAccessDTO.setTenantId(tenantId);

        return  messageServiceAccessServiceFacade.authorizeMessageService(messageServiceAccessDTO);
    }

    @Override
    public void sendTdserverAlarmMsg(String mobile) {
        BatchSendRequestDTO batchSendRequestDTO = new BatchSendRequestDTO();

        SmsAccessCodeRequestDTO smsAccessCodeRequestDTO = new SmsAccessCodeRequestDTO();
        smsAccessCodeRequestDTO.setAccessCode(accessCode);
        smsAccessCodeRequestDTO.setCallerTenantId(tenantId);

        List<String> phoneNumList = new ArrayList<>();
        phoneNumList.add(mobile);

        // 选择报警模版和签名模版
        batchSendRequestDTO.setAccessCodeRequestDTO(smsAccessCodeRequestDTO);
        batchSendRequestDTO.setPhoneNumList(phoneNumList);
        batchSendRequestDTO.setParamsJsonStr("{}");
        batchSendRequestDTO.setSignName(TDSERVER_ALARM_SIGN_NAME);
        batchSendRequestDTO.setTemplateCode(SMS_TEMPLATE_CODE_TDSERVER_ALARM);

        // 发送短信
        smsServiceFacade.batchSendSMS(batchSendRequestDTO);
    }

    @Override
    public void sendTdserverAlarmDeailMsg(String mobile, Integer alarmType, Integer alarmVale) {
        if (alarmType != TDSERVER_ALARM_TYPE_ANTI_THEFT) {
            return;
        }
        BatchSendRequestDTO batchSendRequestDTO = new BatchSendRequestDTO();

        SmsAccessCodeRequestDTO smsAccessCodeRequestDTO = new SmsAccessCodeRequestDTO();
        smsAccessCodeRequestDTO.setAccessCode(accessCode);
        smsAccessCodeRequestDTO.setCallerTenantId(tenantId);

        List<String> phoneNumList = new ArrayList<>();
        phoneNumList.add(mobile);

        // 选择报警模版和签名模版
        batchSendRequestDTO.setAccessCodeRequestDTO(smsAccessCodeRequestDTO);
        batchSendRequestDTO.setPhoneNumList(phoneNumList);

        // 根据报警类型，定义预警短信详细内容
        if (alarmVale == 0x00) {
            batchSendRequestDTO.setParamsJsonStr("{\"type\":\"" + SMS_TDSERVER_ALARM_ANTI_THEFT_0X00 + "\"}");
        } else if (alarmVale == 0x01) {
            batchSendRequestDTO.setParamsJsonStr("{\"type\":\"" + SMS_TDSERVER_ALARM_ANTI_THEFT_0X01 + "\"}");
        } else {
            batchSendRequestDTO.setParamsJsonStr("{\"type\":\"" + SMS_TDSERVER_ALARM_ANTI_THEFT_0XFF + "\"}");
        }

        batchSendRequestDTO.setSignName(TDSERVER_ALARM_SIGN_NAME);
        batchSendRequestDTO.setTemplateCode(SMS_TEMPLATE_CODE_TDSERVER_ALARM_DETAIL);

        // 发送短信
        smsServiceFacade.batchSendSMS(batchSendRequestDTO);
    }

    @Override
    public void sendTdserverVerificationCode(String mobile, String code) {
        BatchSendRequestDTO batchSendRequestDTO = new BatchSendRequestDTO();

        SmsAccessCodeRequestDTO smsAccessCodeRequestDTO = new SmsAccessCodeRequestDTO();
        smsAccessCodeRequestDTO.setAccessCode(accessCode);
        smsAccessCodeRequestDTO.setCallerTenantId(tenantId);

        List<String> phoneNumList = new ArrayList<>();
        phoneNumList.add(mobile);

        // 选择短信模版和签名模版，填入验证码内容
        batchSendRequestDTO.setAccessCodeRequestDTO(smsAccessCodeRequestDTO);
        batchSendRequestDTO.setPhoneNumList(phoneNumList);
        batchSendRequestDTO.setParamsJsonStr("{\"code\":\"" + code + "\"}");
        batchSendRequestDTO.setSignName(TDSERVER_ALARM_SIGN_NAME);
        batchSendRequestDTO.setTemplateCode(SMS_TEMPLATE_CODE_TDSERVER_VERIFICATION_CODE);

        // 发送短信
        smsServiceFacade.batchSendSMS(batchSendRequestDTO);
    }

    @Override
    public void sendTdserverTimeOutMsg(String mobile, String code) {

        BatchSendRequestDTO batchSendRequestDTO = new BatchSendRequestDTO();

        SmsAccessCodeRequestDTO smsAccessCodeRequestDTO = new SmsAccessCodeRequestDTO();
        smsAccessCodeRequestDTO.setAccessCode(accessCode);
        smsAccessCodeRequestDTO.setCallerTenantId(tenantId);

        List<String> phoneNumList = new ArrayList<>();
        phoneNumList.add(mobile);

        // 选择报警模版和签名模版
        batchSendRequestDTO.setAccessCodeRequestDTO(smsAccessCodeRequestDTO);
        batchSendRequestDTO.setPhoneNumList(phoneNumList);
        batchSendRequestDTO.setParamsJsonStr("{\"type\":\"" + code + "\"}");
        //batchSendRequestDTO.setParamsJsonStr(null);
        batchSendRequestDTO.setSignName(TDSERVER_ALARM_SIGN_NAME);
        //batchSendRequestDTO.setTemplateCode(SMS_TEMPLATE_CODE_TDSERVER_FLOW_EXHAUST);
        batchSendRequestDTO.setTemplateCode(SMS_TEMPLATE_CODE_TDSERVER_ALARM_DETAIL);
        // 发送短信
        IoTxResult<String> result = smsServiceFacade.batchSendSMS(batchSendRequestDTO);
        if(result.getCode() !=  IoTxCodes.SUCCESS.getCode())
        {
            errorLog.error("send"+ mobile + "tdserver flow exhaust failed: " + "templatecode: "+batchSendRequestDTO.getTemplateCode()+ "Code: " +result.getCode() + "message: " +result.getMessage()  );
        }else{
            errorLog.error("send"+ mobile + "tdserver flow exhaust success" );
        }
        return ;
    }
}
