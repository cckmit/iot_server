package com.aliyun.iotx.haas.tdserver.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.alibaba.boot.hsf.annotation.HSFProvider;
import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.hitsdb.client.value.response.MultiFieldQueryLastResult;
import com.aliyun.hitsdb.client.value.response.MultiFieldQueryResult;
import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.common.base.result.Result;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;

import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.enums.LockStateMobileEnum;
import com.aliyun.iotx.haas.tdserver.common.enums.TslFieldsEnum;
import com.aliyun.iotx.haas.tdserver.common.utils.IoTxResultUtil;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceShareDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceTrackDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ModuleInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ProductInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceShareDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceTrackDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ModuleInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import com.aliyun.iotx.haas.tdserver.facade.DeviceManageService;
import com.aliyun.iotx.haas.tdserver.facade.UserService;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceBindAndShareInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.PageResultDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceDetailDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DevicePropertyStatusDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.gps.*;
import com.aliyun.iotx.haas.tdserver.facade.enums.FileTypeEnum;
import com.aliyun.iotx.haas.tdserver.sal.amap.AmapLbsManager;
import com.aliyun.iotx.haas.tdserver.sal.iot.IoTClient;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.Listeners.GpsPositionMessageListener;
import com.aliyun.iotx.haas.tdserver.sal.mq.consumer.Listeners.LockStateMobileMessageListener;
import com.aliyun.iotx.haas.tdserver.sal.oss.OssClient;
import com.aliyun.iotx.haas.tdserver.sal.tsdb.TsdbManager;
import com.aliyun.iotx.haas.tdserver.service.builder.DeviceGpsLocationDTOBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author qianfan
 * @date 2020/08/26
 */
@HSFProvider(serviceInterface = DeviceManageService.class)
public class DeviceManageServiceImpl implements DeviceManageService {

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Resource
    private IoTClient ioTClient;

    @Resource
    private DeviceBindDAO deviceBindDAO;

    @Resource
    private ProductInfoDAO productInfoDAO;

    @Resource
    private ModuleInfoDAO moduleInfoDAO;

    @Resource
    private DeviceShareDAO deviceShareDAO;

    @Resource
    private DeviceTrackDAO deviceTrackDAO;

    @Resource
    private DeviceDAO deviceDAO;

    @Resource
    private UserService userService;

    @Resource
    private CryptographUtils cryptographUtils;

    @Resource
    private AmapLbsManager amapLbsManager;

    @Resource
    private TsdbManager tsdbManager;

    @Resource
    private GpsPositionMessageListener gpsPositionMessageListener;

    @Resource
    private LockStateMobileMessageListener lockStateMobileMessageListener;

    @Resource
    private OssClient ossClient;

    private static final Logger errorLog = LoggerFactory.getLogger("error");

    private static final String DEFAULT_PRODUCT_TYPE = "G601";
    private static final String DEFAULT_BRAND = "闪骑侠";
    private static final Boolean DEFAULT_2G_SUPPORT = false;

    private static final Integer DEFAULT_DistantConfig_SUPPORT = 0;
    private static final Integer DEFAULT_STORAGELOCK_SUPPORT = 0;
    private static final Integer DEFAULT_DISPLAYENERY_SUPPORT = 1;
    private static final Integer DEFAULT_DISPLAYBICYLESSTAUS_SUPPORT = 1;

    private static final String BATTERY_VOLTAGE_IDENTIFIER = "battery_state_voltage";
    private static final String BATTERY_LEVEL_IDENTIFIER = "battery_state_level";

    private static final String POSOTION_IDENTIFIER = "position";

    private static final String DEFAULT_BATTERY_LEVEL = "75%";

    private static final int BATTERY_VOLTAGE_LEVEL_MIN = 0;
    private static final int BATTERY_VOLTAGE_LEVEL_MAX = 4;

    private static final Integer LOCK_START = 0;
    private static final Integer LOCK_CLOSE = 1;

    private static final Integer LOCK_START_LIST_MAX = 1;
    private static final Integer LOCK_CLOSE_LIST_MAX = 1;
    private static final Integer LOCK_LIST_MIN = 2;

    private static final String LOCK_LIST_COLUMNS_TIMESTAMP = "timestamp";

    private static final BigDecimal LOCK_START_BIG_DECIMAL = BigDecimal.valueOf(0.0);
    private static final BigDecimal LOCK_CLOSE_BIG_DECIMAL = BigDecimal.valueOf(1.0);

    // 最小轨迹GPS点数量
    private static final Integer MIN_TRACK_POINTS = 2;

    private static final Long MIN_TRACE_TIME = 60 * 1000L;

    private static final int[] BATTERY_LEVEL_POINTS = {0, 25, 50, 75, 100};

    private static final Integer ALGORITHM_STEP = 5;
    private static final double[][] ALGORITHM_TYPES = {{39, 43.5, 45.5, 49.5, 54.6},
            {40, 44.5, 46.5, 47.5, 49},
            {31.5, 34.5, 35.6, 37.4, 39.2},
            {42.5, 43, 44, 48, 49},
            {38.5, 39, 42, 46, 49},
            {42.7, 45.4, 47.4, 50.1, 52.9},
            {36, 49.35, 50.68, 52.41, 54.23},
            {43, 44, 45.5, 47, 48.5},
            {38.5, 47.7, 48.2, 48.6, 50.2},
            {42.5, 43.8, 45.5, 46.6, 48},
            {52.5, 54.375, 56.25, 58.125, 60},
            {31.5, 34.3, 35.2, 36.4, 38.5},
            {42.5, 44, 45.5, 46.5, 48},
            {52.5, 55.5, 57, 58.5, 60},
            {63, 66, 68, 70, 72},
            {39.5, 39.6, 42.2, 45.2, 49.2},
            {47, 48.6, 49.45, 50.4, 51.2},
            {33.5, 35, 36.5, 38, 40}};

