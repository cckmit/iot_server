package com.aliyun.iotx.haas.tdserver.dal.redis.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class BindRequestBO {
    @JSONField(name = "pk")
    private String productKey;
    @JSONField(name = "dn")
    private String deviceName;
    @JSONField(name = "uid")
    private String haasUserId;
    @JSONField(name = "r1")
    private String r1;
}

