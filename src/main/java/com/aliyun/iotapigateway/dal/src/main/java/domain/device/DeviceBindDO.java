package com.aliyun.iotx.haas.tdserver.dal.domain.device;

import com.aliyun.iotx.haas.tdserver.dal.domain.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceBindDO extends BaseDO {
    private String haasUserId;
    private String productKey;
    private String deviceName;
    private String deviceAlias;
    private String k1;
    private String k2;
    private Integer isDefaultDevice = 0;
}

