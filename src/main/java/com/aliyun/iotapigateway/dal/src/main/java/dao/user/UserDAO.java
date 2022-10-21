package com.aliyun.iotx.haas.tdserver.dal.dao.user;

import java.util.List;

import com.aliyun.iotx.haas.tdserver.dal.domain.user.UserDO;
import org.apache.ibatis.annotations.Param;

public interface UserDAO {
    Long insert(UserDO userDO);

    List<UserDO> findUserByPlatform(@Param("platformName") String platformName, @Param("rawUserId") String rawUserId);

    List<UserDO> findUserByHaasUserId(@Param("haasUserId") String haasUserId);

    Long signAgreement(@Param("haasUserId") String haasUserId);

    Long upgradeMobile(@Param("haasUserId") String haasUserId, @Param("mobile") String mobile);

    Long upgradeSmsNotification(@Param("haasUserId") String haasUserId, @Param("isUsingSmsNotification") Boolean isUsingSmsNotification);
}


