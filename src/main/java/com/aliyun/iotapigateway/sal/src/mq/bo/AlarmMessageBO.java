package com.aliyun.iotx.haas.tdserver.sal.mq.bo;

import lombok.Data;

/**
 * @author imost.lwf
 * @date 2020/11/25
 */
@Data
public class AlarmMessageBO extends AbstractMessageBO{
    /**
     * 告警类型：1-防盗告警,2-安全告警
     */
    private Integer  alarmType;

    /**
     * 防盗告警值：0-震动告警,1-推动告警,2-电池偷盗告警
     * 安全告警值：
     */
    private Integer  alarmValue;
}

