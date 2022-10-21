package com.aliyun.iotx.haas.tdserver.dal.dao.device;

import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author imost.lwf
 * @date 2021/04/13
 */
public interface DeviceDAO {
    /**
     * 插入新的设备信息
     *
     * @param deviceDO
     * @return
     */
    Long insert(DeviceDO deviceDO);

    /**
     * 批量插入新的产品记录
     *
     * @param deviceDOList
     * @return
     */
    Long batchInsert(List<DeviceDO> deviceDOList);

    /**
     * 更新设备信息
     *
     * @param deviceDO
     * @return
     */
    Long updateDeviceInfo(DeviceDO deviceDO);

    /**
     * 查询设备
     *
     * @param odmTenantId
     * @param productKey
     * @param deviceName
     * @param environment
     * @return
     */
    DeviceDO getDeviceWithDeviceInfo(@Param("odmTenantId") String odmTenantId,
                                     @Param("productKey") String productKey,
                                     @Param("deviceName") String deviceName,
                                     @Param("environment") String environment);

    /**
     * 查询设备
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param deviceName
     * @param environment
     * @return
     */
    DeviceDO getDeviceWithProductIdAndDeviceInfo(@Param("odmTenantId") String odmTenantId,
                                                 @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                                 @Param("deviceName") String deviceName,
                                                 @Param("environment") String environment);

    /**
     * 查询设备
     *
     * @param productKey
     * @param deviceName
     * @param environment
     * @return
     */
    DeviceDO getDeviceWithProductKeyAndDeviceName(@Param("productKey") String productKey,
                                                  @Param("deviceName") String deviceName,
                                                  @Param("environment") String environment);

    /**
     * 查询设备
     *
     * @param deviceName
     * @return
     */
    DeviceDO getDeviceWithDeviceName(@Param("deviceName") String deviceName);


    /**
     * 查询设备
     *
     * @param odmTenantId
     * @param productKey
     * @param environment
     * @return
     */
    DeviceDO getDeviceWithOdmIdAndProductKey(@Param("odmTenantId") String odmTenantId,
                                             @Param("productKey") String productKey,
                                             @Param("environment") String environment);

    /**
     * 查询设备
     *
     * @param productKey
     * @param environment
     * @return
     */
    List<DeviceDO> getDeviceWithProductKey(@Param("productKey") String productKey,
                                     @Param("environment") String environment);

    /**
     * 查询设备
     *
     * @param odmTenantId
     * @param environment
     * @return
     */
    DeviceDO getDeviceWithOdmId(@Param("odmTenantId") String odmTenantId,
                                @Param("environment") String environment);


    /**
     * 查询设备
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param nameLike
     * @param start
     * @param pageSize
     * @param environment
     * @return
     */
    List<DeviceDO> getDeviceList(@Param("odmTenantId") String odmTenantId,
                                 @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                 @Param("nameLike") String nameLike,
                                 @Param("start") Integer start,
                                 @Param("pageSize") Integer pageSize,
                                 @Param("environment") String environment);

    /**
     * 查询设备
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param nameLike
     * @param environment
     * @return
     */
    Long getDeviceCount(@Param("odmTenantId") String odmTenantId,
                        @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                        @Param("nameLike") String nameLike,
                        @Param("environment") String environment);

    /**
     * 查询绑定设备
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param nameLike
     * @param environment
     * @return
     */
    Long getBoundDeviceCount(@Param("odmTenantId") String odmTenantId,
                             @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                             @Param("nameLike") String nameLike,
                             @Param("environment") String environment);

    /**
     * 迁移设备
     *
     * @param odmTenantId
     * @param toOdmTenantId
     * @param uniqueTdserverProductName
     * @param environment
     * @return
     */
    Long migrateDevice(@Param("odmTenantId") String odmTenantId,
                       @Param("toOdmTenantId") String toOdmTenantId,
                       @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                       @Param("environment") String environment);
     /* 迁移设备信息
     *
     * @param odmTenantId
     * @param productKey
     * @param deviceName
     * @param uniqueTdserverProductName
     * @param uniqueTdModuleName
     * @param environment
     * @return
     */
    Long  migrateDeviceInfo (@Param("odmTenantId") String odmTenantId,
                                           @Param("productKey") String productKey,
                                           @Param("deviceName") String deviceName,
                                           @Param("newOdmTenantId") String newOdmTenantId,
                                           @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                           @Param("uniqueModuleName") String uniqueModuleName,
                                           @Param("environment") String environment);
}

