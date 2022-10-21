package com.aliyun.iotx.haas.tdserver.sal.mq.bo;

import lombok.Data;

/**
 * @author imost.lwf
 * @date 2020/11/25
 */
@Data
public class DeviceBindMessageB0 extends AbstractMessageBO{
    /**
     * 是否开启无感：1 开启,0-不开启
     */
    private Integer  bindState;
}
