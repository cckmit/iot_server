package com.aliyun.iotx.haas.tdserver.service.impl;

import com.alibaba.boot.hsf.annotation.HSFProvider;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.exception.DeviceNotFoundException;
import com.aliyun.iotx.haas.tdserver.common.exception.HaasServerInternalException;
import com.aliyun.iotx.haas.tdserver.common.utils.HexStrTool;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceBindDAO;
import com.aliyun.iotx.haas.tdserver.dal.dao.device.DeviceShareDAO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceBindDO;
import com.aliyun.iotx.haas.tdserver.dal.domain.device.DeviceShareDO;
import com.aliyun.iotx.haas.tdserver.facade.DeviceQRCodeService;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceDetailDTO;
import com.aliyun.iotx.haas.tdserver.facade.dto.device.DeviceQRCodeDTO;
import com.aliyun.iotx.haas.tdserver.sal.iot.IoTClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

/**
 * @author imost.lwf
 * @date 2020/11/02
 */

@HSFProvider(serviceInterface = DeviceQRCodeService.class)
public class DeviceQRCodeServiceImpl implements DeviceQRCodeService {

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Resource
    private DeviceBindDAO deviceBindDAO;

    @Resource
    private DeviceShareDAO deviceShareDAO;

    private final IoTClient ioTClient;

    private static final Logger errorLog = LoggerFactory.getLogger("error");

    @Autowired
    public DeviceQRCodeServiceImpl(IoTClient ioTClient) {
        this.ioTClient = ioTClient;
    }

    private String getDeviceSecret(
            String productKey,
            String deviceName) throws DeviceNotFoundException {

        IoTxResult<DeviceDetailDTO> deviceDetailDTOIoTxResult =
                ioTClient.queryDeviceDetail(productKey, deviceName);
        if (!deviceDetailDTOIoTxResult.hasSucceeded()) {
            throw new DeviceNotFoundException("PK/DN not exist in LP");
        }
        String deviceSecret = deviceDetailDTOIoTxResult.getData().getDeviceSecret();
        if (deviceSecret == null) {
            throw new DeviceNotFoundException("PK/DN not exist in LP");
        }
        return deviceSecret;
    }

    @Override
    public IoTxResult<DeviceQRCodeDTO> queryDeviceQRCode(String haasUserId, String productKey, String deviceName) {
        if (haasUserId == null || haasUserId.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_QR_HAAS_USER_ID_EMPTY);
        }

        if (productKey == null || productKey.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_QR_PK_EMPTY);
        }

        if (deviceName == null || deviceName.isEmpty()) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_QR_DN_EMPTY);
        }

        try {
            // 查询设备与用户绑定、分享关系是否存在
            List<DeviceBindDO> deviceBindDOList = deviceBindDAO.getBinding(productKey, deviceName, haasUserId, environment);
            List<DeviceShareDO> deviceShareDOList = deviceShareDAO.getShareInfoWithDeviceNameAndAuthorizee(productKey, deviceName, haasUserId, environment);
            if (deviceBindDOList.isEmpty() && deviceShareDOList.isEmpty()) {
                errorLog.error("lockVehicle error: " + deviceBindDOList.toString());
                errorLog.error("lockVehicle share error: " + deviceShareDOList.toString());
                errorLog.error("lockVehicle error: pk - " + productKey + " dn - " + deviceName + " env - " + environment);
                return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_QR_UNAUTHORIZED);
            }
        } catch (DataAccessException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DATABASE_ACCESS);
        }

        DeviceQRCodeDTO deviceQRCodeDTO = new DeviceQRCodeDTO();
        deviceQRCodeDTO.setProductKey(productKey);
        deviceQRCodeDTO.setDeviceName(deviceName);

        try {
            // 获取DS，同时对错误PK/DN触发DeviceNotFound异常捕获
            byte[] hexDs = HexStrTool.hexStrToByteArray(
                    getDeviceSecret(productKey, deviceName));

            // checkcode: 以DS为密钥，对PK+DN拼接字符串做HMAC Sha256加密，再对加密结果Base64URL(with Padding)压缩
            String text = productKey + deviceName;

            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hexDs, "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hexCode = sha256Hmac.doFinal(text.getBytes(StandardCharsets.UTF_8));
            String checkCode = Base64.getUrlEncoder().encodeToString(hexCode);

            deviceQRCodeDTO.setCheckCode(checkCode);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            try {
                throw new HaasServerInternalException(e.getMessage());
            } catch (HaasServerInternalException haasServerInternalException) {
                haasServerInternalException.printStackTrace();
            }
        } catch (DeviceNotFoundException e) {
            return new IoTxResult<>(HaasIoTxCodes.ERROR_DEVICE_QR_UNAUTHORIZED);
        }

        return new IoTxResult<>(deviceQRCodeDTO);
    }
}