    private String batteryAlgorithm(String voltage, Integer algoType) {
        Integer levelValue = 0;
        double voltageValue = Double.parseDouble(voltage);

        if (voltageValue < ALGORITHM_TYPES[algoType][BATTERY_VOLTAGE_LEVEL_MIN]) {
            levelValue = BATTERY_LEVEL_POINTS[0];
        } else if (voltageValue >= ALGORITHM_TYPES[algoType][BATTERY_VOLTAGE_LEVEL_MAX]) {
            levelValue = BATTERY_LEVEL_POINTS[4];
        }

        for (int i = 0; i < BATTERY_VOLTAGE_LEVEL_MAX; i++) {
            if ((voltageValue > ALGORITHM_TYPES[algoType][i]) && (voltageValue < ALGORITHM_TYPES[algoType][i + 1])) {
                double range = (ALGORITHM_TYPES[algoType][i + 1] - ALGORITHM_TYPES[algoType][i]) / ALGORITHM_STEP;
                levelValue = (int) (Math.floor(voltageValue - ALGORITHM_TYPES[algoType][i]) / range);
                levelValue = levelValue * ALGORITHM_STEP + BATTERY_LEVEL_POINTS[i];
                break;
            }
        }

        return levelValue.toString();
    }

    private String batteryAlgorithmArray(String voltage, double [] algorithm) {
        Integer levelValue = 0;
        double voltageValue = Double.parseDouble(voltage);

        if (voltageValue < algorithm[BATTERY_VOLTAGE_LEVEL_MIN]) {
            levelValue = BATTERY_LEVEL_POINTS[0];
        } else if (voltageValue >= algorithm[BATTERY_VOLTAGE_LEVEL_MAX]) {
            levelValue = BATTERY_LEVEL_POINTS[4];
        }

        for (int i = 0; i < BATTERY_VOLTAGE_LEVEL_MAX; i++) {
            if ((voltageValue > algorithm[i]) && (voltageValue < algorithm[i + 1])) {
                double range = (algorithm[i + 1] - algorithm[i]) / ALGORITHM_STEP;
                levelValue = (int) (Math.floor(voltageValue - algorithm[i]) / range);
                levelValue = levelValue * ALGORITHM_STEP + BATTERY_LEVEL_POINTS[i];
                break;
            }
        }

        return levelValue.toString();
    }

    private String calcBatteryStateLevel(String voltage, String productKey, String deviceName) {
        Integer level;
        Integer algoType = 0;

        if (voltage == null) {
            return "0";
        }

        ProductInfoDO productInfoDO = null;

        // 能在设备表中找到
        DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(productKey, deviceName, environment);

        if (deviceDO != null) {
            // 根据出行唯一产品ID判定产品
            productInfoDO = productInfoDAO.getProductInfoWithProductKeyAndUniqueTdserverProductName(
                    deviceDO.getOdmTenantId(),
                    deviceDO.getProductKey(),
                    deviceDO.getUniqueTdserverProductName(),
                    environment);
            double [] algorithm = new double[5];
            if ((productInfoDO != null)
                    && (productInfoDO.getPercentage0() != null)
                    && (productInfoDO.getPercentage20() != null)
                    && (productInfoDO.getPercentage40() != null)
                    && (productInfoDO.getPercentage60() != null)
                    && (productInfoDO.getPercentage80() != null)) {
                algorithm[0] = productInfoDO.getPercentage0();
                algorithm[1] = productInfoDO.getPercentage20();
                algorithm[2] = productInfoDO.getPercentage40();
                algorithm[3] = productInfoDO.getPercentage60();
                algorithm[4] = productInfoDO.getPercentage80();
            }

            return batteryAlgorithmArray(voltage, algorithm);
        } else {
            // 根据PK判定产品
            productInfoDO = productInfoDAO.getProductInfoWithProductKeyOnly(productKey, environment);

            if ((productInfoDO != null) && (productInfoDO.getProductAlgoType() != null)) {
                algoType = productInfoDO.getProductAlgoType();
            }

            return batteryAlgorithm(voltage, algoType);
        }
    }

    private List<DevicePropertyStatusDTO> convertPropertyHelper(List<DevicePropertyStatusDTO> devicePropertyStatusDTOList,
                                                                String productKey, String deviceName) {
        String batteryLevel = DEFAULT_BATTERY_LEVEL;
        for (DevicePropertyStatusDTO devicePropertyStatusDTO : devicePropertyStatusDTOList) {
            if (StringUtils.equals(devicePropertyStatusDTO.getIdentifier(), BATTERY_VOLTAGE_IDENTIFIER)) {
                batteryLevel = calcBatteryStateLevel(devicePropertyStatusDTO.getValue(), productKey, deviceName);
                break;
            }
        }

        for (DevicePropertyStatusDTO devicePropertyStatusDTO : devicePropertyStatusDTOList) {
            if (StringUtils.equals(devicePropertyStatusDTO.getIdentifier(), BATTERY_LEVEL_IDENTIFIER)) {
                if (StringUtils.isNotBlank(devicePropertyStatusDTO.getValue())) {
                    int value = Integer.parseInt(devicePropertyStatusDTO.getValue());
                    if ((value >= 0) && (value <= 100)) {
                        devicePropertyStatusDTO.setValue(String.valueOf(value));
                        break;
                    }
                }
                devicePropertyStatusDTO.setValue(batteryLevel);
                break;
            }
        }

        return devicePropertyStatusDTOList;
    }

    private boolean isDefaultDevice(String haasUserId, String productKey, String deviceName) {
        List<DeviceBindDO> deviceInfoDTOList = deviceBindDAO.getDefaultDeviceByUser(haasUserId, environment);
        if (deviceInfoDTOList.isEmpty()) {
            errorLog.error("There is no default device error: pk-{}, dn-{}, env-{}, haasUserId-{}",
                    productKey, deviceName, environment, haasUserId);
            return false;
        } else if (deviceInfoDTOList.size() == 1) {
            return (StringUtils.equals(deviceInfoDTOList.get(0).getDeviceName(), deviceName) &&
                    StringUtils.equals(deviceInfoDTOList.get(0).getProductKey(), productKey));
        } else {
            errorLog.error("There is multi default devices error: pk-{}, dn-{}, env-{}, haasUserId-{}",
                    productKey, deviceName, environment, haasUserId);
        }

        return false;
    }

