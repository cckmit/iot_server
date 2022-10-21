package com.aliyun.iotx.haas.tdserver.sal.mq.consumer.Listeners;

import com.alibaba.fastjson.JSON;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.enums.TslFieldsEnum;
import com.aliyun.iotx.haas.tdserver.facade.msg.SmsService;
import com.aliyun.iotx.haas.tdserver.sal.mq.bo.AlarmMessageBO;
import com.aliyun.iotx.haas.tdserver.sal.mq.bo.GpsPositionMessageBO;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.AutowirableMessageListener;
import com.aliyun.iotx.haas.tdserver.sal.tsdb.TsdbManager;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author imost.lwf
 * @date 2020/11/25
 */

@Component
public class AlarmMessageListener implements AutowirableMessageListener {

    private static final Logger mqLog =  LoggerFactory.getLogger("sal");

    @Value("${mq.aliyun.haas.tdserver.alarm.state.topic}")
    private String topic;

    @Resource
    private SmsService smsService;

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        try {
            if(!StringUtils.equalsIgnoreCase(message.getTopic(), topic)){
                return Action.CommitMessage;
            }
            String msg = new String(message.getBody(),"UTF-8");
            mqLog.info("AlarmMessageListener msg body:  {}", msg);
            AlarmMessageBO messageBO = JSON.parseObject(msg, AlarmMessageBO.class);
            if(null == messageBO){
                return Action.CommitMessage;
            }
            String productKey = messageBO.getProductKey();
            String deviceName = messageBO.getDeviceName();
            Long timestamp = messageBO.getTimestamp();
            if(StringUtils.isBlank(productKey) || StringUtils.isBlank(deviceName) ||
                    null == timestamp || timestamp <=0){
                return Action.CommitMessage;
            }
            if(sendSmsToUser(messageBO)){
                return Action.CommitMessage;
            }
            return Action.ReconsumeLater;

        }catch (Exception e){
            mqLog.error("GpsPositionMessageListener consumer exception", e);
        }

        return Action.CommitMessage;
    }

    private boolean sendSmsToUser(AlarmMessageBO alarmMessageBO) {
//        IoTxResult<String> result = smsService.sendTdserverAlarmMsg(alarmMessageBO.getProductKey(), alarmMessageBO.getDeviceName());
        IoTxResult<String> result = smsService.sendTdserverAlarmDetailMsg(alarmMessageBO.getProductKey(), alarmMessageBO.getDeviceName(),
                alarmMessageBO.getAlarmType(), alarmMessageBO.getAlarmValue());

        if (result.hasSucceeded()) {
            return true;
        } else {
            return false;
        }
    }
}

