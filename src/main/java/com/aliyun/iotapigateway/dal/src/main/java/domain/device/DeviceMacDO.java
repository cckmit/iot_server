package com.aliyun.iotx.haas.tdserver.dal.domain.device;

import lombok.Data;

import java.util.Date;

/**
 * @author imost.lwf
 * @date 2021/04/18
 */
@Data
public class DeviceMacDO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 资源段起始mac地址
     */
    private String startMac;

    /**
     * 资源段结束mac地址
     */
    private String endMac;

    /**
     * 资源段有效mac地址数目
     */
    private Long macCount;

    /**
     * 资源段状态
     */
    private String macStatus;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 数据环境
     */
    private String environment;

    /**
     * 地址段出行平台唯一产品名
     */
    private String uniqueTdserverProductName;

    /**
     * LP申请DN的批次ID
     */
    private Long batchApplyId;
}

