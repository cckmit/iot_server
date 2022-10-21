package com.aliyun.iotx.haas.tdserver.dal.domain.device;

import java.util.Date;

import lombok.Data;

/**
 * @author imost.lwf
 * @date 2020/10/28
 */

@Data
public class DeviceShareDO {
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
     * 是否确认授权分享 0：否，1：是
     */
    private Integer isConfirmed;

    /**
     * 产品pk
     */
    private String productKey;

    /**
     * 设备dn
     */
    private String deviceName;

    /**
     * 被授权用户别名
     */
    private String authorizeeAlias;

    /**
     * 授权用户ID
     */
    private String authorizerHaasUserId;

    /**
     * 被授权用户ID
     */
    private String authorizeeHaasUserId;

    /**
     * 产品所属环境
     */
    private String environment;

    /**
     * 授权分享过期时间
     */
    private Date gmtExpire;

    /**
     * 保留字段
     */
    private String reserve;

    /**
     * 保留字段2
     */
    private String reserve2;
}

