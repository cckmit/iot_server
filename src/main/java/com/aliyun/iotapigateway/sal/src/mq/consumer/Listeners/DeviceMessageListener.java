package com.aliyun.iotx.haas.tdserver.sal.mq.consumer.Listeners;

import com.alibaba.fastjson.JSON;
import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ProductInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import com.aliyun.iotx.haas.tdserver.sal.mq.bo.DeviceBindMessageB0;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.AutowirableMessageListener;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.bo.LvyuanDeviceInfoBO;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.bo.LvyuanDeviceRideBO;
import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.bo.LvyuanUserInfoBO;
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



import com.aliyun.iotx.haas.tdserver.sal.vehicle.lvyuan.LvyuanHttpClient;

/**
 * @author benxiliu
 * @date 2020/08/21
 */

@Component
public class DeviceMessageListener  implements AutowirableMessageListener {

    private static final Logger mqLog = LoggerFactory.getLogger("mq");

    @Value("${mq.aliyun.haas.tdserver.message.topic}")
    private String topic;

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Override
    public String getTopic() {
        return topic;
    }

    @Resource
    private LvyuanHttpClient lvyuanHttpClient;

    @Resource
    private ProductInfoDAO productInfoDAO;

    @Resource
    private DeviceDAO deviceDAO;

    @Override
    public Action consume(Message message, ConsumeContext context) {
        mqLog.info("message received:  {}", message);

        try {
            if(!StringUtils.equalsIgnoreCase(message.getTopic(), topic)){
                return Action.CommitMessage;
            }
            String msg = new String(message.getBody(),"UTF-8");
            mqLog.info("DeviceMessageListener msg body:  {}", msg);
            DeviceBindMessageB0 messageBO = JSON.parseObject(msg, DeviceBindMessageB0.class);
            if(null == messageBO){
                return Action.CommitMessage;
            }
            String productKey = messageBO.getProductKey();
            String deviceName = messageBO.getDeviceName();
            Integer isOpenAutoLock = messageBO.getBindState();
            Long timestamp    = messageBO.getTimestamp();
            if(StringUtils.isBlank(productKey) || StringUtils.isBlank(deviceName) ||
                    null == timestamp || timestamp <=0){
                return Action.CommitMessage;
            }
            // 设备表中改变设备绑定状态
            DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(productKey, deviceName, environment);
            if (null != deviceDO) {
                deviceDO.setIsOpenAutoLock(isOpenAutoLock);
                //deviceDO.setIsBound(1);
                deviceDAO.updateDeviceInfo(deviceDO);

                ProductInfoDO productInfo = productInfoDAO.getProductInfoWithUniqueTdserverProductNameOnly(deviceDO.getUniqueTdserverProductName(), environment);
                if((productInfo != null) || (productInfo.getProductName().contains("绿源"))) {
                    LvyuanUserInfoBO lvyuanUserInfoBO = new LvyuanUserInfoBO();
                    lvyuanUserInfoBO.setProductKey(productKey);
                    lvyuanUserInfoBO.setDeviceName(deviceName);
                    lvyuanUserInfoBO.setBondkeyState(isOpenAutoLock.toString());

                    if(lvyuanHttpClient.sendDeviceUserInfo(lvyuanUserInfoBO).getCode() == IoTxCodes.SUCCESS.getCode()){
                        return Action.CommitMessage;
                    }
                }
            } else {
                mqLog.error("Device is not found when sync bind , pk:{}, dn:{}", productKey, deviceName);
            }

            return Action.ReconsumeLater;

        }catch (Exception e){
            mqLog.error("device message consumer exception", e);
        }

        return Action.CommitMessage;
    }
}

