package com.aliyun.iotx.haas.tdserver.common.constants;

public class LvyuanConstants {
    // 绿源推送序列号长度
    public static final Integer RAMDOM_NUM_LENGTH = 10;
    // 绿源key标签
    public static final String LVYUAN_HTTP_POST_TAG_APP_ID = "appId";
    // 绿源secret标签
    public static final String LVYUAN_HTTP_POST_TAG_SECRET = "secret";
    // 绿源推送序列号标签
    public static final String LVYUAN_HTTP_POST_TAG_SERIAL_NUM = "serialNumber";
    // 绿源消息体标签
    public static final String LVYUAN_HTTP_POST_TAG_BODY = "body";
    // 绿源签名标签
    public static final String LVYUAN_HTTP_POST_TAG_SIGN = "sign";
    // 绿源用户接口参数名称
    public static final String LVYUAN_HTTP_POST_ARG_USER_REQUEST = "lvyuanUserInfoDTO";
    // 绿源URL
    public static final String LVYUAN_HTTP_DEFAULT_URL = "http://api.lyiot.top:12308";
    // 绿源用户信息上报方法路径
    public static final String LVYUAN_HTTP_USER_INFO_PATH = "/api/cooperation/aliyun/user";
    // 绿源设备信息上报方法路径
    public static final String LVYUAN_HTTP_DEVICE_INFO_PATH = "/api/cooperation/aliyun/sampling/cycling";
}

