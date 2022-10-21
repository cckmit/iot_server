package com.aliyun.iotx.haas.tdserver.dal.domain.odm;

import lombok.Data;

import java.util.Date;

/**
 * @author imost.lwf
 * @date 2021/1/7
 */

@Data
public class OdmProductKeyDO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 所属ODM租户id
     */
    private String odmTenantId;

    /**
     * LP平台产品PK
     */
    private String productKey;

    /**
     * odm定义的pk名称
     */
    private String pkName;

    /**
     * 是否为odm默认pk
     */
    private Integer isDefaultPk;

    /**
     * 是否为当前通用大PK
     */
    private Integer isUniveralPk;

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
     * 保留字段
     */
    private String reserve;

    /**
     * 保留字段2
     */
    private String reserve2;

    /**
     * 数据环境
     */
    private String environment;
}

