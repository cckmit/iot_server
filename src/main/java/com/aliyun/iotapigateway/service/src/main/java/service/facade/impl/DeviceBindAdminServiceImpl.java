package com.aliyun.iotx.haas.tdserver.service.impl;

import java.util.List;

import com.alibaba.boot.hsf.annotation.HSFProvider;

import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.exception.DeviceNotFoundException;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceShareDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.user.UserDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.user.UserDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import com.aliyun.iotx.haas.tdserver.facade.admin.DeviceBindAdminService;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceDetailDTO;
import com.aliyun.iotx.haas.tdserver.sal.iot.IoTClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;


@HSFProvider(serviceInterface = DeviceBindAdminService.class)
public class DeviceBindAdminServiceImpl implements DeviceBindAdminService {

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    private final DeviceBindDAO deviceBindDAO;
    private final DeviceShareDAO deviceShareDAO;
    private final DeviceBluetoothInfoServiceImpl deviceBluetoothInfoService;
    private final UserDAO userDAO;
    private final IoTClient ioTClient;

    @Autowired
    DeviceBindAdminServiceImpl(DeviceBindDAO deviceBindDAO, DeviceShareDAO deviceShareDAO, DeviceBluetoothInfoServiceImpl deviceBluetoothInfoService, UserDAO userDAO, IoTClient ioTClient) {
        this.deviceBindDAO = deviceBindDAO;
        this.deviceShareDAO = deviceShareDAO;
        this.deviceBluetoothInfoService = deviceBluetoothInfoService;
        this.userDAO = userDAO;
        this.ioTClient = ioTClient;
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
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> unbind(String productKey, String deviceName, String haasUserId) {
        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_DN_EMPTY);
        }

        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_USER_EMPTY);
        }

        try {
            List<UserDO> userDOList = userDAO.findUserByHaasUserId(haasUserId);
            if (userDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_INVALID_USER);
            }

            getDeviceSecret(productKey, deviceName);

            List<DeviceBindDO> deviceBindDOList =
                    deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);

            if (deviceBindDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_NOT_BIND);
            }

            deviceBindDAO.removeBinding(productKey, deviceName, environment);

            // 删除设备蓝牙信息相关数据
            deviceBluetoothInfoService.removeBluetoothDeviceIdWithoutHaasUserId(productKey, deviceName);

            // 删除所有分享车辆
            deviceShareDAO.removeAllSharedDeviceByUser(productKey, deviceName, haasUserId, environment);

            return new IoTxResult<>(IoTxCodes.SUCCESS,
                    "[ADMIN] 1 device unbind success.",
                    "[ADMIN]成功解绑 1 个设备。");

        } catch (DataAccessException e) {
            e.printStackTrace();
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        } catch (DeviceNotFoundException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_INVALID_DEVICE);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> unbindAllDevicesForUser(String haasUserId) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_USER_EMPTY);
        }

        List<UserDO> userDOList = userDAO.findUserByHaasUserId(haasUserId);
        if (userDOList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_INVALID_USER);
        }

        List<DeviceBindDO> deviceBindDOList =
                deviceBindDAO.getBindingForUser(haasUserId, environment);
        if (deviceBindDOList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_CLEAN_USER);
        }

        for (DeviceBindDO deviceBindDO : deviceBindDOList) {
            deviceBindDAO.removeBinding(
                    deviceBindDO.getProductKey(), deviceBindDO.getDeviceName(), environment);

            // 删除设备蓝牙信息相关数据
            deviceBluetoothInfoService.removeBluetoothDeviceIdWithoutHaasUserId(
                    deviceBindDO.getProductKey(), deviceBindDO.getDeviceName());

            // 删除设备所有分享关系
            deviceShareDAO.removeAllSharedDevice(deviceBindDO.getProductKey(), deviceBindDO.getDeviceName(), environment);
        }

        return new IoTxResult<>(IoTxCodes.SUCCESS,
                "[ADMIN] " + deviceBindDOList.size() + " device(s) unbind success.",
                "[ADMIN] 成功解绑 " + deviceBindDOList.size() + " 个设备。");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> unbindDevice(String productKey, String deviceName) {
        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_DN_EMPTY);
        }

        try {
            getDeviceSecret(productKey, deviceName);

            List<DeviceBindDO> deviceBindDOList =
                    deviceBindDAO.getBindingForDevice(productKey, deviceName, environment);

            if (deviceBindDOList.isEmpty()) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_CLEAN_DEVICE);
            }

            deviceBindDAO.removeBinding(productKey, deviceName, environment);

            // 删除设备蓝牙信息相关数据
            deviceBluetoothInfoService.removeBluetoothDeviceIdWithoutHaasUserId(productKey, deviceName);

            // 删除设备所有分享关系
            deviceShareDAO.removeAllSharedDevice(productKey, deviceName, environment);

            return new IoTxResult<>(IoTxCodes.SUCCESS,
                    "[ADMIN] 1 device unbind success.",
                    "[ADMIN] 成功解绑 1 个设备。");
        } catch (DataAccessException e) {
            e.printStackTrace();
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        } catch (DeviceNotFoundException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_UNBIND_ADMIN_INVALID_DEVICE);
        }

    }
}

