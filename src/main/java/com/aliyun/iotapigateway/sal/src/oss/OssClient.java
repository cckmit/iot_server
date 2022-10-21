package com.aliyun.iotx.haas.tdserver.sal.oss;

import com.aliyun.iotx.common.base.exception.IoTxException;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.product.OSSUploadTicket;
import com.aliyun.oss.model.ObjectMetadata;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

/**
 * @author imost.lwf
 * @date 2021/02/04
 */
public interface OssClient {
    /**
     * 获取OSS文件上传凭证
     *
     * @param ownerId  所有者id
     * @param fileName 文件名
     * @param fileId   文件id
     * @param type     文件类型
     * @return
     */
    OSSUploadTicket getOSSTicketForOdmPictureFile(String ownerId, String fileName, String fileId, String type);

    /**
     * 获取OSS设备迁移文件上传凭证
     *
     * @param ownerId  所有者id
     * @param fileName 文件名
     * @param type     文件类型
     * @return
     */
    OSSUploadTicket getOSSTicketForOdmMigrateFile(String ownerId, String fileName, String type);

    /**
     * 上传三元组CSV文件
     *
     * @param ownerId                   所有者id
     * @param filePath                  文件名
     * @param uniqueTdserverProductName 产品ID
     * @param applyId                   文件id
     * @param type                      文件类型
     * @return
     */
    Boolean uploadOdmTriadFile(String ownerId, String filePath, String uniqueTdserverProductName, String applyId, String type);

    /**
     * 上传三元组CSV文件流
     *
     * @param ownerId                   所有者id
     * @param uniqueTdserverProductName 产品ID
     * @param applyId                   文件id
     * @param type                      文件类型
     * @param input                     输入文件流
     * @return
     */
    Boolean uploadOdmTriadInputStream(String ownerId, String uniqueTdserverProductName,
                                      String applyId, String type, InputStream input);

    /**
     * 上传迁移设备三元组CSV文件流
     *
     * @param tenantId                   所有者id
     * @param fileName                  文件名称
     * @return
     */
     Boolean putOssMigrateFile(String tenantId, String fileName);

    /**
     * 获取三元组CSV文件URL
     *
     * @param ownerId                   所有者id
     * @param uniqueTdserverProductName 产品ID
     * @param applyId                   文件id
     * @param type                      文件类型
     * @return
     */
    String getOdmTriadFileUrl(String ownerId, String uniqueTdserverProductName, String applyId, String type);

    /**
     * 检查产品图片文件是否存在
     *
     * @param ownerId  所有者id
     * @param fileName 文件名
     * @param fileId   文件id
     * @param type     文件类型
     * @return
     */
    Boolean checkOdmPictureFileExist(String ownerId, String fileName, String fileId, String type);

    /**
     * 检查产品图片文件是否存在
     *
     * @param ownerId  所有者id
     * @param fileName 文件名
     * @param type     文件类型
     * @return
     */
    Boolean checkOdmMigrateFileExist(String ownerId, String fileName, String type);

    /**
     * 获取迁移文件
     *
     * @param ownerId  所有者id
     * @param fileName 文件名
     * @return
     */
    InputStream getOSSOdmMigrateFile(String ownerId, String fileName, String enviroment);

    /**
     * 获取文件URL
     *
     * @param ownerId  所有者id
     * @param fileName 文件名
     * @param fileId   文件id
     * @param type     文件类型
     * @param format   文件格式
     * @return
     */
    String getOdmPictureFileUrl(String ownerId, String fileName, String fileId, String type, String format);

    /**
     * 获取迁移文件url
     *
     * @param ownerId  所有者id
     * @param fileName 文件名
     * @param fileId   文件id
     * @param type     文件类型
     * @param format   文件格式
     * @return
     */
    String getOdmMigrateFileUrl(String ownerId, String fileName, String fileId, String type, String format);
    /**
     * 上传迁移文件
     *
     * @param ownerId  所有者id
     * @param fileName 文件名
     * @param type     文件类型
     * @param input   文件格式
     * @return
     */
    Boolean uploadOdmMigrateInputStream(String ownerId, String fileName, String type, InputStream input);
}