    private DeviceInfoDTO getDeviceInfo(DeviceBindDO deviceBindDO) {
        DeviceInfoDTO deviceInfoDTO = new DeviceInfoDTO();
        ProductInfoDO productInfoDO = null;
        ModuleInfoDO moduleInfoDO = null;

        // 查找设备表
        DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(deviceBindDO.getProductKey(),
                deviceBindDO.getDeviceName(), environment);

        if (deviceDO != null) {
            // 能查找到设备
            // 根据出行唯一产品ID判定产品
            productInfoDO = productInfoDAO.getProductInfoWithProductKeyAndUniqueTdserverProductName(
                    deviceDO.getOdmTenantId(),
                    deviceDO.getProductKey(),
                    deviceDO.getUniqueTdserverProductName(),
                    environment);

            // 查询出行产品模块
            moduleInfoDO = moduleInfoDAO.getModuleInfoWithUniqueModuleName(
                    deviceDO.getOdmTenantId(),
                    deviceDO.getUniqueModuleName(),
                    environment);
        } else {
            // 找不到设备
            // 根据PK判定产品
            productInfoDO = productInfoDAO.getProductInfoWithProductKeyOnly(deviceBindDO.getProductKey(), environment);
        }

        if (productInfoDO != null) {
            // 拷贝产品属性
            BeanUtils.copyProperties(productInfoDO, deviceInfoDTO);
            deviceInfoDTO.setBrand(productInfoDO.getProductBrand());
            deviceInfoDTO.setProductType(productInfoDO.getProductModel());
            deviceInfoDTO.setIsSupport2G(productInfoDO.getIsSupport2G());
            deviceInfoDTO.setDeviceAlias(productInfoDO.getProductAlias());
            deviceInfoDTO.setIsDistantConfig(productInfoDO.getIsDistantConfig());
            deviceInfoDTO.setIsUsingStorageLock(productInfoDO.getIsUsingStorageLock());
            deviceInfoDTO.setIsDisplayEnergy(productInfoDO.getIsDisplayEnergy());
            deviceInfoDTO.setIsDisplayBicycleStatus(productInfoDO.getIsDisplayBicycleStatus());
            deviceInfoDTO.setIsSupportKeyModel(productInfoDO.getIsSupportKeyModel());
            deviceInfoDTO.setDefaultKeyModel(productInfoDO.getDefaultKeyModel());

            // 获取产品图片
            if (productInfoDO.getProductImageFileId() != null) {
                deviceInfoDTO.setProductImageFileUrl(ossClient.getOdmPictureFileUrl(deviceDO.getOdmTenantId(), "",
                        productInfoDO.getProductImageFileId(), FileTypeEnum.PICTURE_PNG.getCode(),  FileTypeEnum.PICTURE_PNG.getCode()));
            }
        } else {
            deviceInfoDTO.setBrand(DEFAULT_BRAND);
            deviceInfoDTO.setProductType(DEFAULT_PRODUCT_TYPE);
            deviceInfoDTO.setIsSupport2G(DEFAULT_2G_SUPPORT);
            deviceInfoDTO.setIsDistantConfig(DEFAULT_DistantConfig_SUPPORT);
            deviceInfoDTO.setIsUsingStorageLock(DEFAULT_STORAGELOCK_SUPPORT);
            deviceInfoDTO.setIsDisplayEnergy(DEFAULT_DISPLAYENERY_SUPPORT);
            deviceInfoDTO.setIsDisplayBicycleStatus(DEFAULT_DISPLAYBICYLESSTAUS_SUPPORT);
            deviceInfoDTO.setIsSupportKeyModel(0);
        }

        if (moduleInfoDO != null) {
            // 拷贝模块属性
            BeanUtils.copyProperties(moduleInfoDO, deviceInfoDTO);
        }

        deviceInfoDTO.setDeviceName(deviceBindDO.getDeviceName());
        deviceInfoDTO.setDeviceAlias(deviceBindDO.getDeviceAlias());
        deviceInfoDTO.setProductKey(deviceBindDO.getProductKey());
        deviceInfoDTO.setK1(cryptographUtils.decrypt(deviceBindDO.getK1()));

        return deviceInfoDTO;
    }

