package com.aliyun.iotx.haas.tdserver.service.impl;

import com.alibaba.boot.hsf.annotation.HSFProvider;
import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.common.constants.CommonConstants;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.utils.EmojiFilter;
import com.aliyun.iotx.haas.tdserver.common.utils.HexStrTool;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceShareDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.user.UserDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceShareDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.user.UserDO;
import com.aliyun.iotx.haas.tdserver.dal.redis.CacheService;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.ShareTokenBO;
import com.aliyun.iotx.haas.tdserver.facade.UserService;
import com.aliyun.iotx.haas.tdserver.facade.dto.share.QueryDeviceShareInfoResultDTO;
import com.aliyun.iotx.haas.tdserver.facade.share.DeviceShareService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.aliyun.iotx.haas.tdserver.common.utils.RandomTool.getRamdomAlphanumeric;

/**
 * @author imost.lwf
 * @date 2020/10/28
 */
@HSFProvider(serviceInterface = DeviceShareService.class)
public class DeviceShareServiceImpl implements DeviceShareService {

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    private final CacheService cacheService;

    @Resource
    private DeviceBindDAO deviceBindDAO;

    @Resource
    private DeviceShareDAO deviceShareDAO;

    @Resource
    private UserDAO userDAO;

    @Resource
    private UserService userService;

    @Resource
    private CryptographUtils cryptographUtils;

    private static final Logger errorLog = LoggerFactory.getLogger("error");

