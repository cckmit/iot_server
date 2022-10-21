package com.aliyun.iotx.haas.tdserver.sal.mq;

import java.util.List;
import java.util.Properties;

import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.AutowirableMessageListener;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.Listeners.DeviceMessageListener;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.MQType;
import com.aliyun.openservices.ons.api.MessageSelector;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Base64Utils;

/**
 * @author benxiliu
 * @date 2020/08/21
 */

@Configuration
public class MqConfig {
    @Value("${aliyun.haas.tdserver.encrypted.accessKey}")
    private String encryptedAccessKey;

    @Value("${aliyun.haas.tdserver.encrypted.accessSecret}")
    private String encryptedAccessSecret;

    @Value("${mq.aliyun.haas.tdserver.address}")
    private String address;

    @Value("${mq.aliyun.haas.tdserver.groupId}")
    private String groupId;

    @Autowired
    private CryptographUtils cryptographUtils;

    @Autowired
    private List<AutowirableMessageListener> listeners;

    @Bean("deviceMessageConsumer")
    public Consumer DeviceMessageConsumer() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.AccessKey, cryptographUtils.decrypt(encryptedAccessKey));
        properties.put(PropertyKeyConst.SecretKey, cryptographUtils.decrypt(encryptedAccessSecret));
        properties.put(PropertyKeyConst.GROUP_ID, groupId);
        properties.put(PropertyKeyConst.NAMESRV_ADDR, address);
        properties.put(PropertyKeyConst.MaxReconsumeTimes, "3");
        properties.setProperty(PropertyKeyConst.MQType, MQType.METAQ.name());
        Consumer consumer = ONSFactory.createConsumer(properties);

        listeners.forEach(listener -> {
            String topic = listener.getTopic();
            String subExpression = listener.getSubExpression();
            MessageSelector selector = listener.getMessageSelector();
            consumer.subscribe(topic, "*", listener);
            if(selector != null) {
                consumer.subscribe(topic, selector, listener);
            } else {
                consumer.subscribe(topic, subExpression, listener);
            }
        });

        consumer.start();

        return  consumer;
    }
}

