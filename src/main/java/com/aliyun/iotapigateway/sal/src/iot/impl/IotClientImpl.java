package com.aliyun.iotx.haas.tdserver.sal.iot.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.aliyun.iotx.common.base.code.IoTxCodes;
import com.aliyun.iotx.common.base.exception.IoTxException;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.exception.ProductAlreadyExistedException;
import com.aliyun.iotx.haas.common.security.utils.CryptographUtils;
import com.aliyun.iotx.haas.tdserver.common.utils.IoTxResultUtil;
import com.aliyun.iotx.haas.tdserver.facade.dto.PageResultDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceDetailDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DevicePropertyStatusDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceServiceResultDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.device.BatchRegisterDeviceStatusDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.device.BatchUploadDeviceNameResultDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.device.BriefDeviceInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.device.RegisterDeviceInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.product.ProductDetailDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.product.ProductInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.product.ProductTagDTO;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.CancelOtaStrategyByJobRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.CancelOtaTaskByDeviceRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.CancelOtaTaskByJobRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.CommonOtaRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.CreateOtaDynamicUpgradeJobRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.CreateOtaFirmwareRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.CreateOtaStaticUpgradeJobRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.CreateOtaVerifyJobRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.DeleteOtaFirmwareRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.ListOtaFirmwareRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.ListOtaJobByDeviceRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.ListOtaJobByFirmwareRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.ListOtaTaskByJobRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.QueryOtaFirmwareRequest;
import com.aliyun.iotx.haas.tdserver.facade.request.ota.QueryOtaJobRequest;
import com.aliyun.iotx.haas.tdserver.facade.response.AbstractResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.CancelOtaStrategyByJobResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.CancelOtaTaskByDeviceResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.CancelOtaTaskByJobResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.CommonOtaResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.CreateOtaDynamicUpgradeJobResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.CreateOtaFirmwareResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.CreateOtaStaticUpgradeJobResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.CreateOtaVerifyJobResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.DeleteOtaFirmwareResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.GenerateOtaUploadUrlResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.ListOtaFirmwareResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.ListOtaJobByDeviceResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.ListOtaJobByFirmwareResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.ListOtaTaskByJobResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.QueryOtaFirmwareResponse;
import com.aliyun.iotx.haas.tdserver.facade.response.ota.QueryOtaJobResponse;
import com.aliyun.iotx.haas.tdserver.sal.iot.IoTClient;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.iot.model.v20180120.*;
import com.aliyuncs.iot.model.v20180120.CreateProductTagsRequest.ProductTag;
import com.aliyuncs.iot.model.v20180120.QueryDeviceResponse.DeviceInfo;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.iot.model.v20180120.BatchCheckDeviceNamesRequest;
import com.aliyuncs.iot.model.v20180120.BatchCheckDeviceNamesResponse;
import com.aliyuncs.iot.model.v20180120.BatchRegisterDeviceWithApplyIdRequest;
import com.aliyuncs.iot.model.v20180120.BatchRegisterDeviceWithApplyIdResponse;
import com.aliyuncs.iot.model.v20180120.CreateProductRequest;
import com.aliyuncs.iot.model.v20180120.CreateProductResponse;
import com.aliyuncs.iot.model.v20180120.GetDeviceStatusRequest;
import com.aliyuncs.iot.model.v20180120.GetDeviceStatusResponse;
import com.aliyuncs.iot.model.v20180120.QueryBatchRegisterDeviceStatusRequest;
import com.aliyuncs.iot.model.v20180120.QueryBatchRegisterDeviceStatusResponse;
import com.aliyuncs.iot.model.v20180120.QueryDeviceDetailRequest;
import com.aliyuncs.iot.model.v20180120.QueryDeviceDetailResponse;
import com.aliyuncs.iot.model.v20180120.QueryDevicePropertyStatusRequest;
import com.aliyuncs.iot.model.v20180120.QueryDevicePropertyStatusResponse;
import com.aliyuncs.iot.model.v20180120.QueryPageByApplyIdRequest;
import com.aliyuncs.iot.model.v20180120.QueryPageByApplyIdResponse;
import com.aliyuncs.iot.model.v20180120.QueryProductRequest;
import com.aliyuncs.iot.model.v20180120.QueryProductResponse;
import com.aliyuncs.iot.model.v20180120.QueryPageByApplyIdResponse.ApplyDeviceInfo;
import com.aliyuncs.iot.model.v20180120.SetDevicePropertyRequest;
import com.aliyuncs.iot.model.v20180120.SetDevicePropertyResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author benxiliu
 * @date 2020/08/25
 */

@Component
public class IoTClientImpl implements IoTClient {

    @Value("${iot.aliyun.haas.tdserver.endPointName}")
    private String endpointName;

    @Value("${iot.aliyun.haas.tdserver.regionId}")
    private String regionId;

    @Value("${iot.aliyun.haas.tdserver.product}")
    private String product;

    @Value("${iot.aliyun.haas.tdserver.domain}")
    private String domain;

