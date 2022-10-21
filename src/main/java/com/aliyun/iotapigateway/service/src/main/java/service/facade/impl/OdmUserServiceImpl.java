package com.aliyun.iotx.haas.tdserver.service.impl;

import com.alibaba.boot.hsf.annotation.HSFProvider;

import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.common.utils.RandomTool;
import com.aliyun.iotx.haas.tdserver.dal.dao.odm.OdmInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.odm.OdmInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.redis.CacheService;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.SMSVerificationCodeBO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.OdmInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.msg.SmsService;
import com.aliyun.iotx.haas.tdserver.facade.odm.OdmUserService;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.QueryOdmInfoResultDTO;
import com.aliyun.iotx.haas.tdserver.sal.aep.UserAgreementQueryServiceClient;
import com.aliyun.iotx.haas.tdserver.sal.dingtalk.DingTalkSericeClient;
import com.aliyun.iotx.haas.tdserver.sal.msg.SmsClient;
import com.aliyun.iotx.haas.tdserver.sal.msg.impl.SmsClientImpl;
import com.aliyun.iotx.signing.center.data.dto.UserAgreementInfoDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;

/**
 * @author benxiliu
 * @date 2020/09/08
 */

@HSFProvider(serviceInterface = OdmUserService.class)
public class OdmUserServiceImpl implements OdmUserService {
    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Resource
    private OdmInfoDAO odmInfoDAO;

    @Resource
    private DingTalkSericeClient dingtalkClientSign;

    @Resource
    private CryptographUtils cryptographUtils;

    @Resource
    CacheService cacheService;

    @Resource
    SmsClient smsClient;

    @Resource
    UserAgreementQueryServiceClient userAgreementQueryServiceClient;

    private final String SIGN_TEMPLATE_CODE = "tdserver";

    public static final String SIGN_APPROVAL_STATUS = "approved";

    private static final int SMS_VERIFICATION_CODE_EXPIRED_TIMEMILLIS = 60 * 1000;

    private static final int SMS_VERIFICATION_CODE_LENGTH = 6;

    private static final int SMS_VERIFICATION_CODE_RETRY_TIMES = 10;