    @Override
    public IoTxResult<List<DevicePropertyStatusDTO>> queryDeviceProperties(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName) {
        IoTxResult<List<DevicePropertyStatusDTO>> result;
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        // 绑定关系或分享关系存在
        if ((deviceBindDOList != null && !deviceBindDOList.isEmpty())
                || (deviceShareDOList != null && !deviceShareDOList.isEmpty())) {
            result = ioTClient.queryDevicePropertyStatus(productKey, deviceName);
            if (result.hasSucceeded()) {
                result.setData(convertPropertyHelper(result.getData(), productKey, deviceName));
            }
            result.getData().stream().filter(item -> {
                if (StringUtils.equals(item.getIdentifier(), POSOTION_IDENTIFIER)) {
                    LinkPlatformGpsPositionDTO linkPlatformGpsPositionDTO = JSON.parseObject(item.getValue(), LinkPlatformGpsPositionDTO.class);

                    if (linkPlatformGpsPositionDTO != null) {
                        Long bindTime = null;
                        // 绑定时间或分享时间
                        if (deviceBindDOList != null && !deviceBindDOList.isEmpty()) {
                            bindTime = deviceBindDOList.get(0).getGmtCreate().getTime();
                        } else if (deviceShareDOList != null && !deviceShareDOList.isEmpty()) {
                            bindTime = deviceShareDOList.get(0).getGmtCreate().getTime();
                        } else {
                            errorLog.error("getUserIdByDevice error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
                            return false;
                        }
                        if (linkPlatformGpsPositionDTO.getPosition_state_timestamp() < bindTime) {
                            linkPlatformGpsPositionDTO.setPosition_state_latitude(30.2469836D);
                            linkPlatformGpsPositionDTO.setPosition_state_longitude(120.1491158D);
                            String resultJson = JSONObject.toJSONString(linkPlatformGpsPositionDTO);
                            if (resultJson != null && !resultJson.isEmpty()) {
                                item.setValue(resultJson);
                            }
                        }
                    }
                    return true;
                }
                return false;
            }).findAny();
            return result;
        } else {
            errorLog.error("getUserIdByDevice error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_QUERY_DEVICE_PROPERTIES_NOT_BIND);
        }
    }

    @Override
    public IoTxResult<DevicePropertyStatusDTO> queryDevicePropertySpecific(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName, @NotBlank String identifier) {
        DevicePropertyStatusDTO devicePropertyStatusDTO = new DevicePropertyStatusDTO();
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        // 绑定关系或分享关系存在
        if ((deviceBindDOList != null && !deviceBindDOList.isEmpty())
                || (deviceShareDOList != null && !deviceShareDOList.isEmpty())) {
            IoTxResult<List<DevicePropertyStatusDTO>> result = ioTClient.queryDevicePropertyStatus(productKey, deviceName);
            if (result.hasSucceeded()) {
                result.setData(convertPropertyHelper(result.getData(), productKey, deviceName));
                result.getData().stream().filter(item -> {
                    if (StringUtils.equals(item.getIdentifier(), identifier)) {
                        BeanUtils.copyProperties(item, devicePropertyStatusDTO);
                        return true;
                    }
                    return false;
                }).findAny();
            } else {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_QUERY_DEVICE_PROPERTIES_NO_STATUS);
            }

            if (StringUtils.isNotBlank(devicePropertyStatusDTO.getIdentifier())) {
                return new IoTxResult<>(devicePropertyStatusDTO);
            }

            return new IoTxResult<>(HaasIoTxCodes.ERROR_QUERY_DEVICE_PROPERTIES_IDENTIFIER_BLANK);

        } else {
            errorLog.error("getUserIdByDevice error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_QUERY_DEVICE_PROPERTIES_NOT_BIND);
        }
    }

    @Override
    public IoTxResult<Void> setDeviceProperties(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName, Map<String, Object> items) {
        if ((deviceBindDAO.getUserIdByDevice(productKey, deviceName, environment)).contains(haasUserId)) {
            return ioTClient.setDeviceProperty(productKey, deviceName, JSON.toJSONString(items));
        } else {
            errorLog.error("getUserIdByDevice error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEVICE_PROPERTIES_NOT_BIND);
        }
    }

    @Override
    public IoTxResult<DeviceDetailDTO> queryDeviceDetail(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName) {
        if (userService.info(haasUserId).getCode() != IoTxCodes.SUCCESS.getCode()) {
            return new IoTxResult<>(IoTxCodes.REQUEST_AUTH_ERROR);
        }

        return ioTClient.queryDeviceDetail(productKey, deviceName);
    }

    @Override
    public IoTxResult<List<DeviceInfoDTO>> queryDeviceByUser(@NotBlank String haasUserId) {
        List<DeviceInfoDTO> deviceInfoDTOList = new ArrayList<>();

        if (userService.info(haasUserId).getCode() != IoTxCodes.SUCCESS.getCode()) {
            return new IoTxResult<>(IoTxCodes.REQUEST_AUTH_ERROR);
        }

        deviceBindDAO.getDevicesByUserId(haasUserId, environment)
                .stream()
                .forEach(item -> {
                            DeviceInfoDTO deviceInfoDTO = getDeviceInfo(item);
                            deviceInfoDTOList.add(deviceInfoDTO);
                        }
                );

        return new IoTxResult<>(deviceInfoDTOList);
    }

    @Override
    public IoTxResult<DeviceInfoDTO> queryDefaultDevice(String haasUserId) {
        List<DeviceBindDO> deviceInfoDTOList = deviceBindDAO.getDefaultDeviceByUser(haasUserId, environment);
        DeviceInfoDTO deviceInfoDTO = new DeviceInfoDTO();

        if (deviceInfoDTOList.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_USER_SET_MULTI_DEFAULT_DEVICE);
        } else if (deviceInfoDTOList.size() != 1) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_USER_SET_MULTI_DEFAULT_DEVICE);
        } else {
            DeviceBindDO deviceBindDO = deviceInfoDTOList.get(0);
            deviceInfoDTO = getDeviceInfo(deviceBindDO);

            return new IoTxResult<>(deviceInfoDTO);
        }

    }

    @Override
    public IoTxResult<DeviceBindAndShareInfoDTO> queryDeviceAndShareByUser(String haasUserId) {
        if (userService.info(haasUserId).getCode() != IoTxCodes.SUCCESS.getCode()) {
            return new IoTxResult<>(IoTxCodes.REQUEST_AUTH_ERROR);
        }

        IoTxResult<List<DeviceInfoDTO>> bindDeviceInfoListResult = queryDeviceByUser(haasUserId);
        if (bindDeviceInfoListResult.getCode() != IoTxCodes.SUCCESS.getCode()) {
            return new IoTxResult<>(bindDeviceInfoListResult.getCode());
        }

        List<DeviceInfoDTO> bindDeviceInfoList = bindDeviceInfoListResult.getData();
        List<DeviceInfoDTO> shareDeviceInfoList = new ArrayList<>();

        DeviceBindAndShareInfoDTO deviceBindAndShareInfoDTO = new DeviceBindAndShareInfoDTO();
        deviceBindAndShareInfoDTO.setBindDeviceList(bindDeviceInfoList);

        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithAuthorizee(haasUserId, environment);

        if (deviceShareDOList != null && !deviceShareDOList.isEmpty()) {
            deviceShareDOList.forEach(item -> {
                List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(item.getProductKey(), item.getDeviceName(),
                        item.getAuthorizerHaasUserId(), environment);

                if (deviceBindDOList != null && !deviceBindDOList.isEmpty()) {
                    DeviceInfoDTO deviceInfoDTO = new DeviceInfoDTO();

                    DeviceBindDO deviceBindDO = deviceBindDOList.get(0);
                    deviceInfoDTO = getDeviceInfo(deviceBindDO);

                    shareDeviceInfoList.add(deviceInfoDTO);
                }
            });
        }

        deviceBindAndShareInfoDTO.setShareDeviceList(shareDeviceInfoList);

        return new IoTxResult<>(deviceBindAndShareInfoDTO);
    }

