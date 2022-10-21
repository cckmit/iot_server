package com.aliyun.iotx.haas.tdserver.sal.mq.bo;

/**
 * @author zhangheng
 * @date 20/9/21
 */
public class LockStateMobileMessageBO extends AbstractMessageBO {

    private static final long serialVersionUID = -6333646439677648129L;

    /**
     * 0-开锁,1-上锁
     */
    private Integer lockStateMobile;

    public Integer getLockStateMobile() {
        return lockStateMobile;
    }

    public void setLockStateMobile(Integer lockStateMobile) {
        this.lockStateMobile = lockStateMobile;
    }
}

