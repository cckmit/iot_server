package com.aliyun.iotx.haas.tdserver.common.config;

import com.aliyun.iotx.haas.tdserver.common.security.GeneralCryptograph;
import com.aliyun.iotx.haas.tdserver.common.security.impl.GeneralCryptographFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author benxiliu
 * @date 2019/12/06
 */

//@Configuration
public class SecurityConfig {
    @ConfigurationProperties(prefix = "security.system")
    @Bean(name = "generalCryptographFactoryBean")
    public GeneralCryptographFactoryBean generalCryptographFactoryBean() {
        GeneralCryptographFactoryBean factoryBean = new GeneralCryptographFactoryBean();
        return factoryBean;
    }

    @Bean
    public GeneralCryptograph generalCryptograph(@Qualifier("generalCryptographFactoryBean")
        GeneralCryptographFactoryBean factoryBean) throws Exception {
        return factoryBean.getObject();
    }
}