    @Override
    public IoTxResult<Void> odmSign(String tenantId, OdmInfoDTO odmInfoDTO) {
        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO != null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_ALREADY_EXIST);
        }

        if (StringUtils.isBlank(odmInfoDTO.getName())) {
            return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "odm name is empty", "ODM名称为空");
        }

        OdmInfoDO odmInfoDO = new OdmInfoDO();
        odmInfoDO.setOdmTenantId(tenantId);
        odmInfoDO.setAddr((odmInfoDTO.getAddr()));
        odmInfoDO.setAliyunPk((odmInfoDTO.getAliyunPk()));

        if (odmInfoDTO.getContact() != null) {
            odmInfoDO.setContact(cryptographUtils.encrypt(odmInfoDTO.getContact()));
        }
        if (odmInfoDTO.getMobile() != null) {
            odmInfoDO.setMobile(cryptographUtils.encrypt(odmInfoDTO.getMobile()));
        }
        if (odmInfoDTO.getEmail() != null) {
            odmInfoDO.setEmail(cryptographUtils.encrypt(odmInfoDTO.getEmail()));
        }
        odmInfoDO.setApprovalStatus(SIGN_APPROVAL_STATUS);
        odmInfoDO.setContactPost(odmInfoDTO.getContactPost());
        odmInfoDO.setName(odmInfoDTO.getName());
        odmInfoDO.setRemark(odmInfoDTO.getRemark());
        odmInfoDO.setAliyunPk(odmInfoDTO.getAliyunPk());

        odmInfoDAO.insert(odmInfoDO);
        return new IoTxResult<>();
    }

    @Override
    public IoTxResult<QueryOdmInfoResultDTO> queryOdmInfo(String tenantId) {
        OdmInfoDO odmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);

        IoTxResult<QueryOdmInfoResultDTO> result = new IoTxResult<>();
        QueryOdmInfoResultDTO queryOdmInfoResultDTO = new QueryOdmInfoResultDTO();

        if (odmInfoDO == null) {
            queryOdmInfoResultDTO.setIsSigned(false);
        } else {
            queryOdmInfoResultDTO.setIsSigned(true);
            OdmInfoDTO odmInfoDTO = new OdmInfoDTO();
            if (odmInfoDO.getContact() != null) {
                odmInfoDTO.setContact(cryptographUtils.decrypt(odmInfoDO.getContact()));
            }
            if (odmInfoDO.getMobile() != null) {
                odmInfoDTO.setMobile(cryptographUtils.decrypt(odmInfoDO.getMobile()));
            }
            if (odmInfoDO.getEmail() != null) {
                odmInfoDTO.setEmail(cryptographUtils.decrypt(odmInfoDO.getEmail()));
            }

            odmInfoDTO.setAddr(odmInfoDO.getAddr());
            odmInfoDTO.setName(odmInfoDO.getName());
            odmInfoDTO.setRemark(odmInfoDO.getRemark());
            odmInfoDTO.setContactPost(odmInfoDO.getContactPost());
            queryOdmInfoResultDTO.setOdmInfoDTO(odmInfoDTO);
        }

        result.setData(queryOdmInfoResultDTO);
        return result;
    }

    @Override
    public IoTxResult<Void> updateOdmInfo(String tenantId, OdmInfoDTO odmInfoDTO, String verificationCode) {
        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_SIGN);
        }

        // 验证短信验证码
        String mobile = cryptographUtils.decrypt(oldOdmInfoDO.getMobile());
        SMSVerificationCodeBO smsVerificationCodeBO = cacheService.getCachedOdmVerificationCode(mobile);
        if (smsVerificationCodeBO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_VERIFICATION_CODE_EXPIRE);
        } else if (!StringUtils.equals(smsVerificationCodeBO.getCode(), verificationCode)) {
            // 该验证码失败验证，可用次数减1
            cacheService.decreaseCachedOdmVerificationCodeCount(mobile);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_VERIFICATION_CODE_ERROR);
        } else if (smsVerificationCodeBO.getAvailableCount() <= 0) {
            // 判断是否可用次数为0，若为0，返回稍后重试的错误
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_VERIFICATION_CODE_EXPIRE);
        }

        // 仅允许修改联系人及联系人手机号
        oldOdmInfoDO.setContact(cryptographUtils.encrypt(odmInfoDTO.getContact()));
        oldOdmInfoDO.setMobile(cryptographUtils.encrypt(odmInfoDTO.getMobile()));

        odmInfoDAO.updateOdmInfo(oldOdmInfoDO);

        return new IoTxResult<>();
    }

    @Override
    public IoTxResult<String> querySignAgreement(String identityId) {
        IoTxResult<UserAgreementInfoDTO> userAgreementInfoDTOIoTxResult = userAgreementQueryServiceClient.queryUserAgreementInfo(
                identityId, SIGN_TEMPLATE_CODE);

        if (userAgreementInfoDTOIoTxResult.hasSucceeded() && userAgreementInfoDTOIoTxResult.getData() != null) {
            return new IoTxResult<>(userAgreementInfoDTOIoTxResult.getData().getUserAgreement().getStatus());
        } else {
            return new IoTxResult<>();
        }
    }

    @Override
    public IoTxResult<String> sendOdmVerificationCode(String tenantId) {
        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_SIGN);
        }

        // 发送短信
        String mobile = cryptographUtils.decrypt(oldOdmInfoDO.getMobile());
        // 查找缓存，是否已经存在记录，若请求时间差太短，返回稍后重试的错误
        SMSVerificationCodeBO smsVerificationCodeBO = cacheService.getCachedOdmVerificationCode(mobile);
        if (smsVerificationCodeBO != null) {
            if ((System.currentTimeMillis() - smsVerificationCodeBO.getTimestamp()) < SMS_VERIFICATION_CODE_EXPIRED_TIMEMILLIS) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_GET_VERIFICATION_CODE_LATER);
            }
        }

        // 获取6位随机数
        String code = RandomTool.getRamdomNumeric(SMS_VERIFICATION_CODE_LENGTH).toString();

        // 调用SMS接口发送验证码
        smsClient.sendTdserverVerificationCode(mobile, code);

        // 将验证码存入缓存
        smsVerificationCodeBO = new SMSVerificationCodeBO();
        smsVerificationCodeBO.setMobile(mobile);
        smsVerificationCodeBO.setCode(code);
        smsVerificationCodeBO.setTimestamp(System.currentTimeMillis());
        smsVerificationCodeBO.setAvailableCount(SMS_VERIFICATION_CODE_RETRY_TIMES);

        cacheService.setCachedOdmVerificationCode(smsVerificationCodeBO);

        return new IoTxResult<>();
    }

//    @Override
//    public IoTxResult<String> createDingTalkChat() {
//        return null;
////        return dingtalkServiceClient.createChat();
//    }
}

