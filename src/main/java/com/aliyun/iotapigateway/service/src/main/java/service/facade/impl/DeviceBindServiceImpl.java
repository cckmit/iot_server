package com.aliyun.iotx.haas.tdserver.service.impl;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.alibaba.boot.hsf.annotation.HSFProvider;

import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.exception.DeviceNotFoundException;
import com.aliyun.iotx.haas.tdserver.common.exception.HaasServerInternalException;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.common.utils.AuthTool;
import com.aliyun.iotx.haas.tdserver.common.utils.DateUtils;
import com.aliyun.iotx.haas.tdserver.common.utils.HexStrTool;
import com.aliyun.iotx.haas.tdserver.common.utils.RandKeyTool;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceShareDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ProductInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.user.UserDO;
import com.aliyun.iotx.haas.tdserver.dal.redis.CacheService;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.BindRequestBO;
import com.aliyun.iotx.haas.tdserver.facade.DeviceBindService;
import com.aliyun.iotx.haas.tdserver.facade.UserService;
import com.aliyun.iotx.haas.tdserver.facade.dto.bind.DeviceBindDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceAuthDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceDetailDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.user.UserInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.vehicle.VehicleControlService;
import com.aliyun.iotx.haas.tdserver.sal.iot.IoTClient;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.bo.LvyuanUserInfoBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static com.aliyun.iotx.haas.tdserver.service.impl.OdmDeviceManageServiceImpl.DEVICE_BIND_VALUE;
import static com.aliyun.iotx.haas.tdserver.service.impl.OdmDeviceManageServiceImpl.DEVICE_NOT_BIND_VALUE;

@HSFProvider(serviceInterface = DeviceBindService.class)
public class DeviceBindServiceImpl implements DeviceBindService {

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    private static final Logger errorLog = LoggerFactory.getLogger("error");

    //设备绑定
    private static final Integer USER_STATE_UNBIND = 0;
    private static final Integer USER_STATE_AUTH = 1;
    private static final Integer USER_STATE_BIND = 2;


    private final UserService userService;
    private final DeviceBindDAO deviceBindDAO;
    private final DeviceShareDAO deviceShareDAO;
    private final DeviceBluetoothInfoServiceImpl deviceBluetoothInfoService;
    private final CacheService cacheService;
    private final IoTClient ioTClient;
    private final CryptographUtils cryptographUtils;

    @Resource
    DeviceDAO deviceDAO;

    @Autowired
    private VehicleControlService vehicleControlService;

    @Autowired
    DeviceBindServiceImpl(
            UserService userService,
            DeviceBindDAO deviceBindDAO,
            DeviceShareDAO deviceShareDAO, DeviceBluetoothInfoServiceImpl deviceBluetoothInfoService,
            CacheService cacheService,
            IoTClient ioTClient,
            CryptographUtils cryptographUtils) {
        this.userService = userService;
        this.deviceBindDAO = deviceBindDAO;
        this.deviceShareDAO = deviceShareDAO;
        this.deviceBluetoothInfoService = deviceBluetoothInfoService;
        this.cacheService = cacheService;
        this.ioTClient = ioTClient;
        this.cryptographUtils = cryptographUtils;
    }

    private String getDeviceSecret(
            String productKey,
            String deviceName) throws DeviceNotFoundException {

        IoTxResult<DeviceDetailDTO> deviceDetailDTOIoTxResult =
                ioTClient.queryDeviceDetail(productKey, deviceName);
        if (!deviceDetailDTOIoTxResult.hasSucceeded()) {
            throw new DeviceNotFoundException("PK/DN not exist in LP");
        }
        String deviceSecret = deviceDetailDTOIoTxResult.getData().getDeviceSecret();
        if (deviceSecret == null) {
            throw new DeviceNotFoundException("PK/DN not exist in LP");
        }
        return deviceSecret;
    }

    @Override
    public IoTxResult<DeviceAuthDTO> auth(
            String haasUserId,
            String productKey,
            String deviceName,
            String checkCode) {

        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_AUTH_HAAS_USER_ID_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_AUTH_PK_EMPTY);
        }
        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_AUTH_DN_EMPTY);
        }

