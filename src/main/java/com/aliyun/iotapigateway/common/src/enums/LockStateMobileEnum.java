package com.aliyun.iotx.haas.tdserver.common.enums;

/**
 * @author zhangheng
 * @date 20/9/22
 */
public enum LockStateMobileEnum {

    OPEN("0", "开锁状态"),
    CLOSE("1", "关锁状态"),

    ;

    private String state;

    private String desc;

    LockStateMobileEnum(String state, String desc){
        this.state = state;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

