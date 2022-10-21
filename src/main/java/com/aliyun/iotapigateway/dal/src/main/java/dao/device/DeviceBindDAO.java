package com.aliyun.iotx.haas.tdserver.dal.dao.device;

import java.util.List;

import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import org.apache.ibatis.annotations.Param;

public interface DeviceBindDAO {

    Long addBinding(DeviceBindDO deviceBindDO);

    List<DeviceBindDO> getDevicesByUserId(
        @Param("haasUserId") String haasUserId,
        @Param("environment") String environment);

    List<String> getUserIdByDevice(
        @Param("productKey") String productKey,
        @Param("deviceName") String deviceName,
        @Param("environment") String environment);

    List<DeviceBindDO> getBinding(
        @Param("productKey") String productKey,
        @Param("deviceName") String deviceName,
        @Param("haasUserId") String haasUserId,
        @Param("environment") String environment);

    List<DeviceBindDO> getBindingForDevice(
        @Param("productKey") String productKey,
        @Param("deviceName") String deviceName,
        @Param("environment") String environment);

    List<DeviceBindDO> getBindingForUser(
        @Param("haasUserId") String haasUserId,
        @Param("environment") String environment);

    List<DeviceBindDO> getAllBoundDevice(
            @Param("start") Integer start,
            @Param("pageSize") Integer pageSize,
            @Param("environment") String environment);

    Long getAllBoundDeviceCount(@Param("environment") String environment);

    Long removeBinding(
        @Param("productKey") String productKey,
        @Param("deviceName") String deviceName,
        @Param("environment") String environment);

    List<DeviceBindDO> getDefaultDeviceByUser(
        @Param("haasUserId") String haasUserId,
        @Param("environment") String environment);

    Long setDefaultDevice(
        @Param("haasUserId") String haasUserId,
        @Param("productKey") String productKey,
        @Param("deviceName") String deviceName,
        @Param("environment") String environment);

    Long removeDefaultDevice(
        @Param("haasUserId") String haasUserId,
        @Param("productKey") String productKey,
        @Param("deviceName") String deviceName,
        @Param("environment") String environment);
}

