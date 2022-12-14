package com.aliyun.iotx.haas.tdserver.service.impl;

import com.alibaba.boot.hsf.annotation.HSFProvider;

import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBluetoothInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceShareDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBluetoothInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceShareDO;
import com.aliyun.iotx.haas.tdserver.facade.DeviceBluetoothInfoService;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceBluetoothInfoDTO;
import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author imost.lwf
 * @date 2020/11/10
 */
@HSFProvider(serviceInterface = DeviceBluetoothInfoService.class)
public class DeviceBluetoothInfoServiceImpl implements DeviceBluetoothInfoService {
    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Resource
    private DeviceBindDAO deviceBindDAO;

    @Resource
    private DeviceShareDAO deviceShareDAO;

    @Resource
    private DeviceBluetoothInfoDAO deviceBluetoothInfoDAO;

    private static final Logger errorLog = LoggerFactory.getLogger("error");

    public IoTxResult<String> removeBluetoothDeviceIdWithoutHaasUserId(String productKey, String deviceName) {
        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_BLUETOOTH_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_BLUETOOTH_DN_EMPTY);
        }

        try {
            //???????????????????????????????????????
            deviceBluetoothInfoDAO.removeDeviceBluetoothInfoWithoutUserID(productKey, deviceName, environment);

            return new IoTxResult<>(IoTxCodes.SUCCESS);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    public IoTxResult<String> removeBluetoothDeviceId(String haasUserId, String productKey, String deviceName) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_BLUETOOTH_DN_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_BLUETOOTH_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_BLUETOOTH_DN_EMPTY);
        }

        try {
            //haasUserId???????????????????????????
            deviceBluetoothInfoDAO.removeDeviceBluetoothInfo(haasUserId, productKey, deviceName, environment);

            return new IoTxResult<>(IoTxCodes.SUCCESS);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<String> setBluetoothDeviceId(String haasUserId, String productKey, String deviceName, String bluetoothDeviceIdV1, String bluetoothDeviceIdV2) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_BLUETOOTH_DN_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_BLUETOOTH_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_BLUETOOTH_DN_EMPTY);
        }

        if (bluetoothDeviceIdV1 == null && bluetoothDeviceIdV2 == null) {
            return new IoTxResult<>(IoTxCodes.SUCCESS);
        }

        try {
            // ??????????????????
            // ???????????????????????????
            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
            if (deviceBindDOList.isEmpty()) {
                errorLog.error("setBluetoothDeviceId error: pk - " + productKey + " dn - " + deviceName + " haasUserId - " + haasUserId +" env - " + environment);
                return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_BLUETOOTH_UNAUTHORIZED);
            }

            // TODO:?????????????????????????????????????????????????????????????????????

            DeviceBluetoothInfoDO deviceBluetoothInfoDO = new DeviceBluetoothInfoDO();
            deviceBluetoothInfoDO.setHaasUserId(haasUserId);
            deviceBluetoothInfoDO.setProductKey(productKey);
            deviceBluetoothInfoDO.setDeviceName(deviceName);
            if (bluetoothDeviceIdV1 != null) {
                deviceBluetoothInfoDO.setBluetoothDeviceIdV1(bluetoothDeviceIdV1);
            }
            if (bluetoothDeviceIdV2 != null) {
                deviceBluetoothInfoDO.setBluetoothDeviceIdV2(bluetoothDeviceIdV2);
            }

            deviceBluetoothInfoDO.setEnvironment(environment);

            //?????????????????????????????????????????????????????????????????????????????????????????????
            List<DeviceBluetoothInfoDO> deviceBluetoothInfoDOList = deviceBluetoothInfoDAO.getDeviceBluetoothInfo(haasUserId, productKey, deviceName, environment);
            if (deviceBluetoothInfoDOList != null && !deviceBluetoothInfoDOList.isEmpty()) {
                deviceBluetoothInfoDO.setId(deviceBluetoothInfoDOList.get(0).getId());
                if (bluetoothDeviceIdV1 == null) {
                    deviceBluetoothInfoDO.setBluetoothDeviceIdV1(deviceBluetoothInfoDOList.get(0).getBluetoothDeviceIdV1());
                }
                if (bluetoothDeviceIdV2 == null) {
                    deviceBluetoothInfoDO.setBluetoothDeviceIdV2(deviceBluetoothInfoDOList.get(0).getBluetoothDeviceIdV2());
                }
                deviceBluetoothInfoDAO.updateDeviceBluetoothInfo(deviceBluetoothInfoDO);
            } else {
                deviceBluetoothInfoDAO.insert(deviceBluetoothInfoDO);
            }

            return new IoTxResult<>(IoTxCodes.SUCCESS);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<DeviceBluetoothInfoDTO> getBluetoothDeviceId(String haasUserId, String productKey, String deviceName) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_GET_DEVICE_BLUETOOTH_DN_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_GET_DEVICE_BLUETOOTH_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_GET_DEVICE_BLUETOOTH_DN_EMPTY);
        }

        try {
            // ??????????????????
            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
            List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(productKey,
                    deviceName, haasUserId, environment);
            if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
                errorLog.error("getBluetoothDeviceId error: " + deviceBindDOList.toString());
                errorLog.error("getBluetoothDeviceId error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
                return new IoTxResult<>(HaasIoTxCodes.ERROR_GET_DEVICE_BLUETOOTH_UNAUTHORIZED);
            }

            // TODO:??????????????????????????????????????????????????????????????????
            // ???????????????????????????????????????????????????????????????BluetoothDeviceIdV1???BluetoothDeviceIdV2

            DeviceBluetoothInfoDTO deviceBluetoothInfoDTO = new DeviceBluetoothInfoDTO();
            deviceBluetoothInfoDTO.setHaasUserId(haasUserId);
            deviceBluetoothInfoDTO.setBluetoothDeviceIdV1(null);
            deviceBluetoothInfoDTO.setBluetoothDeviceIdV2(null);

            //??????????????????????????????????????????????????????BluetoothDeviceIdV1???BluetoothDeviceIdV2??????null
            List<DeviceBluetoothInfoDO> deviceBluetoothInfoDOList = deviceBluetoothInfoDAO.getDeviceBluetoothInfo(haasUserId, productKey, deviceName, environment);
            if (deviceBluetoothInfoDOList != null && !deviceBluetoothInfoDOList.isEmpty()) {
                String bluetoothDeviceIdV1 = deviceBluetoothInfoDOList.get(0).getBluetoothDeviceIdV1();
                String bluetoothDeviceIdV2 = deviceBluetoothInfoDOList.get(0).getBluetoothDeviceIdV2();
                if (bluetoothDeviceIdV1 != null) {
                    deviceBluetoothInfoDTO.setBluetoothDeviceIdV1(bluetoothDeviceIdV1);
                }
                if (bluetoothDeviceIdV2 != null) {
                    deviceBluetoothInfoDTO.setBluetoothDeviceIdV2(bluetoothDeviceIdV2);
                }
            }

            return new IoTxResult<>(deviceBluetoothInfoDTO);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }
}

