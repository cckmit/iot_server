package com.aliyun.iotx.haas.tdserver.common.utils;

import com.aliyun.iotx.common.base.code.IoTxCode;
import com.aliyun.iotx.common.base.service.IoTxResult;

/**
 * @author zhangheng
 * @date 20/9/4
 */
public class IoTxResultUtil {

    public static <T> IoTxResult<T> success(T t){
        return new IoTxResult<>(t);
    }

    public static <T> IoTxResult<T> error(IoTxCode errorCode){
        return new IoTxResult<T>(errorCode);
    }
}

