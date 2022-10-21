package com.aliyun.iotx.haas.tdserver.dal.dao.odm;

import com.aliyun.iotx.haas.tdserver.dal.domain.odm.OdmInfoDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author benxiliu
 * @date 2020/09/08
 */

public interface OdmInfoDAO {
    /**
     * 插入新的Odm信息
     * @return
     */
    Long insert(OdmInfoDO odmInfoDO);

    /**
     * 根据租户id查询OdmgetOdmInfoWithOdmTenantId
     * @param odmTenantId
     * @return
     */
    OdmInfoDO getOdmInfoWithOdmTenantId(@Param("odmTenantId") String odmTenantId);

    /**
     * 更新Odm信息
     * @param odmInfoDO
     * @return
     */
    Long updateOdmInfo(OdmInfoDO odmInfoDO);

    List<OdmInfoDO> getAllOdmInfo();
}

