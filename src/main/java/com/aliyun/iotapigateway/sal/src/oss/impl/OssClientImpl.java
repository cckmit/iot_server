package com.aliyun.iotx.haas.tdserver.sal.oss.impl;

import com.alibaba.metrics.reporter.bin.zigzag.io.IntOutputStream;
import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.exception.IoTxException;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.constants.LogConstants;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.product.OSSUploadTicket;
import com.aliyun.iotx.haas.tdserver.facade.enums.FileTypeEnum;
import com.aliyun.iotx.haas.tdserver.sal.oss.OssClient;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import com.google.common.eventbus.AsyncEventBus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ForkJoinPool;

/**
 * @author imost.lwf
 * @date 2021/02/05
 */

@Component
public class OssClientImpl implements OssClient {
    @Resource
    CryptographUtils cryptographUtils;

    @Value("${aliyun.haas.tdserver.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.haas.tdserver.oss.encrypted.accessKey}")
    private String accessKeyIdEncryption;

    @Value("${aliyun.haas.tdserver.oss.encrypted.accessSecret}")
    private String accessKeySecretEncryption;

    @Value("${aliyun.haas.tdserver.oss.bucket.name}")
    private String bucketName;

    @Value("${aliyun.haas.tdserver.oss.bucket.domain}")
    private String bucketDomain;

    // 厂商目录
    private static final String OSS_ODM_DIR = "odm/";

    // 厂商目录
    private static final String OSS_MIGRATE_DIR = "migrate/";
    // 厂商产品目录
    private static final String OSS_ODM_PRODUCT_DIR = "product/";

    // 厂商三元组目录
    private static final String OSS_ODM_DEVICE_TRIAD_DIR = "triad/";

    // 厂商图片目录
    private static final String OSS_ODM_PICTURE_DIR = "picture/";

    //厂商广告图片目录
    private static final String OSS_ODM_ADVERTISE_DIR = "advertise/";

    // OSS上传凭证过期时间
    private static final Long OSS_TICKET_EXPIRED_TIME = 2 * 60 * 60L;

    // OSS中图片URL过期时间
    private static final Long OSS_PICTURE_URL_EXPIRED_TIME = 5 * 365 * 60 * 60L;

    // OSS中迁移文件过期时间
    private static final Long OSS_MIGRATE_URL_EXPIRED_TIME = 5 * 365 * 60 * 60L;

    // OSS中三元组文件URL过期时间
    private static final Long OSS_TRIAD_URL_EXPIRED_TIME = 5 * 60L;

    public static final AsyncEventBus SOC_OSS_FILE_CREATE = new AsyncEventBus("OSS_FILE_CREATE", ForkJoinPool.commonPool());

    private OSS ossClient;

    // 厂商产品图片大小限制，600K
    private static final Long OSS_PRODUCT_PICTURE_LENGTH_LIMIT = 600 * 1024L;

