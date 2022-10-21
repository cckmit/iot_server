package com.aliyun.iotx.haas.tdserver.service.impl;

import akka.actor.Status;
import com.alibaba.boot.hsf.annotation.HSFProvider;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.utils.RandomTool;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ProductInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.user.UserDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.user.UserDO;
import com.aliyun.iotx.haas.tdserver.dal.redis.CacheService;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.SMSVerificationCodeBO;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.SmsSendBO;
import com.aliyun.iotx.haas.tdserver.facade.dto.msg.AlarmSmsNotification;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.product.OdmProductInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.msg.SmsService;
import com.aliyun.iotx.haas.tdserver.sal.msg.SmsClient;
import com.aliyun.iotx.haas.tdserver.sal.msg.impl.SmsClientImpl;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author imost.lwf
 * @date 2020/11/24
 */
@HSFProvider(serviceInterface = SmsService.class)
public class SmsServiceImpl implements SmsService {
    @Autowired
    private SmsClient smsClient;

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Resource
    private CryptographUtils cryptographUtils;

    @Resource
    private DeviceBindDAO deviceBindDAO;

    @Autowired
    private DeviceDAO deviceDAO;

    @Resource
    private ProductInfoDAO productInfoDAO;

    private final UserDAO userDAO;
    private final CacheService cacheService;

    private final Integer SMS_VERIFICATION_CODE_EXPIRED_TIMEMILLIS = 50 * 1000;

    private static final Logger errorLog = LoggerFactory.getLogger("error");

    private static final Logger salLog = LoggerFactory.getLogger("sal");

    @Autowired
    public SmsServiceImpl(DeviceBindDAO deviceBindDAO, UserDAO userDAO, CacheService cacheService) {
        this.deviceBindDAO = deviceBindDAO;
        this.userDAO = userDAO;
        this.cacheService = cacheService;
    }

    @Override
    public IoTxResult<String> sendTdserverAlarmMsg(String productKey, String deviceName) {

        // 验绑定关系
        List<String> userIdList = deviceBindDAO.getUserIdByDevice(
                productKey, deviceName, environment);
        if (userIdList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_NOT_BIND);
        }

