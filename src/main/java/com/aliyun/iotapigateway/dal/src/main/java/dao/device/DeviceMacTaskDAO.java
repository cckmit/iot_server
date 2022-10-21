package com.aliyun.iotx.haas.tdserver.dal.dao.device;

import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceMacDO;
import org.apache.ibatis.annotations.Param;

/**
 * @author imost.lwf
 * @date 2021/04/18
 */
public interface DeviceMacTaskDAO {
    /**
     * 插入新的设备批次信息
     *
     * @param deviceMacDO 设备mac资源项
     * @return
     */
    Long insert(DeviceMacDO deviceMacDO);

    /**
     * 更新设备批次信息
     *
     * @param deviceMacDO 设备mac资源项
     * @return
     */
    Long update(DeviceMacDO deviceMacDO);


    /**
     * 获取恰好满足数量的mac资源段
     *
     * @param macCount    mac数量
     * @param macStatus   资源段状态
     * @param environment 数据环境
     * @return
     */
    DeviceMacDO getDeviceMacWithExactlyNumForUpdate(@Param("macCount") Long macCount,
                                                    @Param("macStatus") String macStatus,
                                                    @Param("environment") String environment);

    /**
     * 获取足够的mac资源段
     *
     * @param macCount    mac数量
     * @param macStatus   资源段状态
     * @param environment 数据环境
     * @return
     */
    DeviceMacDO getDeviceMacWithEnoughNumForUpdate(@Param("macCount") Long macCount,
                                                   @Param("macStatus") String macStatus,
                                                   @Param("environment") String environment);

    /**
     * 根据申请ID获取mac资源段
     *
     * @param batchApplyId  mac地址申请ID
     * @param macStatus   资源段状态
     * @param environment 数据环境
     * @return
     */
    DeviceMacDO getDeviceMacWithMacApplyIdForUpdate(@Param("batchApplyId") Long batchApplyId,
                                                    @Param("macStatus") String macStatus,
                                                    @Param("environment") String environment);

    /**
     * 根据产品名和地址段获取mac资源信息
     *
     * @param startMac                  mac地址申请ID
     * @param endMac                    资源段状态
     * @param uniqueTdserverProductName 出行唯一产品名
     * @param environment               数据环境
     * @return
     */
    DeviceMacDO getDeviceMacForUpdate(@Param("startMac") String startMac,
                                      @Param("endMac") String endMac,
                                      @Param("uniqueTdserverProductName") String uniqueTdserverProductName,
                                      @Param("environment") String environment);
}

