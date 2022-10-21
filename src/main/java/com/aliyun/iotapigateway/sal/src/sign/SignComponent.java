package com.aliyun.iotx.haas.tdserver.sal.sign;

import com.aliyun.iotx.account.service.v2.model.employee.EmployeeDTO;
import com.aliyun.iotx.common.base.exception.IoTxException;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.HaasIoTxCodes;
import com.aliyun.iotx.haas.tdserver.common.constants.LogConstants;
import com.aliyun.iotx.account.service.v2.request.employee.QueryEmployeeRequest;
import com.aliyun.iotx.haas.tdserver.facade.dto.odm.OdmInfoDTO;
import com.aliyun.iotx.haas.tdserver.facade.odm.OdmUserService;
import com.aliyun.iotx.haas.tdserver.sal.aep.EmployeeServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author imost.lwf
 * @date 2021/1/1
 */

@Component
public class SignComponent {

    @Value("${iot.aliyun.haas.tdserver.env}")
    private String environment;

    @Resource
    private EmployeeServiceClient employeeServiceClient;

    @Resource
    private OdmUserService odmUserService;

    private final Logger errLog = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_ERROR);

    public IoTxResult<String> sign(String identityId, String mobile, String email, String contactName, String companyName, String contactPost) {
        try {
            QueryEmployeeRequest queryEmployeeRequest = new QueryEmployeeRequest();
            queryEmployeeRequest.setEmployeeId(identityId);
            IoTxResult<EmployeeDTO> result = employeeServiceClient.queryEmployee(queryEmployeeRequest);

            if (!result.hasSucceeded() || result.getData() == null) {
                throw new IoTxException(HaasIoTxCodes.ERROR_ODM_USER_NOT_EXIST);
            }

            EmployeeDTO mainEmployee = result.getData();
            String tenantId = mainEmployee.getTenantId();
            String aliyunPk = mainEmployee.getMainPk();

            OdmInfoDTO odmInfoDTO = new OdmInfoDTO();
            odmInfoDTO.setContact(contactName);
            odmInfoDTO.setName(companyName);
            odmInfoDTO.setContactPost(contactPost);
            odmInfoDTO.setMobile(mobile);
            odmInfoDTO.setEmail(email);
            odmInfoDTO.setAliyunPk(aliyunPk);

            odmUserService.odmSign(tenantId, odmInfoDTO);

            return new IoTxResult<>();
        } catch (Exception e) {
            errLog.error(e.getMessage(), e);
            return new IoTxResult<>(HaasIoTxCodes.ERROR_ODM_USER_SIGN_FAILED);
        }
    }
}

