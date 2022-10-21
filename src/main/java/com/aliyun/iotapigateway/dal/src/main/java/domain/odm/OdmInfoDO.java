package com.aliyun.iotx.haas.tdserver.dal.domain.odm;

import java.util.Date;

import lombok.Data;

/**
 * @author benxiliu
 * @date 2020/09/08
 */

@Data
public class OdmInfoDO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 所属ODM租户id
     */
    private String odmTenantId;

    /**
     * ODM阿里云PK
     */
    private String aliyunPk;

    /**
     * ODM名称
     */
    private String name;

    /**
     * ODM地址
     */
    private String addr;

    /**
     * ODM联系人
     */
    private String contact;

    /**
     * ODM联系方式，加密
     */
    private String mobile;

    /**
     * ODM联系邮箱，加密
     */
    private String email;

    /**
     * ODM备注
     */
    private String remark;

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
     * 入驻审核状态
     */
    private String approvalStatus;

    /**
     * 联系人职位
     */
    private String contactPost;
}

