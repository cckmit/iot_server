package com.aliyun.iotx.haas.tdserver.dal.domain;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDO {
    protected Long id;
    protected Date gmtCreate;
    protected Date gmtModified;
    protected String environment;
    protected Integer isDeleted;
}

