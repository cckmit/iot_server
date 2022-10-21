package com.aliyun.iotx.haas.tdserver.admin.facade.service;

import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.admin.facade.model.DispatchHttpRequest;

/**
 * @author zhangheng
 * @date 21/1/11
 */
public interface DispatchFacade {

    IoTxResult<Object> dispatchHttpRequest(DispatchHttpRequest request);
}

