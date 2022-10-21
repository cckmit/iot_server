package com.aliyun.iotx.haas.tdserver.dal.dao.device;

import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBluetoothInfoDO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author imost.lwf
 * @date 2020/11/10
 */
public interface DeviceBluetoothInfoDAO {
    /**
     * 插入新的蓝牙设备信息
     * @return
     */
    Long insert(DeviceBluetoothInfoDO deviceBluetoothInfoDO);

    /**
     * 更新蓝牙设备信息
     * @return
     */
    Long updateDeviceBluetoothInfo(DeviceBluetoothInfoDO deviceBluetoothInfoDO);

    /**
     * 根据haasUserId、PK、DN查询授权分享信息
     * @param haasUserId
     * @param productKey
     * @param deviceName
     * @param environment
     * @return
     */
    List<DeviceBluetoothInfoDO> getDeviceBluetoothInfo(@Param("haasUserId") String haasUserId,
                                                       @Param("productKey") String productKey,
                                                       @Param("deviceName") String deviceName,
                                                       @Param("environment") String environment);

    /**
     * 根据PK、DN查询授权分享信息
     * @param productKey
     * @param deviceName
     * @param environment
     * @return
     */
    List<DeviceBluetoothInfoDO> getDeviceBluetoothInfoWithoutUserID(@Param("productKey") String productKey,
                                                                    @Param("deviceName") String deviceName,
                                                                    @Param("environment") String environment);

    /**
     * 根据haasUserId、PK、DN删除蓝牙设备信息
     * @param haasUserId
     * @param productKey
     * @param deviceName
     * @param environment
     * @return
     */
    Long removeDeviceBluetoothInfo(@Param("haasUserId") String haasUserId,
                                   @Param("productKey") String productKey,
                                   @Param("deviceName") String deviceName,
                                   @Param("environment") String environment);

    /**
     * 根据PK、DN删除蓝牙设备信息
     * @param productKey
     * @param deviceName
     * @param environment
     * @return
     */
    Long removeDeviceBluetoothInfoWithoutUserID(@Param("productKey") String productKey,
                                                @Param("deviceName") String deviceName,
                                                @Param("environment") String environment);
}

