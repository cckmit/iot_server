package com.aliyun.iotx.haas.tdserver.dal.redis.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author imost.lwf
 * @date 2020/11/17
 */

@Data
public class GpsUserIdBO {
    @JSONField(name = "productKey")
    private String productKey;
    @JSONField(name = "deviceName")
    private String deviceName;
    @JSONField(name = "haasUserId")
    private String haasUserId;
    @JSONField(name = "timeStamp")
    private Long timeStamp;
}

