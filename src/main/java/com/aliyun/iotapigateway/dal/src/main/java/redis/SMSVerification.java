package com.aliyun.iotx.haas.tdserver.dal.redis.bo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author imost.lwf
 * @date 2020/11/26
 */

@Data
public class SMSVerificationCodeBO {
    @JSONField(name = "mobile")
    private String mobile;
    @JSONField(name = "code")
    private String code;
    @JSONField(name = "timestamp")
    private Long timestamp;
    @JSONField(name = "availableCount")
    private Integer availableCount;
}