    @Value("${aliyun.haas.tdserver.encrypted.accessKey}")
    private String encryptedAccessKey;

    @Value("${aliyun.haas.tdserver.encrypted.accessSecret}")
    private String encryptedAccessSecret;

    @Autowired
    private CryptographUtils cryptographUtils;

    // 阿里云client
    private DefaultAcsClient client;

    // 加密上行topic
    private static final String ENCRYPT_UP_RAW = "/user/encrypt2_upraw";

    // 加密下行topic
    private static final String ENCRYPT_DOWN_RAW = "/user/encrypt2_downraw";


    private static final Logger errorLog = LoggerFactory.getLogger("error");

    private static final String PRODUCT_ALREADY_EXISTED_CODE= "iot.prod.AlreadyExistedProductName";

    private static final int HTTP_REQUEST_STATUS = 200;

    private static final String DEVICE_INVALID_NAME_EXISTED_CODE= "iot.device.InvalidDeviceNameExisted";

    private static final String DEFAULT_ALIYUN_COMMODITY_CODE = "iothub_senior";
    private static final String DEFAULT_NET_TYPE = "CELLULAR";

    @PostConstruct
    public void initClient() {
        try {
            DefaultProfile.addEndpoint(endpointName, regionId, product, domain);
            String accessKey = cryptographUtils.decrypt(encryptedAccessKey);
            String accessSecret = cryptographUtils.decrypt(encryptedAccessSecret);
            IClientProfile profile = DefaultProfile.getProfile(regionId, accessKey, accessSecret);
            client = new DefaultAcsClient(profile);
        } catch (Exception e) {
            errorLog.error("[initClient] init iot pop client failed", e);
        }
    }

