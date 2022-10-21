package com.aliyun.iotx.haas.tdserver.sal.mq.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author imost.lwf
 * @date 2021/02/02
 */
@Data
public class LvyuanDeviceRideMessageBO extends AbstractMessageBO {
    private static final long serialVersionUID = 6338018708210767123L;
    /**
     * 骑行开始时间
     */
    private Long startTime;

    /**
     * 骑行结束时间
     */
    private Long endTime;

    /**
     * 本次骑行SOC最高电量
     */
    private Long socMax;

    /**
     * 本次骑行SOC最低电量
     */
    private Long socMin;

    /**
     * 本次骑行最高温度
     */
    private Long temperatureHigh;

    /**
     * 本次骑行最低温度
     */
    private Long temperatureLow;

    /**
     * 本次骑行总里程
     */
    private Long mileage;

    /**
     * 本次骑行霍尔故障数
     */
    private Long errorHall;

    /**
     * 本次骑行转把故障数
     */
    private Long errorHandle;

    /**
     * 本次骑行控制器故障数
     */
    private Long errorControl;

    /**
     * 本次骑行欠压保护数
     */
    private Long protectionUnderVol;

    /**
     * 本次骑行电机缺相数
     */
    private Long protectionPhase;

    /**
     * 本次骑行控制器保护数
     */
    private Long protectionControl;

    /**
     * 本次骑行防飞车保护数
     */
    private Long protectionOverSpeed;

    /**
     * 本次骑行过温保护数
     */
    private Long protectionOverTemp;

    /**
     * 本次骑行过流保护数
     */
    private Long protectionOverCur;

    /**
     * 本次骑行堵转保护数
     */
    private Long protectionMotor;
}

