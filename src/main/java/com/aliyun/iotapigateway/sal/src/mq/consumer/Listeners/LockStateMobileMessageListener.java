package com.aliyun.iotx.haas.tdserver.sal.mq.consumer.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONArray;
import com.aliyun.hitsdb.client.value.response.MultiFieldQueryResult;
import com.aliyun.iotx.haas.tdserver.common.enums.TslFieldsEnum;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceTrackDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceTrackDO;
import com.aliyun.iotx.haas.tdserver.dal.redis.CacheService;
import com.aliyun.iotx.haas.tdserver.dal.redis.bo.GpsUserIdBO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.gps.DeviceGpsLocationDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.gps.DeviceGpsTrackItemDTO;
import com.aliyun.iotx.haas.tdserver.sal.mq.bo.LockStateMobileMessageBO;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.AutowirableMessageListener;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.builder.DeviceGpsTrackDTOBuilder;
import com.aliyun.iotx.haas.tdserver.sal.tsdb.TsdbManager;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author zhangheng
 * @date 20/9/21
 */
@Component
public class LockStateMobileMessageListener implements AutowirableMessageListener {

    private static final Logger mqLog = LoggerFactory.getLogger("sal");

    @Value("${mq.aliyun.haas.tdserver.lock.state.mobile.topic}")
    private String topic;

    @Resource
    private TsdbManager tsdbManager;

    @Autowired
    private CacheService cacheService;

    @Resource
    private GpsPositionMessageListener gpsPositionMessageListener;

    @Autowired
    DeviceBindDAO deviceBindDAO;

    @Autowired
    DeviceTrackDAO deviceTrackDAO;

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Value("${tsdb.aliyun.haas.lock.state.mobile.metric}")
    private String metric;

    // 开锁状态
    private static final Integer LOCK_STATE_UNLOCK = 0;

    // 关锁状态
    private static final Integer LOCK_STATE_LOCK = 1;

    // 最小轨迹时间间隔
    private static final Long MIN_TRACK_TIME = 60 * 1000L;

    // 最小轨迹GPS点数量
    private static final Integer MIN_TRACK_POINTS = 2;

