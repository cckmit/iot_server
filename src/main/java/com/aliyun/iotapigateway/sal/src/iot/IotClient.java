package com.aliyun.iotx.haas.tdserver.sal.iot;

import com.aliyun.iotx.common.base.service.IoTxResult;
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
import com.aliyuncs.iot.model.v20180120.BatchGetDeviceStateResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author benxiliu
 * @date 2020/08/25
 */

public interface IoTClient {
    IoTxResult<ProductInfoDTO> createProduct(String productName);

    IoTxResult<List<DevicePropertyStatusDTO>> queryDevicePropertyStatus(String productKey, String deviceName);

    IoTxResult<Void> setDeviceProperty(String productKey, String deviceName, String items);

    IoTxResult<DeviceDetailDTO> queryDeviceDetail(String productKey, String deviceName);

    /**
     * 根据PK查询产品信息
     * @param productKey
     * @return
     */
    IoTxResult<ProductDetailDTO> queryProductDetail(String productKey);

    IoTxResult<BatchUploadDeviceNameResultDTO> batchCheckDeviceNames(String productKey, List<String> deviceNameList);

    IoTxResult<BatchRegisterDeviceStatusDTO> queryBatchRegisterDeviceStatus(String productKey,Long applyId);

    IoTxResult<Void> batchRegisterDeviceWithApplyId(String productKey, Long applyId);

    IoTxResult<List<RegisterDeviceInfoDTO>> queryAllRegisterDeviceWithApplyId(String productkey,
        Long applyId);

    IoTxResult<PageResultDTO<RegisterDeviceInfoDTO>> queryRegisterDevicePageWithApplyId(String productkey,
        Long applyId,
        Integer pageSize,
        Integer pageNo);

    IoTxResult<String> queryDeviceStatus(String productKey, String deviceName);

    IoTxResult<Void> createProductTags(String productKey, List<ProductTagDTO> productTagDTOList);

    IoTxResult<PageResultDTO<BriefDeviceInfoDTO>> queryDevicePage(String productKey, Integer pageSize, Integer pageNo);

    IoTxResult<CommonOtaResponse> commonOtaRequest(CommonOtaRequest req);

    IoTxResult<GenerateOtaUploadUrlResponse> generateOtaUploadUrl();

    IoTxResult<CreateOtaFirmwareResponse> CreateOtaFirmware(CreateOtaFirmwareRequest req);

    IoTxResult<DeleteOtaFirmwareResponse> deleteOtaFirmware(DeleteOtaFirmwareRequest req);

    IoTxResult<ListOtaFirmwareResponse> listOtaFirmware(ListOtaFirmwareRequest req);

    IoTxResult<QueryOtaFirmwareResponse> queryOtaFirmware(QueryOtaFirmwareRequest req);

    IoTxResult<CreateOtaVerifyJobResponse> createOtaVerifyJob(CreateOtaVerifyJobRequest req);

    IoTxResult<CreateOtaStaticUpgradeJobResponse> createOtaStaticUpgradeJob(CreateOtaStaticUpgradeJobRequest req);

    IoTxResult<CreateOtaDynamicUpgradeJobResponse> createOtaDynamicUpgradeJob(CreateOtaDynamicUpgradeJobRequest req);

    IoTxResult<ListOtaJobByFirmwareResponse> listOtaJobByFirmware(ListOtaJobByFirmwareRequest req);

    IoTxResult<ListOtaJobByDeviceResponse> listOtaJobByDevice(ListOtaJobByDeviceRequest req);

    IoTxResult<ListOtaTaskByJobResponse> listOtaTaskByJob(ListOtaTaskByJobRequest req);

    IoTxResult<QueryOtaJobResponse> queryOtaJob(QueryOtaJobRequest req);

    IoTxResult<CancelOtaStrategyByJobResponse> cancelOtaStrategyByJob(CancelOtaStrategyByJobRequest req);

    IoTxResult<CancelOtaTaskByJobResponse> cancelOtaTaskByJob(CancelOtaTaskByJobRequest req);

    IoTxResult<CancelOtaTaskByDeviceResponse> cancelOtaTaskByDevice(CancelOtaTaskByDeviceRequest req);

    IoTxResult<DeviceServiceResultDTO>  invokeDeviceSyncService(String productKey, String deviceName, String identifier, String args);

    IoTxResult<BatchGetDeviceStateResponse> batchGetDeviceStatus(String productKey, List<String> dnList);
}

