package com.kkl.kklplus.b2b.canbo.controller;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.canbo.http.command.OperationCommand;
import com.kkl.kklplus.b2b.canbo.http.request.ToAssignEngineerRequestParam;
import com.kkl.kklplus.b2b.canbo.http.response.ResponseBody;
import com.kkl.kklplus.b2b.canbo.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.canbo.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.canbo.service.CanboOrderPlannedService;
import com.kkl.kklplus.b2b.canbo.service.SysLogService;
import com.kkl.kklplus.b2b.canbo.utils.CanboUtils;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderPlanned;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/orderPlanned")
public class CanboOrderPlannedController {

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private CanboOrderPlannedService canboOrderPlannedService;

    @PostMapping("/planned")
    public MSResponse orderPlanned(@RequestBody CanboOrderPlanned canboOrderPlanned){
        MSResponse msResponse = new MSResponse();
        if(canboOrderPlanned == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        Integer dataSource = canboOrderPlanned.getDataSource();
        if(dataSource == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("数据源不能为空！");
            return msResponse;
        }
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.TOASSIGNENGINEER.apiUrl);
        b2BProcesslog.setDataSource(dataSource);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(canboOrderPlanned.getCreateById());
        b2BProcesslog.setUpdateById(canboOrderPlanned.getUpdateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDt()));
        ToAssignEngineerRequestParam reqBody = new ToAssignEngineerRequestParam();
        reqBody.setOrderNo(canboOrderPlanned.getOrderNo());
        reqBody.setEngineerMobile(canboOrderPlanned.getEngineerMobile());
        reqBody.setEngineerName(canboOrderPlanned.getEngineerName());
        String infoJson = new Gson().toJson(reqBody);
        b2BProcesslog.setInfoJson(infoJson);
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.TOASSIGNENGINEER, reqBody);
        //ResponseBody<ResponseBody> resBody = OkHttpUtils.postSyncGeneric(command, ResponseBody.class);
        ResponseBody<ResponseBody> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseBody.class,dataSource);
        try {
            b2BProcesslogService.insert(b2BProcesslog);
            canboOrderPlannedService.insert(canboOrderPlanned);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),
                            CanboUtils.cutOutErrorMessage(resBody.getErrorMsg())));
                }
                msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),
                        CanboUtils.cutOutErrorMessage(resBody.getErrorMsg())));
                canboOrderPlanned.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                canboOrderPlanned.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()));
                canboOrderPlannedService.updateProcessFlag(canboOrderPlanned);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()));
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                canboOrderPlanned.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                canboOrderPlannedService.updateProcessFlag(canboOrderPlanned);
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }
        }catch (Exception e) {
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(CanboUtils.cutOutErrorMessage(e.getMessage()));
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(e.getMessage()));
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = B2BDataSourceEnum.get(dataSource).name+"工单派单失败 ";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(dataSource,1L,infoJson,errorStr + e.getMessage(),
                    errorStr,OperationCommand.OperationCode.TOASSIGNENGINEER.apiUrl, "POST");
            return msResponse;
        }
    }

}
