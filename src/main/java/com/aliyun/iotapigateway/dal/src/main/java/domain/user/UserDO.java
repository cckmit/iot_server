package com.aliyun.iotx.haas.tdserver.dal.domain.user;

import java.util.Date;

import lombok.Data;

@Data
public class UserDO {

    protected Long id;
    protected Date gmtCreate;
    protected Date gmtModified;
    protected Integer isDeleted;

    /**
     * 用户所在平台名
     */
    private String platformName;

    /**
     * 用户所在平台的原始用户id
     */
    private String rawUserId;

    /**
     * haas平台分配的用户id
     */
    private String haasUserId;

    /**
     * 用户手机号码
     */
    private String mobile;

    /**
     * 保留字段
     */
    private String reserved;

    /**
     * 保留字段2
     */
    private String reserved2;

    /**
     * 是否签署用户协议
     */
    private Boolean isAgreementSigned;

    /**
     * 是否开启短信预警通知
     */
    private Boolean isUsingSmsNotification;
}

