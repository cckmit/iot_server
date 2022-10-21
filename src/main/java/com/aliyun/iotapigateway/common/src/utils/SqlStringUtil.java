package com.aliyun.iotx.haas.tdserver.common.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author imost.lwf
 * @date 2021/04/21
 */
public class SqlStringUtils {
    public static String escapeSqlKeyword(String str) {
        if (str == null) {
            return null;
        }

        str = StringEscapeUtils.escapeSql(str);
        str = StringUtils.replace(str, "\\", "/\\");
        str = StringUtils.replace(str, "%", "/%");
        str = StringUtils.replace(str, "_", "/_");
        str = StringUtils.replace(str, "[", "/[");
        return str;
    }
}

