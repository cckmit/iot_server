package com.aliyun.iotx.haas.tdserver.sal.mq.consumer.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONArray;
import com.aliyun.iotx.haas.tdserver.common.enums.TslFieldsEnum;
import com.aliyun.iotx.haas.tdserver.dal.redis.CacheService;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.GpsUserIdBO;
import com.aliyun.iotx.haas.tdserver.sal.mq.bo.GpsPositionMessageBO;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.AutowirableMessageListener;
import com.aliyun.iotx.haas.tdserver.sal.tsdb.TsdbManager;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author zhangheng
 * @date 20/9/21
 */
@Component
public class GpsPositionMessageListener implements AutowirableMessageListener {

    private static final Logger mqLog =  LoggerFactory.getLogger("sal");

    @Value("${mq.aliyun.haas.tdserver.gps.position.topic}")
    private String topic;

    @Resource
    private TsdbManager tsdbManager;

    private final CacheService cacheService;

    @Value("${tsdb.aliyun.haas.gps.position.metric}")
    private String metric;

    @Autowired
    public GpsPositionMessageListener(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    //public static final String METRIC = "position";

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
            mqLog.info("msg body:  {}", msg);
            GpsPositionMessageBO messageBO = JSON.parseObject(msg, GpsPositionMessageBO.class);
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
            if(saveToTsdb(messageBO)){
                return Action.CommitMessage;
            }
            return Action.ReconsumeLater;

        }catch (Exception e){
            mqLog.error("GpsPositionMessageListener consumer exception", e);
        }

        return Action.CommitMessage;
    }

    private boolean saveToTsdb(GpsPositionMessageBO messageBO){
        Long timestamp = messageBO.getTimestamp();
        Map<String,String> tagsMap = new HashMap<>();
        GpsUserIdBO gpsUserIdBO = cacheService.getCachedGpsUserIdRequest(messageBO.getProductKey(), messageBO.getDeviceName());
        if (gpsUserIdBO != null) {
            tagsMap.put(TslFieldsEnum.HAAS_USER_ID.getField(), gpsUserIdBO.getHaasUserId());
        }
        tagsMap.put(TslFieldsEnum.PRODUCT_KEY.getField(), messageBO.getProductKey());
        tagsMap.put(TslFieldsEnum.DEVICE_NAME.getField(), messageBO.getDeviceName());
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put(TslFieldsEnum.LATITUDE.getField(), messageBO.getLatitude());
        fieldsMap.put(TslFieldsEnum.LONGITUDE.getField(), messageBO.getLongitude());
        fieldsMap.put(TslFieldsEnum.NSEW.getField(), messageBO.getNsew());
        fieldsMap.put(TslFieldsEnum.STATUS.getField(), messageBO.getStatus());
        return tsdbManager.multiFieldPut(metric, timestamp, tagsMap, fieldsMap);
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }
}

