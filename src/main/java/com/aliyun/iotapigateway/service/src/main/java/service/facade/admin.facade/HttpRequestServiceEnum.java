package com.aliyun.iotx.haas.tdserver.service.admin.facade.enums;

import com.aliyun.iotx.haas.tdserver.facade.request.StatisticsRequest;
import com.aliyun.iotx.haas.tdserver.service.admin.facade.StatisticsService;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * iotx-paas-admin请求uri和service中的方法映射
 * methodName参数可以是单个组合对象或多个使用@DispatchParam修饰的参数
 *
 * @author zhangheng
 * @date 21/1/11
 */
public enum HttpRequestServiceEnum {
    STATISTICS_QUERY("/api/tdserver/statistics/query", StatisticsService.class, false, StatisticsRequest.class),
    ;

    static Map<String, HttpRequestServiceEnum> SERVICE_POOL = new HashMap<>(8);

    static {
        for (HttpRequestServiceEnum serviceEnum : HttpRequestServiceEnum.values()) {
            SERVICE_POOL.put(serviceEnum.getUri(), serviceEnum);
        }
    }

    public static HttpRequestServiceEnum getServiceEnum(String uri) {
        if (StringUtils.isBlank(uri)) {
            return null;
        }
        return SERVICE_POOL.get(uri);
    }

    HttpRequestServiceEnum(String uri, Class<?> clazz) {
        this.uri = uri;
        this.clazz = clazz;
        if (StringUtils.isNotBlank(uri)) {
            int index = uri.lastIndexOf("/");
            this.methodName = uri.substring(index + 1);
        }
        this.injectUserInfo = false;
    }

    HttpRequestServiceEnum(String uri, Class<?> clazz, String methodName, Boolean injectUserInfo) {
        this.uri = uri;
        this.clazz = clazz;
        this.methodName = methodName;
        this.injectUserInfo = injectUserInfo;
    }

    HttpRequestServiceEnum(String uri, Class<?> clazz, Boolean injectUserInfo, Class<?>... parameterTypes) {
        this.uri = uri;
        this.clazz = clazz;
        if (StringUtils.isNotBlank(uri)) {
            int index = uri.lastIndexOf("/");
            this.methodName = uri.substring(index + 1);
        }
        this.injectUserInfo = injectUserInfo;
        this.parameterTypes = parameterTypes;
    }

    HttpRequestServiceEnum(String uri, Class<?> clazz, String methodName, Boolean injectUserInfo, Class<?>... parameterTypes) {
        this.uri = uri;
        this.clazz = clazz;
        this.methodName = methodName;
        this.injectUserInfo = injectUserInfo;
        this.parameterTypes = parameterTypes;
    }

    private String uri;

    private Class<?> clazz;

    private String methodName;

    /**
     * 请求参数类型,不指定时,会根据方法名查找第一个同名方法进行回调
     */
    private Class<?>[] parameterTypes;

    /**
     * 是否需要注入用户信息
     */
    private Boolean injectUserInfo;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Boolean getInjectUserInfo() {
        return injectUserInfo;
    }

    public void setInjectUserInfo(Boolean injectUserInfo) {
        this.injectUserInfo = injectUserInfo;
    }
}

