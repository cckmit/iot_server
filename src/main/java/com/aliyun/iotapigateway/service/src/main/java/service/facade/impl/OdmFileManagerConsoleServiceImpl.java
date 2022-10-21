package com.aliyun.iotx.haas.tdserver.service.impl;

import com.alibaba.boot.hsf.annotation.HSFProvider;
import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.exception.IoTxException;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.utils.RandomTool;
import com.aliyun.iotx.haas.tdserver.dal.dao.odm.OdmInfoDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.odm.OdmInfoDO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.product.OSSUploadTicket;
import com.aliyun.iotx.haas.tdserver.facade.enums.FileTypeEnum;
import com.aliyun.iotx.haas.tdserver.facade.odm.OdmFileManageConsoleService;
import com.aliyun.iotx.haas.tdserver.sal.oss.OssClient;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author imost.lwf
 * @date 2021/02/08
 */
@HSFProvider(serviceInterface = OdmFileManageConsoleService.class)
public class OdmFileManageConsoleServiceImpl implements OdmFileManageConsoleService {
    @Resource
    OssClient ossClient;

    @Resource
    OdmInfoDAO odmInfoDAO;

    // 随机文件长度
    private static final Integer OSS_FILE_NAME_ID_LENGTH = 20;

    @Override
    public IoTxResult<OSSUploadTicket> getUploadTicket(String tenantId, @NotBlank String fileName, @NotBlank String type) {

        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
        }

        if (!FileTypeEnum.getFileTypeByCode(type).equals(FileTypeEnum.PICTURE_PNG)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_UNSUPPORTED_FILE_TYPE);
        }

        String newFileId = RandomTool.getRamdomAlphanumeric(OSS_FILE_NAME_ID_LENGTH);

        OSSUploadTicket ossUploadTicket = ossClient.getOSSTicketForOdmPictureFile(tenantId, fileName, newFileId, type);

        return new IoTxResult<>(ossUploadTicket);
    }

    @Override
    public IoTxResult<String> getUploadFileUrl(@NotBlank String tenantId, @NotBlank String fileName,
                                               @NotBlank String fileId, @NotBlank String type) {
        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
        }
        if (!ossClient.checkOdmPictureFileExist(tenantId, fileName, fileId, type)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_FILE_NOT_EXIST);
        }

        return new IoTxResult<>(ossClient.getOdmPictureFileUrl(tenantId, fileName, fileId, type, type));
    }

    @Override
    public IoTxResult<OSSUploadTicket> getUploadAdvertiseTicket(String tenantId, @NotBlank String fileName, @NotBlank String type) {

        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
        }

        if (!FileTypeEnum.getFileTypeByCode(type).equals(FileTypeEnum.PICTURE_PNG)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_UNSUPPORTED_FILE_TYPE);
        }

        String newFileId = RandomTool.getRamdomAlphanumeric(OSS_FILE_NAME_ID_LENGTH);

        OSSUploadTicket ossUploadTicket = ossClient.getOSSTicketForOdmPictureFile(tenantId, fileName, newFileId, type);

        return new IoTxResult<>(ossUploadTicket);
    }


    @Override
    public IoTxResult<String> getUploadAdvertiseFileUrl(@NotBlank String tenantId, @NotBlank String fileName,
                                               @NotBlank String fileId, @NotBlank String type) {
        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
        }
        if (!ossClient.checkOdmPictureFileExist(tenantId, fileName, fileId, type)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_FILE_NOT_EXIST);
        }

        return new IoTxResult<>(ossClient.getOdmPictureFileUrl(tenantId, fileName, fileId, type, type));
    }


    @Override
    public IoTxResult<OSSUploadTicket> getUploadMigrateFileTicket(String tenantId, @NotBlank String fileName, @NotBlank String type) {

        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
        }

        if (!FileTypeEnum.getFileTypeByCode(type).equals(FileTypeEnum.CSV)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_UNSUPPORTED_FILE_TYPE);
        }

        OSSUploadTicket ossUploadTicket = ossClient.getOSSTicketForOdmMigrateFile(tenantId, fileName, type);

        return new IoTxResult<>(ossUploadTicket);
    }

    public IoTxResult<Boolean> uploadMigrateFile(@NotBlank String tenantId, @NotBlank String fileName,@NotBlank String fileId, @NotBlank String type){
        FileInputStream mFile;

        // 检查用户是否已经签约入驻
        OdmInfoDO oldOdmInfoDO = odmInfoDAO.getOdmInfoWithOdmTenantId(tenantId);
        if (oldOdmInfoDO == null) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
        }

        if (!FileTypeEnum.getFileTypeByCode(type).equals(FileTypeEnum.CSV)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_UNSUPPORTED_FILE_TYPE);
        }

        try{
            mFile = new FileInputStream(fileName);
        }catch (FileNotFoundException e){
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_FILE_UPLOAD_FAIL);
        }

        return new IoTxResult<>(ossClient.uploadOdmMigrateInputStream(tenantId, fileName, "csv", mFile));
    }
}

