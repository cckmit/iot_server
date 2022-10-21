package com.aliyun.iotx.haas.tdserver.service.impl;

import com.alibaba.boot.hsf.annotation.HSFProvider;
import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.exception.IoTxException;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.constants.LogConstants;
import com.aliyun.iotx.haas.tdserver.common.exception.DeviceNotFoundException;
import com.aliyun.iotx.haas.tdserver.common.exception.HaasServerInternalException;
import com.aliyun.iotx.haas.tdserver.common.utils.RandomTool;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.odm.OdmInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ModuleInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.product.ProductInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.odm.OdmInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ModuleInfoDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.product.ProductInfoDO;
import com.aliyun.iotx.haas.tdserver.facade.dto.PageResultDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.bind.DeviceBindDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.device.BriefDeviceInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.odm.OdmDeviceAdminService;
import com.aliyun.iotx.haas.tdserver.sal.iot.IoTClient;
import com.aliyun.iotx.haas.tdserver.sal.oss.OssClient;
import com.aliyun.iotx.haas.tdserver.sal.oss.impl.OssClientImpl;
import com.taobao.eagleeye.EagleEye;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;

import static com.aliyun.iotx.haas.tdserver.service.impl.OdmProductManageServiceImpl.UNIQUE_MODULE_NAME_LENGTH;
import static com.aliyun.iotx.haas.tdserver.service.impl.OdmProductManageServiceImpl.UNIQUE_PRODUCT_NAME_LENGTH;

import java.util.List;

/**
 * @author imost.lwf
 * @date 2021/06/03
 */
@HSFProvider(serviceInterface = OdmDeviceAdminService.class)
public class OdmDeviceAdminServiceImpl implements OdmDeviceAdminService {
    @Resource
    IoTClient ioTClient;

    @Resource
    ProductInfoDAO productInfoDAO;

    @Resource
    ModuleInfoDAO moduleInfoDAO;

    @Resource
    OdmInfoDAO odmInfoDAO;

    @Resource
    DeviceDAO deviceDAO;

    @Resource
    DeviceBindDAO deviceBindDAO;

    @Resource
    private OssClient ossClient;

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Value("${iot.aliyun.haas.tdserver.product.identifier.value}")
    private String productIdentifierValue;

    private static final int MAX_TRY_NUM = 3;

