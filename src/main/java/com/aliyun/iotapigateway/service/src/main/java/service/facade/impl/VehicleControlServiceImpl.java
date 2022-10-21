package com.aliyun.iotx.haas.tdserver.service.impl;

import com.alibaba.boot.hsf.annotation.HSFProvider;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceShareDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceShareDO;
import com.aliyun.iotx.haas.tdserver.dal.redis.CacheService;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.GpsUserIdBO;
import com.aliyun.iotx.haas.tdserver.facade.DeviceManageService;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceDetailDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceServiceResultDTO;
import com.aliyun.iotx.haas.tdserver.facade.vehicle.VehicleControlService;
import com.aliyun.iotx.haas.tdserver.sal.iot.IoTClient;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qianfan
 * @date 2020/08/26
 */
@HSFProvider(serviceInterface = VehicleControlService.class)
public class VehicleControlServiceImpl implements VehicleControlService {

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Autowired
    private IoTClient ioTClient;

    @Autowired
    private CacheService cacheService;

    @Resource
    private DeviceBindDAO deviceBindDAO;

    @Resource
    private DeviceShareDAO deviceShareDAO;

    private static final String VEHICLE_CONTROL_IDENTIFIER = "sync_cmd_service";

    private static final String VEHICLE_CONTROL_DOWN_CMD_GROUP = "down_cmd_group";
    private static final String VEHICLE_CONTROL_DOWN_CMD_OPCODE = "down_cmd_opcode";
    private static final String VEHICLE_CONTROL_DOWN_CMD_CLASS_ID   = "down_cmd_class_id";
    private static final String VEHICLE_CONTROL_DOWN_CMD_TRANSACTION_ID = "down_cmd_transaction_id";
    private static final String VEHICLE_CONTROL_DOWN_CMD_PARAMETERS = "down_cmd_parameters";

    private static final String VEHICLE_CONTROL_RESPONSE_UP_CMD = "up_cmd";
    private static final String VEHICLE_CONTROL_RESPONSE_UP_RESULT = "up_cmd_parameters";
    private static final String VEHICLE_CONTROL_RESPONSE_SUCCESS = "00";
    private static final String VEHICLE_CONTROL_RESPONSE_ERROR_0X01 = "01";
    private static final String VEHICLE_CONTROL_RESPONSE_ERROR_0X02 = "02";
    private static final String VEHICLE_CONTROL_RESPONSE_ERROR_RIDING = "01";
    private static final String VEHICLE_CONTROL_RESPONSE_ERROR_UNLOCK = "02";

    private static final int VEHICLE_TRANSACTION_ID_NOT_ONLINE = -1;
    private static final int VEHICLE_HEX_BYTE_LENGTH = 2;

    private static final int VEHICLE_CONTROL_DEFAULT_CMD_GROUP_HEX = 0x01;

    private static final int VEHICLE_CONTROL_DEFAULT_CLASS_ID_HEX = 0x02;

    private static final int VEHICLE_CONTROL_SINGLE_LOCK_RSSI_CMD_OPCODE_HEX = 0x0E;

    private static final int VEHICLE_CONTROL_ARMING_SOUND_CMD_OPCODE_HEX = 0x0C;

    private static final int VEHICLE_CONTROL_ARMING_AUTO_CMD_OPCODE_HEX = 0x0D;

    private static final int VEHICLE_CONTROL_WIRLESS_MODE_CMD_GROUP_HEX = 0x08;

    private static final int VEHICLE_CONTROL_WIRLESS_MODE_OPCODE_HEX = 0x0F;

    private static final int VEHICLE_CONTROL_PARAM_OPCODE_HEX = 0xFF;

    private static final Logger errorLog = LoggerFactory.getLogger("error");

