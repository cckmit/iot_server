package com.aliyun.iotx.haas.tdserver.sal.mq.bo;

/**
 * @author zhangheng
 * @date 20/9/21
 * {"nsew":0, "latitude":22.68477,"time":"1600677005000", "productKey":"a18fXiDkHmB","deviceName":"ed:22:93:52:ba:08","longitude":113.98908,"status":2}
 */
public class GpsPositionMessageBO extends AbstractMessageBO{

    private static final long serialVersionUID = -7449712219063018041L;



    private Double latitude;

    private Double longitude;

    private String haasUerId;

    /**
     * 方向(0-East-North,1-East-South,2-West-North,3-West-South,255-不支持)
     */
    private Integer nsew;

    /**
     * 0-GPS常规数据,1-GPS补传数据,2-GPS休眠数据
     */
    private Integer status;


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getHaasUerId() {
        return haasUerId;
    }

    public void setHaasUerId(String haasUerId) {
        this.haasUerId = haasUerId;
    }

    public Integer getNsew() {
        return nsew;
    }

    public void setNsew(Integer nsew) {
        this.nsew = nsew;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

