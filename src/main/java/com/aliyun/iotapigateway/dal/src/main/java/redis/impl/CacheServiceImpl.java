package com.aliyun.iotx.haas.tdserver.dal.redis.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.iotx.haas.tdserver.common.constants.LogConstants;
import com.aliyun.iotx.haas.tdserver.dal.redis.CacheService;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.BindRequestBO;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.SMSVerificationCodeBO;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.ShareTokenBO;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.SmsSendBO;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.GpsUserIdBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author qianfan.qf
 * @date 2020/08/31
 */
@Component
public class CacheServiceImpl implements CacheService {
    private static final Logger errLog = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_ERROR);

    @Value("${redis.key.env.prefix}")
    private String keyEnvPrefix;

    private static final String IOT_TOKEN_TAG = "IOT_TOKEN";

    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    CacheServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final Integer EXPIRED_TIMEOUT_BIND_REQUEST  = 3; //min
    private static final Integer EXPIRED_TIMEOUT_SHARE_REQUEST = 15; //min
    private static final Integer EXPIRED_TIMEOUT_TRANSACTION   = 60; //day
    private static final Integer EXPIRED_TIMEOUT_SMS_SEND = 20; //min
    private static final Integer EXPIRED_TIMEOUT_SMS_VERIFICATION_CODE = 5; //min
    private static final Integer EXPIRED_TIMEOUT_GPS_HAAS_USER_ID = 1; //day

    private String getKeyForRequestId(String requestId) {
        return keyEnvPrefix + "_" + IOT_TOKEN_TAG + "_BindReq_" + requestId;
    }

    private String getKeyForTransactionIdIndex(String productKey, String deviceName) {
        return keyEnvPrefix
                + "_"
                + IOT_TOKEN_TAG
                + "_TransactionId_Index_"
                + productKey
                + "_"
                + deviceName;
    }

    private String getKeyForTransactionId(String productKey, String deviceName, String onlineTime) {
        return keyEnvPrefix
                + "_"
                + IOT_TOKEN_TAG
                + "_TransactionId_"
                + productKey
                + "_"
                + deviceName
                + "_"
                + onlineTime.toString();
    }

    private String getKeyForBindRequest(
            String productKey,
            String deviceName) {
        return keyEnvPrefix
                + "_"
                + IOT_TOKEN_TAG
                + "_BindReqId_"
                + productKey
                + "_"
                + deviceName;
    }

    private String getKeyForShareToken(
            String productKey,
            String deviceName) {
        return keyEnvPrefix
                + "_"
                + IOT_TOKEN_TAG
                + "_ShareTokenId_"
                + productKey
                + "_"
                + deviceName;
    }

    private String getKeyForSmsSend(
            String mobile) {
        return keyEnvPrefix
                + "_"
                + IOT_TOKEN_TAG
                + "_Mobile_"
                + mobile;
    }

    private String getKeyForSmsVerificationCode(
            String mobile) {
        return keyEnvPrefix
                + "_"
                + IOT_TOKEN_TAG
                + "_Mobile_Verification_Code_"
                + mobile;
    }

    private String getKeyForOdmVerificationCode(
            String mobile) {
        return keyEnvPrefix
                + "_"
                + IOT_TOKEN_TAG
                + "_Mobile_Odm_Verification_Code_"
                + mobile;
    }

    private String getKeyForGpsUserIdRequest(
            String productKey,
            String deviceName) {
        return keyEnvPrefix
                + "_"
                + IOT_TOKEN_TAG
                + "_GpsUserId_"
                + productKey
                + "_"
                + deviceName;
    }

    @Override
    public void setCachedBindRequest(String requestId, BindRequestBO bindRequestBO) {
        try {
            String key = getKeyForRequestId(requestId);
            String jsonBindRequest = JSON.toJSONString(bindRequestBO);
            redisTemplate.opsForValue().set(
                    key,
                    jsonBindRequest,
                    EXPIRED_TIMEOUT_BIND_REQUEST,
                    TimeUnit.MINUTES);

            String reqIdKey = getKeyForBindRequest(
                    bindRequestBO.getProductKey(),
                    bindRequestBO.getDeviceName());

            redisTemplate.opsForValue().set(reqIdKey, requestId,
                    EXPIRED_TIMEOUT_BIND_REQUEST,
                    TimeUnit.MINUTES);
        } catch (Exception e) {
            errLog.error("setCachedBindRequest error:{}", e);
        }
    }

    @Override
    public BindRequestBO getCachedBindRequest(String requestId) {
        try {
            String val = redisTemplate.opsForValue().get(getKeyForRequestId(requestId));
            if (val == null) {
                return null;
            }
            return JSON.parseObject(val, BindRequestBO.class);
        } catch (Exception e) {
            errLog.error("getCachedBindRequest error:{}", e);
            return null;
        }
    }

    @Override
    public void deleteCachedBindRequest(String productKey, String deviceName) {
        try {
            String reqIdKey = getKeyForBindRequest(productKey, deviceName);
            String requestId = redisTemplate.opsForValue().get(reqIdKey);
            if (requestId == null) {
                return;
            }
            String bindRequestKey = getKeyForRequestId(requestId);
            List<String> deleteList = new ArrayList<>();
            deleteList.add(reqIdKey);
            deleteList.add(bindRequestKey);
            redisTemplate.delete(deleteList);
        } catch (Exception e) {
            errLog.error("deleteCachedBindRequest error:{}", e);
        }
    }

    @Override
    public Integer getCachedTransactionId(String productKey, String deviceName, String onlineTime) {
        String transactionId = getKeyForTransactionId(productKey, deviceName, onlineTime);
        Integer id = 0;
        try {
            String val = redisTemplate.opsForValue().get(transactionId);
            if (val == null) {
                String key = getKeyForTransactionIdIndex(productKey, deviceName);
                /* Remove invalid record */
                String invalidVal = redisTemplate.opsForValue().get(key);
                if (invalidVal != null) {
                    redisTemplate.delete(invalidVal);
                }

                redisTemplate.opsForValue().set(
                        key,
                        transactionId,
                        EXPIRED_TIMEOUT_TRANSACTION,
                        TimeUnit.DAYS);
                redisTemplate.opsForValue().set(
                        transactionId,
                        id.toString(),
                        EXPIRED_TIMEOUT_TRANSACTION,
                        TimeUnit.DAYS);
            } else {
                id = (Integer.valueOf(val) < 65535 ? Integer.valueOf(val) + 1 : 0);
                redisTemplate.opsForValue().set(
                        transactionId,
                        id.toString(),
                        EXPIRED_TIMEOUT_TRANSACTION,
                        TimeUnit.DAYS);
            }

        } catch (Exception e) {
            errLog.error("getCachedTransactionId error:{}", e);
        }

        return id;
    }

    @Override
    public void setCachedShareToken(ShareTokenBO shareTokenBO) {
        try {
            String shareIdKey = getKeyForShareToken(
                    shareTokenBO.getProductKey(),
                    shareTokenBO.getDeviceName());
            String jsonShareToken = JSON.toJSONString(shareTokenBO);

            redisTemplate.opsForValue().set(shareIdKey, jsonShareToken,
                    EXPIRED_TIMEOUT_SHARE_REQUEST,
                    TimeUnit.MINUTES);
        } catch (Exception e) {
            errLog.error("setCachedShareToken error:{}", e);
        }
    }

    @Override
    public ShareTokenBO getCachedShareToken(String productKey, String deviceName) {
        try {
            String shareIdKey = getKeyForShareToken( productKey, deviceName);
            String val = redisTemplate.opsForValue().get(shareIdKey);
            if (val == null) {
                return null;
            }
            return JSON.parseObject(val, ShareTokenBO.class);
        } catch (Exception e) {
            errLog.error("getCachedShareToken error:{}", e);
            return null;
        }
    }

    @Override
    public void deleteCachedShareToken(String productKey, String deviceName) {
        try {
            String shareIdKey = getKeyForShareToken(productKey, deviceName);
            redisTemplate.delete(shareIdKey);
        } catch (Exception e) {
            errLog.error("deleteCachedShareToken error:{}", e);
        }
    }

    @Override
    public void setCachedSmsRequest(SmsSendBO smsSendBO) {
        try {
            String shareIdKey = getKeyForSmsSend(smsSendBO.getMobile());
            String jsonShareToken = JSON.toJSONString(smsSendBO);
            redisTemplate.opsForValue().set(shareIdKey, jsonShareToken,
                    EXPIRED_TIMEOUT_SMS_SEND,
                    TimeUnit.MINUTES);
        } catch (Exception e) {
            errLog.error("setCachedSmsRequest error:{}", e);
        }
    }
    @Override
    public SmsSendBO getCachedSmsRequest(String mobile) {
        try {
            String shareIdKey = getKeyForSmsSend(mobile);
            String val = redisTemplate.opsForValue().get(shareIdKey);
            if (val == null) {
                return null;
            }
            return JSON.parseObject(val, SmsSendBO.class);
        } catch (Exception e) {
            errLog.error("getCachedSmsRequest error:{}", e);
            return null;
        }
    }

    @Override
    public void setCachedSmsVerificationCode(SMSVerificationCodeBO smsVerificationCodeBO) {
        try {
            String verificationCodeIdKey = getKeyForSmsVerificationCode(smsVerificationCodeBO.getMobile());
            String jsonShareToken = JSON.toJSONString(smsVerificationCodeBO);

            redisTemplate.opsForValue().set(verificationCodeIdKey, jsonShareToken,
                    EXPIRED_TIMEOUT_SMS_VERIFICATION_CODE,
                    TimeUnit.MINUTES);
        } catch (Exception e) {
            errLog.error("setCachedSmsVerificationCode error:{}", e);
        }
    }

    @Override
    public void deleteCachedSmsVerificationCode(SMSVerificationCodeBO smsVerificationCodeBO) {
        try {
            String verificationCodeIdKey = getKeyForSmsVerificationCode(smsVerificationCodeBO.getMobile());
            redisTemplate.delete(verificationCodeIdKey);
        } catch (Exception e) {
            errLog.error("deleteCachedSmsVerificationCode error:{}", e);
        }
    }

    @Override
    public SMSVerificationCodeBO getCachedSmsVerificationCode(String mobile) {
        try {
            String verificationCodeIdKey = getKeyForSmsVerificationCode(mobile);
            String val = redisTemplate.opsForValue().get(verificationCodeIdKey);
            if (val == null) {
                return null;
            }
            return JSON.parseObject(val, SMSVerificationCodeBO.class);
        } catch (Exception e) {
            errLog.error("getCachedSmsVerificationCode error:{}", e);
            return null;
        }
    }

    @Override
    public void decreaseCachedSmsVerificationCodeCount(String mobile) {
        try {
            String verificationCodeIdKey = getKeyForSmsVerificationCode(mobile);
            String val = redisTemplate.opsForValue().get(verificationCodeIdKey);

            SMSVerificationCodeBO smsVerificationCodeBO = JSON.parseObject(val, SMSVerificationCodeBO.class);
            smsVerificationCodeBO.setAvailableCount(smsVerificationCodeBO.getAvailableCount() - 1);
            Long expireTime = redisTemplate.getExpire(verificationCodeIdKey, TimeUnit.MILLISECONDS);

            setCachedSmsVerificationCode(smsVerificationCodeBO);

            redisTemplate.expire(verificationCodeIdKey, expireTime, TimeUnit.MILLISECONDS);
            return;
        } catch (Exception e) {
            errLog.error("decreaseCachedSmsVerificationCodeCount error:{}", e);
            return;
        }
    }

    @Override
    public void setCachedOdmVerificationCode(SMSVerificationCodeBO smsVerificationCodeBO) {
        try {
            String verificationCodeIdKey = getKeyForOdmVerificationCode(smsVerificationCodeBO.getMobile());
            String jsonShareToken = JSON.toJSONString(smsVerificationCodeBO);

            redisTemplate.opsForValue().set(verificationCodeIdKey, jsonShareToken,
                    EXPIRED_TIMEOUT_SMS_VERIFICATION_CODE,
                    TimeUnit.MINUTES);
        } catch (Exception e) {
            errLog.error("setCachedOdmVerificationCode error:{}", e);
        }
    }

    @Override
    public void deleteCachedOdmVerificationCode(SMSVerificationCodeBO smsVerificationCodeBO) {
        try {
            String verificationCodeIdKey = getKeyForOdmVerificationCode(smsVerificationCodeBO.getMobile());
            redisTemplate.delete(verificationCodeIdKey);
        } catch (Exception e) {
            errLog.error("deleteCachedOdmVerificationCode error:{}", e);
        }
    }

    @Override
    public SMSVerificationCodeBO getCachedOdmVerificationCode(String mobile) {
        try {
            String verificationCodeIdKey = getKeyForOdmVerificationCode(mobile);
            String val = redisTemplate.opsForValue().get(verificationCodeIdKey);
            if (val == null) {
                return null;
            }
            return JSON.parseObject(val, SMSVerificationCodeBO.class);
        } catch (Exception e) {
            errLog.error("getCachedOdmVerificationCode error:{}", e);
            return null;
        }
    }

    @Override
    public void decreaseCachedOdmVerificationCodeCount(String mobile) {
        try {
            String verificationCodeIdKey = getKeyForOdmVerificationCode(mobile);
            String val = redisTemplate.opsForValue().get(verificationCodeIdKey);

            SMSVerificationCodeBO smsVerificationCodeBO = JSON.parseObject(val, SMSVerificationCodeBO.class);
            smsVerificationCodeBO.setAvailableCount(smsVerificationCodeBO.getAvailableCount() - 1);
            Long expireTime = redisTemplate.getExpire(verificationCodeIdKey, TimeUnit.MILLISECONDS);

            setCachedSmsVerificationCode(smsVerificationCodeBO);

            redisTemplate.expire(verificationCodeIdKey, expireTime, TimeUnit.MILLISECONDS);
            return;
        } catch (Exception e) {
            errLog.error("decreaseCachedOdmVerificationCodeCount error:{}", e);
            return;
        }
    }

    @Override
    public void setCachedGpsUserIdRequest(GpsUserIdBO gpsUserIdBO) {
        try {
            String shareIdKey = getKeyForGpsUserIdRequest(
                    gpsUserIdBO.getProductKey(),
                    gpsUserIdBO.getDeviceName());
            String jsonShareToken = JSON.toJSONString(gpsUserIdBO);

            GpsUserIdBO lastGpsUserIdBO = getCachedGpsUserIdRequest(gpsUserIdBO.getProductKey(), gpsUserIdBO.getDeviceName());

            if (lastGpsUserIdBO != null) {
                if (lastGpsUserIdBO.getTimeStamp() > gpsUserIdBO.getTimeStamp()) {
                    return;
                }
            }

            redisTemplate.opsForValue().set(shareIdKey, jsonShareToken,
                    EXPIRED_TIMEOUT_GPS_HAAS_USER_ID,
                    TimeUnit.DAYS);
        } catch (Exception e) {
            errLog.error("setCachedGpsUserIdRequest error:{}", e);
        }
    }

    @Override
    public GpsUserIdBO getCachedGpsUserIdRequest(String productKey, String deviceName) {
        try {
            String shareIdKey = getKeyForGpsUserIdRequest( productKey, deviceName);
            String val = redisTemplate.opsForValue().get(shareIdKey);
            if (val == null) {
                return null;
            }
            return JSON.parseObject(val, GpsUserIdBO.class);
        } catch (Exception e) {
            errLog.error("getCachedGpsUserIdRequest error:{}", e);
            return null;
        }
    }

    @Override
    public void deleteCachedGpsUserIdRequest(String productKey, String deviceName, Long timeStamp) {
        try {
            String shareIdKey = getKeyForGpsUserIdRequest(productKey, deviceName);

            GpsUserIdBO lastGpsUserIdBO = getCachedGpsUserIdRequest(productKey, deviceName);

            if (lastGpsUserIdBO != null) {
                if (lastGpsUserIdBO.getTimeStamp() > timeStamp) {
                    return;
                }
            }
            redisTemplate.delete(shareIdKey);
        } catch (Exception e) {
            errLog.error("deleteCachedGpsUserIdRequest error:{}", e);
        }
    }
}


