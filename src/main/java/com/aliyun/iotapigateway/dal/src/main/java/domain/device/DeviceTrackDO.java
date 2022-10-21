package com.aliyun.iotx.haas.tdserver.dal.domain.device;

import lombok.Data;

import java.util.Date;

/**
 * @author imost.lwf
 * @date 2021/1/20
 */

@Data
public class DeviceTrackDO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 是否删除 0：未删除，1：删除
     */
    private Integer isDeleted;

    /**
     * 产品pk
     */
    private String productKey;

    /**
     * 设备dn
     */
    private String deviceName;

    /**
     * 产生轨迹时，车辆所绑定的用户id
     */
    private String haasUserId;

    /**
     * 轨迹开始时间
     */
    private Long trackStartTime;

    /**
     * 轨迹结束时间
     */
    private Long trackStopTime;

    /**
     * 轨迹开始经度
     */
    private Double trackStartGpsLongitude;

    /**
     * 轨迹开始纬度
     */
    private Double trackStartGpsLatitude;

    /**
     * 轨迹结束经度
     */
    private Double trackStopGpsLongitude;

    /**
     * 轨迹结束纬度
     */
    private Double trackStopGpsLatitude;

    /**
     * 轨迹开始方向
     */
    private Integer trackStartGpsNsew;

    /**
     * 轨迹结束方向
     */
    private Integer trackStopGpsNsew;

    /**
     * 轨迹开始状态
     */
    private Integer trackStartGpsStatus;

    /**
     * 轨迹结束状态
     */
    private Integer trackStopGpsStatus;

    /**
     * 保留字段
     */
    private String reserve;

    /**
     * 保留字段2
     */
    private String reserve2;

    /**
     * 产品所属环境
     */
    private String environment;

}

