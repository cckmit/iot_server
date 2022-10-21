package com.aliyun.iotx.haas.tdserver.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zhangheng
 * @date 20/9/4
 */
public enum UpgradeTargetSelectionEnum {

    /**
     * 全量
     */
    ALL,
    /**
     * 定向
     */
    SPECIFIC,
    /**
     * 灰度
     */
    GRAY
    ;

    public static boolean contain(String selection){
        if(StringUtils.isBlank(selection)){
            return false;
        }
        return selection.equalsIgnoreCase(ALL.name()) || selection.equalsIgnoreCase(SPECIFIC.name())
            ||selection.equalsIgnoreCase(GRAY.name());
    }

}