    @PostConstruct
    public void initClient() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProtocol(Protocol.HTTPS);
        String accessKeyId = cryptographUtils.decrypt(accessKeyIdEncryption);
        String accessKeySecret = cryptographUtils.decrypt(accessKeySecretEncryption);
        ossClient = new com.aliyun.oss.OSSClient(endpoint, accessKeyId, accessKeySecret, clientConfiguration);
    }

    @Override
    public OSSUploadTicket getOSSTicketForOdmPictureFile(String ownerId, String fileName, String fileId, String type) {
        // 确保路径正确
        if (StringUtils.isBlank(ownerId) || StringUtils.isBlank(fileId)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_INVALID_FILE_UPLOAD_PARAM);
        }

        PolicyConditions policyConditions = new PolicyConditions();

        String objectName = getOssOdmPictureObjectFullName(ownerId, fileName, fileId, type);

        // 确保只能上传到指令的路径
        policyConditions.addConditionItem(MatchMode.Exact, PolicyConditions.COND_KEY, objectName);

        // 限定bucket，避免跨应用访问
        policyConditions.addConditionItem("bucket", bucketName);

        // 限制文件大小
        // 限制文件类型 会影响上传
        if (FileTypeEnum.PICTURE_PNG.equals(FileTypeEnum.getFileTypeByCode(type))) {
            policyConditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0,
                    OSS_PRODUCT_PICTURE_LENGTH_LIMIT);
        }

        // 设置到期时间
        Long expire = System.currentTimeMillis() + OSS_TICKET_EXPIRED_TIME * 1000;
        Date expireDate = new Date(expire);

        // 生成签名
        String postPolicy = ossClient.generatePostPolicy(expireDate, policyConditions);
        String encodedPolicy = "";

        try {
            byte[] binaryData = postPolicy.getBytes("UTF-8");
            encodedPolicy = BinaryUtil.toBase64String(binaryData);
        } catch (Exception e) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_SIGNATURE_GENERATE_FILED);
        }

        String postSignature = ossClient.calculatePostSignature(postPolicy);

        OSSUploadTicket ossUploadTicket = new OSSUploadTicket();
        ossUploadTicket.setHost("https://" + bucketName + "." + endpoint);
        ossUploadTicket.setKey(objectName);
        ossUploadTicket.setFileId(fileId);
        ossUploadTicket.setPolicy(encodedPolicy);
        ossUploadTicket.setExpire(expire);
        ossUploadTicket.setSignature(postSignature);
        ossUploadTicket.setAccessKeyId(cryptographUtils.decrypt(accessKeyIdEncryption));

        return ossUploadTicket;
    }

    @Override
    public OSSUploadTicket getOSSTicketForOdmMigrateFile(String ownerId, String fileName, String type) {
        // 确保路径正确
        if (StringUtils.isBlank(ownerId)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_INVALID_FILE_UPLOAD_PARAM);
        }

        PolicyConditions policyConditions = new PolicyConditions();

        String objectName = getOssOdmMigrateFullName(ownerId, fileName, type);

        // 确保只能上传到指令的路径
        policyConditions.addConditionItem(MatchMode.Exact, PolicyConditions.COND_KEY, objectName);

        // 限定bucket，避免跨应用访问
        policyConditions.addConditionItem("bucket", bucketName);

        // 限制文件大小
        // 限制文件类型 会影响上传
        if (FileTypeEnum.CSV.equals(FileTypeEnum.getFileTypeByCode(type))) {
            policyConditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0,
                    OSS_PRODUCT_PICTURE_LENGTH_LIMIT);
        }

        // 设置到期时间
        Long expire = System.currentTimeMillis() + OSS_TICKET_EXPIRED_TIME * 1000;
        Date expireDate = new Date(expire);

        // 生成签名
        String postPolicy = ossClient.generatePostPolicy(expireDate, policyConditions);
        String encodedPolicy = "";

        try {
            byte[] binaryData = postPolicy.getBytes("UTF-8");
            encodedPolicy = BinaryUtil.toBase64String(binaryData);
        } catch (Exception e) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_SIGNATURE_GENERATE_FILED);
        }

        String postSignature = ossClient.calculatePostSignature(postPolicy);

        OSSUploadTicket ossUploadTicket = new OSSUploadTicket();
        ossUploadTicket.setHost("https://" + bucketName + "." + endpoint);
        ossUploadTicket.setKey(objectName);
        ossUploadTicket.setFileId("");
        ossUploadTicket.setPolicy(encodedPolicy);
        ossUploadTicket.setExpire(expire);
        ossUploadTicket.setSignature(postSignature);
        ossUploadTicket.setAccessKeyId(cryptographUtils.decrypt(accessKeyIdEncryption));

        return ossUploadTicket;
    }


    @Override
    public Boolean uploadOdmTriadFile(String ownerId, String filePath, String uniqueTdserverProductName, String applyId, String type) {
        // 确保路径正确
        if (StringUtils.isBlank(ownerId)
                || StringUtils.isBlank(filePath)
                || StringUtils.isBlank(uniqueTdserverProductName)
                || StringUtils.isBlank(applyId)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_INVALID_FILE_UPLOAD_PARAM);
        }

        String objectName = getOssOdmTriadObjectFullName(ownerId, uniqueTdserverProductName, applyId, type);

        // 创建PutObjectRequest对象
        // 填写Bucket名称、Object完整路径和本地文件的完整路径。Object完整路径中不能包含Bucket名称
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File(filePath));

        // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("text/csv");
        meta.setContentDisposition(String.format("attachment; filename=\"%s-%s.csv\"",
                uniqueTdserverProductName,
                applyId));
        meta.setContentEncoding("utf-8");

        // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码