    @Override
    public IoTxResult<String> queryDeviceStatus(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName) {
        List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
        List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(
                productKey, deviceName, haasUserId, environment);
        // 绑定关系或分享关系存在
        if ((deviceBindDOList != null && !deviceBindDOList.isEmpty())
                || (deviceShareDOList != null && !deviceShareDOList.isEmpty())) {
            return ioTClient.queryDeviceStatus(productKey, deviceName);
        } else {
            errorLog.error("getUserIdByDevice error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_QUERY_DEVICE_STATUS_NOT_BIND);
        }
    }

    @Override
    public IoTxResult<String> getDeviceLbs(String haasUserId, String productKey, String deviceName,
                                           @NotBlank String longitude, @NotBlank String latitude) {
        Result<String> result = amapLbsManager.getLbs(longitude, latitude);
        if (result.isOk()) {
            return IoTxResultUtil.success(result.getValue());
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<List<DeviceGpsLocationDTO>> queryDeviceGpsDetail(@NotBlank String haasUserId, @NotBlank String productKey,
                                                                       @NotBlank String deviceName) {

        if (CollectionUtils.isEmpty(deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment))) {
            errorLog.error(
                    "getBinding error: pk-{}, dn-{}, env-{}, haasUserId-{}", productKey, deviceName, environment, haasUserId);
            return IoTxResultUtil.error(HaasIoTxCodes.ERROR_SET_DEVICE_GPS_NOT_BIND);
        }
        Long startTime = getLockStateMobileTime(productKey, deviceName, LockStateMobileEnum.OPEN);
        Long endTime = getLockStateMobileTime(productKey, deviceName, LockStateMobileEnum.CLOSE);
        Map<String, String> tagsMap = buildCommonTagMap(productKey, deviceName);
        List<MultiFieldQueryResult> results = tsdbManager.multiFieldQuery(gpsPositionMessageListener.getMetric(), tagsMap, startTime, endTime, null);
        DeviceGpsLocationDTO locationDTO = new DeviceGpsLocationDTOBuilder(results, startTime, endTime).build();
        List<DeviceGpsLocationDTO> locationDTOs = Arrays.asList(locationDTO);
        return IoTxResultUtil.success(locationDTOs);
    }

    private Long getLockStateMobileTime(String productKey, String deviceName, LockStateMobileEnum lockStateMobileEnum) {
        List<String> fields = new ArrayList<>();
        fields.add(TslFieldsEnum.LOCK_STATE_MOBILE.getField());
        List<MultiFieldQueryLastResult> lastResults = tsdbManager.multiFieldLastPointQuery(lockStateMobileMessageListener.getMetric(),
                buildLockStateTagMap(productKey, deviceName, lockStateMobileEnum.getState()), fields);
        if (CollectionUtils.isEmpty(lastResults)) {
            return System.currentTimeMillis();
        }
        MultiFieldQueryLastResult result = lastResults.get(0);
        List<List<Object>> values = result.getValues();
        if (CollectionUtils.isEmpty(values)) {
            return System.currentTimeMillis();
        }
        List<Object> objects = values.get(0);
        if (CollectionUtils.isEmpty(objects)) {
            return System.currentTimeMillis();
        }
        return (Long) objects.get(0);
    }

    private DeviceGpsLocationDTO getLastGpsState(String productKey, String deviceName, Long time) {
        Map<String, String> tagsMap = buildCommonTagMap(productKey, deviceName);
        List<MultiFieldQueryLastResult> lastResults = tsdbManager.multiFieldLastTimePointQuery(gpsPositionMessageListener.getMetric(), tagsMap, time);

        if (CollectionUtils.isEmpty(lastResults)) {
            return null;
        }
        MultiFieldQueryLastResult result = lastResults.get(0);
        MultiFieldQueryResult queryResult = new MultiFieldQueryResult();
        queryResult.setMetric(result.getMetric());
        queryResult.setColumns(result.getColumns());
        queryResult.setTags(result.getTags());
        queryResult.setValues(result.getValues());

        List<MultiFieldQueryResult> queryResultList = new ArrayList<>();
        queryResultList.add(queryResult);

        DeviceGpsLocationDTO locationDTO = new DeviceGpsLocationDTOBuilder(queryResultList, time, time).build();
        List<DeviceGpsLocationDTO> locationDTOs = Arrays.asList(locationDTO);

        return locationDTO;
    }

    private List<DeviceGpsTrackItemDTO> getTrackTimeList(String productKey, String deviceName, Long startTime, Long endTime, Integer limit) {
        List<String> fields = new ArrayList<>();
        fields.add(TslFieldsEnum.LOCK_STATE_MOBILE.getField());

        // 从TSDB拉去开关锁列表
        List<MultiFieldQueryResult> lastResults = tsdbManager.multiFieldQuery(lockStateMobileMessageListener.getMetric(),
                buildLockStateTagMapWithoutLockState(productKey, deviceName), startTime, endTime, limit);

        // 查询结果为空，或者没有两个列表（一个开锁，一个关锁），直接返回
        if ((CollectionUtils.isEmpty(lastResults)) || (lastResults.size() < LOCK_LIST_MIN)) {
            return null;
        }

        // 开锁列表是否只有一条
        if (lastResults.stream().filter(multiFieldQueryResult ->
                StringUtils.equals(multiFieldQueryResult.getTags().get(TslFieldsEnum.LOCK_STATE_MOBILE_TAG.getField()),
                        LockStateMobileEnum.OPEN.getState())).count() != LOCK_START_LIST_MAX) {
            errorLog.error("getTrackTimeList error: TSDB OPEN list has more than one list");
            return null;
        }

        // 关锁列表是否只有一条
        if (lastResults.stream().filter(multiFieldQueryResult ->
                StringUtils.equals(multiFieldQueryResult.getTags().get(TslFieldsEnum.LOCK_STATE_MOBILE_TAG.getField()),
                        LockStateMobileEnum.CLOSE.getState())).count() != LOCK_CLOSE_LIST_MAX) {
            errorLog.error("getTrackTimeList error: TSDB CLOSE list has more than one list");
            return null;
        }

        // 获取开锁列表
        MultiFieldQueryResult startList = lastResults.stream().filter(multiFieldQueryResult ->
                StringUtils.equals(multiFieldQueryResult.getTags().get(TslFieldsEnum.LOCK_STATE_MOBILE_TAG.getField()),
                        LockStateMobileEnum.OPEN.getState())).findAny().orElse(null);

        // 获取关锁列表
        MultiFieldQueryResult endList = lastResults.stream().filter(multiFieldQueryResult ->
                StringUtils.equals(multiFieldQueryResult.getTags().get(TslFieldsEnum.LOCK_STATE_MOBILE_TAG.getField()),
                        LockStateMobileEnum.CLOSE.getState())).findAny().orElse(null);

        // 列表为空或列表值为空直接返回
        if ((startList == null) || (endList == null)
                || (startList.getValues() == null) || (endList.getValues() == null)) {
            return null;
        }

        int startNum = startList.getValues().size();
        int endNum = endList.getValues().size();

        // 判定为设备轨迹时间戳列表
        List<DeviceTrackTimeDTO> deviceTrackTimeDTOS = new ArrayList<>();

        // 创建合并开锁列表
        List<LockItem> lockItemList = new ArrayList<>();

        // 根据key值获取时间戳,按照列值去拿数据
        int startTimestampIndex = -1;
        int closeTimestampIndex = -1;
        for (int i = 0; i < startList.getColumns().size(); ++i) {
            if (LOCK_LIST_COLUMNS_TIMESTAMP.equals(startList.getColumns().get(i))) {
                startTimestampIndex = i;
                break;
            }
        }

        for (int i = 0; i < startList.getColumns().size(); ++i) {
            if (LOCK_LIST_COLUMNS_TIMESTAMP.equals(endList.getColumns().get(i))) {
                closeTimestampIndex = i;
                break;
            }
        }

        // 添加开锁列表
        if (startTimestampIndex >= 0) {
            for (int i = 0; i < startNum; ++i) {
                lockItemList.add(new LockItem((Long) startList.getValues().get(i).get(startTimestampIndex), LOCK_START));
            }
        } else {
            return null;
        }

        // 添加关锁列表
        if (closeTimestampIndex >= 0) {
            for (int i = 0; i < endNum; ++i) {
                lockItemList.add(new LockItem((Long) endList.getValues().get(i).get(closeTimestampIndex), LOCK_CLOSE));
            }
        } else {
            return null;
        }

        // 按照时间戳对开关锁进行排序；相同时间按照开锁在前，关锁在后
        lockItemList.sort(Comparator.comparing(LockItem::getTimestamp).thenComparing(LockItem::getLock));

        // 合并开锁列表迭代器
        ListIterator<LockItem> iter = lockItemList.listIterator();
        if (!iter.hasNext()) {
            return null;
        }

        // 先移到第一个元素，保证循环从第二个元素开始
        LockItem lockItem = iter.next();
        LockItem lastLockItem = lockItem;

        while (iter.hasNext()) {
            // lastLockItem为上一个元素，lockItem为当前元素
            lastLockItem = lockItem;
            lockItem = iter.next();
            // 当前点为关闭点，且上一个点存在
            if ((LOCK_CLOSE.equals(lockItem.getLock())) && (iter.hasPrevious())) {
                // 若上一点为开始点，且关闭与开启时间间隔大于最小轨迹时间间隔，产生一条轨迹
                if ((lastLockItem != null)
                        && LOCK_START.equals(lastLockItem.getLock())
                        && ((lockItem.getTimestamp() - lastLockItem.getTimestamp()) > MIN_TRACE_TIME)) {
                    DeviceTrackTimeDTO timestamp = new DeviceTrackTimeDTO();
                    timestamp.setStart(lastLockItem.getTimestamp());
                    timestamp.setEnd(lockItem.getTimestamp());
                    deviceTrackTimeDTOS.add(timestamp);
                }
            }
        }

        // 带有GPS数据的轨迹列表
        List<DeviceGpsTrackItemDTO> deviceGpsTrackItemDTOList = new ArrayList<>();

        deviceTrackTimeDTOS.forEach(deviceTrackTimeDTO -> {
            DeviceGpsTrackItemDTO deviceGpsTrackItemDTO = new DeviceGpsTrackItemDTO();
            deviceGpsTrackItemDTO.setStartTime(deviceTrackTimeDTO.getStart());
            deviceGpsTrackItemDTO.setEndTime(deviceTrackTimeDTO.getEnd());

            // 对于每段轨迹，从TSDB拉取GPS点
            Map<String, String> tagsMap = buildCommonTagMap(productKey, deviceName);
            List<MultiFieldQueryResult> results = tsdbManager.multiFieldQuery(gpsPositionMessageListener.getMetric(),
                    tagsMap, deviceTrackTimeDTO.getStart(), deviceTrackTimeDTO.getEnd(), null);
            DeviceGpsLocationDTO locationDTO = new DeviceGpsLocationDTOBuilder(results, startTime, endTime).build();

            if ((locationDTO != null)
                    && (locationDTO.getItems() != null)
                    && (locationDTO.getItems().size() >= MIN_TRACK_POINTS)) {
                deviceGpsTrackItemDTO.setStartLocation(locationDTO.getItems().get(0));
                deviceGpsTrackItemDTO.setEndLocation(locationDTO.getItems().get(locationDTO.getItems().size() - 1));
                deviceGpsTrackItemDTOList.add(deviceGpsTrackItemDTO);
            }
        });

        return deviceGpsTrackItemDTOList;
    }

    private Map<String, String> buildCommonTagMap(String productKey, String deviceName) {
        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.put(TslFieldsEnum.PRODUCT_KEY.getField(), productKey);
        tagsMap.put(TslFieldsEnum.DEVICE_NAME.getField(), deviceName);
        return tagsMap;
    }

    private Map<String, String> buildCommonTagWithUserIdMap(String productKey, String deviceName, String haasUserId) {
        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.put(TslFieldsEnum.PRODUCT_KEY.getField(), productKey);
        tagsMap.put(TslFieldsEnum.DEVICE_NAME.getField(), deviceName);
        tagsMap.put(TslFieldsEnum.HAAS_USER_ID.getField(), haasUserId);
        return tagsMap;
    }

    private Map<String, String> buildLockStateTagMap(String productKey, String deviceName, String lockStateMobileTag) {
        Map<String, String> tagsMap = buildCommonTagMap(productKey, deviceName);
        tagsMap.put(TslFieldsEnum.LOCK_STATE_MOBILE_TAG.getField(), lockStateMobileTag);
        return tagsMap;
    }

    private Map<String, String> buildLockStateTagMapWithoutLockState(String productKey, String deviceName) {
        Map<String, String> tagsMap = buildCommonTagMap(productKey, deviceName);
        return tagsMap;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public IoTxResult<Void> setDefaultDeviceForUser(@NotBlank String haasUserId, @NotBlank String productKey, @NotBlank String deviceName) {
        if ((deviceBindDAO.getUserIdByDevice(productKey, deviceName, environment)).contains(haasUserId)) {
            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getDefaultDeviceByUser(haasUserId, environment);
            if (deviceBindDOList.size() >= 1) {
                for (DeviceBindDO deviceBindDO : deviceBindDOList) {
                    deviceBindDAO.removeDefaultDevice(haasUserId, deviceBindDO.getProductKey(), deviceBindDO.getDeviceName(), environment);
                }
            }

            deviceBindDAO.setDefaultDevice(haasUserId, productKey, deviceName, environment);

            return new IoTxResult<>(IoTxCodes.SUCCESS);
        } else {
            errorLog.error("setDefaultDeviceForUser error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_SET_DEFALUT_DEVICE_NOT_BIND);
        }
    }

    private IoTxResult<DeviceGpsTrackDTO> queryDeviceTrackList(String haasUserId, String productKey, String deviceName,
                                                               Long startTime, Long endTime) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_TRACK_HAAS_USER_ID_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_TRACK_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_TRACK_DN_EMPTY);
        }

        try {
            // 验证绑定关系
            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
            if (deviceBindDOList.isEmpty()) {
                errorLog.error("queryDeviceTrackList error: " + deviceBindDOList.toString());
                errorLog.error("queryDeviceTrackList error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
                return new IoTxResult<>(IoTxCodes.REQUEST_FORBIDDEN);
            }

            DeviceGpsTrackDTO deviceGpsTrackDTO = new DeviceGpsTrackDTO();
            startTime = deviceBindDOList.get(0).getGmtCreate().getTime() > startTime ? deviceBindDOList.get(0).getGmtCreate().getTime() : startTime;

            if (startTime < endTime) {
                List<DeviceGpsTrackItemDTO> trackTimeList = getTrackTimeList(productKey, deviceName, startTime, endTime, 200);
                if (trackTimeList != null && !trackTimeList.isEmpty()) {
                    deviceGpsTrackDTO.setItems(trackTimeList);
                }
            }

            return IoTxResultUtil.success(deviceGpsTrackDTO);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<PageResultDTO<DeviceGpsTrackItemDTO>> queryDeviceTrackListPage(String haasUserId, String productKey, String deviceName, Long endTime, @Max(value = 200, message = "pageSize 最大取值为200") @Min(value = 1, message = "pageSize 最小取值为1") Integer pageSize, @Min(value = 0, message = "pageNo 最小取值为0") Integer pageNo) {
        Long startTime = endTime - 7 * 24 * 60 * 60 * 1000;
        IoTxResult<DeviceGpsTrackDTO> deviceTrackList = queryDeviceTrackList(haasUserId, productKey, deviceName, startTime, endTime);

        PageResultDTO<DeviceGpsTrackItemDTO> resultDTO = new PageResultDTO<>();
        resultDTO.setTotal(0L);
        resultDTO.setPageNo(pageNo);
        resultDTO.setPageSize(pageSize);

        if (!deviceTrackList.hasSucceeded()) {
            return new IoTxResult<>(deviceTrackList.getCode(), deviceTrackList.getMessage(), deviceTrackList.getLocalizedMsg(), resultDTO);
        }

        if (deviceTrackList.getData() != null && deviceTrackList.getData().getItems() != null && !deviceTrackList.getData().getItems().isEmpty()) {
            DeviceGpsTrackDTO deviceGpsTrackDTO = deviceTrackList.getData();
            Collections.reverse(deviceGpsTrackDTO.getItems());
            resultDTO.setTotal((long) deviceGpsTrackDTO.getItems().size());
            resultDTO.setPageNo(pageNo);
            resultDTO.setPageSize(pageSize);

            if (resultDTO.getTotal() >= pageSize * pageNo) {
                if (pageNo * pageSize + pageSize > resultDTO.getTotal()) {
                    resultDTO.setData(deviceGpsTrackDTO.getItems().subList(pageNo * pageSize, Math.toIntExact(resultDTO.getTotal())));
                } else {
                    resultDTO.setData(deviceGpsTrackDTO.getItems().subList(pageNo * pageSize, pageNo * pageSize + pageSize));
                }
            }
        }

        return new IoTxResult<>(resultDTO);
    }

    private IoTxResult<List<DeviceGpsLocationDTO>> queryDeviceTrackGpsDetail(String haasUserId, String productKey, String deviceName, Long startTime, Long endTime) {
        if (CollectionUtils.isEmpty(deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment))) {
            errorLog.error(
                    "getBinding error: pk-{}, dn-{}, env-{}, haasUserId-{}", productKey, deviceName, environment, haasUserId);
            return IoTxResultUtil.error(HaasIoTxCodes.ERROR_SET_DEVICE_GPS_NOT_BIND);
        }
        Map<String, String> tagsMap = buildCommonTagMap(productKey, deviceName);
        List<MultiFieldQueryResult> results = tsdbManager.multiFieldQuery(gpsPositionMessageListener.getMetric(), tagsMap, startTime, endTime, null);
        DeviceGpsLocationDTO locationDTO = new DeviceGpsLocationDTOBuilder(results, startTime, endTime).build();
        List<DeviceGpsLocationDTO> locationDTOs = Arrays.asList(locationDTO);
        return IoTxResultUtil.success(locationDTOs);
    }

    private IoTxResult<PageResultDTO<DeviceGpsTrackItemDTO>> queryDeviceTrackListPageV2(String haasUserId, String productKey, String deviceName, @Max(value = 600, message = "pageSize 最大取值为600") @Min(value = 1, message = "pageSize 最小取值为1") Integer pageSize, @Min(value = 0, message = "pageNo 最小取值为0") Integer pageNo) {
        if (CollectionUtils.isEmpty(deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment))) {
            errorLog.error(
                    "getBinding error: pk-{}, dn-{}, env-{}, haasUserId-{}", productKey, deviceName, environment, haasUserId);
            return IoTxResultUtil.error(HaasIoTxCodes.ERROR_SET_DEVICE_GPS_NOT_BIND);
        }

        PageResultDTO<DeviceGpsTrackItemDTO> resultDTO = new PageResultDTO<>();
        resultDTO.setTotal(0L);
        resultDTO.setPageNo(pageNo);
        resultDTO.setPageSize(pageSize);

        try {
            // 查询轨迹
            List<DeviceTrackDO> deviceTrackDOList = deviceTrackDAO.getDeviceTrackList(haasUserId, productKey, deviceName, pageNo, pageSize, environment);

            if (deviceTrackDOList != null && !deviceTrackDOList.isEmpty()) {
                resultDTO.setTotal((long) deviceTrackDOList.size());
                List<DeviceGpsTrackItemDTO> deviceGpsTrackItemDTOList = new ArrayList<>();


                // 封装轨迹数据
                deviceTrackDOList.forEach(deviceTrackDO -> {
                    DeviceGpsTrackItemDTO deviceGpsTrackItemDTO = new DeviceGpsTrackItemDTO();
                    DeviceGpsLocationItemDTO startGps = new DeviceGpsLocationItemDTO();
                    DeviceGpsLocationItemDTO stopGps = new DeviceGpsLocationItemDTO();
                    startGps.setTimestamp(deviceTrackDO.getTrackStartTime());
                    startGps.setLatitude(deviceTrackDO.getTrackStartGpsLatitude());
                    startGps.setLongitude(deviceTrackDO.getTrackStartGpsLongitude());
                    startGps.setStatus(deviceTrackDO.getTrackStartGpsStatus());
                    startGps.setNsew(deviceTrackDO.getTrackStartGpsNsew());

                    stopGps.setTimestamp(deviceTrackDO.getTrackStopTime());
                    stopGps.setLatitude(deviceTrackDO.getTrackStopGpsLatitude());
                    stopGps.setLongitude(deviceTrackDO.getTrackStopGpsLongitude());
                    stopGps.setStatus(deviceTrackDO.getTrackStopGpsStatus());
                    stopGps.setNsew(deviceTrackDO.getTrackStopGpsNsew());

                    deviceGpsTrackItemDTO.setStartTime(startGps.getTimestamp());
                    deviceGpsTrackItemDTO.setEndTime(stopGps.getTimestamp());
                    deviceGpsTrackItemDTO.setStartLocation(startGps);
                    deviceGpsTrackItemDTO.setEndLocation(stopGps);

                    deviceGpsTrackItemDTOList.add(deviceGpsTrackItemDTO);
                });

                resultDTO.setData(deviceGpsTrackItemDTOList);
            }

            return new IoTxResult<>(resultDTO);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }
    }

    @Override
    public IoTxResult<PageResultDTO<DeviceGpsLocationDTO>> queryDeviceTrackGpsDetailPage(String haasUserId, String productKey, String deviceName, Long startTime, Long endTime, @Max(value = 600, message = "pageSize 最大取值为600") @Min(value = 1, message = "pageSize 最小取值为1") Integer pageSize, @Min(value = 1, message = "pageNo 最小取值为1") Integer pageNo) {

        /*if (CollectionUtils.isEmpty(deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment))) {
            errorLog.error(
                    "getBinding error: pk-{}, dn-{}, env-{}, haasUserId-{}", productKey, deviceName, environment, haasUserId);
            return IoTxResultUtil.error(HaasIoTxCodes.ERROR_SET_DEVICE_GPS_NOT_BIND);
        }*/

        PageResultDTO<DeviceGpsLocationDTO> pageResultDTO = new PageResultDTO<>();
        pageResultDTO.setTotal(0L);

        Map<String, String> tagsMap = buildCommonTagMap(productKey, deviceName);
        List<MultiFieldQueryResult> results = new ArrayList<>();

        if (startTime >= endTime) {
            pageResultDTO.setTotal(0L);
            pageResultDTO.setPageSize(pageSize);
            pageResultDTO.setPageNo(pageNo);

            return IoTxResultUtil.success(pageResultDTO);
        }

        results = tsdbManager.multiFieldCountQuery(gpsPositionMessageListener.getMetric(), tagsMap, startTime, endTime, null);

        if (results != null && !results.isEmpty()) {
            pageResultDTO.setTotal((long) results.get(0).getValues().size());
        }
        results = tsdbManager.multiFieldQueryPage(gpsPositionMessageListener.getMetric(), tagsMap, startTime, endTime, pageSize, pageNo*pageSize);

        DeviceGpsLocationDTO locationDTO = new DeviceGpsLocationDTOBuilder(results, startTime, endTime).build();
        List<DeviceGpsLocationDTO> locationDTOs = Arrays.asList(locationDTO);

        pageResultDTO.setData(Arrays.asList(locationDTO));
        pageResultDTO.setPageSize(pageSize);
        pageResultDTO.setPageNo(pageNo);

        return IoTxResultUtil.success(pageResultDTO);
    }

    @Data
    @AllArgsConstructor
    public class LockItem implements Comparable<LockItem> {
        private Long timestamp;
        private Integer lock;

        @Override
        public int compareTo(LockItem o) {
            Long diff = new Long(o.getTimestamp() - this.getTimestamp());
            return Math.toIntExact(diff);
        }
    }
}