    @Autowired
    public DeviceShareServiceImpl(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public IoTxResult<String> getDeviceShareToken(String productKey, String deviceName, String haasUserId) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_QR_HAAS_USER_ID_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_QR_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_QR_DN_EMPTY);
        }

        try {
            // 验证绑定关系
            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
            if (deviceBindDOList.isEmpty()) {
                errorLog.error("getDeviceShareQRCode error: " + deviceBindDOList.toString());
                errorLog.error("getDeviceShareQRCode error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
                return new IoTxResult<>(IoTxCodes.REQUEST_FORBIDDEN);
            }

            ShareTokenBO shareTokenBO = new ShareTokenBO();
            shareTokenBO.setProductKey(productKey);
            shareTokenBO.setDeviceName(deviceName);
            shareTokenBO.setHaasUserId(haasUserId);

            String ramdomNum = getRamdomAlphanumeric(CommonConstants.SHARE_TOKEN_LENGTH);

            // 构造token的原始信息
            String tokenOrigin = haasUserId + productKey + deviceName + ramdomNum + System.currentTimeMillis();
            // 对token原始信息做SHA256
            MessageDigest tokenDigest = MessageDigest.getInstance("SHA256");

            tokenDigest.update(tokenOrigin.getBytes());
            String token = HexStrTool.byteArrayToHexStr(tokenDigest.digest());

            shareTokenBO.setToken(token);

            // 写入缓存数据库
            cacheService.setCachedShareToken(shareTokenBO);

            return new IoTxResult<>(token);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        } catch (NoSuchAlgorithmException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SERVER_INTERNAL);
        }
    }

    @Override
    public IoTxResult<String> shareDevice(String productKey, String deviceName, String haasUserId, String token) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_HAAS_USER_ID_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_DN_EMPTY);
        }

        try {
            // 获取分享上下文缓存
            ShareTokenBO shareTokenBO = cacheService.getCachedShareToken(productKey, deviceName);

            if (shareTokenBO == null) {
                // 没有找到上下文，token过期或未生成
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_TOKEN_TIMEOUT);
            }

            if (!token.equals(shareTokenBO.getToken())) {
                // 入参token非法
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_INVALID_TOKEN);
            }

            if (shareTokenBO.getHaasUserId().equals(haasUserId)) {
                // 授权者与被授权者相同
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_USER_SELF);
            }

            // 验证设备是否与授权者绑定关系是否依然存在
            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, shareTokenBO.getHaasUserId(), environment);
            if (deviceBindDOList.isEmpty()) {
                errorLog.error("bindShareDevice error: " + deviceBindDOList.toString());
                errorLog.error("bindShareDevice error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_DN_UNAUTHORIZED);
            }

            // 验证被授权用户是否存在
            List<UserDO> userDOList = userDAO.findUserByHaasUserId(haasUserId);
            if (userDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_INVALID_USER);
            }

            // 验证分享关系是否已经存在
            List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(productKey, deviceName, haasUserId, environment);
            if (deviceShareDOList != null && !deviceShareDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_EXIST);
            }

            DeviceShareDO deviceShareDO = new DeviceShareDO();
            deviceShareDO.setAuthorizerHaasUserId(shareTokenBO.getHaasUserId());
            deviceShareDO.setProductKey(productKey);
            deviceShareDO.setDeviceName(deviceName);
            deviceShareDO.setAuthorizeeHaasUserId(haasUserId);
            // 对支付宝Id做脱敏
            StringBuffer buffer = new StringBuffer(userDOList.get(0).getRawUserId());
            buffer.replace(4, 12 , "****");
            if (StringUtils.isNotBlank(buffer.toString())) {
                deviceShareDO.setAuthorizeeAlias(buffer.toString());
            }
            deviceShareDO.setEnvironment(environment);

            deviceShareDAO.insert(deviceShareDO);

            return new IoTxResult<>(IoTxCodes.SUCCESS);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<String> shareDeviceV2(String productKey, String deviceName, String haasUserId, String alipayNickName, String token) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_HAAS_USER_ID_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_DN_EMPTY);
        }

        try {
            // 获取分享上下文缓存
            ShareTokenBO shareTokenBO = cacheService.getCachedShareToken(productKey, deviceName);

            if (shareTokenBO == null) {
                // 没有找到上下文，token过期或未生成
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_TOKEN_TIMEOUT);
            }

            if (!token.equals(shareTokenBO.getToken())) {
                // 入参token非法
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_INVALID_TOKEN);
            }

            if (shareTokenBO.getHaasUserId().equals(haasUserId)) {
                // 授权者与被授权者相同
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_USER_SELF);
            }

            // 验证设备是否与授权者绑定关系是否依然存在
            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, shareTokenBO.getHaasUserId(), environment);
            if (deviceBindDOList.isEmpty()) {
                errorLog.error("bindShareDevice error: " + deviceBindDOList.toString());
                errorLog.error("bindShareDevice error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_DN_UNAUTHORIZED);
            }

            // 验证被授权用户是否存在
            List<UserDO> userDOList = userDAO.findUserByHaasUserId(haasUserId);
            if (userDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_INVALID_USER);
            }

            // 验证分享关系是否已经存在
            List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(productKey, deviceName, haasUserId, environment);
            if (deviceShareDOList != null && !deviceShareDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SHARE_EXIST);
            }

            DeviceShareDO deviceShareDO = new DeviceShareDO();
            deviceShareDO.setAuthorizerHaasUserId(shareTokenBO.getHaasUserId());
            deviceShareDO.setProductKey(productKey);
            deviceShareDO.setDeviceName(deviceName);
            deviceShareDO.setAuthorizeeHaasUserId(haasUserId);
            // 用户昵称可能会有Emoji表情符号，直接过滤
            if (StringUtils.isNotBlank(alipayNickName)) {
                deviceShareDO.setAuthorizeeAlias(EmojiFilter.filterEmoji(alipayNickName));
            } else {
                // 对支付宝Id做脱敏
                StringBuffer buffer = new StringBuffer(userDOList.get(0).getRawUserId());
                buffer.replace(4, 12 , "****");
                if (StringUtils.isNotBlank(buffer.toString())) {
                    deviceShareDO.setAuthorizeeAlias(buffer.toString());
                }
            }
            deviceShareDO.setEnvironment(environment);

            deviceShareDAO.insert(deviceShareDO);

            return new IoTxResult<>(IoTxCodes.SUCCESS);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<String> unshareDevice(String productKey, String deviceName, String authorizeeHaasUserId, String haasUserId) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNSHARE_HAAS_USER_ID_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNSHARE_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNSHARE_DN_EMPTY);
        }

        if (authorizeeHaasUserId == null || authorizeeHaasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNSHARE_AUTHORIZEE_HAAS_USER_ID_EMPTY);
        }

        try {
            // 验证分享关系是否已经存在
            List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(productKey, deviceName, authorizeeHaasUserId, environment);
            if (deviceShareDOList.isEmpty()) {
                errorLog.error("UshareDevice error: " + deviceShareDOList.toString());
                errorLog.error("UshareDevice error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
                return new IoTxResult<>(HaasIoTxCodes.ERROR_UNSHARE_NOT_EXIST);
            }

            // 验证撤销者是否为授权人或被授权人撤销自己权限
            if (StringUtils.equals(haasUserId, deviceShareDOList.get(0).getAuthorizerHaasUserId())
                    || StringUtils.equals(haasUserId, authorizeeHaasUserId)) {
                deviceShareDAO.removeDeviceShareInfo(productKey, deviceName, authorizeeHaasUserId, environment);
            } else {
                // 非授权者或被授权者发出的撤销分享申请
                return new IoTxResult<>(IoTxCodes.REQUEST_FORBIDDEN);
            }
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }

        return new IoTxResult<>(IoTxCodes.SUCCESS);
    }

    @Override
    public IoTxResult<List<QueryDeviceShareInfoResultDTO>> getDeviceShareList(String haasUserId) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_GET_SHARE_HAAS_USER_ID_EMPTY);
        }

        if (userService.info(haasUserId).getCode() != IoTxCodes.SUCCESS.getCode()) {
            return new IoTxResult<>(IoTxCodes.REQUEST_AUTH_ERROR);
        }

        try {
            // 查询分享关系是否已经存在
            List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithAuthorizer(haasUserId, environment);

            List<QueryDeviceShareInfoResultDTO> deviceShareInfoResultDTOList  = new ArrayList<>();

            deviceShareDOList.forEach(item -> {
                QueryDeviceShareInfoResultDTO deviceShareInfoResultDTO = new QueryDeviceShareInfoResultDTO();
                BeanUtils.copyProperties(item, deviceShareInfoResultDTO);
                List<UserDO> userDOList = userDAO.findUserByHaasUserId(item.getAuthorizeeHaasUserId());
                if (userDOList != null && !userDOList.isEmpty() && StringUtils.isNotBlank(userDOList.get(0).getMobile())) {
                    StringBuffer authorizeeMobile = new StringBuffer(
                            cryptographUtils.decrypt(userDOList.get(0).getMobile()));
                    // 将第4为到第8位进行脱敏
                    authorizeeMobile.replace(6, 10, "****");
                    deviceShareInfoResultDTO.setAuthorizeeMobile(authorizeeMobile.toString());
                }
                deviceShareInfoResultDTOList.add(deviceShareInfoResultDTO);
            });

            return new IoTxResult<>(deviceShareInfoResultDTOList);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }
}

