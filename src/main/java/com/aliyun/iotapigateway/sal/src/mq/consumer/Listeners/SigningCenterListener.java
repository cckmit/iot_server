package com.aliyun.iotx.haas.tdserver.sal.mq.consumer.Listeners;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.iotx.account.service.v2.model.employee.EmployeeDTO;
import com.aliyun.iotx.account.service.v2.request.employee.QueryEmployeeRequest;
import com.aliyun.iotx.common.base.exception.IoTxException;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.constants.LogConstants;
import com.aliyun.iotx.haas.tdserver.dal.domain.odm.OdmInfoDO;
import com.aliyun.iotx.haas.tdserver.sal.aep.EmployeeServiceClient;
import com.aliyun.iotx.haas.tdserver.sal.aep.UserAgreementQueryServiceClient;
import com.aliyun.iotx.haas.tdserver.sal.dingtalk.DingTalkNotificationComponent;
import com.aliyun.iotx.haas.tdserver.sal.sign.SignComponent;
import com.aliyun.iotx.signing.center.data.dto.UserAgreementFormDTO;
import com.aliyun.iotx.signing.center.data.dto.UserAgreementInfoDTO;
import com.aliyun.openservices.ons.api.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * @author imost.lwf
 * @date 2020/12/29
 */

@Component
public class SigningCenterListener implements MessageListener {

