package com.aliyun.iotx.haas.tdserver.dal.domain.product;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author benxiliu
 * @date 2020/08/25
 */

@Data
@NoArgsConstructor
public class ProductInfoDO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 产品pk
     */
    private String productKey;

    /**
     * 产品名
     */
    private String productName;

    /**
     * 平台产品名
     */
    private String uniqueProductName;

    /**
     * 所属ODM身份Id
     */
    private String odmTenantId;

    /**
     * 产品所属环境
     */
    private String environment;

    /**
     * 是否删除 0：未删除，1：删除
     */
    private Integer isDeleted;

    /**
     * 产品品牌
     */
    private String productBrand;

    /**
     * 产品类型
     */
    private String productModel;

    /**
     * 产品电量换算算法类别
     */
    private Integer productAlgoType;

    /**
     * 产品是否支持2G
     */
    private Boolean isSupport2G;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 出行平台唯一产品名
     */
    private String uniqueTdserverProductName;

    /**
     * 警告电量时电压
     */
    private Float percentage0;

    /**
     * 20%电量时电压
     */
    private Float percentage20;

    /**
     * 40%电量时电压
     */
    private Float percentage40;

    /**
     * 60%电量时电压
     */
    private Float percentage60;

    /**
     * 80%电量时电压
     */
    private Float percentage80;

    /**
     * 产品状态
     */
    private String productStatus;

    /**
     * 产品图片OSS文件ID
     */
    private String productImageFileId;

    /**
     * 广告图片OSS文件filedid
     */
    private String image_url;

    /**
     * 广告链接
     */
    private String advertisem_url;

    /**
     * 车辆车轮轮径(寸)
     */
    private Integer productWheelDiameter;

    /**
     * 产品别名
     */
    private String productAlias;

    /**
     * 车辆是否显示电压/电量
     */
    private Integer isDisplayEnergy;

    /**
     * 车辆是否支持无感自定义
     */
    private Integer isDistantConfig;

    /**
     * 车辆是否支持车辆状态显示
     */
    private Integer isDisplayBicycleStatus;

    /**
     * 是否开启储物锁功能
     */
    private Integer isUsingStorageLock;

    /**
     * 是否支持配置按键模式
     */
    private Integer isSupportKeyModel;

    /**
     * 默认按键模式
     */
    private Integer defaultKeyModel;
}

