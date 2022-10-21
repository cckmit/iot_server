package com.aliyun.iotx.haas.tdserver.common.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastvalidator.constraints.exception.FastValidatorException;

import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.exception.IoTxException;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.LogConstants;
import com.taobao.eagleeye.EagleEye;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author benxiliu
 * @date 2020/08/19
 */

@Aspect
@Component
public class LogAspect {
    private final Logger serviceLogger = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_SERVICE);

    private final Logger salLogger = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_SAL);

    private final Logger dalLogger = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_DAL);

    private final Logger errorLogger = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_ERROR);

    @Around("execution(public * com.aliyun.iotx.haas.tdserver.service.impl.*.*(..))")
    public Object serviceLog(ProceedingJoinPoint jp) {
        long start = System.currentTimeMillis();
        Object result = null;
        boolean hasError = false;
        String args = null;

        try {
            args = JSON.toJSONString(jp.getArgs());
            result = jp.proceed();
            return result;
        } catch (IoTxException e) {
            hasError = true;
            errorLogger.error(e.getMessage() + ",traceId:" + EagleEye.getTraceId(), e);
            return new IoTxResult<>(e.getCode(), e.getMessage());
        } catch (FastValidatorException e) {
            hasError = true;
            errorLogger.error(e.getMessage() + ",traceId:" + EagleEye.getTraceId(), e);
            return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR.getCode(), "request param error");
        } catch (Throwable e) {
            hasError = true;
            errorLogger.error(e.getMessage() + ",traceId:" + EagleEye.getTraceId(), e);
            return new IoTxResult<>(IoTxCodes.REQUEST_ERROR.getCode(), "unknown error");
        } finally {
            long end = System.currentTimeMillis();
            long costTime = end - start;
            try {
                String[] arrays = StringUtils.split(jp.getSignature().toString(), ".");
                String method = arrays[arrays.length - 2] + "." + arrays[arrays.length - 1];
                serviceLogger.info("invoke:{}, args:{}, hasError:{}, costTime:{}ms, traceId:{}", method , args, hasError, costTime, EagleEye.getTraceId());
            } catch (Exception e) {
                serviceLogger.warn("invoke:" + jp.getSignature().toString(), ",costTime" + costTime + "ms" + ",traceId:" + EagleEye.getTraceId(), e);
            }
        }
    }

    @Around("execution(public * com.aliyun.iotx.haas.tdserver.sal..*.impl.*.*(..))")
    public Object salLog(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result;
        boolean hasError = false;
        String args = null;

        try {
            args = JSON.toJSONString(jp.getArgs());
            result = jp.proceed();
            return result;
        } catch (Throwable e) {
            hasError = true;
            errorLogger.error(e.getMessage() + ",traceId:" + EagleEye.getTraceId(), e);
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            long costTime = end - start;
            try {
                String[] arrays = StringUtils.split(jp.getSignature().toString(), ".");
                String method = arrays[arrays.length - 2] + "." + arrays[arrays.length - 1];
                salLogger.info("invoke:{}, args:{}, hasError:{}, costTime:{}ms, traceId:{}", method , args, hasError, costTime, EagleEye.getTraceId());
            } catch (Exception e) {
                salLogger.warn("invoke:" + jp.getSignature().toString(), ",costTime" + costTime + "ms" + ",traceId:" + EagleEye.getTraceId(), e);
            }
        }
    }

    @Around("execution(public * com.aliyun.iotx.haas.tdserver.dal.dao..*.*(..))")
    public Object dalLog(ProceedingJoinPoint jp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result;
        boolean hasError = false;
        String args = null;

        try {
            args = JSON.toJSONString(jp.getArgs());
            result = jp.proceed();
            return result;
        } catch (Throwable e) {
            hasError = true;
            errorLogger.error(e.getMessage() + ",traceId:" + EagleEye.getTraceId(), e);
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            long costTime = end - start;
            try {
                String[] arrays = StringUtils.split(jp.getSignature().toString(), ".");
                String method = arrays[arrays.length - 2] + "." + arrays[arrays.length - 1];
                dalLogger.info("invoke:{}, args:{}, hasError:{}, costTime:{}ms, traceId:{}", method , args, hasError, costTime, EagleEye.getTraceId());
            } catch (Exception e) {
                dalLogger.warn("invoke:" + jp.getSignature().toString(), ",costTime" + costTime + "ms" + ",traceId:" + EagleEye.getTraceId(), e);
            }
        }
    }
}