    private static final Logger mqLog = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_MQ);

    private static final Logger errLog = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_ERROR);

    /**
     * 签约模板、状态、联系人、电话
     */
    public static final String SIGN_TEMPLATE_CODE = "tdserver";
    public static final String SIGN_STATUS_JOINED = "joined";
    public static final String SIGN_STATUS_APPLYING = "applying";
    public static final String PERSONAL_CONTACT_KEY = "realname";
    public static final String COMPANY_CONTACT_NAME_KEY = "companyName";
    public static final String COMPANY_CONTACT_POST_KEY = "contactPost";
    public static final String COMPANY_CONTACT_KEY = "companyContact";
    public static final String PERSONAL_EMAIL_KEY = "email";
    public static final String COMPANY_EMAIL_KEY = "companyEmail";
    public static final String PERSONAL_PHONE_KEY = "cellphone";
    public static final String COMPANY_PHONE_KEY = "companyCellphone";

    @Value("${aliyun.iotx.signing.center.encrypted.accessKey}")
    private String encryptedAccessKey;

    @Value("${aliyun.iotx.signing.center.encrypted.accessSecret}")
    private String encryptedAccessSecret;

    @Value("${mq.aliyun.iotx.signing.center.address}")
    private String address;

    @Value("${mq.aliyun.iotx.signing.center.groupId}")
    private String groupId;

    @Value("${mq.aliyun.iotx.signing.center.topic}")
    private String topic;

    @Resource
    private CryptographUtils cryptographUtils;

    @Resource
    UserAgreementQueryServiceClient userAgreementQueryServiceClient;

    @Resource
    private EmployeeServiceClient employeeServiceClient;

    @Resource
    private DingTalkNotificationComponent dingTalkNotificationComponent;

    @Resource
    SignComponent signComponent;

    private Consumer consumer;

    @PostConstruct
    private void init() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.AccessKey, cryptographUtils.decrypt(encryptedAccessKey));
        properties.put(PropertyKeyConst.SecretKey, cryptographUtils.decrypt(encryptedAccessSecret));
        properties.put(PropertyKeyConst.GROUP_ID, groupId);
        properties.put(PropertyKeyConst.NAMESRV_ADDR, address);
        properties.put(PropertyKeyConst.MaxReconsumeTimes, "3");

        // 消息消费失败时的最大重试次数
        mqLog.debug("MQ consumer scheduler: {}", properties);

        consumer = ONSFactory.createConsumer(properties);

        // tag过滤, 订阅所有设备消息, tag为*
        consumer.subscribe(topic, "*", this);
        consumer.start();

        mqLog.warn("signing center mq consumer started.");
    }

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        mqLog.info("message received:  {}", message);

        try {
            String topic = message.getTopic();
            String tag = message.getTag();
            String messageBody = new String(message.getBody(), Charset.defaultCharset());

            mqLog.info("topic:{}, tag:{}, msgBody:{}", topic, tag, messageBody);
            JSONObject msgJson = JSONObject.parseObject(messageBody);

            mqLog.info("receiving signing center message : {}", JSON.toJSONString(msgJson));

            String identityId = msgJson.getString("identityId");
            String templateCode = msgJson.getString("templateCode");
            String toStatus = msgJson.getString("toStatus");

            // 查询是否为签约完成状态，如果是则为用户创建默认公司和OA账号
            if (SIGN_STATUS_JOINED.equals(toStatus) && SIGN_TEMPLATE_CODE.equalsIgnoreCase(templateCode)) {
                IoTxResult<UserAgreementInfoDTO> signResult = userAgreementQueryServiceClient.queryUserAgreementInfo(
                        identityId, templateCode);
                if (signResult.hasSucceeded() && signResult.getData() != null) {
                    UserAgreementInfoDTO agreementInfoDTO = signResult.getData();
                    String companyName = null, contactName = null, contactPhone = null, contactEmail = null, contactPost = null;

                    if (agreementInfoDTO.getUserAgreementFormList() != null) {
                        for (UserAgreementFormDTO formDTO : agreementInfoDTO.getUserAgreementFormList()) {
                            // 企业认证

                            if (COMPANY_CONTACT_NAME_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(companyName)) {
                                companyName = formDTO.getFieldValue();
                            }

                            if (COMPANY_CONTACT_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(contactName)) {
                                contactName = formDTO.getFieldValue();
                            }

                            if (COMPANY_PHONE_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(contactPhone)) {
                                contactPhone = formDTO.getFieldValue();
                            }

                            if (COMPANY_EMAIL_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(contactEmail)) {
                                contactEmail = formDTO.getFieldValue();
                            }

                            if (COMPANY_CONTACT_POST_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(contactPost)) {
                                contactPost = formDTO.getFieldValue();
                            }
                        }

                        signComponent.sign(identityId, contactPhone, contactEmail, contactName, companyName, contactPost);
                    }  else {
                        mqLog.error("topic:{}, tag:{}, signResult:{}", topic, tag, signResult.getData());
                    }
                }
            } else if (SIGN_STATUS_APPLYING.equals(toStatus) && SIGN_TEMPLATE_CODE.equalsIgnoreCase(templateCode)) {
                IoTxResult<UserAgreementInfoDTO> signResult = userAgreementQueryServiceClient.queryUserAgreementInfo(
                        identityId, templateCode);
                if (signResult.hasSucceeded() && signResult.getData() != null) {
                    UserAgreementInfoDTO agreementInfoDTO = signResult.getData();
                    if (agreementInfoDTO.getUserAgreementFormList() != null) {
                        String companyName = null, contactName = null, contactPhone = null, contactEmail = null, contactPost = null;
                        OdmInfoDO odmInfoDO = new OdmInfoDO();

                        QueryEmployeeRequest queryEmployeeRequest = new QueryEmployeeRequest();
                        queryEmployeeRequest.setEmployeeId(identityId);
                        IoTxResult<EmployeeDTO> result = employeeServiceClient.queryEmployee(queryEmployeeRequest);

                        if (!result.hasSucceeded() || result.getData() == null) {
                            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
                        }

                        EmployeeDTO mainEmployee = result.getData();

                        odmInfoDO.setOdmTenantId(mainEmployee.getTenantId());

                        for (UserAgreementFormDTO formDTO : agreementInfoDTO.getUserAgreementFormList()) {
                            // 企业认证

                            if (COMPANY_CONTACT_NAME_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(companyName)) {
                                odmInfoDO.setName(formDTO.getFieldValue());
                            }

                            if (COMPANY_CONTACT_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(contactName)) {
                                odmInfoDO.setContact(formDTO.getFieldValue());
                            }

                            if (COMPANY_PHONE_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(contactPhone)) {
                                odmInfoDO.setMobile(formDTO.getFieldValue());
                            }

                            if (COMPANY_EMAIL_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(contactEmail)) {
                                odmInfoDO.setEmail(formDTO.getFieldValue());
                            }

                            if (COMPANY_CONTACT_POST_KEY.equals(formDTO.getFieldKey()) && StringUtils.isBlank(contactPost)) {
                                odmInfoDO.setContactPost(formDTO.getFieldValue());
                            }
                        }

                        IoTxResult<Void> dingTalkResult = dingTalkNotificationComponent.sendContractNotification(odmInfoDO);

                        if (!dingTalkResult.hasSucceeded()) {
                            mqLog.error("topic:{}, tag:{}, dingTalkNotification code:{}, dingTalkNotification msg:{}, dingTalkNotification LocalizedMsg:{}",
                                    topic, tag, dingTalkResult.getCode(), dingTalkResult.getMessage(), dingTalkResult.getLocalizedMsg());
                        }
                    } else {
                        mqLog.error("topic:{}, tag:{}, signResult:{}", topic, tag, signResult.getData());
                    }
                }
            }

        } catch (Exception e) {
            errLog.error(e.getMessage(), e);
        }

        return Action.CommitMessage;
    }
}


