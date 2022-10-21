package com.aliyun.iotx.haas.tdserver.dal.domain.product;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author imost.lwf
 * @date 2021/1/7
 */

@Data
@NoArgsConstructor
public class ModuleInfoDO {
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
     * 是否支持一线通协议
     */
    private Integer isSupportOneline;

    /**
     * 超速告警功能触发类型
     */
    private Integer overspeedWarningType;

    /**
     * 开关类型
     */
    private Integer switchType;

    /**
     * 触发类型
     */
    private Integer triggerType;

    /**
     * 车辆静置默认锁车时间
     */
    private Integer defaultLockTime;

    /**
     * 震动告警灵敏度
     */
    private Integer vibrationWarningSensitivity;

    /**
     * 超速告警音量
     */
    private Integer overspeedWarningVolume;

    /**
     * 是否允许锁车时绑定
     */
    private Integer isLockedBind;

    /**
     * 车辆上电后限时绑定时间
     */
    private Integer bindTimeAfterPoweron;

    /**
     * 模块通信自定义指令一
     */
    private String customCommunicationInstructions1;

    /**
     * 模块通信自定义指令二
     */
    private String customCommunicationInstructions2;

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

    /**
     * odm租户id，关联iot_haas_td_odm. odm_tenant_id
     */
    private String odmTenantId;

    /**
     * 唯一模块Id
     */
    private String uniqueModuleName;

    /**
     * 唯一产品Id
     */
    private String uniqueTdserverProductName;

    /**
     * 车辆是否开始按键功能
     */
    private Integer isOpenButtonFunction;
}

