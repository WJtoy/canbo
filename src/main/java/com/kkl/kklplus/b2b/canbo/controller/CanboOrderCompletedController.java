package com.kkl.kklplus.b2b.canbo.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.canbo.http.command.OperationCommand;
import com.kkl.kklplus.b2b.canbo.http.request.OrderFinishRequestParam;
import com.kkl.kklplus.b2b.canbo.http.response.ResponseBody;
import com.kkl.kklplus.b2b.canbo.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.canbo.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.canbo.service.CanboFailedProcessLogService;
import com.kkl.kklplus.b2b.canbo.service.CanboOrderCompletedService;
import com.kkl.kklplus.b2b.canbo.service.SysLogService;
import com.kkl.kklplus.b2b.canbo.utils.CanboUtils;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.canbo.sd.CanboOrderCompleted;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orderCompleted")
public class CanboOrderCompletedController {

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private CanboOrderCompletedService canboOrderCompletedService;

    @Autowired
    private CanboFailedProcessLogService canboFailedProcessLogService;

    @PostMapping("/completed")
    public MSResponse orderCompleted(@RequestBody CanboOrderCompleted canboOrderCompleted){
        MSResponse msResponse = new MSResponse();
        if(canboOrderCompleted == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("信息不能为空！");
            return msResponse;
        }
        Integer dataSource = canboOrderCompleted.getDataSource();
        if(dataSource == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("数据源不能为空！");
            return msResponse;
        }
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        Gson gson = new Gson();
        List<CanboOrderCompleted.CompletedItem> items = canboOrderCompleted.getItems();
        if(items == null || items.size() <= 0){
            if(items == null){
                canboOrderCompleted.setItems(new ArrayList<>());
            }
            CanboOrderCompleted.CompletedItem completedItem = new CanboOrderCompleted.CompletedItem();
            completedItem.setItemCode("123456");
            completedItem.setPic1("");
            completedItem.setPic2("");
            completedItem.setPic3("");
            completedItem.setPic4("");
            completedItem.setBarcode("123456");
            canboOrderCompleted.getItems().add(completedItem);
        }else{
            List<CanboOrderCompleted.CompletedItem> newItems = new ArrayList<>();
            for(CanboOrderCompleted.CompletedItem item : items ){
                if(StringUtils.isEmpty(item.getItemCode())){
                    item.setItemCode("123456");
                }
                if(StringUtils.isEmpty(item.getBarcode())){
                    item.setBarcode("123456");
                }
                newItems.add(item);
            }
            canboOrderCompleted.setItems(newItems);
        }
        canboOrderCompleted.setItemsJson(gson.toJson(canboOrderCompleted.getItems()));
        OrderFinishRequestParam reqBody = new OrderFinishRequestParam();
        reqBody.setOrderNo(canboOrderCompleted.getOrderNo());
        reqBody.setPmCode(canboOrderCompleted.getPmCode());
        reqBody.setReasonCode(canboOrderCompleted.getReasonCode());
        reqBody.setStepCode(canboOrderCompleted.getStepCode());
        reqBody.setItems(gson.fromJson(canboOrderCompleted.getItemsJson(),
                new TypeToken<List<OrderFinishRequestParam.ProductDetail>>(){}.getType()));
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.ORDERFINISH, reqBody);
        ResponseBody<ResponseBody> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseBody.class,dataSource);
        resBody.getData();
        String infoJson = gson.toJson(reqBody);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.ORDERFINISH.apiUrl);
        b2BProcesslog.setDataSource(dataSource);
        b2BProcesslog.setInfoJson(infoJson);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(canboOrderCompleted.getCreateById());
        b2BProcesslog.setUpdateById(canboOrderCompleted.getUpdateById());
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDt()));

        try {
            b2BProcesslogService.insert(b2BProcesslog);
            canboOrderCompletedService.insert(canboOrderCompleted);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()));
                canboFailedProcessLogService.insertOrUpdateFailedLog
                        (b2BProcesslog,canboOrderCompleted.getUniqueId(),canboOrderCompleted.getB2bOrderId());
                if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                    msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),
                            CanboUtils.cutOutErrorMessage(resBody.getErrorMsg())));
                }
                msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),
                        CanboUtils.cutOutErrorMessage(resBody.getErrorMsg())));
                canboOrderCompleted.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                canboOrderCompleted.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()));
                canboOrderCompletedService.updateProcessFlag(canboOrderCompleted);
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                return msResponse;
            }else{
                canboOrderCompleted.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                canboOrderCompletedService.updateProcessFlag(canboOrderCompleted);
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
                    (b2BProcesslog,canboOrderCompleted.getUniqueId(),canboOrderCompleted.getB2bOrderId());
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = B2BDataSourceEnum.get(dataSource).name+"工单完工操作失败 ";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(dataSource,1L,infoJson,errorStr + e.getMessage(),
                    errorStr, OperationCommand.OperationCode.ORDERFINISH.apiUrl, "POST");
            return msResponse;
        }
    }
}
