package com.kkl.kklplus.b2b.canbo.controller;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.canbo.http.command.OperationCommand;
import com.kkl.kklplus.b2b.canbo.http.request.CancelOrderRequestParam;
import com.kkl.kklplus.b2b.canbo.http.response.ResponseBody;
import com.kkl.kklplus.b2b.canbo.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.canbo.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.canbo.service.CanboFailedProcessLogService;
import com.kkl.kklplus.b2b.canbo.service.CanboOrderCancelledService;
import com.kkl.kklplus.b2b.canbo.service.SysLogService;
import com.kkl.kklplus.b2b.canbo.utils.CanboUtils;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCancelled;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/orderCancelled")
public class CanboOrderCancelledController {

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private CanboOrderCancelledService canboOrderCancelledService;

    @Autowired
    private CanboFailedProcessLogService canboFailedProcessLogService;

    @PostMapping("/cancelled")
    public MSResponse orderCancelled(@RequestBody CanboOrderCancelled canboOrderCancelled){
        MSResponse msResponse = new MSResponse();
        if(canboOrderCancelled == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        Integer dataSource = canboOrderCancelled.getDataSource();
        if(dataSource == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("数据源不能为空！");
            return msResponse;
        }
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        CancelOrderRequestParam reqBody = new CancelOrderRequestParam();
        reqBody.setOrderNo(canboOrderCancelled.getOrderNo());
        reqBody.setCancelMan(canboOrderCancelled.getCancelMan());
        reqBody.setCancelDate(new Date(canboOrderCancelled.getCancelDt()));
        reqBody.setCancelRemark("退单原因："+canboOrderCancelled.getCancelRemark());
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.CANCELORDER, reqBody);
        ResponseBody<ResponseBody> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseBody.class,dataSource);
        String infoJson = CanboUtils.toGson(reqBody);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.CANCELORDER.apiUrl);
        b2BProcesslog.setDataSource(dataSource);
        b2BProcesslog.setInfoJson(infoJson);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(canboOrderCancelled.getCreateById());
        b2BProcesslog.setUpdateById(canboOrderCancelled.getUpdateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDt()));
        try {
            b2BProcesslogService.insert(b2BProcesslog);
            canboOrderCancelledService.insert(canboOrderCancelled);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()));
                canboFailedProcessLogService.insertOrUpdateFailedLog
                        (b2BProcesslog,canboOrderCancelled.getUniqueId(),canboOrderCancelled.getB2bOrderId());
                if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),
                            CanboUtils.cutOutErrorMessage(resBody.getErrorMsg())));
                }
                msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),
                        CanboUtils.cutOutErrorMessage(resBody.getErrorMsg())));
                canboOrderCancelled.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                canboOrderCancelled.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()));
                canboOrderCancelledService.updateProcessFlag(canboOrderCancelled);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                canboOrderCancelled.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                canboOrderCancelledService.updateProcessFlag(canboOrderCancelled);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }
        }catch (Exception e) {
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(CanboUtils.cutOutErrorMessage(e.getMessage()));
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(e.getMessage()));
            canboFailedProcessLogService.insertOrUpdateFailedLog
                    (b2BProcesslog,canboOrderCancelled.getUniqueId(),canboOrderCancelled.getB2bOrderId());
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = B2BDataSourceEnum.get(dataSource).name+"工单取消失败 ";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(dataSource,1L,infoJson,errorStr + e.getMessage(),
                    errorStr, OperationCommand.OperationCode.CANCELORDER.apiUrl, "POST");
            return msResponse;
        }
    }
}