    private final Logger errLog = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_ERROR);
    private final Logger sucLog = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_SERVICE);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> migrateDevice(String tenantId, String fileName) {
        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
        }
        //String new_uniqueTdserverProductName;
        //String new_uniqueTdserverModuleName;
        String new_tenantId;
        String productkey;
        String deviceName;
        String cc;
        String newProductName;
        InputStreamReader inReader ;
        Integer errorNum = -1;
        Integer errorNum1 = 0;
        Integer errorNum2 = 0;
        Integer successNum = 0;


        try {
            if(tenantId.isEmpty() || fileName.isEmpty()){
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "paramter error");
            }

            if(false == ossClient.checkOdmMigrateFileExist(tenantId, fileName, "csv")){
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "oss file not exists");
            }

            InputStream in = ossClient.getOSSOdmMigrateFile(tenantId, fileName, environment);
            if (in == null){
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "open oss file failed");
            }

            inReader = new InputStreamReader(in, "GBK");
            if( inReader == null ) {
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "read stream failed");
            }

            /*if (filepath.endsWith("csv")) {
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "file is not *.csv");
            }*/
            BufferedReader reade = new BufferedReader(inReader);
            String line = null;
            int index = 0;
            while ((line = reade.readLine()) != null) {
                Integer i = 0;
                String item[] = line.split("\\,");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                productkey = item[i];
                deviceName = item[i+1];
                cc  = item[i+2];
                new_tenantId = item[i+3];
                newProductName = item[i+4];
                //newProductName = newProductName.getContents().replaceAll(" ", "");
                // 设备表中改变设备绑定状态
                DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(
                        productkey, deviceName, environment);
                if (deviceDO != null) {
                    ProductInfoDO productInfoDO = productInfoDAO.getProductInfoWithProductName(new_tenantId,newProductName,environment);
                    //ProductInfoDO productInfoDO = productInfoDAO.getProductInfoWithProductName(new_tenantId,"文件夹-model2",environment);

                    if(productInfoDO != null)
                    {
                        ModuleInfoDO moduleInfoDO = moduleInfoDAO.getModuleInfoWithUniqueTdserverProductName(new_tenantId, productInfoDO.getUniqueTdserverProductName(), environment);
                        if(moduleInfoDO != null){
                            deviceDAO.migrateDeviceInfo(tenantId, productkey, deviceName, new_tenantId, productInfoDO.getUniqueTdserverProductName(), moduleInfoDO.getUniqueModuleName(), environment);
                            successNum++;
                        }else{
                            errorNum1++;
                        }
                    }else{
                        errLog.error("error log 错误信息:  new_tenantId: " + new_tenantId + "newProductName : " + newProductName + "deviceName ：" + deviceName);
                        errorNum2++;
                    }

                } else {
                    errorNum++;
                }

            }

            if((errorNum + errorNum1 + errorNum2)> 0  ) {
                errLog.error("Migrate device success: " + successNum + ", failed : " + errorNum + ",error1: " + errorNum1 + ",error2 : " + errorNum2);
                return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_OSS_MIGRATE_FAILED, "Migrate device success: " + successNum + ", failed : " + errorNum + errorNum1 + errorNum2);
            }

            return new IoTxResult<>(IoTxCodes.SUCCESS, "migrate device success: " + successNum);
        } catch (FileNotFoundException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_OSS_MIGRATEFILE_NOT_EXIST);
        } catch (IOException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_OSS_MIGRATEFILE_IO_FAILED);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        } catch (Exception e) {

            e.printStackTrace();
        }

        return new IoTxResult<>(IoTxCodes.SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> adminMigrateDevice(String tenantId, String fileName) {
        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
        }
        String new_uniqueTdserverProductName;
        String new_uniqueTdserverModuleName;
        String new_tenantId;
        String old_tenantId;
        String productkey;
        String deviceName;
        String cc;
        InputStreamReader inReader ;
        Integer errorNum = -1;
        Integer successNum = 0;

        try {

            InputStream in = ossClient.getOSSOdmMigrateFile(tenantId, fileName, environment);
            if (in == null){
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "open oss file failed");
            }

            inReader = new InputStreamReader(in);
            if( inReader == null )
            {
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "read stream failed");
            }

            BufferedReader reade = new BufferedReader(inReader);
            String line = null;
            int index = 0;
            while ((line = reade.readLine()) != null) {
                String item[] = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                Integer i = 0;
                old_tenantId = item[i];
                productkey = item[i+1];
                deviceName = item[i+2];
                cc = item[i+3];
                new_tenantId = item[i+4];
                new_uniqueTdserverProductName = item[i+5];
                new_uniqueTdserverModuleName = item[i+6];

                // 设备表中改变设备绑定状态
                DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(
                        productkey, deviceName, environment);
                if (deviceDO != null) {

                    deviceDAO.migrateDeviceInfo(old_tenantId, productkey, deviceName, new_tenantId, new_uniqueTdserverProductName, new_uniqueTdserverModuleName, environment);
                    successNum++;
                } else {
                    errorNum++;
                }

            }

            if(errorNum > 0) {
                errLog.error("migratelDevice : admin migrate device %d failed ", errorNum);
            }
            return new IoTxResult<>(IoTxCodes.SUCCESS, "admin migrate device success: " + successNum + ", failed : " + errorNum);
        } catch (FileNotFoundException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_OSS_MIGRATEFILE_NOT_EXIST);
        } catch (IOException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_OSS_MIGRATEFILE_IO_FAILED);
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        } catch (Exception e) {

            e.printStackTrace();
        }

        return new IoTxResult<>(IoTxCodes.SUCCESS);
    }

    /**
     * 设备三元组迁移到厂商对应产品类型
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> adminUpdateDeviceInfo(String tenantId, String productName) {
        try {
            // 检查用户是否已经签约入驻
            OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
            if (oldOdmInfoDO == null) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
            }
            // 检查用户和产品的从属关系
            ProductInfoDO productInfo = productInfoDAO.getProductInfoWithProductName(tenantId, productName, environment);
            if (productInfo != null) {
                String uniqueTdserverProductName = productInfo.getUniqueTdserverProductName();
                if (uniqueTdserverProductName == null) {
                    int tries = 0;
                    while (tries < MAX_TRY_NUM) {
                        // 生成出行平台上唯一的产品名
                        uniqueTdserverProductName = productIdentifierValue +
                                "_" + RandomTool.getRamdomAlphanumeric(UNIQUE_PRODUCT_NAME_LENGTH) +
                                "_" + environment;
                        uniqueTdserverProductName = uniqueTdserverProductName.toLowerCase();

                        ProductInfoDO productInfoDO = productInfoDAO.getProductInfoWithUniqueTdserverProductName(tenantId,
                                uniqueTdserverProductName, environment);

                        if (productInfoDO == null) {
                            break;
                        } else if (productInfoDO != null && tries == 2) {
                            // 生成出行平台上唯一的模块名失败
                            throw new IoTxException(IoTxCodes.SERVER_ERROR, "生成出行平台上唯一的模块名失败");
                        }
                    }
                    // 更新出行平台上唯一的产品名
                    productInfo.setUniqueTdserverProductName(uniqueTdserverProductName);
                    productInfoDAO.updateProductInfo(productInfo);
                }

                String uniqueModuleName = null;
                // 检查模块数据是否存在，不存在创建一个默认模块
                ModuleInfoDO moduleInfoDO = moduleInfoDAO.getModuleInfoWithUniqueTdserverProductName(
                        tenantId, uniqueTdserverProductName, environment);

                if (moduleInfoDO == null) {
                    moduleInfoDO = new ModuleInfoDO();

                    int tries = 0;
                    while (tries < MAX_TRY_NUM) {
                        // 生成出行平台上唯一的模块名
                        uniqueModuleName = productIdentifierValue + "_module_" +
                                RandomTool.getRamdomAlphanumeric(UNIQUE_MODULE_NAME_LENGTH) + "_" + environment;
                        uniqueModuleName = uniqueModuleName.toLowerCase();

                        ModuleInfoDO moduleInfoDOResult = moduleInfoDAO.getModuleInfoWithUniqueModuleName(tenantId,
                                uniqueModuleName, environment);

                        if (moduleInfoDOResult == null) {
                            break;
                        } else if (moduleInfoDOResult != null && tries == 2) {
                            // 生成出行平台上唯一的模块名失败
                            throw new IoTxException(IoTxCodes.SERVER_ERROR, "生成出行平台上唯一的模块名失败");
                        }
                        ++tries;
                    }

                    moduleInfoDO.setOdmTenantId(tenantId);
                    moduleInfoDO.setUniqueTdserverProductName(uniqueTdserverProductName);
                    moduleInfoDO.setUniqueModuleName(uniqueModuleName);
                    moduleInfoDO.setIsSupportOneline(0);
                    moduleInfoDO.setOverspeedWarningType(0);
                    moduleInfoDO.setOverspeedWarningVolume(0);
                    moduleInfoDO.setSwitchType(0);
                    moduleInfoDO.setTriggerType(0);
                    moduleInfoDO.setDefaultLockTime(0);
                    moduleInfoDO.setVibrationWarningSensitivity(0);
                    moduleInfoDO.setOverspeedWarningType(0);
                    moduleInfoDO.setIsLockedBind(0);
                    moduleInfoDO.setBindTimeAfterPoweron(0);
                    moduleInfoDO.setIsOpenButtonFunction(0);
                    moduleInfoDO.setEnvironment(environment);

                    // 插入新的配置默认模块
                    moduleInfoDAO.insert(moduleInfoDO);
                } else {
                    uniqueModuleName = moduleInfoDO.getUniqueModuleName();
                }

                // 拉取初始页设备列表
                int pageNo = 1;
                int pageSize = 100;
                String productKey = productInfo.getProductKey();
                IoTxResult<PageResultDTO<BriefDeviceInfoDTO>> pageResultDTOIoTxResult = ioTClient.queryDevicePage(
                        productKey, pageSize, pageNo);

                // 循环获取设备列表
                while ((pageResultDTOIoTxResult != null)
                        && (pageResultDTOIoTxResult.hasSucceeded())
                        && (pageResultDTOIoTxResult.getData() != null)
                        && (pageResultDTOIoTxResult.getData().getTotal() > (pageNo - 1) * pageSize)) {
                    List<BriefDeviceInfoDTO> deviceInfoDTOList = pageResultDTOIoTxResult.getData().getData();

                    List<DeviceDO> deviceDOList = new ArrayList<>();

                    if (!deviceInfoDTOList.isEmpty()) {
                        // 查到的设备创建设备项
                        String finalUniqueModuleName = uniqueModuleName;
                        String finalUniqueTdserverProductName = uniqueTdserverProductName;
                        deviceInfoDTOList.forEach(item -> {
                            DeviceDO existDeviceDO = deviceDAO.getDeviceWithDeviceName(item.getDeviceName());

                            if (existDeviceDO == null) {
                                DeviceDO deviceDO = new DeviceDO();
                                deviceDO.setOdmTenantId(tenantId);
                                deviceDO.setProductKey(productKey);
                                deviceDO.setDeviceName(item.getDeviceName());
                                deviceDO.setUniqueTdserverProductName(finalUniqueTdserverProductName);
                                deviceDO.setUniqueModuleName(finalUniqueModuleName);
                                deviceDO.setIsBound(0);
                                deviceDO.setIsDeleted(0);
                                deviceDO.setEnvironment(environment);

                                deviceDOList.add(deviceDO);
                            } else {
                                errLog.error("adminUpdateDeviceInfo error: device name: {} has exist!", existDeviceDO);
                            }
                        });

                        // 批量插入设备
                        if (!deviceDOList.isEmpty()) {
                            deviceDAO.batchInsert(deviceDOList);
                        }
                    }

                    // 从LP拉去下一页设备
                    ++pageNo;
                    pageResultDTOIoTxResult = ioTClient.queryDevicePage(productInfo.getProductKey(), pageSize, pageNo);
                }

            } else {
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "odm product name is not exist");
            }
            return new IoTxResult<>();
        } catch (DataAccessException e) {
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "更新产品信息错误:数据库异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> adminMigrateDeviceInfo(String tenantId, String productName, String toTenantId) {
        try {
            if (odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId) == null) {
                return new IoTxResult<>(IoTxCodes.REQUEST_SERVICE_NOT_FOUND, "账户不存在");
            }
            if (odmInfoDAO.getOdmInfoWithOdmTenantId(toTenantId) == null) {
                return new IoTxResult<>(IoTxCodes.REQUEST_SERVICE_NOT_FOUND, "迁入账户不存在");
            }
            // 检查用户和产品的从属关系
            ProductInfoDO productInfo = productInfoDAO.getProductInfoWithProductName(tenantId, productName, environment);
            if (productInfo != null) {
                String uniqueTdserverProductName = productInfo.getUniqueTdserverProductName();
                if (uniqueTdserverProductName == null) {
                    int tries = 0;
                    while (tries < MAX_TRY_NUM) {
                        // 生成出行平台上唯一的产品名
                        uniqueTdserverProductName = productIdentifierValue +
                                "_" + RandomTool.getRamdomAlphanumeric(UNIQUE_PRODUCT_NAME_LENGTH) +
                                "_" + environment;
                        uniqueTdserverProductName = uniqueTdserverProductName.toLowerCase();

                        ProductInfoDO productInfoDO = productInfoDAO.getProductInfoWithUniqueTdserverProductName(tenantId,
                                uniqueTdserverProductName, environment);

                        if (productInfoDO == null) {
                            break;
                        } else if (productInfoDO != null && tries == 2) {
                            // 生成出行平台上唯一的模块名失败
                            throw new IoTxException(IoTxCodes.SERVER_ERROR, "生成出行平台上唯一的模块名失败");
                        }
                    }
                    // 更新出行平台上唯一的产品名
                    productInfo.setUniqueTdserverProductName(uniqueTdserverProductName);
                    productInfoDAO.updateProductInfo(productInfo);
                }

                String uniqueModuleName = null;
                // 检查模块数据是否存在，不存在创建一个默认模块
                ModuleInfoDO moduleInfoDO = moduleInfoDAO.getModuleInfoWithUniqueTdserverProductName(
                        tenantId, uniqueTdserverProductName, environment);

                if (moduleInfoDO == null) {
                    moduleInfoDO = new ModuleInfoDO();

                    int tries = 0;
                    while (tries < MAX_TRY_NUM) {
                        // 生成出行平台上唯一的模块名
                        uniqueModuleName = productIdentifierValue + "_module_" +
                                RandomTool.getRamdomAlphanumeric(UNIQUE_MODULE_NAME_LENGTH) + "_" + environment;
                        uniqueModuleName = uniqueModuleName.toLowerCase();

                        ModuleInfoDO moduleInfoDOResult = moduleInfoDAO.getModuleInfoWithUniqueModuleName(tenantId,
                                uniqueModuleName, environment);

                        if (moduleInfoDOResult == null) {
                            break;
                        } else if (moduleInfoDOResult != null && tries == 2) {
                            // 生成出行平台上唯一的模块名失败
                            throw new IoTxException(IoTxCodes.SERVER_ERROR, "生成出行平台上唯一的模块名失败");
                        }
                        ++tries;
                    }

                    moduleInfoDO.setOdmTenantId(toTenantId);
                    moduleInfoDO.setUniqueTdserverProductName(uniqueTdserverProductName);
                    moduleInfoDO.setUniqueModuleName(uniqueModuleName);
                    moduleInfoDO.setIsSupportOneline(0);
                    moduleInfoDO.setOverspeedWarningType(0);
                    moduleInfoDO.setOverspeedWarningVolume(0);
                    moduleInfoDO.setSwitchType(0);
                    moduleInfoDO.setTriggerType(0);
                    moduleInfoDO.setDefaultLockTime(0);
                    moduleInfoDO.setVibrationWarningSensitivity(0);
                    moduleInfoDO.setOverspeedWarningType(0);
                    moduleInfoDO.setIsLockedBind(0);
                    moduleInfoDO.setBindTimeAfterPoweron(0);
                    moduleInfoDO.setIsOpenButtonFunction(0);
                    moduleInfoDO.setEnvironment(environment);

                    // 插入新的配置默认模块
                    moduleInfoDAO.insert(moduleInfoDO);
                } else {
                    moduleInfoDO.setOdmTenantId(toTenantId);
                    moduleInfoDAO.updateModuleInfo(moduleInfoDO);
                }

                productInfo.setOdmTenantId(toTenantId);
                productInfoDAO.updateProductInfo(productInfo);

                OdmInfoDO toOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(toTenantId);

                // 更新设备归属
                // 确保toTenantId存在，且uniqueTdserverProductName非空
                if (toOdmInfoDO != null && StringUtils.isNotBlank(uniqueTdserverProductName)) {
                    deviceDAO.migrateDevice(tenantId, toTenantId, uniqueTdserverProductName, environment);
                }else {
                    return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "toOdmInfoDO is not exist || uniqueTdserverProductName is null");
                }
                return new IoTxResult<>();
            } else {
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "odm product name is not exist");
            }

        } catch (DataAccessException e) {
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "产品迁移错误:数据库异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> adminUpdateAllDeviceBound() {
        try {
            // 拉取初始页绑定设备列表
            int pageNo = 1;
            int pageSize = 100;

            Long boundDeviceCount = deviceBindDAO.getAllBoundDeviceCount(environment);

            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getAllBoundDevice((pageNo - 1) * pageSize, pageSize, environment);
            // 循环获取绑定设备列表
            while ((boundDeviceCount > 0)
                    && (deviceBindDOList != null)
                    && (boundDeviceCount > (pageNo - 1) * pageSize)) {

                deviceBindDOList.forEach(item -> {
                    DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(item.getProductKey(), item.getDeviceName(), environment);
                    // 对于未在设备标表中设置绑定状态的设备数据做订正
                    if ((deviceDO != null) && (deviceDO.getIsBound() == 0)) {
                        deviceDO.setIsBound(1);
                        deviceDO.setGmtBound(item.getGmtCreate());
                        deviceDAO.updateDeviceInfo(deviceDO);
                    }
                });

                // 获取下一页数据
                ++pageNo;
                deviceBindDOList = deviceBindDAO.getAllBoundDevice((pageNo - 1) * pageSize, pageSize, environment);
            }

            return new IoTxResult<>();
        } catch (DataAccessException e) {
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "更新设备绑定状态错误:数据库异常");
        }
    }

}

