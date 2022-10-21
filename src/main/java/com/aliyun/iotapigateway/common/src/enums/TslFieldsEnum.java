package com.aliyun.iotx.haas.tdserver.common.enums;

/**
 * @author zhangheng
 * @date 20/9/21
 */
public enum TslFieldsEnum {

    // 用户ID
    HAAS_USER_ID("haasUserId", "用户ID"),
    PRODUCT_KEY("productKey", "产品key"),
    DEVICE_NAME("deviceName", "设备mac"),
    LATITUDE("latitude","gps经度"),
    LONGITUDE("longitude", "gps纬度"),
    NSEW("nsew", "方向"),
    STATUS("status", "数据类型"),

    LOCK_STATE_MOBILE_TAG("lockStateMobileTag", "设备开锁关锁状态"),
    LOCK_STATE_MOBILE("lockStateMobile", "设备开锁关锁状态"),

    TIMESTAMP("timestamp", "时间戳ms"),
    ;

    private String field;

    private String desc;

    TslFieldsEnum(String field, String desc){
        this.field = field;
        this.desc = desc;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

