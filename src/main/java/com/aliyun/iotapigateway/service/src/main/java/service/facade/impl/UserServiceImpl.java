package com.aliyun.iotx.haas.tdserver.service.impl;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import com.alibaba.boot.hsf.annotation.HSFProvider;

import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.exception.HaasServerInternalException;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.common.utils.UserTool;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ProductInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.user.UserDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.user.UserDO;
import com.aliyun.iotx.haas.tdserver.dal.redis.CacheService;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.SMSVerificationCodeBO;
import com.aliyun.iotx.haas.tdserver.facade.UserService;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.user.UserDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.user.UserInfoDTO;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.LvyuanHttpClient;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.bo.LvyuanDeviceRideBO;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.bo.LvyuanUserInfoBO;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.dto.LvyuanDeviceDataDTO;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.dto.LvyuanUserInfoDTO;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.iotx.haas.tdserver.facade.dto.AuthDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.user.UserDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.user.UserInfoDTO;
import com.aliyun.iotx.haas.tdserver.service.aspects.aop.NeedTdAuth;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import javax.annotation.Resource;

@HSFProvider(serviceInterface = UserService.class)
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger("dal");

    @Resource
    UserDAO userDAO;

    private final CryptographUtils cryptograghUtils;
    private final CacheService cacheService;
    private final LvyuanHttpClient lvyuanHttpClient;

    @Resource
    private DeviceBindDAO deviceBindDAO;

    @Resource
    private ProductInfoDAO productInfoDAO;

    @Resource
    private DeviceDAO deviceDAO;

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    private static final String TAOBAO_PLATFORM_NAME = "taobao";

    private static final String ALIPAY_PLATFORM_NAME = "alipay";

    // 手机号码正则
    private static final String REGEX_MOBILE = "((\\+86|0086)?\\s*)((134[0-8]\\d{7})|(((13([0-3]|[5-9]))|(14[5-9])|15([0-3]|[5-9])|(16(2|[5-7]))|17([0-3]|[5-8])|18[0-9]|19(1|[8-9]))\\d{8})|(14(0|1|4)0\\d{7})|(1740([0-5]|[6-9]|[10-12])\\d{7}))";

    @Autowired
    UserServiceImpl(UserDAO userDAO, CryptographUtils cryptographUtils, CacheService cacheService, LvyuanHttpClient lvyuanHttpClient) {
        this.userDAO = userDAO;
        this.cryptograghUtils = cryptographUtils;
        this.cacheService = cacheService;
        this.lvyuanHttpClient = lvyuanHttpClient;
    }

    @Override
    public IoTxResult<UserDTO> login(
            String platform,
            String userId,
            String mobile) {

        if (platform == null || platform.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_LOGIN_PLATFORM_EMPTY);
        }
        if (platform.length() > 32) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_LOGIN_PLATFORM_TOO_LONG);
        }
        if (userId == null || userId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_LOGIN_USER_ID_EMPTY);
        }
        if (userId.length() > 64) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_LOGIN_USER_ID_TOO_LONG);
        }

        try {
            List<UserDO> userDoList = userDAO.findUserByPlatform(platform, userId);
            UserDO userDo;
            if (userDoList.isEmpty()) {
                String haasUserId = UserTool.genHaasUserId(platform, userId);
                List<UserDO> existUserDOList = userDAO.findUserByHaasUserId(haasUserId);
                if (!existUserDOList.isEmpty()) {
                    return new IoTxResult<>(HaasIoTxCodes.ERROR_LOGIN_USER_ID_COLLISION);
                }
                userDo = new UserDO();
                userDo.setHaasUserId(haasUserId);
                userDo.setPlatformName(platform);
                userDo.setRawUserId(userId);
                userDo.setIsUsingSmsNotification(true);
                userDo.setIsAgreementSigned(false);
                if (mobile != null && !mobile.isEmpty()) {
                    userDo.setMobile(cryptograghUtils.encrypt(mobile));
                }
                userDAO.insert(userDo);
            } else {
                userDo = userDoList.get(0);
            }
            UserDTO userDTO = new UserDTO();
            userDTO.setHaasUserId(userDo.getHaasUserId());
            userDTO.setAgreementSigned(userDo.getIsAgreementSigned());
            return new IoTxResult<>(IoTxCodes.SUCCESS, userDTO);
        } catch (HaasServerInternalException e) {
            e.printStackTrace();
            return new IoTxResult<>(IoTxCodes.SERVER_ERROR);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }

    }

    @Override
    public IoTxResult<UserInfoDTO> info(String haasUserId) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_INFO_HAAS_USER_ID_EMPTY);
        }
        try {
            List<UserDO> userDOList = userDAO.findUserByHaasUserId(haasUserId);
            if (userDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_INFO_NOT_FOUND);
            }
            UserDO userDO = userDOList.get(0);
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setHaasUserId(userDO.getHaasUserId());
            if (userDO.getMobile() != null && !userDO.getMobile().isEmpty()) {
                userInfoDTO.setMobile(cryptograghUtils.decrypt(userDO.getMobile()));
            }
            userInfoDTO.setPlatformName(userDO.getPlatformName());
            userInfoDTO.setRawUserId(userDO.getRawUserId());
            userInfoDTO.setReserved(userDO.getReserved());
            userInfoDTO.setReserved2(userDO.getReserved2());
            userInfoDTO.setIsAgreementSigned(userDO.getIsAgreementSigned());
            userInfoDTO.setIsUsingSmsNotification(userDO.getIsUsingSmsNotification());

            return new IoTxResult<>(userInfoDTO);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<String> signAgreement(String haasUserId) {
        try {

            String productKey;

            Long ret = userDAO.signAgreement(haasUserId);
            if (ret == 0) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_AGREEMENT_SIGN_USER_NOT_EXIST);
            }

            return new IoTxResult<>(IoTxCodes.SUCCESS);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<String> upgradeMobile(String haasUserId, String mobile, String verificationCode) {
        try {
            if (haasUserId == null || haasUserId.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_HAAS_USER_ID_EMPTY);
            }

            if (mobile == null || mobile.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_MOBILE_EMPTY);
            }

            if (verificationCode == null || verificationCode.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_VERIFICATION_CODE_EMPTY);
            }

            // 去除云端判断手机号码格式的逻辑，前端判断
