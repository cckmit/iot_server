package com.aliyun.iotx.haas.tdserver.service.admin.facade;

import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.facade.request.StatisticsRequest;
import com.aliyun.iotx.haas.tdserver.facade.response.StatisticsResponse;
import com.aliyun.iotx.haas.tdserver.service.annotation.DispatchParam;

/**
 *
 * @author 少清
 * @date 2021/11/16
 */
public interface StatisticsService {
    IoTxResult<Object> query(@DispatchParam("request") StatisticsRequest request);
}

