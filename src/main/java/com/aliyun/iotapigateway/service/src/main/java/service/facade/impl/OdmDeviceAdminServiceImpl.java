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
        // ????????????????????????????????????
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
                String item[] = line.split("\\,");//CSV???????????????????????????????????????????????????????????????
                productkey = item[i];
                deviceName = item[i+1];
                cc  = item[i+2];
                new_tenantId = item[i+3];
                newProductName = item[i+4];
                //newProductName = newProductName.getContents().replaceAll(" ", "");
                // ????????????????????????????????????
                DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(
                        productkey, deviceName, environment);
                if (deviceDO != null) {
                    ProductInfoDO productInfoDO = productInfoDAO.getProductInfoWithProductName(new_tenantId,newProductName,environment);
                    //ProductInfoDO productInfoDO = productInfoDAO.getProductInfoWithProductName(new_tenantId,"?????????-model2",environment);

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
                        errLog.error("error log ????????????:  new_tenantId: " + new_tenantId + "newProductName : " + newProductName + "deviceName ???" + deviceName);
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
        // ????????????????????????????????????
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
                String item[] = line.split(",");//CSV???????????????????????????????????????????????????????????????
                Integer i = 0;
                old_tenantId = item[i];
                productkey = item[i+1];
                deviceName = item[i+2];
                cc = item[i+3];
                new_tenantId = item[i+4];
                new_uniqueTdserverProductName = item[i+5];
                new_uniqueTdserverModuleName = item[i+6];

                // ????????????????????????????????????
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
     * ????????????????????????????????????????????????
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> adminUpdateDeviceInfo(String tenantId, String productName) {
        try {
            // ????????????????????????????????????
            OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
            if (oldOdmInfoDO == null) {
                return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
            }
            // ????????????????????????????????????
            ProductInfoDO productInfo = productInfoDAO.getProductInfoWithProductName(tenantId, productName, environment);
            if (productInfo != null) {
                String uniqueTdserverProductName = productInfo.getUniqueTdserverProductName();
                if (uniqueTdserverProductName == null) {
                    int tries = 0;
                    while (tries < MAX_TRY_NUM) {
                        // ???????????????????????????????????????
                        uniqueTdserverProductName = productIdentifierValue +
                                "_" + RandomTool.getRamdomAlphanumeric(UNIQUE_PRODUCT_NAME_LENGTH) +
                                "_" + environment;
                        uniqueTdserverProductName = uniqueTdserverProductName.toLowerCase();

                        ProductInfoDO productInfoDO = productInfoDAO.getProductInfoWithUniqueTdserverProductName(tenantId,
                                uniqueTdserverProductName, environment);

                        if (productInfoDO == null) {
                            break;
                        } else if (productInfoDO != null && tries == 2) {
                            // ?????????????????????????????????????????????
                            throw new IoTxException(IoTxCodes.SERVER_ERROR, "?????????????????????????????????????????????");
                        }
                    }
                    // ???????????????????????????????????????
                    productInfo.setUniqueTdserverProductName(uniqueTdserverProductName);
                    productInfoDAO.updateProductInfo(productInfo);
                }

                String uniqueModuleName = null;
                // ??????????????????????????????????????????????????????????????????
                ModuleInfoDO moduleInfoDO = moduleInfoDAO.getModuleInfoWithUniqueTdserverProductName(
                        tenantId, uniqueTdserverProductName, environment);

                if (moduleInfoDO == null) {
                    moduleInfoDO = new ModuleInfoDO();

                    int tries = 0;
                    while (tries < MAX_TRY_NUM) {
                        // ???????????????????????????????????????
                        uniqueModuleName = productIdentifierValue + "_module_" +
                                RandomTool.getRamdomAlphanumeric(UNIQUE_MODULE_NAME_LENGTH) + "_" + environment;
                        uniqueModuleName = uniqueModuleName.toLowerCase();

                        ModuleInfoDO moduleInfoDOResult = moduleInfoDAO.getModuleInfoWithUniqueModuleName(tenantId,
                                uniqueModuleName, environment);

                        if (moduleInfoDOResult == null) {
                            break;
                        } else if (moduleInfoDOResult != null && tries == 2) {
                            // ?????????????????????????????????????????????
                            throw new IoTxException(IoTxCodes.SERVER_ERROR, "?????????????????????????????????????????????");
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

                    // ??????????????????????????????
                    moduleInfoDAO.insert(moduleInfoDO);
                } else {
                    uniqueModuleName = moduleInfoDO.getUniqueModuleName();
                }

                // ???????????????????????????
                int pageNo = 1;
                int pageSize = 100;
                String productKey = productInfo.getProductKey();
                IoTxResult<PageResultDTO<BriefDeviceInfoDTO>> pageResultDTOIoTxResult = ioTClient.queryDevicePage(
                        productKey, pageSize, pageNo);

                // ????????????????????????
                while ((pageResultDTOIoTxResult != null)
                        && (pageResultDTOIoTxResult.hasSucceeded())
                        && (pageResultDTOIoTxResult.getData() != null)
                        && (pageResultDTOIoTxResult.getData().getTotal() > (pageNo - 1) * pageSize)) {
                    List<BriefDeviceInfoDTO> deviceInfoDTOList = pageResultDTOIoTxResult.getData().getData();

                    List<DeviceDO> deviceDOList = new ArrayList<>();

                    if (!deviceInfoDTOList.isEmpty()) {
                        // ??????????????????????????????
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

                        // ??????????????????
                        if (!deviceDOList.isEmpty()) {
                            deviceDAO.batchInsert(deviceDOList);
                        }
                    }

                    // ???LP?????????????????????
                    ++pageNo;
                    pageResultDTOIoTxResult = ioTClient.queryDevicePage(productInfo.getProductKey(), pageSize, pageNo);
                }

            } else {
                return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, "odm product name is not exist");
            }
            return new IoTxResult<>();
        } catch (DataAccessException e) {
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "????????????????????????:???????????????");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> adminMigrateDeviceInfo(String tenantId, String productName, String toTenantId) {
        try {
            if (odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId) == null) {
                return new IoTxResult<>(IoTxCodes.REQUEST_SERVICE_NOT_FOUND, "???????????????");
            }
            if (odmInfoDAO.getOdmInfoWithOdmTenantId(toTenantId) == null) {
                return new IoTxResult<>(IoTxCodes.REQUEST_SERVICE_NOT_FOUND, "?????????????????????");
            }
            // ????????????????????????????????????
            ProductInfoDO productInfo = productInfoDAO.getProductInfoWithProductName(tenantId, productName, environment);
            if (productInfo != null) {
                String uniqueTdserverProductName = productInfo.getUniqueTdserverProductName();
                if (uniqueTdserverProductName == null) {
                    int tries = 0;
                    while (tries < MAX_TRY_NUM) {
                        // ???????????????????????????????????????
                        uniqueTdserverProductName = productIdentifierValue +
                                "_" + RandomTool.getRamdomAlphanumeric(UNIQUE_PRODUCT_NAME_LENGTH) +
                                "_" + environment;
                        uniqueTdserverProductName = uniqueTdserverProductName.toLowerCase();

                        ProductInfoDO productInfoDO = productInfoDAO.getProductInfoWithUniqueTdserverProductName(tenantId,
                                uniqueTdserverProductName, environment);

                        if (productInfoDO == null) {
                            break;
                        } else if (productInfoDO != null && tries == 2) {
                            // ?????????????????????????????????????????????
                            throw new IoTxException(IoTxCodes.SERVER_ERROR, "?????????????????????????????????????????????");
                        }
                    }
                    // ???????????????????????????????????????
                    productInfo.setUniqueTdserverProductName(uniqueTdserverProductName);
                    productInfoDAO.updateProductInfo(productInfo);
                }

                String uniqueModuleName = null;
                // ??????????????????????????????????????????????????????????????????
                ModuleInfoDO moduleInfoDO = moduleInfoDAO.getModuleInfoWithUniqueTdserverProductName(
                        tenantId, uniqueTdserverProductName, environment);

                if (moduleInfoDO == null) {
                    moduleInfoDO = new ModuleInfoDO();

                    int tries = 0;
                    while (tries < MAX_TRY_NUM) {
                        // ???????????????????????????????????????
                        uniqueModuleName = productIdentifierValue + "_module_" +
                                RandomTool.getRamdomAlphanumeric(UNIQUE_MODULE_NAME_LENGTH) + "_" + environment;
                        uniqueModuleName = uniqueModuleName.toLowerCase();

                        ModuleInfoDO moduleInfoDOResult = moduleInfoDAO.getModuleInfoWithUniqueModuleName(tenantId,
                                uniqueModuleName, environment);

                        if (moduleInfoDOResult == null) {
                            break;
                        } else if (moduleInfoDOResult != null && tries == 2) {
                            // ?????????????????????????????????????????????
                            throw new IoTxException(IoTxCodes.SERVER_ERROR, "?????????????????????????????????????????????");
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

                    // ??????????????????????????????
                    moduleInfoDAO.insert(moduleInfoDO);
                } else {
                    moduleInfoDO.setOdmTenantId(toTenantId);
                    moduleInfoDAO.updateModuleInfo(moduleInfoDO);
                }

                productInfo.setOdmTenantId(toTenantId);
                productInfoDAO.updateProductInfo(productInfo);

                OdmInfoDO toOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(toTenantId);

                // ??????????????????
                // ??????toTenantId????????????uniqueTdserverProductName??????
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
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "??????????????????:???????????????");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IoTxResult<String> adminUpdateAllDeviceBound() {
        try {
            // ?????????????????????????????????
            int pageNo = 1;
            int pageSize = 100;

            Long boundDeviceCount = deviceBindDAO.getAllBoundDeviceCount(environment);

            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getAllBoundDevice((pageNo - 1) * pageSize, pageSize, environment);
            // ??????????????????????????????
            while ((boundDeviceCount > 0)
                    && (deviceBindDOList != null)
                    && (boundDeviceCount > (pageNo - 1) * pageSize)) {

                deviceBindDOList.forEach(item -> {
                    DeviceDO deviceDO = deviceDAO.getDeviceWithProductKeyAndDeviceName(item.getProductKey(), item.getDeviceName(), environment);
                    // ?????????????????????????????????????????????????????????????????????
                    if ((deviceDO != null) && (deviceDO.getIsBound() == 0)) {
                        deviceDO.setIsBound(1);
                        deviceDO.setGmtBound(item.getGmtCreate());
                        deviceDAO.updateDeviceInfo(deviceDO);
                    }
                });

                // ?????????????????????
                ++pageNo;
                deviceBindDOList = deviceBindDAO.getAllBoundDevice((pageNo - 1) * pageSize, pageSize, environment);
            }

            return new IoTxResult<>();
        } catch (DataAccessException e) {
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "??????????????????????????????:???????????????");
        }
    }

}