    private String generateArgs(Integer group, Integer opcode, Integer classId, Integer transactionId, String params) {
        Map<String, Object> args = new HashMap<>();

        args.put(VEHICLE_CONTROL_DOWN_CMD_GROUP, group);
        args.put(VEHICLE_CONTROL_DOWN_CMD_OPCODE, opcode);
        args.put(VEHICLE_CONTROL_DOWN_CMD_CLASS_ID, classId);
        args.put(VEHICLE_CONTROL_DOWN_CMD_TRANSACTION_ID, transactionId);
        if (params != null) {
            args.put(VEHICLE_CONTROL_DOWN_CMD_PARAMETERS, params);
        }

        return JSON.toJSONString(args);
    }

    private Integer getTransactionId(String haasUserId, String productKey, String deviceName) {
        IoTxResult<DeviceDetailDTO> result = ioTClient.queryDeviceDetail(productKey, deviceName);

        if (!StringUtils.equals(result.getData().getStatus(), "ONLINE")) {
            errorLog.error("device not online , result code[" + result.getData().getStatus() + "]error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return VEHICLE_TRANSACTION_ID_NOT_ONLINE;
        }

        return cacheService.getCachedTransactionId(productKey, deviceName, result.getData().getUtcOnline());
    }

    private IoTxResult<Void> invokeDeviceSyncService(String productKey, String deviceName, String args) {
        IoTxResult<DeviceServiceResultDTO> serviceResponse =  ioTClient.invokeDeviceSyncService(productKey, deviceName, VEHICLE_CONTROL_IDENTIFIER, args);
        JSONObject json = new JSONObject();
        if (serviceResponse.getCode() == IoTxCodes.SUCCESS.getCode()) {
            DeviceServiceResultDTO deviceServiceResultDTO = serviceResponse.getData();
            json = JSONObject.parseObject(deviceServiceResultDTO.getResult());
            json = JSONObject.parseObject(json.getString(VEHICLE_CONTROL_RESPONSE_UP_CMD));
            if (StringUtils.equals(VEHICLE_CONTROL_RESPONSE_SUCCESS, json.getString(VEHICLE_CONTROL_RESPONSE_UP_RESULT))) {
                return new IoTxResult<>();
            }
            errorLog.error("invokeDeviceSyncService response : " + json.getString(VEHICLE_CONTROL_RESPONSE_UP_RESULT) + " args : " + args);
            if (StringUtils.equals(VEHICLE_CONTROL_RESPONSE_ERROR_0X01, json.getString(VEHICLE_CONTROL_RESPONSE_UP_RESULT))) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL);
            } else if (StringUtils.equals(VEHICLE_CONTROL_RESPONSE_ERROR_0X02, json.getString(VEHICLE_CONTROL_RESPONSE_UP_RESULT))) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL_0X02);
            }
        }

        return new IoTxResult<>(IoTxCodes.REQUEST_ERROR, json.getString(VEHICLE_CONTROL_RESPONSE_UP_RESULT));
    }

    private static String addZeroForNum(String str, int strLength) {
        int strlen = str.length();
        if (strlen < strLength) {
            while (strlen < strLength) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("0").append(str);
                str = stringBuffer.toString();
                strlen++;
            }
        }

        return str;
    }

    @Override
    public IoTxResult<Void> lockVehicle(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName, boolean isLockMobile, boolean isLockBox) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
            errorLog.error("lockVehicle error: " + deviceBindDOList.toString());
            errorLog.error("lockVehicle share error: " + deviceShareDOList.toString());
            errorLog.error("lockVehicle error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";

        if (isLockMobile) {
            parameters += "01";
        } else {
            parameters += "ff";
        }

        if (isLockBox) {
            parameters += "01";
        } else {
            parameters += "ff";
        }

        cacheService.deleteCachedGpsUserIdRequest(productKey, deviceName, System.currentTimeMillis());

        String args = generateArgs(1, 1, 2, transactionId, parameters);

        IoTxResult<Void> result = invokeDeviceSyncService(productKey, deviceName, args);
        if (!result.hasSucceeded()) {
            if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_RIDING);
            } else if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL_0X02.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_UNLOCK);
            } else if(IoTxCodes.REQUEST_ERROR.getCode() == result.getCode()){
                errorLog.error("unlockVehicle error, maybe request timeout: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
                return new IoTxResult<>(IoTxCodes.SUCCESS);
            }
        }

        return new IoTxResult<>(IoTxCodes.SUCCESS);

        //return result;
    }

    @Override
    public IoTxResult<Void> unlockVehicle(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName, boolean isunLockMobile, boolean isunLockBox) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
            errorLog.error("lockVehicle share error: " + deviceShareDOList.toString());
            errorLog.error("unlockVehicle error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";

        if (isunLockMobile) {
            parameters += "00";
        } else {
            parameters += "ff";
        }

        if (isunLockBox) {
            parameters += "00";
        } else {
            parameters += "ff";
        }

        GpsUserIdBO gpsUserIdBO = new GpsUserIdBO();
        gpsUserIdBO.setHaasUserId(haasUserId);
        gpsUserIdBO.setDeviceName(deviceName);
        gpsUserIdBO.setProductKey(productKey);
        gpsUserIdBO.setTimeStamp(System.currentTimeMillis());
        cacheService.setCachedGpsUserIdRequest(gpsUserIdBO);

        String args = generateArgs(1, 1, 2, transactionId, parameters);

        IoTxResult<Void> result = invokeDeviceSyncService(productKey, deviceName, args);
        if (!result.hasSucceeded()) {
            if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_RIDING);
            } else if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL_0X02.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_UNLOCK);
            } else if(IoTxCodes.REQUEST_ERROR.getCode() == result.getCode()){
                return new IoTxResult<>(IoTxCodes.SUCCESS);
            }
        }

        return result;
    }

    @Override
    public IoTxResult<Void> startVehicle(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
            errorLog.error("startVehicle error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "01";

        String args = generateArgs(1, 2, 2, transactionId, parameters);

        IoTxResult<Void> result = invokeDeviceSyncService(productKey, deviceName, args);
        if (!result.hasSucceeded()) {
            if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL);
            } else if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL_0X02.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_UNLOCK);
            } else if(IoTxCodes.REQUEST_ERROR.getCode() == result.getCode()){
                return new IoTxResult<>(IoTxCodes.SUCCESS);
            }
        }

        return result;
    }

    @Override
    public IoTxResult<Void> stopVehicle(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
            errorLog.error("stopVehicle error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "00";

        String args = generateArgs(1, 2, 2, transactionId, parameters);

        IoTxResult<Void> result = invokeDeviceSyncService(productKey, deviceName, args);
        if (!result.hasSucceeded()) {
            if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL);
            } else if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL_0X02.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_UNLOCK);
            } else if(IoTxCodes.REQUEST_ERROR.getCode() == result.getCode()){
                return new IoTxResult<>(IoTxCodes.SUCCESS);
            }
        }

        return result;
    }

    @Override
    public IoTxResult<Void> searchVehicle(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName, boolean isStartSearching, int timeout) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
            errorLog.error("searchVehicle error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        if (isStartSearching) {
            parameters += "01";
        } else {
            parameters += "00";
        }

        parameters += addZeroForNum(Integer.toHexString(timeout & 0xff), VEHICLE_HEX_BYTE_LENGTH);

        String args = generateArgs(1, 3, 2, transactionId, parameters);

        return invokeDeviceSyncService(productKey, deviceName, args);
    }

    @Override
    public IoTxResult<Void> setVehicleLockRssi(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName, int mode, int lockRssi, int unlockRssi, String osType, String imei) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
            errorLog.error("setVehicleLockRssi error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        parameters += addZeroForNum(Integer.toHexString(mode & 0xff), VEHICLE_HEX_BYTE_LENGTH);
        parameters += addZeroForNum(Integer.toHexString(unlockRssi & 0xff), VEHICLE_HEX_BYTE_LENGTH);
        parameters += addZeroForNum(Integer.toHexString(lockRssi & 0xff), VEHICLE_HEX_BYTE_LENGTH);

        switch (osType) {
            case "Android":
                parameters += "00";
                break;
            case "IOS":
                parameters += "01";
                break;
            default:
                parameters += "02";
                break;
        }

        parameters += imei;

        String args = generateArgs(1, 4, 2, transactionId, parameters);

        return invokeDeviceSyncService(productKey, deviceName, args);
    }

    @Override
    public IoTxResult<Void> calibrateVehicle(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName, boolean isStartSearching, boolean isStartCalibrating, int timeout) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
            errorLog.error("calibrateVehicle error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        if (isStartCalibrating) {
            parameters += "01";
        } else {
            parameters += "00";
        }

        parameters += addZeroForNum(Integer.toHexString(timeout & 0xff), VEHICLE_HEX_BYTE_LENGTH);

        String args = generateArgs(1, 5, 2, transactionId, parameters);

        return invokeDeviceSyncService(productKey, deviceName, args);
    }

    @Override
    public IoTxResult<Void> unbindVehicle(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName) {
        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String args = generateArgs(1, 6, 2, transactionId, null);

        return invokeDeviceSyncService(productKey, deviceName, args);
    }

    @Override
    public IoTxResult<Void> armingVehicle(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName, boolean isArming) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
            errorLog.error("armingVehicle error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        if (isArming) {
            parameters += "01";
        } else {
            parameters += "00";
        }
        String args = generateArgs(1, 7, 2, transactionId, parameters);

        IoTxResult<Void> result = invokeDeviceSyncService(productKey, deviceName, args);
        if (!result.hasSucceeded()) {
            if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL);
            } else if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL_0X02.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_UNLOCK);
            }else if(IoTxCodes.REQUEST_ERROR.getCode() == result.getCode()){
                return new IoTxResult<>(IoTxCodes.SUCCESS);
            }
        }

        return result;
    }

    @Override
    public IoTxResult<Void> armingVehicleV2(String haasUserId, String productKey, String deviceName, Integer armingArg) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
            errorLog.error("armingVehicle error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        if (armingArg == 1) {
            parameters += "01";
        } else if (armingArg == 2) {
            parameters += "02";
        } else {
            parameters += "00";
        }
        String args = generateArgs(1, 7, 2, transactionId, parameters);

        IoTxResult<Void> result = invokeDeviceSyncService(productKey, deviceName, args);
        if (!result.hasSucceeded()) {
            if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL);
            } else if (result.getCode() == HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_GENERAL_0X02.getCode()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_VEHICLE_CONTROL_DEVICE_UNLOCK);
            } else if(IoTxCodes.REQUEST_ERROR.getCode() == result.getCode()){
                return new IoTxResult<>(IoTxCodes.SUCCESS);
            }
        }

        return result;
    }

    @Override
    public IoTxResult<Void> setSingleLockRssiClose(String haasUserId, String productKey, String deviceName, int mode) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty()) {
            errorLog.error("setVehicleLockRssi error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        parameters += addZeroForNum(Integer.toHexString(mode & 0xff), VEHICLE_HEX_BYTE_LENGTH);

        String args = generateArgs(VEHICLE_CONTROL_DEFAULT_CMD_GROUP_HEX, VEHICLE_CONTROL_SINGLE_LOCK_RSSI_CMD_OPCODE_HEX,
                VEHICLE_CONTROL_DEFAULT_CLASS_ID_HEX, transactionId, parameters);

        return invokeDeviceSyncService(productKey, deviceName, args);
    }

    @Override
    public IoTxResult<Void> setArmingSound(String haasUserId, String productKey, String deviceName, int mode) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty()) {
            errorLog.error("setVehicleLockRssi error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        parameters += addZeroForNum(Integer.toHexString(mode & 0xff), VEHICLE_HEX_BYTE_LENGTH);

        String args = generateArgs(VEHICLE_CONTROL_DEFAULT_CMD_GROUP_HEX, VEHICLE_CONTROL_ARMING_SOUND_CMD_OPCODE_HEX,
                VEHICLE_CONTROL_DEFAULT_CLASS_ID_HEX, transactionId, parameters);

        return invokeDeviceSyncService(productKey, deviceName, args);
    }

    @Override
    public IoTxResult<Void> setAutoArming(String haasUserId, String productKey, String deviceName, int mode, int timeout) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty()) {
            errorLog.error("setVehicleLockRssi error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        parameters += addZeroForNum(Integer.toHexString(mode & 0xff), VEHICLE_HEX_BYTE_LENGTH);
        // 数据参数大于1字节，按小端转换
        // 取低8位
        parameters += addZeroForNum(Integer.toHexString(timeout & 0xff), VEHICLE_HEX_BYTE_LENGTH);
        // 取高8位
        parameters += addZeroForNum(Integer.toHexString((timeout >> 8) & 0xff), VEHICLE_HEX_BYTE_LENGTH);

        String args = generateArgs(VEHICLE_CONTROL_DEFAULT_CMD_GROUP_HEX, VEHICLE_CONTROL_ARMING_AUTO_CMD_OPCODE_HEX,
                VEHICLE_CONTROL_DEFAULT_CLASS_ID_HEX, transactionId, parameters);

        return invokeDeviceSyncService(productKey, deviceName, args);
    }

    @Override
    public IoTxResult<Void> setWirlessKey(String haasUserId, String productKey, String deviceName, int mode, int timeout) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty()) {
            errorLog.error("setAddWirlessKey error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_INVALID_DEVICE);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        parameters += addZeroForNum(Integer.toHexString(mode & 0xff), VEHICLE_HEX_BYTE_LENGTH);
        // 数据参数大于1字节，按小端转换
        // 取低8位
        parameters += addZeroForNum(Integer.toHexString(timeout & 0xff), VEHICLE_HEX_BYTE_LENGTH);
        // 取高8位
        parameters += addZeroForNum(Integer.toHexString((timeout >> 8) & 0xff), VEHICLE_HEX_BYTE_LENGTH);

        String args = generateArgs(VEHICLE_CONTROL_DEFAULT_CMD_GROUP_HEX, VEHICLE_CONTROL_WIRLESS_MODE_OPCODE_HEX,
                VEHICLE_CONTROL_DEFAULT_CLASS_ID_HEX, transactionId, parameters);

        return invokeDeviceSyncService(productKey, deviceName, args);
    }

    @Override
    public IoTxResult<Void> setWirlessMode(String haasUserId, String productKey, String deviceName, int mode, int timeout) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        if (deviceBindDOList.isEmpty()) {
            errorLog.error("setAddWirlessKey error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_INVALID_DEVICE);
        }

        Integer transactionId = getTransactionId(haasUserId, productKey, deviceName);
        if (transactionId == VEHICLE_TRANSACTION_ID_NOT_ONLINE) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_NOT_ONLINE);
        }

        String parameters = "";
        parameters += addZeroForNum(Integer.toHexString((0x060700 & 0xffffff) | (mode & 0xff)), 0x03);
        // 数据参数大于1字节，按小端转换
        // 取低8位
        parameters += addZeroForNum(Integer.toHexString(timeout & 0xff), VEHICLE_HEX_BYTE_LENGTH);
        // 取高8位
        parameters += addZeroForNum(Integer.toHexString((timeout >> 8) & 0xff), VEHICLE_HEX_BYTE_LENGTH);

        String args = generateArgs(VEHICLE_CONTROL_WIRLESS_MODE_CMD_GROUP_HEX, VEHICLE_CONTROL_PARAM_OPCODE_HEX,
                VEHICLE_CONTROL_DEFAULT_CLASS_ID_HEX, transactionId, parameters);

        return invokeDeviceSyncService(productKey, deviceName, args);
    }
}

