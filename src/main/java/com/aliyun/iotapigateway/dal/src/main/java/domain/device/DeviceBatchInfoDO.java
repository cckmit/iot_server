package com.aliyun.iotx.haas.tdserver.dal.domain.device;

import java.util.Date;

import lombok.Data;

/**
 * @author benxiliu
 * @date 2020/09/02
 */

@Data
public class DeviceBatchInfoDO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 产品pk
     */
    private String productKey;

    /**
     * 所属ODM租户Id
     */
    private String odmTenantId;

    /**
     * 批次id
     */
    private Long batchId;

    /**
     * 批次出行平台唯一产品名
     */
    private String uniqueTdserverProductName;

    /**
     * 批次开始设备dn
     */
    private String startDeviceName;

    /**
     * 批次结束设备dn
     */
    private String endDeviceName;

    /**
     * 批次设备数量
     */
    private Integer deviceNumber;

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
     * 批次状态
     */
    private String batchStatus;

    /**
     * 产品所属环境
     */
    private String environment;
}

