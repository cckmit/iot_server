package com.aliyun.iotx.haas.tdserver.dal.redis.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author imost.lwf
 * @date 2020/10/29
 */

@Data
public class ShareTokenBO {
    @JSONField(name = "pk")
    private String productKey;
    @JSONField(name = "dn")
    private String deviceName;
    @JSONField(name = "uid")
    private String haasUserId;
    @JSONField(name = "tk")
    private String token;
}
