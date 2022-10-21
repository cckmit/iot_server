package com.aliyun.iotx.haas.tdserver.dal.dao.device;

import java.util.List;

import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceShareDO;

import org.apache.ibatis.annotations.Param;

/**
 * @author imost.lwf
 * @date 2020/10/28
 */

public interface DeviceShareDAO {
    /**
     * 插入新的分享信息
     * @return
     */
    Long insert(DeviceShareDO deviceShareDO);

    /**
     * 更新授权分享
     * @return
     */
    Long updateDeviceShareInfo(DeviceShareDO deviceShareDO);

    /**
     * 根据设备dn查询授权分享信息
     * @param productKey
     * @param deviceName
     * @param environment
     * @return
     */
    List<DeviceShareDO> getShareInfoWithDeviceName(@Param("productKey") String productKey,
                                                   @Param("deviceName") String deviceName,
                                                   @Param("environment") String environment);

    /**
     * 根据设备dn和被授权用户id查询授权分享信息
     * @param productKey
     * @param deviceName
     * @param authorizeeHaasUserId
     * @param environment
     * @return
     */
    List<DeviceShareDO> getShareInfoWithDeviceNameAndAuthorizee(@Param("productKey") String productKey,
                                                                @Param("deviceName") String deviceName,
                                                                @Param("authorizeeHaasUserId") String authorizeeHaasUserId,
                                                                @Param("environment") String environment);

    /**
     * 根据授权用户查询授权分享信息
     * @param authorizerHaasUserId
     * @param environment
     * @return
     */
    List<DeviceShareDO> getShareInfoWithAuthorizer(@Param("authorizerHaasUserId") String authorizerHaasUserId,
                                                   @Param("environment") String environment);

    /**
     * 根据被授权用户查询授权分享信息
     * @param authorizeeHaasUserId
     * @param environment
     * @return
     */
    List<DeviceShareDO> getShareInfoWithAuthorizee(@Param("authorizeeHaasUserId") String authorizeeHaasUserId,
                                                   @Param("environment") String environment);

    /**
     * 撤销授权分享
     * @param productKey
     * @param deviceName
     * @param authorizeeHaasUserId
     * @param environment
     * @return
     */
    Long removeDeviceShareInfo(@Param("productKey") String productKey,
                               @Param("deviceName") String deviceName,
                               @Param("authorizeeHaasUserId") String authorizeeHaasUserId,
                               @Param("environment") String environment);

    /**
     * 撤销授权分享
     * @param productKey
     * @param deviceName
     * @param authorizerHaasUserId
     * @param environment
     * @return
     */
    Long removeAllSharedDeviceByUser(@Param("productKey") String productKey,
                                     @Param("deviceName") String deviceName,
                                     @Param("authorizerHaasUserId") String authorizerHaasUserId,
                                     @Param("environment") String environment);

    /**
     * 撤销车辆授权分享
     * @param productKey
     * @param deviceName
     * @param environment
     * @return
     */
    Long removeAllSharedDevice(@Param("productKey") String productKey,
                               @Param("deviceName") String deviceName,
                               @Param("environment") String environment);
}

