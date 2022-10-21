package com.aliyun.iotx.haas.tdserver.dal.dao.device;

import java.util.List;

import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBatchInfoDO;
import org.apache.ibatis.annotations.Param;

/**
 * @author benxiliu
 * @date 2020/09/02
 */

public interface DeviceBatchInfoDAO {
    /**
     * 插入新的设备批次信息
     *
     * @return
     */
    Long insert(DeviceBatchInfoDO deviceBatchInfoDO);

    /**
     * 更新batchId状态
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param batchId
     * @param batchStatus
     * @param environment
     * @return
     */
    Long updateBatchStatus(@Param("odmTenantId") String odmTenantId,
                           @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                           @Param("batchId") Long batchId,
                           @Param("batchStatus") String batchStatus,
                           @Param("environment") String environment);

    /**
     * 查询当前batchId
     *
     * @param odmTenantId
     * @param productKey
     * @param batchId
     * @param environment
     * @return
     */
    DeviceBatchInfoDO getDeviceBatchInfoWithBatchId(@Param("odmTenantId") String odmTenantId,
                                                    @Param("productKey") String productKey,
                                                    @Param("batchId") Long batchId,
                                                    @Param("environment") String environment);

    /**
     * 查询当前batchId
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param batchId
     * @param environment
     * @return
     */
    DeviceBatchInfoDO getDeviceBatchInfoWithBatchIdAndProductId(@Param("odmTenantId") String odmTenantId,
                                                                @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                                                @Param("batchId") Long batchId,
                                                                @Param("environment") String environment);

    /**
     * 查询产品的设备批次列表
     *
     * @param odmTenantId
     * @param productKey
     * @param start
     * @param pageSize
     * @param environment
     *
     * @return
     */
    List<DeviceBatchInfoDO> getDeviceBatchInfoListWithOdmTenantId(@Param("odmTenantId") String odmTenantId,
                                                                  @Param("productKey") String productKey,
                                                                  @Param("start") Integer start,
                                                                  @Param("pageSize") Integer pageSize,
                                                                  @Param("environment") String environment);

    /**
     * 查询产品的设备批次列表
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param start
     * @param pageSize
     * @param environment
     *
     * @return
     */
    List<DeviceBatchInfoDO> getDeviceBatchInfoListWithProductId(@Param("odmTenantId") String odmTenantId,
                                                                @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                                                @Param("start") Integer start,
                                                                @Param("pageSize") Integer pageSize,
                                                                @Param("environment") String environment);

    /**
     * 查询产品的设备批次数量
     *
     * @param odmTenantId
     * @param uniqueTdserverProductName
     * @param environment
     *
     * @return
     */
    Long getDeviceBatchCountWithProductId(@Param("odmTenantId") String odmTenantId,
                                          @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                          @Param("environment") String environment);

    /**
     * 根据状态查询产品的设备批次列表
     *
     * @param batchStatus 批次状体
     * @param start       页索引
     * @param pageSize    页大小
     * @param environment 数据环境
     * @return
     */
    List<DeviceBatchInfoDO> getDeviceBatchInfoListWithStatus(@Param("batchStatus") String batchStatus,
                                                             @Param("start") Integer start,
                                                             @Param("pageSize") Integer pageSize,
                                                             @Param("environment") String environment);
}