    //public static final String METRIC = "lock_state_mobile";


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
            LockStateMobileMessageBO messageBO = JSON.parseObject(msg, LockStateMobileMessageBO.class);
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
                //handleTrackItem(messageBO);
                return Action.CommitMessage;
            }
            return Action.ReconsumeLater;

        }catch (Exception e){
            mqLog.error("LockStateMobileMessageListener consumer exception", e);
        }

        return Action.CommitMessage;
    }

    private boolean saveToTsdb(LockStateMobileMessageBO messageBO){
        Long timestamp = messageBO.getTimestamp();
        Map<String,String> tagsMap = new HashMap<>();
        tagsMap.put(TslFieldsEnum.PRODUCT_KEY.getField(), messageBO.getProductKey());
        tagsMap.put(TslFieldsEnum.DEVICE_NAME.getField(), messageBO.getDeviceName());
        tagsMap.put(TslFieldsEnum.LOCK_STATE_MOBILE_TAG.getField(), String.valueOf(messageBO.getLockStateMobile()));
        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put(TslFieldsEnum.LOCK_STATE_MOBILE.getField(), messageBO.getLockStateMobile());
        return tsdbManager.multiFieldPut(metric, timestamp, tagsMap, fieldsMap);
    }

    private boolean handleTrackItem(LockStateMobileMessageBO messageBO){
        if (messageBO != null && LOCK_STATE_UNLOCK.equals(messageBO.getLockStateMobile())) {
            GpsUserIdBO gpsUserIdBO = new GpsUserIdBO();
            // 查询开锁时用户id
            List<String> userId = deviceBindDAO.getUserIdByDevice(messageBO.getProductKey(), messageBO.getDeviceName(), environment);
            if (userId != null && !userId.isEmpty()) {
                // 将开锁相关数据写入缓存
                gpsUserIdBO.setHaasUserId(userId.get(0));
                gpsUserIdBO.setProductKey(messageBO.getProductKey());
                gpsUserIdBO.setDeviceName(messageBO.getDeviceName());
                gpsUserIdBO.setTimeStamp(messageBO.getTimestamp());
                cacheService.setCachedGpsUserIdRequest(gpsUserIdBO);
                return true;
            }
        } else if (messageBO != null && LOCK_STATE_LOCK.equals(messageBO.getLockStateMobile())) {
            // 查询关锁时用户id
            List<String> userId = deviceBindDAO.getUserIdByDevice(messageBO.getProductKey(), messageBO.getDeviceName(), environment);
            if (userId != null && !userId.isEmpty()) {
                GpsUserIdBO gpsUserIdBO = cacheService.getCachedGpsUserIdRequest(messageBO.getProductKey(), messageBO.getDeviceName());
                // 当缓存存在 && 且用户为同一用户 && 开关锁时间大于最小轨迹时间，则判定为一条轨迹
                if (gpsUserIdBO != null && StringUtils.equals(userId.get(0), gpsUserIdBO.getHaasUserId()) && messageBO.getTimestamp() - gpsUserIdBO.getTimeStamp() > MIN_TRACK_TIME) {
                    //
                    DeviceGpsTrackItemDTO deviceGpsTrackItemDTO = getDeviceGpsTrackItemDTO(
                            messageBO.getProductKey(), messageBO.getDeviceName(), gpsUserIdBO.getTimeStamp(), messageBO.getTimestamp());

                    mqLog.info("deviceGpsTrackItemDTO args:  {}", messageBO.getProductKey() + " " + messageBO.getDeviceName() + " " + gpsUserIdBO.getTimeStamp() + " " + messageBO.getTimestamp());

                    if (deviceGpsTrackItemDTO != null) {
                        // 将轨迹数据落库
                        mqLog.info("deviceGpsTrackItemDTO args:  {}", deviceGpsTrackItemDTO);
                        DeviceTrackDO deviceTrackDO = new DeviceTrackDO();
                        deviceTrackDO.setHaasUserId(gpsUserIdBO.getHaasUserId());
                        deviceTrackDO.setProductKey(messageBO.getProductKey());
                        deviceTrackDO.setDeviceName(messageBO.getDeviceName());
                        deviceTrackDO.setTrackStartTime(gpsUserIdBO.getTimeStamp());
                        deviceTrackDO.setTrackStopTime(messageBO.getTimestamp());
                        deviceTrackDO.setTrackStartGpsLatitude(deviceGpsTrackItemDTO.getStartLocation().getLatitude());
                        deviceTrackDO.setTrackStartGpsLongitude(deviceGpsTrackItemDTO.getStartLocation().getLongitude());
                        deviceTrackDO.setTrackStopGpsLatitude(deviceGpsTrackItemDTO.getEndLocation().getLatitude());
                        deviceTrackDO.setTrackStopGpsLongitude(deviceGpsTrackItemDTO.getEndLocation().getLongitude());
                        deviceTrackDO.setTrackStartGpsNsew(deviceGpsTrackItemDTO.getStartLocation().getNsew());
                        deviceTrackDO.setTrackStopGpsNsew(deviceGpsTrackItemDTO.getEndLocation().getNsew());
                        deviceTrackDO.setTrackStartGpsStatus(deviceGpsTrackItemDTO.getStartLocation().getStatus());
                        deviceTrackDO.setTrackStopGpsStatus(deviceGpsTrackItemDTO.getEndLocation().getStatus());
                        deviceTrackDO.setEnvironment(environment);
                        deviceTrackDAO.insert(deviceTrackDO);
                        cacheService.deleteCachedGpsUserIdRequest(gpsUserIdBO.getProductKey(), gpsUserIdBO.getDeviceName(), messageBO.getTimestamp());
                        return true;
                    }
                }
                cacheService.deleteCachedGpsUserIdRequest(gpsUserIdBO.getProductKey(), gpsUserIdBO.getDeviceName(), messageBO.getTimestamp());
            }
        }

        return false;
    }

    private DeviceGpsTrackItemDTO getDeviceGpsTrackItemDTO(String productKey, String deviceName, Long start, Long end) {
        DeviceGpsTrackItemDTO deviceGpsTrackItemDTO = new DeviceGpsTrackItemDTO();
        deviceGpsTrackItemDTO.setStartTime(start);
        deviceGpsTrackItemDTO.setEndTime(end);

        Map<String,String> tagsMap = buildCommonTagMap(productKey, deviceName);
        List<MultiFieldQueryResult> results = tsdbManager.multiFieldQuery(gpsPositionMessageListener.getMetric(), tagsMap, start, end, null);
        DeviceGpsLocationDTO locationDTO = new DeviceGpsTrackDTOBuilder(results, start, end).build();

        if (locationDTO != null && locationDTO.getItems() != null && locationDTO.getItems().size() < MIN_TRACK_POINTS) {
            return null;
        }

        deviceGpsTrackItemDTO.setStartLocation(locationDTO.getItems().get(0));
        deviceGpsTrackItemDTO.setEndLocation(locationDTO.getItems().get(locationDTO.getItems().size() - 1));

        return deviceGpsTrackItemDTO;
    }

    private Map<String, String> buildCommonTagMap(String productKey, String deviceName){
        Map<String,String> tagsMap = new HashMap<>();
        tagsMap.put(TslFieldsEnum.PRODUCT_KEY.getField(), productKey);
        tagsMap.put(TslFieldsEnum.DEVICE_NAME.getField(), deviceName);
        return tagsMap;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }
}

