package com.aliyun.iotx.haas.tdserver.common.enums;

import lombok.Getter;

/**
 *
 * @author 少清
 * @date 2021/11/16
 */
public enum PeriodEnum {
    DAY(0, 1),
    WEEK(1, 7),
    MONTH(2, 30),
    QUARTER(3, 90);

    @Getter
    private final int type;

    @Getter
    private final int days;

    PeriodEnum(int type, int days){
        this.type = type;
        this.days = days;
    }

    public static PeriodEnum getByType(Integer type){
        if(type == null){
            return DAY;
        }
        for(PeriodEnum value: PeriodEnum.values()){
            if(value.type == type){
                return value;
            }
        }
        return DAY;
    }
}

