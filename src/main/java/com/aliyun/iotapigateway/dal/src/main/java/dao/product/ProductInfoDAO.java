package com.aliyun.iotx.haas.tdserver.dal.dao.product;

import java.util.List;

import com.aliyun.iotx.haas.tdserver.dal.domain.odm.OdmInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import org.apache.ibatis.annotations.Param;

import javax.validation.constraints.NotNull;

/**
 * @author benxiliu
 * @date 2020/08/25
 */
public interface ProductInfoDAO {
    /**
     * 插入新的产品记录
     *
     * @param productInfoDO
     * @return
     */
    Long insert(ProductInfoDO productInfoDO);

    /**
     * 插入新的产品记录
     *
     * @param productInfoDO
     * @return
     */
    Long updateProductInfo(ProductInfoDO productInfoDO);

    /**
     * 更新产品的品牌、型号、转换算法类型信息
     *
     * @param productKey
     * @param productBrand
     * @param productModel
     * @param productAlgoType
     * @param isSupport2G
     * @param environment
     * @return
     */
    Long updateProductSpecificInfo(@Param("productKey") String productKey,
                                   @Param("productBrand") String productBrand,
                                   @Param("productAlias") String productAlias,
                                   @Param("productModel") String productModel,
                                   @Param("productAlgoType") Integer productAlgoType,
                                   @Param("isSupport2G") Boolean isSupport2G,
                                   @Param("isDisplayEnergy") Integer isDisplayEnergy,
                                   @Param("isDistantConfig") Integer isDistantConfig,
                                   @Param("isDisplayBicycleStatus") Integer isDisplayBicycleStatus,
                                   @Param("isUsingStorageLock") Integer isUsingStorageLock,
                                   @Param("isSupportKeyModel") Integer isSupportKeyModel,
                                   @Param("defaultKeyModel") Integer defaultKeyModel,
                                   @Param("environment") String environment);

    /**
     * 查询厂商的产品列表
     *
     * @param odmTenantId
     * @param start
     * @param pageSize
     * @param environment
     * @return
     */
    List<ProductInfoDO> getProductInfoListWithOdmTenantId(@Param("odmTenantId") String odmTenantId,
                                                          @Param("start") Integer start,
                                                          @Param("pageSize") Integer pageSize,
                                                          @Param("environment") String environment);

    /**
     * 查询厂商的产品列表
     *
     * @param odmTenantId
     * @param start
     * @param pageSize
     * @param brandLike
     * @param modelLike
     * @param environment
     * @return
     */
    List<ProductInfoDO> getProductInfoListWithOdmTenantIdLike(@Param("odmTenantId") String odmTenantId,
                                                              @Param("start") Integer start,
                                                              @Param("pageSize") Integer pageSize,
                                                              @Param("brandLike") String brandLike,
                                                              @Param("modelLike") String modelLike,
                                                              @Param("environment") String environment);

    /**
     * 查询厂商的产品数量
     *
     * @param odmTenantId
     * @param brandLike
     * @param modelLike
     * @param environment
     * @return
     */
    Long getProductCountWithOdmTenantIdLike(@Param("odmTenantId") String odmTenantId,
                                            @Param("brandLike") String brandLike,
                                            @Param("modelLike") String modelLike,
                                            @Param("environment") String environment);

    /**
     * 查询厂商的产品信息
     *
     * @param odmTenantId
     * @param productName
     * @param environment
     * @return
     */
    ProductInfoDO getProductInfoWithProductName(@Param("odmTenantId") String odmTenantId,
                                                @Param("productName") String productName,
                                                @Param("environment") String environment);

    /**
     * 查询厂商的产品信息
     *
     * @param odmTenantId
     * @param productKey
     * @param environment
     * @return
     */
    ProductInfoDO getProductInfoWithProductKey(@Param("odmTenantId") String odmTenantId,
                                               @Param("productKey") String productKey,
                                               @Param("environment") String environment);

    /**
     * 查询厂商的产品信息
     *
     * @param odmTenantId
     * @param productKey
     * @param uniqueTdserverProductName
     * @param environment
     * @return
     */
    ProductInfoDO getProductInfoWithProductKeyAndUniqueTdserverProductName(@Param("odmTenantId") String odmTenantId,
                                                                           @Param("productKey") String productKey,
                                                                           @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                                                           @Param("environment") String environment);

    /**
     * 查询厂商的产品信息
     *
     * @param productKey
     * @param environment
     * @return
     */
    ProductInfoDO getProductInfoWithProductKeyOnly(@Param("productKey") String productKey,
                                                   @Param("environment") String environment);

    /**
     * 查询厂商的产品信息
     *
     * @param uniqueTdserverProductName
     * @param environment
     * @return
     */
    ProductInfoDO getProductInfoWithUniqueTdserverProductNameOnly(@Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                                         @Param("environment") String environment);


    /**
     * 查询厂商的产品信息
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param environment
     * @return
     */
    ProductInfoDO getProductInfoWithUniqueTdserverProductName(@Param("odmTenantId") String odmTenantId,
                                                              @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                                              @Param("environment") String environment);


    /**
     * 删除厂商的产品信息
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param environment
     * @return
     */
    Long deleteProductWithUniqueTdserverProductName(@Param("odmTenantId") String odmTenantId,
                                                    @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                                    @Param("environment") String environment);

    /**
     * 更新广告图片url
     * @param odm_tenant_id
     * @param image_url
     * @param environment
     * @return
     */
    Long updateProductAdvertiseImage(@Param("odm_tenant_id") String odm_tenant_id,
                                    @Param("image_url") String image_url,
                                    @Param("environment") String environment);

    /**
     * 更新广告链接的
     * @param odm_tenant_id
     * @param advertisem_url
     * @param environment
     * @return
     */
    Long updateProductAdvertiseUrl(@Param("odm_tenant_id") String odm_tenant_id,
                                    @Param("advertisem_url") String advertisem_url,
                                    @Param("environment") String environment);
}

