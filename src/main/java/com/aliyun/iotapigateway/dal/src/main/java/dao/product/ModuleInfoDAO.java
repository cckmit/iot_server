package com.aliyun.iotx.haas.tdserver.dal.dao.product;

import com.aliyun.iotx.haas.tdserver.dal.domain.product.ModuleInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author imost.lwf
 * @date 2021/1/7
 */
public interface ModuleInfoDAO {
    /**
     * 插入新的模块记录
     * @param moduleInfoDO
     * @return
     */
    Long insert(ModuleInfoDO moduleInfoDO);

    /**
     * 更新模块数据
     * @param moduleInfoDO
     * @return
     */
    Long updateModuleInfo(ModuleInfoDO moduleInfoDO);

    /**
     * 查询厂商的模块列表
     * @param odmTenantId
     * @param start
     * @param pageSize
     * @param environment
     * @return
     */
    List<ModuleInfoDO> getModuleInfoListWithOdmTenantId(@Param("odmTenantId") String odmTenantId,
                                                        @Param("start") Integer start,
                                                        @Param("pageSize") Integer pageSize,
                                                        @Param("environment") String environment);

    /**
     * 查询厂商的产品信息
     * @param odmTenantId
     * @param uniqueModuleName
     * @param environment
     * @return
     */
    ModuleInfoDO getModuleInfoWithUniqueModuleName(@Param("odmTenantId") String odmTenantId,
                                                   @Param("uniqueModuleName") String uniqueModuleName,
                                                   @Param("environment") String environment);

    /**
     * 查询厂商的产品信息
     * @param uniqueModuleName
     * @param environment
     * @return
     */
    ModuleInfoDO getModuleInfoWithUniqueModuleNameOnly(@Param("uniqueModuleName") String uniqueModuleName,
                                                        @Param("environment") String environment);

    /**
     * 查询厂商的模块信息
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param environment
     * @return
     */
    ModuleInfoDO getModuleInfoWithUniqueTdserverProductName(@Param("odmTenantId") String odmTenantId,
                                                            @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                                            @Param("environment") String environment);

    /**
     * 删除厂商模块的信息
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param environment
     * @return
     */
    Long deleteModuleWithUniqueTdserverProductName(@Param("odmTenantId") String odmTenantId,
                                                   @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                                   @Param("environment") String environment);
}

