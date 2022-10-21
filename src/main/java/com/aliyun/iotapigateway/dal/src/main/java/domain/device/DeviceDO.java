package com.aliyun.iotx.haas.tdserver.dal.domain.device;

import lombok.Data;

import java.util.Date;

/**
 * @author imost.lwf
 * @date 2021/04/13
 */
@Data
public class DeviceDO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 产品pk
     */
    private String productKey;

    /**
     * 设备dn
     */
    private String deviceName;

    /**
     * 厂商SN
     */
    private String deviceOdmSerialNumber;

    /**
     * 出行平台唯一产品名
     */
    private String uniqueTdserverProductName;

    /**
     * 唯一模块Id
     */
    private String uniqueModuleName;

    /**
     * 是否删除 0：未删除，1：删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 绑定时间
     */
    private Date gmtBound;

    /**
     * 数据环境
     */
    private String environment;

    /**
     * 租户ID
     */
    private String odmTenantId;

    /**
     * 设备是否被绑定 0：未绑定，1：绑定
     */
    private Integer isBound;

    /**
     * 设备是否开无感 0：未开无感，1：开启无感
     */
    private Integer isOpenAutoLock;

}

