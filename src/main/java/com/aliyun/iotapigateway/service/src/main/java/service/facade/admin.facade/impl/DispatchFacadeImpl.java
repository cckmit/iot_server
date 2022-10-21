package com.aliyun.iotx.haas.tdserver.service.admin.facade.impl;

import com.alibaba.boot.hsf.annotation.HSFProvider;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.iotx.common.base.service.IoTxResult;

import com.aliyun.iotx.haas.common.base.utils.IoTxResultUtil;
import com.aliyun.iotx.haas.tdserver.admin.facade.model.DispatchHttpRequest;
import com.aliyun.iotx.haas.tdserver.admin.facade.service.DispatchFacade;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.service.admin.facade.enums.HttpRequestServiceEnum;
import com.aliyun.iotx.haas.tdserver.service.annotation.DispatchParam;
import com.aliyun.iotx.haas.tdserver.service.utils.ApplicationUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangheng
 * @date 21/1/11
 * 小二后台通过iotx-paas-admin统一回调的入口,目前只会处理请求body内的参数
 */
@HSFProvider(serviceInterface = DispatchFacade.class)
public class DispatchFacadeImpl implements DispatchFacade {

    private static final Logger errorLogger = LoggerFactory.getLogger(DispatchFacade.class);

    @Autowired
    private ApplicationUtils applicationUtils;

    private static final String DEFAULT_USER_NAME = "admin";

    @Override
    public IoTxResult<Object> dispatchHttpRequest(DispatchHttpRequest request) {
        try {
            String uri = request.getRequestURI();
            HttpRequestServiceEnum serviceEnum = HttpRequestServiceEnum.getServiceEnum(uri);
            if (null == serviceEnum || null == serviceEnum.getClazz() || StringUtils.isBlank(serviceEnum.getMethodName())) {
                return IoTxResultUtil.error(HaasIoTxCodes.ERROR_NOT_FOUND_METHOD);
            }
            Class<?> clazz = serviceEnum.getClazz();
            String methodName = serviceEnum.getMethodName();
            Object target = applicationUtils.getBean(clazz);
            if (null == target) {
                return IoTxResultUtil.error(HaasIoTxCodes.ERROR_NOT_FOUND_CLASS);
            }
            Map<String, Object> args = JSON.parseObject(request.getRequestBody(), new TypeReference<Map<String, Object>>() {});
            injectEmpInfo(request, args, serviceEnum);
            Object invokeResult = invoke(clazz, target, methodName, args, serviceEnum.getParameterTypes());
            if (null == invokeResult) {
                return IoTxResultUtil.error(HaasIoTxCodes.SYSTEM_ERROR);
            }
            if (invokeResult instanceof IoTxResult) {
                return (IoTxResult<Object>) invokeResult;
            } else {
                return IoTxResultUtil.success(invokeResult);
            }
        } catch (Exception e) {
            errorLogger.error("dispatchHttpRequest", e);
        }
        return IoTxResultUtil.error(HaasIoTxCodes.SYSTEM_ERROR);
    }

    private Object invoke(Class<?> clazz, Object target, String methodName, Map<String, Object> args, Class<?>... parameterTypes) throws Exception {
        if (parameterTypes != null && parameterTypes.length > 0) {
            return invoke0(clazz, target, methodName, args, parameterTypes);

        } else {
            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                if (!StringUtils.equals(methodName, m.getName())) {
                    continue;
                }
                Type[] types = m.getParameterTypes();
                if (types.length <= 0) {
                    return m.invoke(target);
                }
                if (types.length == 1 && isNotExcludeClazz(m.getParameterTypes()[0])) {
                    //必须先转为Object才能通过invoke调用,比如type[0]是Map类型,无法直接转为Object...args
                    Object argsOj = JSON.parseObject(JSON.toJSONString(args), types[0]);
                    return m.invoke(target, argsOj);
                }
                return invoke0(clazz, target, methodName, args, m.getParameterTypes());
            }
        }
        return null;

    }

    private Object invoke0(Class<?> clazz, Object target, String methodName, Map<String, Object> args, Class<?>... parameterTypes) throws Exception {
        Method method = clazz.getMethod(methodName, parameterTypes);
        Parameter[] parameters = method.getParameters();
        Object[] paramValues = new Object[parameterTypes.length];
        if (parameterTypes.length == 1 && isNotExcludeClazz(parameterTypes[0])) {
            return method.invoke(target, JSON.parseObject(JSON.toJSONString(args), parameterTypes[0]));
        }
        for (int index = 0; index < parameters.length; index++) {
            DispatchParam dispatchParam = parameters[index].getDeclaredAnnotation(DispatchParam.class);
            if (null == dispatchParam) {
                throw new RuntimeException("dispatch method param must decorate with @DispatchParam");
            }
            String paramName = dispatchParam.value();
            paramValues[index] = args.get(paramName);
        }
        return method.invoke(target, paramValues);
    }

    private void injectEmpInfo(DispatchHttpRequest request, Map<String, Object> args, HttpRequestServiceEnum serviceEnum) {
        DispatchHttpRequest.User user = request.getUser();
        if (user != null) {
            args.put("empId", user.getEmpId());
            args.put("empName", user.getEmpName());
            if(args.get("userId") == null && serviceEnum.getInjectUserInfo()){
                args.put("userId", user.getEmpId());
                args.put("userName", DEFAULT_USER_NAME);
            }
        }
    }

    private boolean isNotExcludeClazz(Class<?>... parameterTypes) {
        if (null == parameterTypes || parameterTypes.length <= 0) {
            return true;
        }
        for (Class<?> clazz : parameterTypes) {
            if (excludeClazzSet.contains(clazz)) {
                return false;
            }
        }
        return true;
    }

    private static final Set<Class<?>> excludeClazzSet = new HashSet<Class<?>>() {
        {
            add(String.class);
            add(Integer.class);
            add(Float.class);
            add(Double.class);
            add(Long.class);
            add(Short.class);
            add(Boolean.class);
            add(List.class);
            add(Set.class);
            add(int.class);
            add(double.class);
            add(float.class);
            add(long.class);
            add(short.class);
            add(boolean.class);
            add(char.class);
        }
    };
}

