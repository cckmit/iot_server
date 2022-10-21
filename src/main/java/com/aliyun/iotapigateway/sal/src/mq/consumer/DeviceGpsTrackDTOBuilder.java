package com.aliyun.iotx.haas.tdserver.sal.mq.consumer.builder;

import com.aliyun.hitsdb.client.value.response.MultiFieldQueryResult;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.enums.TslFieldsEnum;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.gps.DeviceGpsLocationDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.gps.DeviceGpsLocationItemDTO;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author imost.lwf
 * @date 2021/01/20
 */
public class DeviceGpsTrackDTOBuilder {

    private List<MultiFieldQueryResult> results;

    private Long startTime;

    private Long endTime;

    public DeviceGpsTrackDTOBuilder(List<MultiFieldQueryResult> results,
                                       Long startTime, Long endTime){
        this.results = results;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DeviceGpsLocationDTO build(){
        DeviceGpsLocationDTO deviceGpsLocationDTO = new DeviceGpsLocationDTO();
        if(CollectionUtils.isEmpty(results)){
            return deviceGpsLocationDTO;
        }
        MultiFieldQueryResult result = results.get(0);
        if(null == result){
            return deviceGpsLocationDTO;
        }
        List<List<Object>> values = result.getValues();
        if(CollectionUtils.isEmpty(values)){ 
            return deviceGpsLocationDTO;
        }
        deviceGpsLocationDTO.setStartTime(startTime);
        deviceGpsLocationDTO.setEndTime(endTime);
        List<String> columns = result.getColumns();
        Integer indexTimestamp = columns.indexOf(TslFieldsEnum.TIMESTAMP.getField());
        Integer indexLatitude = columns.indexOf(TslFieldsEnum.LATITUDE.getField());
        Integer indexLongitude = columns.indexOf(TslFieldsEnum.LONGITUDE.getField());
        Integer indexNsew = columns.indexOf(TslFieldsEnum.NSEW.getField());
        Integer indexStatus = columns.indexOf(TslFieldsEnum.STATUS.getField());
        try {
            List<DeviceGpsLocationItemDTO> itemsDTOs = values
                    .stream()
                    .map(it ->{
                        DeviceGpsLocationItemDTO locationItemsDTO = new DeviceGpsLocationItemDTO();
                        if(indexTimestamp != null && indexTimestamp >=0){
                            Long timestamp = (Long)it.get(indexTimestamp);
                            if(null == timestamp || timestamp <=0){
                                return null;
                            }
                            locationItemsDTO.setTimestamp(timestamp);
                        }
                        if(indexLatitude != null && indexLatitude >=0){
                            Object latitude = it.get(indexLatitude);
                            if(null == latitude){
                                return null;
                            }
                            locationItemsDTO.setLatitude(((BigDecimal)latitude).doubleValue());
                        }
                        if(indexLongitude != null && indexLongitude >=0){
                            Object longitude = it.get(indexLongitude);
                            if(null == longitude){
                                return null;
                            }
                            locationItemsDTO.setLongitude(((BigDecimal)longitude).doubleValue());
                        }
                        if(indexNsew != null && indexNsew >=0 && it.get(indexNsew) != null){
                            locationItemsDTO.setNsew(((BigDecimal)it.get(indexNsew)).intValue());
                        }
                        if(indexStatus != null && indexStatus >=0 && it.get(indexStatus) != null){
                            locationItemsDTO.setStatus(((BigDecimal)it.get(indexStatus)).intValue());
                        }
                        return locationItemsDTO;
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList());
            deviceGpsLocationDTO.setItems(itemsDTOs
                    .stream()
                    .sorted(Comparator.comparing(DeviceGpsLocationItemDTO::getTimestamp))
                    .collect(Collectors.toList()));
            return deviceGpsLocationDTO;
        } catch (ClassCastException exception) {
            throw exception;
        }
    }
}