//         ObjectMetadata metadata = new ObjectMetadata();
//         metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
//         metadata.setObjectAcl(CannedAccessControlList.Private);
//         putObjectRequest.setMetadata(metadata);

        try {
            // 上传文件
            ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_FILE_UPLOAD_FAIL);
        }

        return Boolean.TRUE;
    }

    public Boolean uploadOdmMigrateFile(String ownerId, String filePath, String uniqueTdserverProductName, String type) {
        // 确保路径正确
        if (StringUtils.isBlank(ownerId)
                || StringUtils.isBlank(filePath)
                || StringUtils.isBlank(uniqueTdserverProductName)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_INVALID_FILE_UPLOAD_PARAM);
        }

        String objectName = getOssOdmMigrateFullName(ownerId, uniqueTdserverProductName, type);

        // 创建PutObjectRequest对象
        // 填写Bucket名称、Object完整路径和本地文件的完整路径。Object完整路径中不能包含Bucket名称
        // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File(filePath));

        // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("text/csv");
        meta.setContentDisposition(String.format("attachment; filename=\"%s.csv\"",
                uniqueTdserverProductName));
        meta.setContentEncoding("utf-8");

        // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码
//         ObjectMetadata metadata = new ObjectMetadata();
//         metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
//         metadata.setObjectAcl(CannedAccessControlList.Private);
//         putObjectRequest.setMetadata(metadata);

        try {
            // 上传文件
            ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_FILE_UPLOAD_FAIL);
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean uploadOdmTriadInputStream(String ownerId, String uniqueTdserverProductName,
                                             String applyId, String type, InputStream input) {
        // 确保路径正确
        if (StringUtils.isBlank(ownerId)
                || StringUtils.isBlank(uniqueTdserverProductName)
                || StringUtils.isBlank(applyId)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_INVALID_FILE_UPLOAD_PARAM);
        }

        String objectName = getOssOdmTriadObjectFullName(ownerId, uniqueTdserverProductName, applyId, type);

        // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("text/csv");
        meta.setContentDisposition(String.format("attachment; filename=\"%s-%s.csv\"",
                uniqueTdserverProductName,
                applyId));
        meta.setContentEncoding("utf-8");

        try {
            // 上传文件流
            ossClient.putObject(bucketName, objectName, input, meta);
        } catch (Exception e) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_FILE_UPLOAD_FAIL);
        }

        return Boolean.TRUE;
    }

    @Override
    public String getOdmTriadFileUrl(String ownerId, String uniqueTdserverProductName, String applyId, String type) {
        String objectName = getOssOdmTriadObjectFullName(ownerId, uniqueTdserverProductName, applyId, type);

        return getOssFileUrlWithName(objectName, System.currentTimeMillis() + OSS_TRIAD_URL_EXPIRED_TIME * 1000);
    }

    @Override
    public Boolean checkOdmPictureFileExist(String ownerId, String fileName, String fileId, String type) {
        return ossClient.doesObjectExist(bucketName, getOssOdmPictureObjectFullName(ownerId, fileName, fileId, type));
    }

    @Override
    public Boolean checkOdmMigrateFileExist(String ownerId, String fileName, String type) {
        return ossClient.doesObjectExist(bucketName, getOssOdmMigrateFullName(ownerId, fileName, type));
    }


    @Override
    public String getOdmPictureFileUrl(String ownerId, String fileName, String fileId, String type, String format) {

        String objectName = getOssOdmPictureObjectFullName(ownerId, fileName, fileId, type);

        return getOssFileUrlWithName(objectName, System.currentTimeMillis() + OSS_PICTURE_URL_EXPIRED_TIME * 1000);
    }


    @Override
    public String getOdmMigrateFileUrl(String ownerId, String fileName, String fileId, String type, String format) {

        String objectName = getOssOdmMigrateFullName(ownerId, fileName, type);

        return getOssFileUrlWithName(objectName, System.currentTimeMillis() + OSS_MIGRATE_URL_EXPIRED_TIME * 1000);
    }

    @Override
    public Boolean putOssMigrateFile(String tenantId, String fileName){
        FileInputStream mFile;
        if(!FileTypeEnum.PICTURE_PNG.equals(FileTypeEnum.getFileTypeByCode(fileName.substring(fileName.lastIndexOf("."))))) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_INVALID_FILE_UPLOAD_PARAM);
        }

        try{
            mFile = new FileInputStream(fileName);
        }catch (FileNotFoundException e){
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_FILE_UPLOAD_FAIL);
        }
        return uploadOdmMigrateInputStream(tenantId, fileName, "csv", mFile);
    }

    @Override
    public InputStream getOSSOdmMigrateFile(String ownerId, String fileName, String enviroment) {
        OSSObject oss = null;

        // 确保路径正确
        if (StringUtils.isBlank(ownerId) || StringUtils.isBlank(fileName) || StringUtils.isBlank(enviroment)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_INVALID_FILE_DOWNLOAD_PARAM);
        }
        String objectName = OSS_MIGRATE_DIR + fileName;
        GetObjectRequest objectRequest = new GetObjectRequest("tdserver-" + enviroment,objectName);

        try{
            oss = ossClient.getObject(objectRequest);

        }catch (OSSException e){
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_MIGRATEFILE_IO_FAILED);
        }catch (ClientException e) {
             throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_MIGRATEFILE_IO_FAILED);
        }

        if(oss != null){
            return oss.getObjectContent();
        }else {
            return null;
        }
    }



    @Override
    public Boolean uploadOdmMigrateInputStream(String ownerId, String fileName, String type, InputStream input) {
        // 确保路径正确
        if (StringUtils.isBlank(ownerId) || StringUtils.isBlank(fileName)) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_INVALID_FILE_UPLOAD_PARAM);
        }

        String objectName = getOssOdmMigrateFullName(ownerId, fileName, type);

        // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("text/csv");
        meta.setContentDisposition(String.format("attachment; filename=\"%s.csv\"", fileName));
        meta.setContentEncoding("utf-8");

        try {
            // 上传文件流
            ossClient.putObject(bucketName, objectName, input, meta);
        } catch (Exception e) {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_OSS_FILE_UPLOAD_FAIL);
        }

        return Boolean.TRUE;
    }

    private String getOssFileUrlWithName(String objectName, Long expire) {
        // 设置到期时间
        Date expireDate = new Date(expire);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectName);
        generatePresignedUrlRequest.setExpiration(expireDate);

        return ossClient.generatePresignedUrl(generatePresignedUrlRequest).toString();

    }

    /*
    private String  getOssAdvertisePictureObjectFullName(String ownerId, String fileName, String fileId, String type) {
        if (FileTypeEnum.getFileTypeByCode(type) == FileTypeEnum.PICTURE_PNG) {
            return OSS_ODM_DIR + ownerId + "/" + OSS_ODM_PRODUCT_DIR + OSS_ODM_PICTURE_DIR + OSS_ODM_ADVERTISE_DIR + fileId;
        } else {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_UNSUPPORTED_FILE_TYPE);
        }
    }*/

    private String  getOssOdmPictureObjectFullName(String ownerId, String fileName, String fileId, String type) {
        if (FileTypeEnum.getFileTypeByCode(type) == FileTypeEnum.PICTURE_PNG) {
            return OSS_ODM_DIR + ownerId + "/" + OSS_ODM_PRODUCT_DIR + OSS_ODM_PICTURE_DIR + fileId;
        } else {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_UNSUPPORTED_FILE_TYPE);
        }
    }

    private String getOssOdmTriadObjectFullName(String ownerId, String uniqueTdserverProductName, String applyId, String type) {
        if (FileTypeEnum.getFileTypeByCode(type) == FileTypeEnum.CSV) {
            return OSS_ODM_DIR + ownerId + "/" + OSS_ODM_DEVICE_TRIAD_DIR + uniqueTdserverProductName + "-" + applyId + ".csv";
        } else {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_UNSUPPORTED_FILE_TYPE);
        }
    }

    private String  getOssOdmMigrateFullName(String ownerId, String fileName, String type) {
        if (FileTypeEnum.getFileTypeByCode(type) == FileTypeEnum.CSV) {
            return OSS_MIGRATE_DIR + fileName  ;
        } else {
            throw new IoTxException(HaasIoTxCodes.ERROR_ODM_UNSUPPORTED_FILE_TYPE);
        }
    }

}