//            if (!Pattern.matches(REGEX_MOBILE, mobile)) {
//                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_NUMBER_FORMAT_ERROR);
//            }

            // 验证用户是否存在
            List<UserDO> userDOList = userDAO.findUserByHaasUserId(haasUserId);
            if (userDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_USER_NOT_EXIST);
            }

            // 验证短信验证码
            SMSVerificationCodeBO smsVerificationCodeBO = cacheService.getCachedSmsVerificationCode(mobile);
            if (smsVerificationCodeBO == null) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_VERIFICATION_CODE_EXPIRE);
            } else if (!StringUtils.equals(smsVerificationCodeBO.getCode(), verificationCode)) {
                // 该验证码失败验证，可用次数减1
                cacheService.decreaseCachedSmsVerificationCodeCount(mobile);
                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_VERIFICATION_CODE_ERROR);
            } else if (smsVerificationCodeBO.getAvailableCount() <= 0) {
                // 判断是否可用次数为0，若为0，返回稍后重试的错误
                return new IoTxResult<>(HaasIoTxCodes.ERROR_MOBILE_VERIFICATION_CODE_EXPIRE);
            }

            // 更新用户手机号码
            userDAO.upgradeMobile(haasUserId, cryptograghUtils.encrypt(mobile));

            // 删除改次验证码有效性，一个验证码只能单次有效
            cacheService.deleteCachedSmsVerificationCode(smsVerificationCodeBO);

            return new IoTxResult<>(IoTxCodes.SUCCESS);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<String> upgradeUserSmsNotification(String haasUserId, Boolean isEnable) {
        try {
            if (haasUserId == null || haasUserId.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SMS_NOTIFICATION_HAAS_USER_ID_EMPTY);
            }

            if (isEnable == null) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SMS_NOTIFICATION_IS_ENABLE_EMPTY);
            }

            // 验证用户是否存在
            List<UserDO> userDOList = userDAO.findUserByHaasUserId(haasUserId);
            if (userDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SMS_NOTIFICATION_USER_NOT_EXIST);
            }

            // 更新用户短信预警设置
            userDAO.upgradeSmsNotification(haasUserId, isEnable);

            return new IoTxResult<>(IoTxCodes.SUCCESS, isEnable.toString());
        } catch (DataAccessException e) {
            e.printStackTrace();
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<String> syncUserInfo(String haasUserId , String productKey, String deviceName, Integer userState) {
        //List<DeviceBindDO> devicesByUserId = deviceBindDAO.getDevicesByUserId(haasUserId, environment);
        LvyuanUserInfoBO lvyuanUserInfoBO = new LvyuanUserInfoBO();
        try{
            DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(productKey, deviceName, environment);
            if (deviceDO != null) {
                /*查询厂商*/
                ProductInfoDO productInfo = productInfoDAO.getProductInfoWithUniqueTdserverProductNameOnly(deviceDO.getUniqueTdserverProductName(), environment);

                if ((productInfo != null) && (productInfo.getProductName().contains("绿源"))) {
                    List<UserDO> list = userDAO.findUserByHaasUserId(haasUserId);
                    if (!list.isEmpty()) {
                        lvyuanUserInfoBO.setUserId(haasUserId);
                        lvyuanUserInfoBO.setProductKey(productKey);
                        lvyuanUserInfoBO.setDeviceName(deviceName);
                        //lvyuanUserInfoBO.setDeviceKey(item.);
                        lvyuanUserInfoBO.setMobileNumber(cryptograghUtils.decrypt(list.get(0).getMobile()));
                        lvyuanUserInfoBO.setVehicleType(productInfo.getProductName());
                        lvyuanUserInfoBO.setUserState(userState.toString());
                        lvyuanUserInfoBO.setRegisterData(list.get(0).getGmtCreate().getTime());/*待确认*/
                        lvyuanHttpClient.sendUserInfo(lvyuanUserInfoBO);
                        log.info("User Sync to lvyuan haasUserId : " + haasUserId + "productKey : " + productKey + "deviceName: " + deviceName);
                    }
                }
            }
        }catch(Exception e){
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SERVER_EXTERNAL);
        }
        return new IoTxResult<>(IoTxCodes.SUCCESS);
    }

    @Override
    public IoTxResult<String> syncBindStateInfo(String haasUserId , String productKey, String deviceName,Integer userState) {
        //List<DeviceBindDO> devicesByUserId = deviceBindDAO.getDevicesByUserId(haasUserId, environment);
        DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(productKey, deviceName, environment);
        try{
            if (deviceDO != null) {
                /*查询厂商*/
                ProductInfoDO productInfo = productInfoDAO.getProductInfoWithUniqueTdserverProductNameOnly(deviceDO.getUniqueTdserverProductName(), environment);

                if ((productInfo != null) && (productInfo.getProductName().contains("绿源"))) {
                    LvyuanUserInfoBO lvyuanUserInfoBO = new LvyuanUserInfoBO();
                    lvyuanUserInfoBO.setUserId(haasUserId);
                    lvyuanUserInfoBO.setProductKey(productKey);
                    lvyuanUserInfoBO.setDeviceName(deviceName);
                    lvyuanUserInfoBO.setUserState(userState.toString());
                    lvyuanUserInfoBO.setRegisterData(System.currentTimeMillis());
                    lvyuanHttpClient.sendUserInfo(lvyuanUserInfoBO);
                    log.info("User Sync bind info to lvyuan haasUserId : " + haasUserId + "productKey : " + productKey + "deviceName: " + deviceName + "userState : " + userState );
                }
            }
        }catch(Exception e){
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SERVER_EXTERNAL);
        }
        return new IoTxResult<>(IoTxCodes.SUCCESS);
    }

    @Override
    public IoTxResult<String> testLvyuanUserInfo(String haasUserId) {


        LvyuanUserInfoBO lvyuanUserInfoBO = new LvyuanUserInfoBO();
        lvyuanUserInfoBO.setUserId(haasUserId);
        lvyuanUserInfoBO.setProductKey("pk");
        lvyuanUserInfoBO.setDeviceName("dn");
        lvyuanUserInfoBO.setDeviceKey("dk");
        lvyuanUserInfoBO.setMobileNumber("188");
        lvyuanUserInfoBO.setRegisterData(System.currentTimeMillis());

        lvyuanHttpClient.sendUserInfo(lvyuanUserInfoBO);

        LvyuanDeviceRideBO lvyuanDeviceRideBO = new LvyuanDeviceRideBO();
        lvyuanDeviceRideBO.setStartTime(System.currentTimeMillis());
        lvyuanDeviceRideBO.setEndTime(System.currentTimeMillis());
        lvyuanDeviceRideBO.setDeviceName("dn");
        lvyuanDeviceRideBO.setProductKey("pk");
        lvyuanDeviceRideBO.setErrorControl(0L);
        lvyuanDeviceRideBO.setErrorHall(0L);
        lvyuanDeviceRideBO.setMileage(0L);
        lvyuanDeviceRideBO.setProtectionControl(0L);
        lvyuanDeviceRideBO.setProtectionMotor(0L);
        lvyuanDeviceRideBO.setProtectionOverCur(0L);
        lvyuanDeviceRideBO.setProtectionOverSpeed(0L);
        lvyuanDeviceRideBO.setProtectionOverTemp(0L);
        lvyuanDeviceRideBO.setProtectionPhase(0L);
        lvyuanDeviceRideBO.setProtectionUnderVol(0L);
        lvyuanDeviceRideBO.setSocMax(0L);
        lvyuanDeviceRideBO.setSocMin(0L);
        lvyuanDeviceRideBO.setTemperatureHigh(0L);
        lvyuanDeviceRideBO.setTemperatureLow(0L);
        lvyuanDeviceRideBO.setErrorHandle(0L);

        lvyuanHttpClient.sendDeviceInfo(lvyuanDeviceRideBO);
        return null;
    }
}

