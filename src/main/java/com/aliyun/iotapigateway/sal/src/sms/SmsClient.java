package com.aliyun.iotx.haas.tdserver.sal.msg;

import java.util.List;

/**
 * @author imost.lwf
 * @date 2020/11/24
 */
public interface SmsClient {
    /**
     * 发送验证吗
     *
     * @param mobile 手机号
     * @param code   验证码
     * @return
     */
    public void sendTdserverVerificationCode(String mobile, String code);

    /**
     * 发送验证吗
     *
     * @param mobile    手机号
     * @param alarmType 报警类型
     * @param alarmVale 报警值
     * @return
     */
    public void sendTdserverAlarmDeailMsg(String mobile, Integer alarmType, Integer alarmVale);

    /**
     * 发送流量卡超期报警
     * @param mobile 手机号列表
     * @param code   短信内容
     * @return
     */
    public void sendTdserverTimeOutMsg(String mobile, String code);

    /**
     * 发送验证吗
     *
     * @param mobile 手机号
     * @return
     */
    public void sendTdserverAlarmMsg(String mobile);
}