        // 查找用户
        List<UserDO> userDOList = userDAO.findUserByHaasUserId(userIdList.get(0));
        if (userDOList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_USER_NOT_EXIST);
        }

        // 检验用户是否设置预警号码
        if (userDOList.get(0).getMobile() == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_NUMBER_NOT_EXIST);
        }

        // 验证用户是否开启短信通知
        if (userDOList.get(0).getIsUsingSmsNotification() != null && userDOList.get(0).getIsUsingSmsNotification() == false) {
            return new IoTxResult<>();
        }

        String mobile = cryptographUtils.decrypt(userDOList.get(0).getMobile());

        if (isMobileSendable(mobile)) {
            smsClient.sendTdserverAlarmMsg(cryptographUtils.decrypt(userDOList.get(0).getMobile()));
        } else {
            salLog.info("send too many alarm message: pk:{}, dn:{}, mobile:{}", productKey, deviceName, mobile);
        }

        return new IoTxResult<>();
    }

    @Override
    public IoTxResult<String> sendTdserverAlarmDetailMsg(String productKey, String deviceName, Integer alarmType, Integer alarmValue) {

        // 验绑定关系
        List<String> userIdList = deviceBindDAO.getUserIdByDevice(
                productKey, deviceName, environment);
        if (userIdList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_NOT_BIND);
        }

        // 查找用户
        List<UserDO> userDOList = userDAO.findUserByHaasUserId(userIdList.get(0));
        if (userDOList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_USER_NOT_EXIST);
        }

        // 检验用户是否设置预警号码
        if (userDOList.get(0).getMobile() == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_NUMBER_NOT_EXIST);
        }

        // 验证用户是否开启短信通知
        if (userDOList.get(0).getIsUsingSmsNotification() != null && userDOList.get(0).getIsUsingSmsNotification() == false) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_NUMBER_NOT_EXIST);
        }

        String mobile = cryptographUtils.decrypt(userDOList.get(0).getMobile());

        // 判断该手机号是否符合发送规则
        if (isMobileSendable(mobile)) {
            smsClient.sendTdserverAlarmDeailMsg(mobile, alarmType, alarmValue);
        } else {
            salLog.info("send too many detail alarm message: pk:{}, dn:{}, mobile:{}, alarmType:{}, alarmValue:{}",
                    productKey, deviceName, mobile, alarmType,alarmValue);
        }

        return new IoTxResult<>();
    }

    @Override
    public IoTxResult<String> sendSmsVerificationCode(String mobile) {
        // 查找缓存，是否已经存在记录，若请求时间差太短，返回稍后重试的错误
        SMSVerificationCodeBO smsVerificationCodeBO = cacheService.getCachedSmsVerificationCode(mobile);
        if (smsVerificationCodeBO != null) {
            if ((System.currentTimeMillis() - smsVerificationCodeBO.getTimestamp()) < SMS_VERIFICATION_CODE_EXPIRED_TIMEMILLIS) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_GET_VERIFICATION_CODE_LATER);
            }
        }

        // 获取6位随机数
        String code = RandomTool.getRamdomNumeric(6).toString();

        // 调用SMS接口发送验证码
        smsClient.sendTdserverVerificationCode(mobile, code);

        // 将验证码存入缓存
        smsVerificationCodeBO = new SMSVerificationCodeBO();
        smsVerificationCodeBO.setMobile(mobile);
        smsVerificationCodeBO.setCode(code);
        smsVerificationCodeBO.setTimestamp(System.currentTimeMillis());
        smsVerificationCodeBO.setAvailableCount(10);

        cacheService.setCachedSmsVerificationCode(smsVerificationCodeBO);

        return new IoTxResult<>();
    }

    @Override
    public IoTxResult<AlarmSmsNotification> queryAlarmSmsNotification(String haasUserId) {
        // 查找用户
        List<UserDO> userDOList = userDAO.findUserByHaasUserId(haasUserId);
        if (userDOList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_USER_NOT_EXIST);
        }

        AlarmSmsNotification alarmSmsNotification = new AlarmSmsNotification();
        alarmSmsNotification.setMobile(cryptographUtils.decrypt(userDOList.get(0).getMobile()));
        if (userDOList.get(0).getIsUsingSmsNotification() == null) {
            alarmSmsNotification.setIsEnable(false);
        } else {
            alarmSmsNotification.setIsEnable(userDOList.get(0).getIsUsingSmsNotification());
        }

        return new IoTxResult<>(alarmSmsNotification);
    }

    private boolean isMobileSendable(String mobile) {
        // 发送警告时，若缓存存在记录，则说明在规定的时间段内已经发送过预警短信，此时无法发送预警短信
        if (cacheService.getCachedSmsRequest(mobile) == null) {
            SmsSendBO smsSendBO = new SmsSendBO();
            smsSendBO.setMobile(mobile);
            smsSendBO.setTimestamp(System.currentTimeMillis());
            cacheService.setCachedSmsRequest(smsSendBO);
            return true;
        } else {
            return false;
        }
    }

    public IoTxResult<String> decryptMobile(String productKey, String deviceName){
        // 验绑定关系
        List<String> userIdList = deviceBindDAO.getUserIdByDevice(productKey, deviceName, environment);
        if (userIdList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_NOT_BIND);
        }

        // 查找用户
        List<UserDO> userDOList = userDAO.findUserByHaasUserId(userIdList.get(0));
        if (userDOList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_USER_NOT_EXIST);
        }

        // 检验用户是否设置预警号码
        if (userDOList.get(0).getMobile() == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_NUMBER_NOT_EXIST);
        }

        // 验证用户是否开启短信通知
        if (userDOList.get(0).getIsUsingSmsNotification() != null && userDOList.get(0).getIsUsingSmsNotification() == false) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_NUMBER_NOT_EXIST);
        }

        return new IoTxResult<>("Get mobile success: " + cryptographUtils.decrypt(userDOList.get(0).getMobile()));
    }

    public IoTxResult<String> sendTimeOutMsg(String productKey) {

        String targetMobile = new String();
        String msg = new String();
        ProductInfoDO productInfo = productInfoDAO.getProductInfoWithProductKeyOnly(productKey, environment);
        if(productInfo != null) {
            String productName = productInfo.getProductName();
            msg = "您的闪骑侠智能服务已经到期，到期后将影响车辆智能功能使用。请联系销售客服。如未能及时续费将有销号风险，智能功能将无法恢复使用";
        }

        List<DeviceDO> deviceList= deviceDAO.getDeviceWithProductKey(productKey,environment);
        for (DeviceDO device: deviceList) {
            List<String> userIdList = deviceBindDAO.getUserIdByDevice(
                    productKey, device.getDeviceName(), environment);
            if (!userIdList.isEmpty()) {
                // 查找用户
                List<UserDO> userDOList = userDAO.findUserByHaasUserId(userIdList.get(0));
                if (!userDOList.isEmpty()) {
                    // 检验用户是否设置预警号码
                    if (userDOList.get(0).getMobile() != null) {

                        // 短信通知客户所有客户
                        String mobile = cryptographUtils.decrypt(userDOList.get(0).getMobile());
                        // 发送短信
                        smsClient.sendTdserverTimeOutMsg(mobile,   msg);

                    }
                }
            }
        }

        return new IoTxResult<>();
    }
}

