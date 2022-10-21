package com.aliyun.iotx.haas.tdserver.dal.config;

import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author qianfan.qf
 * @date 2020/08/31
 */

@Configuration
public class RedisDataSourceConfig {

    @Value("${redis.datasource.host}")
    private String host;

    @Value("${redis.datasource.passwordEncryption}")
    private String passwordEncryption;

    @Value("${redis.datasource.port}")
    private Integer port;

    @Autowired
    private CryptographUtils cryptographUtils;

    @Bean(name = "jedisPoolConfig")
    @ConfigurationProperties(prefix = "redis.pool")
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        return config;
    }

    @Bean("jedisConnectionFactory")
    public JedisConnectionFactory jedisConnectionFactory(@Qualifier("jedisPoolConfig") JedisPoolConfig jedisPoolConfig){
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setPoolConfig(jedisPoolConfig);
        String password = cryptographUtils.decrypt(passwordEncryption);
        factory.setPassword(password);
        factory.setHostName(host);
        factory.setPort(port);
        return factory;
    }

    @Bean
    public RedisTemplate redisTemplate(@Qualifier("jedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory){
        RedisTemplate template = new RedisTemplate<String, String>();
        template.setConnectionFactory(jedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}

