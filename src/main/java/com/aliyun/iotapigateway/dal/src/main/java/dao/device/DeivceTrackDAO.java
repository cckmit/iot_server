package com.aliyun.iotx.haas.tdserver.dal.dao.device;

import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceTrackDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author imost.lwf
 * @date 2021/1/20
 */

public interface DeviceTrackDAO {
    /**
     * 插入新的轨迹段信息
     * @return
     */
    Long insert(DeviceTrackDO deviceTrackDO);

    /**
     * 分页查询用户轨迹
     * @param haasUserId
     * @param start
     * @param pageSize
     * @param environment
     * @return
     */
    List<DeviceTrackDO> getUserTrackList(@Param("haasUserId") String haasUserId,
                                         @Param("start") Integer start,
                                         @Param("pageSize") Integer pageSize,
                                         @Param("environment") String environment);

    /**
     * 分页查询车辆轨迹
     * @param haasUserId
     * @param productKey
     * @param deviceName
     * @param start
     * @param pageSize
     * @param environment
     * @return
     */
    List<DeviceTrackDO> getDeviceTrackList(@Param("haasUserId") String haasUserId,
                                         @Param("productKey") String productKey,
                                         @Param("deviceName") String deviceName,
                                         @Param("start") Integer start,
                                         @Param("pageSize") Integer pageSize,
                                         @Param("environment") String environment);
}