//        if (checkCode == null || checkCode.isEmpty()) {
//            return new IoTxResult<>(HaasIoTxCodes.ERROR_CHECK_CODE_EMPTY);
//        }

        try {

            // 获取DS，同时对错误PK/DN触发DeviceNotFound异常捕获
            byte[] hexDs = HexStrTool.hexStrToByteArray(
                    getDeviceSecret(productKey, deviceName));

            // 校验码对比
            if (checkCode != null && !checkCode.isEmpty()) {
                if (!AuthTool.verifyCheckCode(productKey, deviceName, hexDs, checkCode)) {
                    return new IoTxResult<>(HaasIoTxCodes.ERROR_INVALID_CHECK_CODE);
                }
            }

            // 用户校验
            IoTxResult<UserInfoDTO> userInfoDTOIoTxResult = userService.info(haasUserId);
            if (userInfoDTOIoTxResult.getCode() != IoTxCodes.SUCCESS.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_AUTH_INVALID_USER);
            }

            // 查绑定表，看设备是否已被绑定
            List<String> userIds = deviceBindDAO.getUserIdByDevice(
                    productKey, deviceName, environment);

            if(userIds.contains(haasUserId)){
                return new IoTxResult<>(HaasIoTxCodes.ERROR_AUTH_DEVICE_ALREADY_BOUND_CURRENT_USER);
            }

            // 区分策略配置处理：
            // TODO: 当前是全局策略，后续应允许按机型配置策略
            if (false) {
                // 如果允许抢占绑定
                if (userIds.size() > 0) {
                    // 删除当前绑定信息
                    deviceBindDAO.removeBinding(productKey, deviceName, environment);
                }
            } else {
                // 若不允许抢占绑定
                if (userIds.size() > 0) {
                    // 设备已被其他人绑定，返回错误
                    return new IoTxResult<>(HaasIoTxCodes.ERROR_AUTH_DEVICE_ALREADY_BOUND);
                }
            }


            // 执行到这里，则通过了设备和用户验证流程，进入绑定信息生成阶段
            // 首先，尝试清除上一次请求的上下文
            cacheService.deleteCachedBindRequest(productKey, deviceName);

            // 创建新的缓存对象
            BindRequestBO bindRequestBO = new BindRequestBO();
            bindRequestBO.setProductKey(productKey);
            bindRequestBO.setDeviceName(deviceName);
            bindRequestBO.setHaasUserId(haasUserId);

            // 生成R1
            byte[] hexR1 = RandKeyTool.genHexRand();
            String r1 = HexStrTool.byteArrayToHexStr(hexR1);
            // 生成RequestId
            String requestId = AuthTool.genRequestId(haasUserId, productKey, deviceName, r1);

            // 缓存对象写入R1，对象准备完毕
            bindRequestBO.setR1(r1);

            // 以下开始构造接口响应对象
            DeviceAuthDTO deviceAuthDTO = new DeviceAuthDTO();

            // 计算R1签
            String signedR1 = HexStrTool.byteArrayToHexStr(RandKeyTool.signHexRand(hexR1, hexDs));

            // 数据写入响应对象
            deviceAuthDTO.setRequestId(requestId);
            deviceAuthDTO.setR1(r1);
            deviceAuthDTO.setSign(signedR1);

            // sync user info to manufature
            userService.syncUserInfo(haasUserId, productKey, deviceName, USER_STATE_AUTH);

            // 数据全部就绪
            // 写入缓存：
            cacheService.setCachedBindRequest(requestId, bindRequestBO);
            // 返回响应对象：
            return new IoTxResult<>(IoTxCodes.SUCCESS, deviceAuthDTO);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        } catch (NumberFormatException | NoSuchAlgorithmException | HaasServerInternalException e) {
            return new IoTxResult<>(IoTxCodes.SERVER_ERROR);
        } catch (DeviceNotFoundException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_AUTH_DEVICE_NOT_FOUND);
        }
    }

    @Override
    public IoTxResult<DeviceBindDTO> bind(
            String requestId,
            String r2,
            String sign) {

        if (requestId == null || requestId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_BIND_REQUEST_ID_EMPTY);
        }
        if (r2 == null || r2.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_BIND_R2_EMPTY);
        }
        if (sign == null || sign.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_BIND_SIGN_EMPTY);
        }

        // 获取绑定上下文缓存
        BindRequestBO bindRequestBO = cacheService.getCachedBindRequest(requestId);

        if (bindRequestBO == null) {
            // 没有找到上下文，这是一次非法请求
            return new IoTxResult<>(HaasIoTxCodes.ERROR_BIND_UNAUTHORIZED_REQUEST);
        }

        // 清掉缓存，bind只给一次机会
        cacheService.deleteCachedBindRequest(
                bindRequestBO.getProductKey(), bindRequestBO.getDeviceName());

        // 判断R2有没有作弊直接使用R1
        if (bindRequestBO.getR1().equals(r2)) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_BIND_R2_INVALID);
        }

        try {
            // 创建绑定数据对象
            DeviceBindDO deviceBindDO = new DeviceBindDO();
            // 写入缓存的信息
            deviceBindDO.setHaasUserId(bindRequestBO.getHaasUserId());
            deviceBindDO.setProductKey(bindRequestBO.getProductKey());
            deviceBindDO.setDeviceName(bindRequestBO.getDeviceName());
            // 写入当前环境
            deviceBindDO.setEnvironment(environment);

            // 设备表中改变设备绑定状态
            DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(
                    deviceBindDO.getProductKey(), deviceBindDO.getDeviceName(), environment);
            if (deviceDO != null) {
                deviceDO.setIsBound(DEVICE_BIND_VALUE);
                deviceDO.setGmtBound(new Date());
                deviceDAO.updateDeviceInfo(deviceDO);
                userService.syncBindStateInfo(deviceBindDO.getHaasUserId(), deviceBindDO.getProductKey(), deviceBindDO.getDeviceName(), USER_STATE_BIND);

            } else {
                errorLog.error("Device is not found when bind, pk:{}, dn:{}", deviceBindDO.getProductKey(), deviceBindDO.getDeviceName());
                return new IoTxResult<>(HaasIoTxCodes.ERROR_BIND_DEVICE_NOT_FOUND,"UserId:" + deviceBindDO.getHaasUserId() + "Produckey :" + deviceBindDO.getProductKey() + "DeviceName :" + deviceBindDO.getDeviceName());
            }

            // 验证R2
            byte[] hexR2 = HexStrTool.hexStrToByteArray(r2);
            byte[] hexSignedR2 = HexStrTool.hexStrToByteArray(sign);
            byte[] hexDs = HexStrTool.hexStrToByteArray(getDeviceSecret(
                    bindRequestBO.getProductKey(),
                    bindRequestBO.getDeviceName()));
            if (!Arrays.equals(hexSignedR2, RandKeyTool.signHexRand(hexR2, hexDs))) {
                // R2验证失败
                return new IoTxResult<>(HaasIoTxCodes.ERROR_BIND_DEVICE_VERIFY);
            }

            // 设备合法，为其生成K1K2
            // 生成完整key
            byte[] hexR1 = HexStrTool.hexStrToByteArray(bindRequestBO.getR1());
            String key = HexStrTool.byteArrayToHexStr(RandKeyTool.genHexKey(hexR1, hexR2, hexDs));
            // 拆解
            String k1 = key.substring(0, key.length() / 2);

            // 落库时需要加密K1K2
            String encK1 = cryptographUtils.encrypt(k1);
            deviceBindDO.setK1(encK1);
            deviceBindDO.setK2(cryptographUtils.encrypt(key.substring(key.length() / 2)));
            // 绑定信息落库
            deviceBindDAO.addBinding(deviceBindDO);

            // 创建响应对象
            DeviceBindDTO deviceBindDTO = new DeviceBindDTO();
            // 写入原始K1和时间戳
            deviceBindDTO.setK1(k1);
            deviceBindDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));

            return new IoTxResult<>(IoTxCodes.SUCCESS, deviceBindDTO);
        } catch (DeviceNotFoundException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_AUTH_DEVICE_NOT_FOUND);
        } catch (HaasServerInternalException e) {
            return new IoTxResult<>(IoTxCodes.SERVER_ERROR);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> unbind(
            String productKey,
            String deviceName,
            String haasUserId,
            String k1) {

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_DN_EMPTY);
        }

        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_USER_EMPTY);
        }

        if (k1 == null || k1.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_K1_EMPTY);
        }

        try {
            // 验设备
            getDeviceSecret(productKey, deviceName);

            // 验人
            IoTxResult<UserInfoDTO> userInfoDTOIoTxResult = userService.info(haasUserId);
            if (userInfoDTOIoTxResult.getCode() != IoTxCodes.SUCCESS.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_INVALID_USER);
            }

            // 验设备有效
            IoTxResult<DeviceDetailDTO> deviceDetailDTOIoTxResult =
                    ioTClient.queryDeviceDetail(productKey, deviceName);
            if (deviceDetailDTOIoTxResult.getCode() != IoTxCodes.SUCCESS.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_INVALID_DEVICE);
            }

            //验绑定关系
            List<DeviceBindDO> deviceBindDoList = deviceBindDAO.getBinding(
                    productKey, deviceName, haasUserId, environment);
            if (deviceBindDoList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_NOT_BIND);
            }

            // 验K1
            DeviceBindDO deviceBindDo = deviceBindDoList.get(0);
            String boundK1 = cryptographUtils.decrypt(deviceBindDo.getK1());
            if (!boundK1.equals(k1)) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_DEVICE_VERIFY);
            }

            // 做解绑
            deviceBindDAO.removeBinding(productKey, deviceName, environment);

            // 设备表中改变设备绑定状态
            DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(productKey, deviceName, environment);
            if (deviceDO != null) {
                deviceDO.setIsBound(DEVICE_NOT_BIND_VALUE);
                deviceDO.setGmtBound(new Date());
                deviceDAO.updateDeviceInfo(deviceDO);
                userService.syncBindStateInfo(haasUserId, productKey, deviceName,USER_STATE_UNBIND);
            } else {
                errorLog.error("Device is not found when unbind, pk:{}, dn:{}", productKey, deviceName);
            }

            // 删除所有分享车辆
            deviceShareDAO.removeAllSharedDeviceByUser(productKey, deviceName, haasUserId, environment);

            // 删除设备蓝牙信息相关数据
            deviceBluetoothInfoService.removeBluetoothDeviceIdWithoutHaasUserId(productKey, deviceName);

            // 下发设备解绑命令
            vehicleControlService.unbindVehicle(haasUserId, productKey, deviceName);

            return new IoTxResult<>(IoTxCodes.SUCCESS);

        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        } catch (DeviceNotFoundException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_AUTH_DEVICE_NOT_FOUND);
        }
    }
}

