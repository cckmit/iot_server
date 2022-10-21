package com.aliyun.iotx.haas.tdserver.sal.mq.bo;

import java.io.Serializable;

/**
 * @author zhangheng
 * @date 20/9/21
 */
public abstract class AbstractMessageBO implements Serializable {

    private static final long serialVersionUID = 5555342626479048803L;
    protected String productKey;

    protected String deviceName;

    /**
     * 设备的时间戳1600677005000
     */
    protected Long timestamp;

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