    @Override
    public IoTxResult<ProductInfoDTO> createProduct(String uniqueProductName) {
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        IoTxResult<ProductInfoDTO> result = new IoTxResult<>();
        CreateProductRequest createProductRequest = new CreateProductRequest();
        createProductRequest.setProductName(uniqueProductName);
        createProductRequest.setNodeType(0);
        createProductRequest.setDataFormat(0);
        createProductRequest.setAliyunCommodityCode(DEFAULT_ALIYUN_COMMODITY_CODE);
        createProductRequest.setNetType(DEFAULT_NET_TYPE);

        try {
            CreateProductResponse createProductResponse = client.getAcsResponse(createProductRequest);
            if (!createProductResponse.getSuccess()) {
                if (PRODUCT_ALREADY_EXISTED_CODE.equals(
                    createProductResponse.getCode())) {
                    throw new ProductAlreadyExistedException("existed product name");
                } else {
                    throw new IoTxException(IoTxCodes.REQUEST_ERROR.getCode(), createProductResponse.getErrorMessage());
                }
            } else  {
                productInfoDTO.setProductKey(createProductResponse.getProductKey());
                productInfoDTO.setUniqueProductName(uniqueProductName);
                result.setData(productInfoDTO);
                return result;
            }

        } catch (ClientException e) {
            errorLog.error(e.getErrMsg(), e);
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "failed to create product");
        }
    }

    @Override
    public IoTxResult<List<DevicePropertyStatusDTO>> queryDevicePropertyStatus(String productKey, String deviceName) {
        QueryDevicePropertyStatusRequest queryDevicePropertyStatusRequest = new QueryDevicePropertyStatusRequest();
        queryDevicePropertyStatusRequest.setProductKey(productKey);
        queryDevicePropertyStatusRequest.setDeviceName(deviceName);
        List<DevicePropertyStatusDTO> devicePropertyStatusDTOList = new ArrayList<>();

        try {
            QueryDevicePropertyStatusResponse queryDevicePropertyStatusResponse = client.getAcsResponse(queryDevicePropertyStatusRequest);
            queryDevicePropertyStatusResponse.getData().getList().stream().forEach(item -> {
                DevicePropertyStatusDTO devicePropertyStatusDTO = new DevicePropertyStatusDTO();
                BeanUtils.copyProperties(item, devicePropertyStatusDTO);
                devicePropertyStatusDTOList.add(devicePropertyStatusDTO);
            });
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }

        return new IoTxResult<>(devicePropertyStatusDTOList);
    }

    @Override
    public IoTxResult<Void> setDeviceProperty(String productKey, String deviceName, String items) {
        IoTxResult<Void> result = new IoTxResult<>();
        SetDevicePropertyRequest setDevicePropertyRequest = new SetDevicePropertyRequest();
        setDevicePropertyRequest.setProductKey(productKey);
        setDevicePropertyRequest.setDeviceName(deviceName);
        setDevicePropertyRequest.setItems(items);

        try {
            SetDevicePropertyResponse setDevicePropertyResponse = client.getAcsResponse(setDevicePropertyRequest);
            if (!setDevicePropertyResponse.getSuccess()) {
                result.setMessage(setDevicePropertyResponse.getErrorMessage());
                result.setCode(IoTxCodes.REQUEST_ERROR.getCode());
                errorLog.error(setDevicePropertyResponse.getCode() + " : " + setDevicePropertyResponse.getErrorMessage());
            }
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public IoTxResult<DeviceDetailDTO> queryDeviceDetail(String productKey, String deviceName) {
        DeviceDetailDTO deviceDetailDTO = new DeviceDetailDTO();
        QueryDeviceDetailRequest queryDeviceDetailRequest = new QueryDeviceDetailRequest();
        queryDeviceDetailRequest.setProductKey(productKey);
        queryDeviceDetailRequest.setDeviceName(deviceName);

        try {
            QueryDeviceDetailResponse queryDeviceDetailResponse = client.getAcsResponse(queryDeviceDetailRequest);
            BeanUtils.copyProperties(queryDeviceDetailResponse.getData(), deviceDetailDTO);
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }

        return new IoTxResult<>(deviceDetailDTO);
    }

    @Override
    public IoTxResult<ProductDetailDTO> queryProductDetail(String productKey) {
        ProductDetailDTO productDetailDTO = new ProductDetailDTO();
        QueryProductRequest queryProductRequest = new QueryProductRequest();
        queryProductRequest.setProductKey(productKey);

        try {
            QueryProductResponse queryProductResponse = client.getAcsResponse(queryProductRequest);
            BeanUtils.copyProperties(queryProductResponse.getData(), productDetailDTO);
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }

        return new IoTxResult<>(productDetailDTO);
    }

    @Override
    public IoTxResult<BatchUploadDeviceNameResultDTO> batchCheckDeviceNames(String productKey, List<String> deviceNameList) {
        BatchUploadDeviceNameResultDTO batchUploadDeviceNameResultDTO = new BatchUploadDeviceNameResultDTO();
        IoTxResult<BatchUploadDeviceNameResultDTO> result = new IoTxResult<>();
        BatchCheckDeviceNamesRequest batchCheckDeviceNamesRequest = new BatchCheckDeviceNamesRequest();
        batchCheckDeviceNamesRequest.setDeviceNames(deviceNameList);
        batchCheckDeviceNamesRequest.setProductKey(productKey);

        try {
            BatchCheckDeviceNamesResponse batchCheckDeviceNamesResponse = client.getAcsResponse(batchCheckDeviceNamesRequest);
            // 失败需要返回非法设备名列表
            batchUploadDeviceNameResultDTO.setBatchId(batchCheckDeviceNamesResponse.getData().getApplyId());
            batchUploadDeviceNameResultDTO.setInvalidList(batchCheckDeviceNamesResponse.getData().getInvalidDeviceNameList());

            result.setData(batchUploadDeviceNameResultDTO);

            if (!batchCheckDeviceNamesResponse.getSuccess()) {
                if (DEVICE_INVALID_NAME_EXISTED_CODE.equals(batchCheckDeviceNamesResponse.getCode())) {
                    batchUploadDeviceNameResultDTO.setInvalidList(batchCheckDeviceNamesResponse.getData().getInvalidDeviceNameList());
                    return new IoTxResult<>(IoTxCodes.REQUEST_PARAM_ERROR, batchUploadDeviceNameResultDTO);
                }
                throw new IoTxException(IoTxCodes.REQUEST_ERROR.getCode(), batchCheckDeviceNamesResponse.getErrorMessage());
            }
            return result;
        } catch (ClientException e) {
            errorLog.error(e.getMessage(), e);
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "failed to check device names");
        }
    }

    @Override
    public IoTxResult<BatchRegisterDeviceStatusDTO> queryBatchRegisterDeviceStatus(String productKey, Long applyId) {
        BatchRegisterDeviceStatusDTO batchRegisterDeviceStatusDTO = new BatchRegisterDeviceStatusDTO();
        IoTxResult<BatchRegisterDeviceStatusDTO> result = new IoTxResult<>();
        QueryBatchRegisterDeviceStatusRequest queryBatchRegisterDeviceStatusRequest = new QueryBatchRegisterDeviceStatusRequest();
        queryBatchRegisterDeviceStatusRequest.setApplyId(applyId);
        queryBatchRegisterDeviceStatusRequest.setProductKey(productKey);

        try {
            QueryBatchRegisterDeviceStatusResponse queryBatchRegisterDeviceStatusResponse = client.getAcsResponse(queryBatchRegisterDeviceStatusRequest);
            batchRegisterDeviceStatusDTO.setStatus(queryBatchRegisterDeviceStatusResponse.getData().getStatus());
            batchRegisterDeviceStatusDTO.setInvalidList(queryBatchRegisterDeviceStatusResponse.getData().getInvalidList());
            batchRegisterDeviceStatusDTO.setValidList(queryBatchRegisterDeviceStatusResponse.getData().getValidList());

            result.setData(batchRegisterDeviceStatusDTO);

            if (!queryBatchRegisterDeviceStatusResponse.getSuccess()) {
                throw new IoTxException(IoTxCodes.REQUEST_ERROR.getCode(), queryBatchRegisterDeviceStatusResponse.getErrorMessage());
            }

            return result;
        } catch (ClientException e) {
            errorLog.error(e.getMessage(), e);
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "query device register status");
        }
    }

    @Override
    public IoTxResult<Void> batchRegisterDeviceWithApplyId(String productKey, Long applyId) {
        IoTxResult<Void> result = new IoTxResult<>();
        BatchRegisterDeviceWithApplyIdRequest batchRegisterDeviceWithApplyIdRequest = new BatchRegisterDeviceWithApplyIdRequest();
        batchRegisterDeviceWithApplyIdRequest.setProductKey(productKey);
        batchRegisterDeviceWithApplyIdRequest.setApplyId(applyId);

        try {
            BatchRegisterDeviceWithApplyIdResponse batchRegisterDeviceWithApplyIdResponse = client.getAcsResponse(batchRegisterDeviceWithApplyIdRequest);

            if (!batchRegisterDeviceWithApplyIdResponse.getSuccess()) {
                throw new IoTxException(IoTxCodes.REQUEST_ERROR.getCode(), batchRegisterDeviceWithApplyIdResponse.getErrorMessage());
            }

            return result;
        } catch (ClientException e) {
            errorLog.error(e.getMessage(), e);
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "batch register device failed");
        }
    }

    @Override
    public IoTxResult<List<RegisterDeviceInfoDTO>> queryAllRegisterDeviceWithApplyId(String productkey,
        Long applyId) {
        IoTxResult<List<RegisterDeviceInfoDTO>> result = new IoTxResult<>();
        List<RegisterDeviceInfoDTO> registerDeviceInfoList = new ArrayList<>();
        QueryPageByApplyIdRequest queryPageByApplyIdRequest = new QueryPageByApplyIdRequest();

        int pageNum = 0;

        // 已确认目前分页最大值为200
        queryPageByApplyIdRequest.setPageSize(200);
        queryPageByApplyIdRequest.setApplyId(applyId);

        // 每批次最多2000个设备
        while (pageNum < 10) {
            pageNum++;
            queryPageByApplyIdRequest.setCurrentPage(pageNum);

            try {
                QueryPageByApplyIdResponse queryPageByApplyIdResponse = client.getAcsResponse(queryPageByApplyIdRequest);

                if (!queryPageByApplyIdResponse.getSuccess()) {
                    throw new IoTxException(IoTxCodes.REQUEST_ERROR.getCode(), queryPageByApplyIdResponse.getErrorMessage());
                }

                if (queryPageByApplyIdResponse.getApplyDeviceList().size() > 0) {
                    List<ApplyDeviceInfo> applyDeviceInfoList = queryPageByApplyIdResponse.getApplyDeviceList();
                    for (ApplyDeviceInfo applyDeviceInfo : applyDeviceInfoList) {
                        RegisterDeviceInfoDTO registerDeviceInfoDTO = new RegisterDeviceInfoDTO();
                        BeanUtils.copyProperties(applyDeviceInfo, registerDeviceInfoDTO);
                        registerDeviceInfoList.add(registerDeviceInfoDTO);
                    }
                } else {
                    break;
                }

            } catch (ClientException e) {
                errorLog.error(e.getMessage(), e);
                throw new IoTxException(IoTxCodes.SERVER_ERROR, "query register device page failed");
            }
        }

        result.setData(registerDeviceInfoList);
        return result;
    }

    @Override
    public IoTxResult<PageResultDTO<RegisterDeviceInfoDTO>> queryRegisterDevicePageWithApplyId(String productkey,
        Long applyId,
        Integer pageSize,
        Integer pageNo) {
        IoTxResult<PageResultDTO<RegisterDeviceInfoDTO>> result = new IoTxResult<>();
        PageResultDTO<RegisterDeviceInfoDTO> pageResult = new PageResultDTO<>();
        List<RegisterDeviceInfoDTO> registerDeviceInfoList = new ArrayList<>();

        QueryPageByApplyIdRequest queryPageByApplyIdRequest = new QueryPageByApplyIdRequest();
        queryPageByApplyIdRequest.setPageSize(pageSize);
        queryPageByApplyIdRequest.setCurrentPage(pageNo);
        queryPageByApplyIdRequest.setApplyId(applyId);

        try {
            QueryPageByApplyIdResponse queryPageByApplyIdResponse = client.getAcsResponse(queryPageByApplyIdRequest);

            if (!queryPageByApplyIdResponse.getSuccess()) {
                throw new IoTxException(IoTxCodes.REQUEST_ERROR, queryPageByApplyIdResponse.getErrorMessage());
            }

            List<ApplyDeviceInfo> applyDeviceInfoList = queryPageByApplyIdResponse.getApplyDeviceList();
            for (ApplyDeviceInfo applyDeviceInfo : applyDeviceInfoList) {
                RegisterDeviceInfoDTO registerDeviceInfoDTO = new RegisterDeviceInfoDTO();
                BeanUtils.copyProperties(applyDeviceInfo, registerDeviceInfoDTO);
                registerDeviceInfoList.add(registerDeviceInfoDTO);
            }

            pageResult.setPageNo(queryPageByApplyIdResponse.getPage());
            pageResult.setPageSize(queryPageByApplyIdResponse.getPageSize());
            pageResult.setTotal(queryPageByApplyIdResponse.getTotal().longValue());
            pageResult.setData(registerDeviceInfoList);

            result.setData(pageResult);
            return result;

        } catch (ClientException e) {
            errorLog.error(e.getMessage(), e);
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "query register device page failed");
        }
    }

    @Override
    public IoTxResult<String> queryDeviceStatus(String productKey, String deviceName) {
        GetDeviceStatusRequest getDeviceStatusRequest = new GetDeviceStatusRequest();
        getDeviceStatusRequest.setProductKey(productKey);
        getDeviceStatusRequest.setDeviceName(deviceName);

        try {
            GetDeviceStatusResponse getDeviceStatusResponse = client.getAcsResponse(getDeviceStatusRequest);
            return new IoTxResult<>(getDeviceStatusResponse.getData().getStatus());
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }

        return new IoTxResult<>(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<Void> createProductTags(String productKey, List<ProductTagDTO> productTagDTOList) {
        IoTxResult<Void> result = new IoTxResult<>();
        CreateProductTagsRequest createProductTagsRequest = new CreateProductTagsRequest();
        createProductTagsRequest.setProductKey(productKey);
        List<ProductTag> productTagList = new ArrayList<>();

        for (ProductTagDTO productTagDTO : productTagDTOList) {
            ProductTag productTag = new ProductTag();
            productTag.setTagKey(productTagDTO.getTagKey());
            productTag.setTagValue(productTagDTO.getTagValue());
            productTagList.add(productTag);
        }

        createProductTagsRequest.setProductTags(productTagList);

        try {
            CreateProductTagsResponse createProductResponse = client.getAcsResponse(createProductTagsRequest);
            if (!createProductResponse.getSuccess()) {
                throw new IoTxException(IoTxCodes.SERVER_ERROR, createProductResponse.getErrorMessage());
            }
        } catch (ClientException e) {
            errorLog.error(e.getMessage(), e);
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "create product tags failed");
        }

        return result;
    }

    @Override
    public IoTxResult<PageResultDTO<BriefDeviceInfoDTO>> queryDevicePage(String productKey, Integer pageSize, Integer pageNo) {
        IoTxResult<PageResultDTO<BriefDeviceInfoDTO>> result = new IoTxResult<>();
        PageResultDTO<BriefDeviceInfoDTO> pageResult = new PageResultDTO<>();
        List<BriefDeviceInfoDTO> briefDeviceInfoList = new ArrayList<>();

        QueryDeviceRequest queryDeviceRequest = new QueryDeviceRequest();
        queryDeviceRequest.setProductKey(productKey);
        queryDeviceRequest.setPageSize(pageSize);
        queryDeviceRequest.setCurrentPage(pageNo);

        try {
            QueryDeviceResponse queryDeviceResponse = client.getAcsResponse(queryDeviceRequest);

            if (!queryDeviceResponse.getSuccess()) {
                throw new IoTxException(IoTxCodes.REQUEST_ERROR, queryDeviceResponse.getErrorMessage());
            }

            List<DeviceInfo> deviceInfoList = queryDeviceResponse.getData();
            for (DeviceInfo deviceInfo : deviceInfoList) {
                BriefDeviceInfoDTO briefDeviceInfoDTO = new BriefDeviceInfoDTO();
                briefDeviceInfoDTO.setDeviceName(deviceInfo.getDeviceName());
                briefDeviceInfoDTO.setDeviceSecret(deviceInfo.getDeviceSecret());
                briefDeviceInfoDTO.setDeviceStatus(deviceInfo.getDeviceStatus());
                briefDeviceInfoList.add(briefDeviceInfoDTO);
            }

            pageResult.setData(briefDeviceInfoList);
            pageResult.setTotal(queryDeviceResponse.getTotal().longValue());
            pageResult.setPageSize(queryDeviceResponse.getPageSize());
            pageResult.setPageNo(queryDeviceResponse.getPage());

            result.setData(pageResult);
            return result;
        } catch (ClientException e) {
            errorLog.error(e.getMessage(), e);
            throw new IoTxException(IoTxCodes.SERVER_ERROR, "query device page failed");
        }
    }

    public IoTxResult<CommonOtaResponse> commonOtaRequest(CommonOtaRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest(req);
        CommonOtaResponse response = new CommonOtaResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<GenerateOtaUploadUrlResponse> generateOtaUploadUrl() {
        CommonRequest request = buildCommonRequest("GenerateOTAUploadURL");
        GenerateOtaUploadUrlResponse response = new GenerateOtaUploadUrlResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<CreateOtaFirmwareResponse> CreateOtaFirmware(CreateOtaFirmwareRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("CreateOTAFirmware");
        request.putQueryParameter("RegionId", regionId);
        request.putQueryParameter("FirmwareName", req.getFirmwareName());
        request.putQueryParameter("DestVersion", req.getDestVersion());
        request.putQueryParameter("FirmwareUrl", req.getFirmwareUrl());
        if(StringUtils.isNotBlank(req.getProductKey())){
            request.putQueryParameter("ProductKey", req.getProductKey());
        }
        request.putQueryParameter("FirmwareDesc", req.getFirmwareDesc());
        request.putQueryParameter("SrcVersion", req.getSrcVersion());
        request.putQueryParameter("ModuleName", req.getModuleName());
        CreateOtaFirmwareResponse response = new CreateOtaFirmwareResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }


    @Override
    public IoTxResult<ListOtaFirmwareResponse> listOtaFirmware(ListOtaFirmwareRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("ListOTAFirmware");
        request.putQueryParameter("RegionId", regionId);
        request.putQueryParameter("CurrentPage", req.getCurrentPage() + "");
        request.putQueryParameter("PageSize", req.getPageSize() + "");
        if(StringUtils.isNotBlank(req.getProductKey())){
            request.putQueryParameter("ProductKey", req.getProductKey());
        }
        request.putQueryParameter("DestVersion", req.getDestVersion());

        ListOtaFirmwareResponse response = new ListOtaFirmwareResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<CreateOtaVerifyJobResponse> createOtaVerifyJob(CreateOtaVerifyJobRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        String deviceNames = req.getTargetDeviceNames();
        CommonRequest request = buildCommonRequest("CreateOTAVerifyJob");
        request.putQueryParameter("ProductKey",req.getProductKey());
        request.putQueryParameter("FirmwareId", req.getFirmwareId());
        Set<String> deviceSet = Arrays.asList(deviceNames.split(",")).stream().collect(Collectors.toSet());
        int index = 1;
        for(String deviceName : deviceSet){
            request.putQueryParameter("TargetDeviceName." + index, deviceName);
        }
        if(req.getTimeoutInMinutes() != null && req.getTimeoutInMinutes() >0){
            request.putQueryParameter("TimeoutInMinutes", req.getTimeoutInMinutes() + "");
        }
        CreateOtaVerifyJobResponse response = new CreateOtaVerifyJobResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }

        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<CreateOtaStaticUpgradeJobResponse> createOtaStaticUpgradeJob(
        CreateOtaStaticUpgradeJobRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("CreateOTAStaticUpgradeJob");
        request.putQueryParameter("ProductKey",req.getProductKey());
        request.putQueryParameter("FirmwareId", req.getFirmwareId());
        if(req.getScheduleTime() != null && req.getScheduleTime() >0){
            request.putQueryParameter("ScheduleTime", req.getFirmwareId());
        }
        if(req.getRetryInterval() != null && req.getRetryInterval() >=0 && req.getRetryInterval() <=1440 && req.getRetryCount() != null && req.getRetryCount() >=1
            &&req.getRetryCount() <= 5){
            request.putQueryParameter("RetryInterval", req.getRetryInterval() + "");
            request.putQueryParameter("RetryCount", req.getRetryCount() + "");
        }
        if(req.getTimeoutInMinutes() != null && req.getTimeoutInMinutes() >=0 && req.getTimeoutInMinutes() <= 1440){
            request.putQueryParameter("TimeoutInMinutes", req.getTimeoutInMinutes() + "");
        }
        if(req.getMaximumPerMinute() != null && req.getMaximumPerMinute() >=0 && req.getMaximumPerMinute()<= 1000){
            request.putQueryParameter("MaximumPerMinute", req.getMaximumPerMinute() + "");
        }
        if(req.getScheduleFinishTime() != null && req.getScheduleFinishTime() >0){
            //TODO 结束时间距发起时间（ScheduleTime）最少1小时，最多为30天。取值为13位毫秒值时间戳,是否需要校验
            request.putQueryParameter("ScheduleFinishTime", req.getScheduleFinishTime() + "");
        }
        if(req.getOverWriteMode() != null && req.getOverWriteMode()  == 1 || req.getOverWriteMode() == 2){
            request.putQueryParameter("OverwriteMode", req.getOverWriteMode() + "");
        }
        if(req.getTargetSelection().equalsIgnoreCase("GRAY") && StringUtils.isNotBlank(req.getGrayPercent())){
            request.putQueryParameter("GrayPercent", req.getGrayPercent() + "");
        }
        if(req.getTargetSelection().equalsIgnoreCase("GRAY") || req.getTargetSelection().equalsIgnoreCase("ALL")){
            //全量或灰度升级必须填SrcVersion
            if(StringUtils.isBlank(req.getSrcVersions())){
                return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
            }
            Set<String> srcVersions = Arrays.asList(req.getSrcVersions().split(",")).stream().collect(Collectors.toSet());
            if(srcVersions.size() > 10){
                return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
            }
            int index = 1;
            for(String srcVersion : srcVersions){
                request.putQueryParameter("SrcVersion." + index, srcVersion);
            }
        }
        if(req.getTargetSelection().equalsIgnoreCase("SPECIFIC") && StringUtils.isNotBlank(req.getTargetDeviceNames())){
            List<String> deviceList = Arrays.asList(req.getTargetDeviceNames().split(","));
            Set<String> deviceSet = deviceList.stream().collect(Collectors.toSet());
            int index = 1;
            for(String deviceName : deviceSet){
                request.putQueryParameter("TargetDeviceName." + index, deviceName);
                index++;
            }
        }
        CreateOtaStaticUpgradeJobResponse response = new CreateOtaStaticUpgradeJobResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<CreateOtaDynamicUpgradeJobResponse> createOtaDynamicUpgradeJob(
        CreateOtaDynamicUpgradeJobRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("CreateOTADynamicUpgradeJob");
        request.putQueryParameter("ProductKey",req.getProductKey());
        request.putQueryParameter("FirmwareId", req.getFirmwareId());
        if(StringUtils.isNotBlank(req.getSrcVersions())){
            Set<String> srcVersions = Arrays.asList(req.getSrcVersions().split(",")).stream().collect(Collectors.toSet());
            if(srcVersions.size() > 10){
                return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
            }
            int index = 1;
            for(String srcVersion : srcVersions){
                request.putQueryParameter("SrcVersion." + index, srcVersion);
            }
        }

        if(req.getRetryInterval() != null && req.getRetryInterval() >=0 && req.getRetryInterval() <=1440 && req.getRetryCount() != null && req.getRetryCount() >=1
            &&req.getRetryCount() <= 5){
            request.putQueryParameter("RetryInterval", req.getRetryInterval() + "");
            request.putQueryParameter("RetryCount", req.getRetryCount() + "");
        }
        if(req.getTimeoutInMinutes() != null && req.getTimeoutInMinutes() >=0 && req.getTimeoutInMinutes() <= 1440){
            request.putQueryParameter("TimeoutInMinutes", req.getTimeoutInMinutes() + "");
        }
        if(req.getMaximumPerMinute() != null && req.getMaximumPerMinute() >=0 && req.getMaximumPerMinute()<= 1000){
            request.putQueryParameter("MaximumPerMinute", req.getMaximumPerMinute() + "");
        }
        if(req.getOverWriteMode() != null && req.getOverWriteMode()  == 1 || req.getOverWriteMode() == 2){
            request.putQueryParameter("OverwriteMode", req.getOverWriteMode() + "");
        }
        if(req.getDynamicMode() != null && req.getDynamicMode()  == 1 || req.getDynamicMode() == 2){
            request.putQueryParameter("DynamicMode", req.getOverWriteMode() + "");
        }
        CreateOtaDynamicUpgradeJobResponse response = new CreateOtaDynamicUpgradeJobResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<DeleteOtaFirmwareResponse> deleteOtaFirmware(DeleteOtaFirmwareRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("DeleteOTAFirmware");
        request.putQueryParameter("FirmwareId", req.getFirmwareId());
        DeleteOtaFirmwareResponse response = new DeleteOtaFirmwareResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<QueryOtaFirmwareResponse> queryOtaFirmware(QueryOtaFirmwareRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("QueryOTAFirmware");
        request.putQueryParameter("FirmwareId", req.getFirmwareId());
        QueryOtaFirmwareResponse response = new QueryOtaFirmwareResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<ListOtaJobByFirmwareResponse> listOtaJobByFirmware(ListOtaJobByFirmwareRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("ListOTAJobByFirmware");
        request.putQueryParameter("FirmwareId", req.getFirmwareId());
        request.putQueryParameter("PageSize", req.getPageSize() + "");
        request.putQueryParameter("CurrentPage", req.getCurrentPage() + "");
        QueryOtaFirmwareResponse response = new QueryOtaFirmwareResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<ListOtaJobByDeviceResponse> listOtaJobByDevice(ListOtaJobByDeviceRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("ListOTAJobByDevice");
        request.putQueryParameter("FirmwareId", req.getFirmwareId());
        request.putQueryParameter("PageSize", req.getPageSize() + "");
        request.putQueryParameter("CurrentPage", req.getCurrentPage() + "");
        request.putQueryParameter("ProductKey", req.getProductKey());
        request.putQueryParameter("DeviceName", req.getDeviceName());
        ListOtaJobByDeviceResponse response = new ListOtaJobByDeviceResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<ListOtaTaskByJobResponse> listOtaTaskByJob(ListOtaTaskByJobRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("ListOTATaskByJob");
        request.putQueryParameter("JobId", req.getJobId());
        request.putQueryParameter("PageSize", req.getPageSize() + "");
        request.putQueryParameter("CurrentPage", req.getCurrentPage() + "");
        request.putQueryParameter("TaskStatus", req.getTaskStatus());
        ListOtaJobByDeviceResponse response = new ListOtaJobByDeviceResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<QueryOtaJobResponse> queryOtaJob(QueryOtaJobRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("QueryOTAJob");
        request.putQueryParameter("JobId", req.getJobId());
        QueryOtaJobResponse response = new QueryOtaJobResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<CancelOtaStrategyByJobResponse> cancelOtaStrategyByJob(CancelOtaStrategyByJobRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("CancelOTAStrategyByJob");
        request.putQueryParameter("JobId", req.getJobId());
        CancelOtaStrategyByJobResponse response = new CancelOtaStrategyByJobResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<CancelOtaTaskByJobResponse> cancelOtaTaskByJob(CancelOtaTaskByJobRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("CancelOTATaskByJob");
        request.putQueryParameter("JobId", req.getJobId());
        request.putQueryParameter("CancelScheduledTask", req.getCancelScheduledTask() + "");
        request.putQueryParameter("CancelQueuedTask", req.getCancelQueuedTask() + "");
        request.putQueryParameter("CancelInProgressTask", req.getCancelInProgressTask() + "");
        request.putQueryParameter("CancelNotifiedTask", req.getCancelNotifiedTask() + "");
        CancelOtaTaskByJobResponse response = new CancelOtaTaskByJobResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<CancelOtaTaskByDeviceResponse> cancelOtaTaskByDevice(CancelOtaTaskByDeviceRequest req) {
        if(null == req || !req.isValid()){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        CommonRequest request = buildCommonRequest("CancelOTATaskByDevice");
        request.putQueryParameter("JobId", req.getJobId());
        Set<String> deviceSet = Arrays.asList(req.getDeviceNames().split(",")).stream().collect(Collectors.toSet());
        if(deviceSet.size() > 200){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_PARAM_ERROR);
        }
        int index = 1;
        for(String deviceName : deviceSet){
            request.putQueryParameter("TargetDeviceName." + index, deviceName);
        }
        request.putQueryParameter("ProductKey", req.getProductKey());
        request.putQueryParameter("FirmwareId", req.getFirmwareId());
        request.putQueryParameter("JobId", req.getJobId());
        CancelOtaTaskByDeviceResponse response = new CancelOtaTaskByDeviceResponse();
        try {
            return response(request, response);
        }  catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
    }

    private boolean checkCommonResponse(CommonResponse response){
        if(null == response){
            return false;
        }
        if(response.getHttpStatus() != HTTP_REQUEST_STATUS || StringUtils.isBlank(response.getData())){
            return false;
        }
        String data = response.getData();
        JSONObject jsonObject = JSON.parseObject(data);
        if(null == jsonObject){
            return false;
        }
        Boolean success = jsonObject.getBoolean("Success");
        return success;
    }

    private CommonRequest buildCommonRequest(String sysAction){
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(domain);
        request.putQueryParameter("RegionId", regionId);
        request.setSysVersion("2018-01-20");
        request.setSysAction(sysAction);
        return request;
    }

    private CommonRequest buildCommonRequest(CommonOtaRequest req){
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain(domain);
        request.putQueryParameter("RegionId", regionId);
        request.setSysVersion("2018-01-20");
        Map<String, String> params = req.getParams();
        request.setSysAction(req.getSysAction());
        params.forEach((key, value) -> request.putQueryParameter(key, value));
        return request;
    }

    private IoTxResult response(CommonRequest request, AbstractResponse response) throws Exception{
        CommonResponse resp = client.getCommonResponse(request);
        if(!checkCommonResponse(resp)){
            return IoTxResultUtil.error(IoTxCodes.REQUEST_ERROR);
        }
        response.setLpResp(resp.getData());
        return IoTxResultUtil.success(response);
    }

    @Override
    public IoTxResult<DeviceServiceResultDTO> invokeDeviceSyncService(String productKey, String deviceName, String identifier, String args) {
        InvokeThingServiceRequest invokeThingServiceRequest = new InvokeThingServiceRequest();
        invokeThingServiceRequest.setProductKey(productKey);
        invokeThingServiceRequest.setDeviceName(deviceName);
        invokeThingServiceRequest.setIdentifier(identifier);
        invokeThingServiceRequest.setArgs(args);

        try {
            InvokeThingServiceResponse invokeThingServiceResponse = client.getAcsResponse(invokeThingServiceRequest);
            if (invokeThingServiceResponse.getSuccess()) {
                DeviceServiceResultDTO deviceServiceResultDTO = new DeviceServiceResultDTO();
                deviceServiceResultDTO.setMessageId(invokeThingServiceResponse.getData().getMessageId());
                deviceServiceResultDTO.setResult(invokeThingServiceResponse.getData().getResult());
                return new IoTxResult<>(deviceServiceResultDTO);
            }
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }

        return new IoTxResult<>(IoTxCodes.REQUEST_ERROR);
    }

    @Override
    public IoTxResult<BatchGetDeviceStateResponse> batchGetDeviceStatus(String productKey, List<String> dnList) {
        BatchGetDeviceStateRequest request = new BatchGetDeviceStateRequest();
        request.setDeviceNames(dnList);
        request.setProductKey(productKey);
        try {
            BatchGetDeviceStateResponse response = client.getAcsResponse(request);
            return IoTxResultUtil.success(response);
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }
        return new IoTxResult<>(IoTxCodes.REQUEST_ERROR);
    }

}

