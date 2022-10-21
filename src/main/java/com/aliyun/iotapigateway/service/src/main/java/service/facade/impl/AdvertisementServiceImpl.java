package com.aliyun.iotx.haas.tdserver.service.impl;

import akka.io.TcpConnection;
import com.alibaba.boot.hsf.annotation.HSFProvider;
import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ProductInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import com.aliyun.iotx.haas.tdserver.facade.DeviceManageService;
import com.aliyun.iotx.haas.tdserver.facade.UserService;
import com.aliyun.iotx.haas.tdserver.facade.ads.AdvertisementService;
import com.aliyun.iotx.haas.tdserver.facade.dto.ads.AdvertisementItemDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceBindAndShareInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.product.ProductAndModuleInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.enums.FileTypeEnum;
import com.aliyun.iotx.haas.tdserver.sal.oss.OssClient;
import com.aliyun.oss.OSS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author imost.lwf
 * @date 2020/12/10
 */
@HSFProvider(serviceInterface = AdvertisementService.class)
public class AdvertisementServiceImpl implements AdvertisementService {
    private final String IMAGE_URL = "https://tdserver.oss-cn-shanghai.aliyuncs.com/E9A02492-CCF7-459F-B755-A74121271E69.png";

    private final String ADS_TMAIL_URL = "https://pages.tmall.com/wow/a/act/tmall/tmc/31953/4670/wupr?wh_pid=industry-242459&disableNav=YES&status_bar_transparent=true";

    private final String ADS_TAOBAO_URL = "https://pages.tmall.com/wow/a/act/tmall/tmc/31953/4670/wupr?wh_pid=industry-242459&disableNav=YES&status_bar_transparent=true";

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceManageService deviceManageService;

    @Autowired
    private ProductInfoDAO productInfoDAO;

    @Autowired
    private DeviceDAO deviceDAO;

    @Resource
    OssClient ossClient;

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    private static final Logger log = LoggerFactory.getLogger("sql");

    @Override
    public IoTxResult<List<AdvertisementItemDTO>> queryAdsResoucre(String haasUserId) {

        // 验证用户真实性
        List<AdvertisementItemDTO> advertisementItemDTOList = new ArrayList<>();

        try {
            // 获取厂商广告
            AdvertisementItemDTO advertisementCustomer = new AdvertisementItemDTO();
            if (userService.info(haasUserId).getCode() == IoTxCodes.SUCCESS.getCode()) {
                IoTxResult<DeviceBindAndShareInfoDTO> bindDeviceInfoListResult = deviceManageService.queryDeviceAndShareByUser(haasUserId);
                if ((bindDeviceInfoListResult.getCode() == IoTxCodes.SUCCESS.getCode()) && (bindDeviceInfoListResult != null)) {
                    DeviceBindAndShareInfoDTO deviceBindAndShareDto = bindDeviceInfoListResult.getData();
                    if (deviceBindAndShareDto != null && !deviceBindAndShareDto.getBindDeviceList().isEmpty()) {
                        DeviceInfoDTO deviceDTO = deviceBindAndShareDto.getBindDeviceList().get(0);
                        if (deviceDTO != null) {
                            DeviceDO device = deviceDAO.getDeviceWithProductKeyAndDeviceName(deviceDTO.getProductKey(), deviceDTO.getDeviceName(), environment);
                            if (device != null) {
                                //使用tdserver的图片
                                ProductInfoDO productInfoDO = productInfoDAO.getProductInfoWithUniqueTdserverProductName(device.getOdmTenantId(), device.getUniqueTdserverProductName(), environment);
                                if (productInfoDO != null) {
                                    if (!(productInfoDO.getImage_url().isEmpty() || productInfoDO.getAdvertisem_url().isEmpty())) {
                                        if (ossClient.checkOdmPictureFileExist(productInfoDO.getOdmTenantId(), "", productInfoDO.getImage_url(), FileTypeEnum.PICTURE_PNG.getCode())) {
                                            advertisementCustomer.setImageUrl(ossClient.getOdmPictureFileUrl(productInfoDO.getOdmTenantId(), "", productInfoDO.getImage_url(), FileTypeEnum.PICTURE_PNG.getCode(), FileTypeEnum.PICTURE_PNG.getCode()));
                                            advertisementCustomer.setAdvertisementUrl(productInfoDO.getAdvertisem_url());
                                            advertisementItemDTOList.add(advertisementCustomer);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            /*天猫广告*/
            AdvertisementItemDTO advertisementItemDTO = new AdvertisementItemDTO();
            advertisementItemDTO.setImageUrl(IMAGE_URL);
            advertisementItemDTO.setAdvertisementUrl(ADS_TAOBAO_URL);
            advertisementItemDTOList.add(advertisementItemDTO);
            return new IoTxResult<>(advertisementItemDTOList);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }
}

