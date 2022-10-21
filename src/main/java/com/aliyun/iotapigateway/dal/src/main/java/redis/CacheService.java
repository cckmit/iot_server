package com.aliyun.iotx.haas.tdserver.dal.redis;


import com.aliyun.iotx.haas.tdserver.dal.redis.bo.BindRequestBO;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.ShareTokenBO;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.GpsUserIdBO;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.SMSVerificationCodeBO;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.SmsSendBO;

import java.util.Date;

/**
 * @author qianfan.qf
 * @date 2020/08/31
 */

public interface CacheService {
    void setCachedBindRequest(String requestId, BindRequestBO bindRequestDO);
    BindRequestBO getCachedBindRequest(String requestId);
    void deleteCachedBindRequest(String productKey, String deviceName);

    Integer getCachedTransactionId(String productKey, String deviceName, String onlineTime);

    void setCachedShareToken(ShareTokenBO shareRequestBO);
    ShareTokenBO getCachedShareToken(String productKey, String deviceName);
    void deleteCachedShareToken(String productKey, String deviceName);

    void setCachedGpsUserIdRequest(GpsUserIdBO gpsUserIdBO);
    GpsUserIdBO getCachedGpsUserIdRequest(String productKey, String deviceName);
    void deleteCachedGpsUserIdRequest(String productKey, String deviceName, Long timeStamp);

    void setCachedSmsRequest(SmsSendBO smsSendBO);
    SmsSendBO getCachedSmsRequest(String mobile);

    void setCachedSmsVerificationCode(SMSVerificationCodeBO smsVerificationCodeBO);
    void deleteCachedSmsVerificationCode(SMSVerificationCodeBO smsVerificationCodeBO);
    SMSVerificationCodeBO getCachedSmsVerificationCode(String mobile);
    void decreaseCachedSmsVerificationCodeCount(String mobile);

    void setCachedOdmVerificationCode(SMSVerificationCodeBO smsVerificationCodeBO);
    void deleteCachedOdmVerificationCode(SMSVerificationCodeBO smsVerificationCodeBO);
    SMSVerificationCodeBO getCachedOdmVerificationCode(String mobile);
    void decreaseCachedOdmVerificationCodeCount(String mobile);
}

