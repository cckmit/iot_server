package com.aliyun.iotx.haas.tdserver.sal.mq.consumer.Listeners;

import com.alibaba.fastjson.JSON;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ProductInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import com.aliyun.iotx.haas.tdserver.sal.mq.bo.LockStateMobileMessageBO;
import com.aliyun.iotx.haas.tdserver.sal.mq.bo.LvyuanDeviceRideMessageBO;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.AutowirableMessageListener;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.LvyuanHttpClient;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.bo.LvyuanDeviceRideBO;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.dto.LvyuanDeviceDataDTO;
import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author imost.lwf
 * @date 2021/02/02
 */
@Component
public class LvyuanDeviceRideListener  implements AutowirableMessageListener {
    private static final Logger mqLog = LoggerFactory.getLogger("sal");

    @Value("${mq.aliyun.haas.tdserver.lvyuan.device.ride.topic}")
    private String topic;

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Resource
    ProductInfoDAO productInfoDAO;

    @Autowired
    private DeviceDAO deviceDAO;

    @Resource
    private LvyuanHttpClient lvyuanHttpClient;

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        try{
            if(!StringUtils.equalsIgnoreCase(message.getTopic(), topic)){
                return Action.CommitMessage;
            }
            String msg = new String(message.getBody(),"UTF-8");
            mqLog.info("msg body111:  {}", msg);


            LvyuanDeviceRideMessageBO messageBO = JSON.parseObject(msg, LvyuanDeviceRideMessageBO.class);
            if(null == messageBO){
                return Action.CommitMessage;
            }

            String productKey = messageBO.getProductKey();
            String deviceName = messageBO.getDeviceName();

            Long timestamp = messageBO.getTimestamp();

            if(StringUtils.isBlank(productKey) || StringUtils.isBlank(deviceName)){
                return Action.CommitMessage;
            }

            DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(productKey, deviceName, environment);
            if (deviceDO == null) {
                return Action.CommitMessage;
            }
            /*查询厂商*/
            ProductInfoDO productInfo = productInfoDAO.getProductInfoWithUniqueTdserverProductNameOnly(deviceDO.getUniqueTdserverProductName(), environment);

            if((productInfo == null) || (!productInfo.getProductName().contains("绿源")))
            {
                return Action.CommitMessage;
            }

            LvyuanDeviceRideBO lvyuanDeviceRideBO = new LvyuanDeviceRideBO();
            BeanUtils.copyProperties(messageBO, lvyuanDeviceRideBO);
            if(lvyuanHttpClient.sendDeviceInfo(lvyuanDeviceRideBO).hasSucceeded()){
                return Action.CommitMessage;
            }
            return Action.ReconsumeLater;

        }catch (Exception e){
            mqLog.error("LvyuanDeviceRideListener consumer exception", e);
        }

        return Action.CommitMessage;
    }
}

