package com.aliyun.iotx.haas.tdserver.admin.facade.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangheng
 * @date 21/1/11
 */
public class DispatchHttpRequest implements Serializable {

    private static final long serialVersionUID = 3801579725451907054L;
    private String requestURI;
    private String queryString;
    private String method;
    private Map<String, String[]> parameterMap = new HashMap<>();
    private String requestBody;
    private User user;

    public static class User {
        private String empId;
        private String empName;

        public String getEmpId() {
            return empId;
        }

        public void setEmpId(String empId) {
            this.empId = empId;
        }

        public String getEmpName() {
            return empName;
        }

        public void setEmpName(String empName) {
            this.empName = empName;
        }
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, String[]> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

