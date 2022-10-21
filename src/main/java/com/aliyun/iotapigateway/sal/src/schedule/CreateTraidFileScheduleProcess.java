package com.aliyun.iotx.haas.tdserver.sal.schedulerx;

import com.alibaba.schedulerx.worker.domain.JobContext;
import com.alibaba.schedulerx.worker.processor.JavaProcessor;
import com.alibaba.schedulerx.worker.processor.ProcessResult;
import com.aliyun.iotx.common.base.service.IoTxResult;
import com.aliyun.iotx.haas.tdserver.common.constants.LogConstants;
import com.aliyun.iotx.haas.tdserver.facade.odm.OdmDeviceManageService;
import com.aliyun.iotx.haas.tdserver.sal.databoard.DataBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author imost.lwf
 * @date 2021/05/31
 */
@Component
public class CreateTriadFileScheduleProcessor extends JavaProcessor {

    private static final Logger scheduleLog = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_SCHEDULE);

    private static final Logger errorLog = LoggerFactory.getLogger(LogConstants.LOGGER_NAME_ERROR);

    @Resource
    OdmDeviceManageService odmDeviceManageService;

    @Resource
    DataBoard dataBoard;

    private long lastStatisticTimestamp;

    private static final long STATISTICS_INTERVAL = 18 * 3600 * 1000L;

    @Override
    public ProcessResult process(JobContext jobContext) throws Exception {
        scheduleLog.info("CreateTriadFileScheduleProcessor start");

        try {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            date = calendar.getTime();

            IoTxResult<String> result = odmDeviceManageService.updateBatchTaskStatus();
            if (!result.hasSucceeded()) {
                errorLog.error("CreateTriadFileScheduleProcessor updateBatchTaskStatus failed, date:{}, msg:{}", date, result.getLocalizedMsg());
            }
            //每天凌晨计算在线设备数
            scheduleLog.info("do statistics start");
            SimpleDateFormat df = new SimpleDateFormat("HH");
            String hourStr = df.format(new Date());
            int hour = Integer.parseInt(hourStr);
            if(hour >= 1 && hour <= 3 && System.currentTimeMillis() - lastStatisticTimestamp > STATISTICS_INTERVAL){
                dataBoard.statistics();
                lastStatisticTimestamp = System.currentTimeMillis();
            }
            scheduleLog.info("do statistics finish");
        } catch (Exception e) {
            errorLog.error(e.getMessage(), e);
        }

        scheduleLog.info("AdsDailyBillScheduleProcessor finish");
        return new ProcessResult(true);
    }
}

