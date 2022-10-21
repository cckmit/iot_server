package com.aliyun.iotx.haas.tdserver.dal.domain.device;

import lombok.Data;

import java.util.Date;

/**
 * @author imost.lwf
 * @date 2020/11/10
 */

@Data
public class DeviceBluetoothInfoDO {
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
     * 用户ID
     */
    private String haasUserId;

    /**
     * 产品pk
     */
    private String productKey;

    /**
     * 设备dn
     */
    private String deviceName;

    /**
     * 无感开启状态下，蓝牙设备DeviceID
     */
    private String bluetoothDeviceIdV1;

    /**
     * 无感关闭状态下，蓝牙设备DeviceID
     */
    private String bluetoothDeviceIdV2;

    /**
     * 产品所属环境
     */
    private String environment;
}

