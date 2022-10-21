package com.aliyun.iotx.haas.tdserver.dal.config;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
//import com.alibaba.druid.pool.DruidDataSource;
/**
 * @author benxiliu
 * @date 2020/08/18
 */

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.aliyun.iotx.haas.tdserver.dal.dao", sqlSessionFactoryRef = "rdsSqlSessionFactory")
public class RdsDataSourceConfig {
    @Value("${rds.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${rds.datasource.url}")
    private String url;

    @Value("${rds.datasource.usernameEncryption}")
    private String userNameEncryption;

    @Value("${rds.datasource.passwordEncryption}")
    private String passwordEncryption;

    @Autowired
    private CryptographUtils cryptographUtils;

    @Bean(name = "rdsDataSource")
    public DataSource rdsDataSource() {
        //DriverManagerDataSource dataSource = new DriverManagerDataSource();
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(cryptographUtils.decrypt(userNameEncryption));
        dataSource.setPassword(cryptographUtils.decrypt(passwordEncryption));
        return dataSource;
    }

    @Bean(name = "rdsSqlSessionFactory")
    public SqlSessionFactory rdsSqlSessionFactory(@Qualifier("rdsDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/**/*.xml"));
        bean.setTypeAliasesPackage("com.aliyun.iotx.haas.tdserver.dal.dao");
        return bean.getObject();
    }

    @Bean(name = "rdsSqlSessionTemplate")
    public SqlSessionTemplate rdsSqlSessionTemplate(
            @Qualifier("rdsSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}

