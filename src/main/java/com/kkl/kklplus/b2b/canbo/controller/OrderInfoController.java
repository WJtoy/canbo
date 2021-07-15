package com.kkl.kklplus.b2b.canbo.controller;

import com.github.pagehelper.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.canbo.entity.CanboOrderConfirm;
import com.kkl.kklplus.b2b.canbo.entity.CanboOrderInfo;
import com.kkl.kklplus.b2b.canbo.http.config.B2BTooneProperties;
import com.kkl.kklplus.b2b.canbo.http.request.GetOrderInfoChangeRequestParam;
import com.kkl.kklplus.b2b.canbo.http.request.RequestParam;
import com.kkl.kklplus.b2b.canbo.http.response.GetOrderChangeResponseData;
import com.kkl.kklplus.b2b.canbo.mq.sender.B2BOrderMQSender;
import com.kkl.kklplus.b2b.canbo.service.*;
import com.kkl.kklplus.b2b.canbo.utils.CanboUtils;
import com.kkl.kklplus.entity.b2bcenter.md.B2BShopEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderMessage;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.b2b.canbo.http.command.OperationCommand;
import com.kkl.kklplus.b2b.canbo.http.request.GetOrderRequestParam;
import com.kkl.kklplus.b2b.canbo.http.request.GetSendOrderResultRequestParam;
import com.kkl.kklplus.b2b.canbo.http.response.GetOrderResponseData;
import com.kkl.kklplus.b2b.canbo.http.response.ResponseBody;
import com.kkl.kklplus.b2b.canbo.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.canbo.mq.sender.B2BWorkcardQtyDailyMQSend;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.order.B2BWorkcardQtyDaily;
import com.kkl.kklplus.entity.b2b.pb.MQB2BWorkcardQtyDailyMessage;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/orderInfo")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private CanboOrderConfirmService canboOrderConfirmService;

    @Autowired
    private B2BTooneProperties tooneProperties;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private B2BWorkcardQtyDailyMQSend b2BWorkcardQtyDailyMQSend;

    @Autowired
    private B2BOrderMQSender b2BOrderMQSender;

    @Autowired
    private CanboOrderChangeService orderChangeService;

    /**
     * 康宝抓取工单JOB
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void canboOrderJob() {
        B2BTooneProperties.DataSourceConfig dataSourceConfig =
                tooneProperties.getDataSourceConfig().get(B2BDataSourceEnum.CANBO.id);
        if (dataSourceConfig.getScheduleEnabled()) {
            this.getOrderOld(B2BDataSourceEnum.CANBO,B2BShopEnum.CANBO);
        }
    }

    /**
     * 阿诗丹顿抓取工单JOB
     */
    @Scheduled(cron = "0 */3 * * * ?")
    public void usatonOrderJob() {
        B2BTooneProperties.DataSourceConfig dataSourceConfig =
                tooneProperties.getDataSourceConfig().get(B2BDataSourceEnum.USATON.id);
        if (dataSourceConfig.getScheduleEnabled()) {
            this.getOrderOld(B2BDataSourceEnum.USATON,B2BShopEnum.USATON);
        }
    }
    @Scheduled(cron = "0 */10 * * * ?")
    public void canboOrderChangeJob() {
        B2BTooneProperties.DataSourceConfig dataSourceConfig =
                tooneProperties.getDataSourceConfig().get(B2BDataSourceEnum.CANBO.id);
        if (dataSourceConfig.getScheduleEnabledNew()) {
            this.getOrderNew(B2BDataSourceEnum.CANBO,B2BShopEnum.CANBO);
        }
    }
    @Scheduled(cron = "0 */10 * * * ?")
    public void usatonOrderChangeJob() {
        B2BTooneProperties.DataSourceConfig dataSourceConfig =
                tooneProperties.getDataSourceConfig().get(B2BDataSourceEnum.CANBO.id);
        if (dataSourceConfig.getScheduleEnabledNew()) {
            this.getOrderNew(B2BDataSourceEnum.USATON,B2BShopEnum.USATON);
        }
    }

    private void getOrderOld(B2BDataSourceEnum dataSource, B2BShopEnum shopId) {
        GetOrderRequestParam reqBody = new GetOrderRequestParam();
        reqBody.setOrderType(1);
        reqBody.setMaxQty(50);
        reqBody.setDates(30);
        B2BTooneProperties.DataSourceConfig dataSourceConfig =
                tooneProperties.getDataSourceConfig().get(dataSource.id);
        reqBody.setCompanyName(dataSourceConfig.getCompanyName());
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.GETORDER, reqBody);
        publicOrderJob(dataSource,shopId,reqBody,command);
    }
    private void getOrderNew(B2BDataSourceEnum dataSource, B2BShopEnum shopId) {
        long timeMillis = System.currentTimeMillis();
        GetOrderInfoChangeRequestParam reqBody = new GetOrderInfoChangeRequestParam();
        reqBody.setMaxQty("100");
        reqBody.setDates("30");
        reqBody.setBeginDate(new Date(timeMillis - 600000));
        reqBody.setEndDate(new Date(timeMillis));
        B2BTooneProperties.DataSourceConfig dataSourceConfig =
                tooneProperties.getDataSourceConfig().get(dataSource.id);
        reqBody.setCompanyName(dataSourceConfig.getCompanyName());
        OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.GETORDERINFOCHANGE, reqBody);
        B2BOrderProcesslog processlog = new B2BOrderProcesslog();
        processlog.setDataSource(dataSource.id);
        processlog.setInterfaceName(command.getOpCode().apiUrl);
        processlog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        processlog.setProcessTime(0);
        processlog.setCreateById(1L);
        processlog.setUpdateById(1L);
        processlog.setCreateDt(timeMillis);
        processlog.setUpdateDt(timeMillis);
        processlog.setQuarter(QuarterUtils.getQuarter(processlog.getCreateDt()));
        ResponseBody<GetOrderChangeResponseData> resBody =
                OkHttpUtils.postSyncGenericNew(command, GetOrderChangeResponseData.class, dataSource.id);
        try {
            //记录原始数据
            processlog.setInfoJson(CanboUtils.toGson(reqBody));
            b2BProcesslogService.insert(processlog);
            processlog.setResultJson(resBody.getOriginalJson());
            if (resBody.getErrorCode() == ResponseBody.ErrorCode.SUCCESS.code) {
                List<GetOrderChangeResponseData.CanboOrderChange> canboChangeOrders = resBody.getData().getData();
                if(canboChangeOrders.size()>0) {
                    orderChangeService.processChangeMsg(canboChangeOrders,dataSource);
                }
                processlog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                b2BProcesslogService.updateProcessFlag(processlog);
            }else{
                processlog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                if(resBody.getErrorDetailMsg()!=null){
                    processlog.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorDetailMsg()));
                }else {
                    processlog.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()));
                }
                processlog.setResultJson(resBody.getOriginalJson());
                b2BProcesslogService.updateProcessFlag(processlog);
            }
        }catch (Exception e) {
            processlog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            processlog.setProcessComment(CanboUtils.cutOutErrorMessage(e.getMessage()));
            b2BProcesslogService.updateProcessFlag(processlog);
            String errorStr = "抓取"+dataSource.name+"工单变更信息异常 ";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(dataSource.id,1L,new Gson().toJson(reqBody),
                    errorStr + e.getMessage(),
                    errorStr,OperationCommand.OperationCode.GETORDER.apiUrl, "POST");
        }

    }

    private void publicOrderJob(B2BDataSourceEnum dataSource, B2BShopEnum shopId,
                                RequestParam reqBody,OperationCommand command) {
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.preInsert();
        b2BProcesslog.setDataSource(dataSource.id);
        b2BProcesslog.setInterfaceName(command.getOpCode().apiUrl);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDt()));
        ResponseBody<GetOrderResponseData> resBody = OkHttpUtils.postSyncGenericNew(command, GetOrderResponseData.class, dataSource.id);
        try {
            log.info("抓取得数据:"+resBody.getOriginalJson());
            //记录原始数据
            b2BProcesslog.setInfoJson(CanboUtils.toGson(reqBody));
            b2BProcesslogService.insert(b2BProcesslog);
            b2BProcesslog.setResultJson(resBody.getOriginalJson());
            if (resBody.getErrorCode() == ResponseBody.ErrorCode.SUCCESS.code) {
                List<GetOrderResponseData.CanboOrder> canboOrders = resBody.getData().getData();
                if(canboOrders.size()>0) {
                    MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage b2BWorkcardQtyDailyMessage=
                            MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage.newBuilder()
                                    .setUniqueId(System.currentTimeMillis()+"")
                                    .setDataSource(dataSource.id)
                                    .setObtainMethod(B2BWorkcardQtyDaily.ObtainMethod.PUSH.value)
                                    .setStatisticType(B2BWorkcardQtyDaily.StatisticType.IN_B2B_MULTI.value)
                                    .setIncreasedQty(canboOrders.size())
                                    .setUpdateDate(System.currentTimeMillis())
                                    .setUpateById(B2BWorkcardQtyDaily.B2BWorkcardUpdateBy.B2BCANBO.id)
                                    .build();
                    b2BWorkcardQtyDailyMQSend.send(b2BWorkcardQtyDailyMessage);

                    List<CanboOrderConfirm> canboOrderConfirms= this.insertManyOrderInfo(canboOrders,dataSource,shopId);
                    //记录失败的订单
                    StringBuffer failureCcomment = new StringBuffer();
                    //记录成功的订单
                    StringBuffer successCcomment = new StringBuffer();
                    for(CanboOrderConfirm canboOrderConfirm : canboOrderConfirms ){
                        if(canboOrderConfirm.getThirdSendFlag() == 1){
                            failureCcomment.append(canboOrderConfirm.getOrderNo()+" ");
                        }else {
                            successCcomment.append(canboOrderConfirm.getOrderNo()+" ");
                        }
                    }
                    if(failureCcomment.length()>0){
                        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_REJECT.value);
                        b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(dataSource.name+"数据不全→失败的订单有"+failureCcomment.toString()
                                +";成功的订单有"+successCcomment.toString()));
                        b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                    }else{
                        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                        b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                    }
                    //调用接单的接口
                    orderConfirm(canboOrderConfirms);
                }else {
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }
            }else{
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                if(resBody.getErrorDetailMsg()!=null){
                    b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorDetailMsg()));
                }else {
                    b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()));
                }
                b2BProcesslog.setResultJson(resBody.getOriginalJson());
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            }
        }catch (Exception e) {
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(e.getMessage()));
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            String errorStr = "抓取"+dataSource.name+"工单失败 ";
            log.error(errorStr, e.getMessage());
            sysLogService.insert(dataSource.id,1L,new Gson().toJson(reqBody),
                    errorStr + e.getMessage(),
                    errorStr,OperationCommand.OperationCode.GETORDER.apiUrl, "POST");
        }
    }

    @PostMapping("/confirm")
    public MSResponse orderConfirm(@RequestBody List<CanboOrderConfirm> canboOrderConfirms) {
        MSResponse msResponse = new MSResponse();
        msResponse.setErrorCode(MSErrorCode.SUCCESS);
        if(canboOrderConfirms == null || canboOrderConfirms.size() <= 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            return msResponse;
        }
        //根据数据源进行分组
        Map<Integer, List<CanboOrderConfirm>> confirms = canboOrderConfirms.stream().collect(Collectors.groupingBy(CanboOrderConfirm::getDataSource));

        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.GETSENDORDERRESULT.apiUrl);
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        //循环数据源请求不同数据源对应得链接
        for(Integer dataSource : confirms.keySet()){
            b2BProcesslog.setDataSource(dataSource);
            GetSendOrderResultRequestParam reqBody = new GetSendOrderResultRequestParam();
            reqBody.setResult_flag(0);
            List<GetSendOrderResultRequestParam.OrderResult> orderResults = new ArrayList<>();
            for(CanboOrderConfirm canboOrderConfirm : canboOrderConfirms){
                GetSendOrderResultRequestParam.OrderResult orderResult = new GetSendOrderResultRequestParam.OrderResult();
                orderResult.setOrderNo(canboOrderConfirm.getOrderNo());
                orderResult.setThirdSendMessage(canboOrderConfirm.getThirdSendMessage());
                orderResult.setThirdSendFlag(canboOrderConfirm.getThirdSendFlag());
                orderResults.add(orderResult);
            }
            reqBody.setData(orderResults);
            String infoJson = new Gson().toJson(reqBody);
            try {
                OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.GETSENDORDERRESULT, reqBody);
                ResponseBody<ResponseBody> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseBody.class,dataSource);
                b2BProcesslog.setInfoJson(infoJson);
                b2BProcesslogService.insert(b2BProcesslog);
                canboOrderConfirmService.insert(canboOrderConfirms);
                b2BProcesslog.setResultJson(resBody.getOriginalJson());
                if( resBody.getErrorCode() != ResponseBody.ErrorCode.SUCCESS.code){
                    if(resBody.getErrorCode() >= ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE.code){
                        msResponse.setErrorCode(new MSErrorCode(resBody.getErrorCode(),
                                CanboUtils.cutOutErrorMessage(resBody.getErrorMsg())));
                    }
                    msResponse.setThirdPartyErrorCode(new MSErrorCode(resBody.getErrorCode(),
                            CanboUtils.cutOutErrorMessage(resBody.getErrorMsg())));
                    canboOrderConfirmService.updateProcessFlag(canboOrderConfirms,
                            B2BProcessFlag.PROCESS_FLAG_FAILURE.value,
                            CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()),System.currentTimeMillis());
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                    b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(resBody.getErrorMsg()));
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                    return msResponse;
                }else{
                    canboOrderConfirmService.updateProcessFlag(canboOrderConfirms,
                            B2BProcessFlag.PROCESS_FLAG_SUCESS.value,"",System.currentTimeMillis());
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                }
            }catch (Exception e) {
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg(CanboUtils.cutOutErrorMessage(e.getMessage()));
                b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(e.getMessage()));
                b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                String errorStr = B2BDataSourceEnum.get(dataSource).name+"工单接单失败 ";
                log.error(errorStr, e.getMessage());
                sysLogService.insert(dataSource,1L,infoJson,errorStr + e.getMessage(),
                        errorStr,OperationCommand.OperationCode.GETSENDORDERRESULT.apiUrl, "POST");
                return msResponse;
            }
        }
        return msResponse;
    }



    /**
     * 获取康宝工单(分页)
     * @param workcardSearchModel
     * @return
     */
    @PostMapping("/getList")
    public MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel workcardSearchModel) {
        Gson gson = new Gson();
        try {
            Page<CanboOrderInfo> orderInfoPage = orderInfoService.getList(workcardSearchModel);
            Page<B2BOrder> customerPoPage = new Page<>();
            for(CanboOrderInfo orderInfo:orderInfoPage){
                B2BOrder customerPo = new B2BOrder();
                //数据源
                Integer dataSource = orderInfo.getDataSource();
                customerPo.setId(orderInfo.getId());
                customerPo.setB2bOrderId(orderInfo.getId());
                customerPo.setDataSource(dataSource);
                customerPo.setOrderNo(orderInfo.getOrderNo());
                customerPo.setParentBizOrderId(orderInfo.getOrderNo());
                //康宝店铺
                String buyShop = orderInfo.getBuyShop();
                if(StringUtils.isNotBlank(buyShop) && dataSource == B2BDataSourceEnum.USATON.id){
                    customerPo.setShopId(buyShop);
                }else {
                    customerPo.setShopId(orderInfo.getShopId());
                }
                customerPo.setUserName(orderInfo.getUserName());
                customerPo.setUserMobile(orderInfo.getUserMobile());
                customerPo.setUserPhone(orderInfo.getUserPhone());
                customerPo.setUserAddress(orderInfo.getUserAddress());
                customerPo.setBrand(orderInfo.getBrandName());
                customerPo.setServiceType(orderInfo.getServiceTypeName());
                customerPo.setWarrantyType(orderInfo.getInOrOut());
                String issueBy = orderInfo.getIsSueBy()!=null?orderInfo.getIsSueBy():"";
                String remark = orderInfo.getRemark()!=null?orderInfo.getRemark():"";
                remark = remark.replace("null","");
                customerPo.setDescription
                        (StringUtils.left(issueBy+"，"+remark, 200));
                customerPo.setStatus(orderInfo.getStatus());
                customerPo.setIssueBy(orderInfo.getIsSueBy());
                customerPo.setProcessFlag(orderInfo.getProcessFlag());
                customerPo.setProcessTime(orderInfo.getProcessTime());
                customerPo.setProcessComment(orderInfo.getProcessComment());
                customerPo.setQuarter(orderInfo.getQuarter());
                List<CanboOrderInfo.Product> products = gson.fromJson(orderInfo.getItems(),new TypeToken<List<CanboOrderInfo.Product>>() {
                }.getType());
                for(CanboOrderInfo.Product product : products){
                    B2BOrder.B2BOrderItem orderItem = new B2BOrder.B2BOrderItem();
                    orderItem.setProductName(product.getItemName());
                    orderItem.setProductCode(product.getItemCode());
                    orderItem.setProductSpec(product.getItemShortName());
                    orderItem.setClassName(product.getItemClassName());
                    orderItem.setServiceType(orderInfo.getServiceTypeName());
                    orderItem.setWarrantyType(orderInfo.getInOrOut());
                    orderItem.setQty(product.getQty());
                    customerPo.getItems().add(orderItem);
                }
                customerPoPage.add(customerPo);
            }
            MSPage<B2BOrder> returnPage = new MSPage<>();
            returnPage.setPageNo(orderInfoPage.getPageNum());
            returnPage.setPageSize(orderInfoPage.getPageSize());
            returnPage.setPageCount(orderInfoPage.getPages());
            returnPage.setRowCount((int) orderInfoPage.getTotal());
            returnPage.setList(customerPoPage.getResult());
            return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
        } catch (Exception e) {
            log.error("查询工单失败", e.getMessage());
            sysLogService.insert(1L,gson.toJson(workcardSearchModel),
                    "查询工单失败：" + e.getMessage(),
                    "查询工单失败", CanboUtils.ORDERLIST, CanboUtils.REQUESTMETHOD);
            return new MSResponse<>(new MSErrorCode(1000, CanboUtils.cutOutErrorMessage(e.getMessage())));
        }
    }

    /**
     * 检查工单是否可以转换
     * @param orderNos
     * @return
     */
    @PostMapping("/checkWorkcardProcessFlag")
    public MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> orderNos){
        try {
            if(orderNos == null){
                return new MSResponse(new MSErrorCode(1000, "参数错误，工单编号不能为空"));
            }
            //根据数据源对数据进行分组
            //Map<Integer,List<B2BOrderTransferResult>> maps =
            //    orderNos.stream().collect(Collectors.groupingBy(B2BOrderTransferResult::getDataSource));
            //List<CanboOrderInfo> orderInfos = orderInfoService.findWorkcardProcessFlag(workcardIds);
            //查询出对应工单的状态
            List<CanboOrderInfo> orderInfos = orderInfoService.findOrdersProcessFlag(orderNos);
            if(orderInfos == null){
                return new MSResponse(MSErrorCode.FAILURE);
            }
            for (CanboOrderInfo orderInfo : orderInfos) {
                if (orderInfo.getStatus() != null && orderInfo.getStatus() == 10) {
                    return new MSResponse(new MSErrorCode(1000, orderInfo.getOrderNo()+"工单已经取消,无法进行转换"));
                }
                if (orderInfo.getProcessFlag() != null && orderInfo.getProcessFlag() == B2BProcessFlag.PROCESS_FLAG_SUCESS.value) {
                    return new MSResponse(new MSErrorCode(1000, orderInfo.getOrderNo()+"工单已经转换成功,不能重复转换"));
                }
            }
            return new MSResponse(MSErrorCode.SUCCESS);
        }catch (Exception e){
            log.error("检查工单失败", e.getMessage());
            sysLogService.insert(1L,new Gson().toJson(orderNos),"检查工单失败：" + e.getMessage(),
                    "检查工单失败",CanboUtils.CHECKPROCESSFLAG, CanboUtils.REQUESTMETHOD);
            return new MSResponse(new MSErrorCode(1000, CanboUtils.cutOutErrorMessage(e.getMessage())));
        }
    }

    @PostMapping("/updateTransferResult")
    public MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> workcardTransferResults) {
        try {
            //根据数据源对数据分组
            Map<Integer,List<B2BOrderTransferResult>> maps =
                    workcardTransferResults.stream().collect(Collectors.groupingBy(B2BOrderTransferResult::getDataSource));
            //根据数据源分组，在根据订单号分组
            Map<Integer, Map<String, B2BOrderTransferResult>> orderNoMap = workcardTransferResults.stream().collect(
                    Collectors.groupingBy(B2BOrderTransferResult::getDataSource,
                            Collectors.toMap(B2BOrderTransferResult::getB2bOrderNo, Function.identity(),(key1, key2) -> key2))
            );
            //根据数据源分组，在根据B2BOrderID分组
            Map<Integer, Map<Long, B2BOrderTransferResult>> b2bOrderIdMap = workcardTransferResults.stream().collect(
                    Collectors.groupingBy(B2BOrderTransferResult::getDataSource,
                            Collectors.toMap(B2BOrderTransferResult::getB2bOrderId, Function.identity(),(key1, key2) -> key2))
            );
            //查询出需要转换的工单
            List<CanboOrderInfo> orderInfos = orderInfoService.findOrdersProcessFlag(workcardTransferResults);
            //用来存放各个数据源转换成功的数量
            Map<Integer,Integer> count = new HashMap<>();
            //存放需要转换的工单集合
            List<CanboOrderInfo> wis = new ArrayList<>();
            for(CanboOrderInfo canboOrderInfo:orderInfos){
                //如果工单为转换成功的才存放进工单集合
                if(canboOrderInfo.getProcessFlag() != B2BProcessFlag.PROCESS_FLAG_SUCESS.value){
                    Integer dataSource = canboOrderInfo.getDataSource();
                    Map<Long, B2BOrderTransferResult> b = b2bOrderIdMap.get(dataSource);
                    if(b != null ){
                        B2BOrderTransferResult transferResult = b.get(canboOrderInfo.getId());
                        if(transferResult == null){
                            Map<String, B2BOrderTransferResult> orderNoResultMap = orderNoMap.get(dataSource);
                            if(orderNoResultMap != null ){
                                transferResult = orderNoResultMap.get(canboOrderInfo.getOrderNo());
                            }
                        }
                        if(transferResult != null){
                            //成功转换的才计算
                            if(transferResult.getProcessFlag() == B2BProcessFlag.PROCESS_FLAG_SUCESS.value){
                                count.put(dataSource,count.get(dataSource)!=null?count.get(dataSource)+1:1);
                            }
                            canboOrderInfo.setProcessFlag(transferResult.getProcessFlag());
                            canboOrderInfo.setOrderId(transferResult.getOrderId());
                            canboOrderInfo.setUpdateDt(transferResult.getUpdateDt());
                            canboOrderInfo.setProcessComment(transferResult.getProcessComment());
                            wis.add(canboOrderInfo);
                        }
                    }
                }
            }
            orderInfoService.updateTransferResult(wis);
            //成功条数大于0才发送消息队列
            for(Integer dataSource:count.keySet()){
                if(count.get(dataSource) > 0) {
                    MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage b2BWorkcardQtyDailyMessage =
                            MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage.newBuilder()
                                    .setUniqueId(System.currentTimeMillis() + "")
                                    .setDataSource(dataSource)
                                    .setObtainMethod(B2BWorkcardQtyDaily.ObtainMethod.PUSH.value)
                                    .setStatisticType(B2BWorkcardQtyDaily.StatisticType.IN_KKLPLUSWEB.value)
                                    .setIncreasedQty(count.get(dataSource))
                                    .setUpdateDate(System.currentTimeMillis())
                                    .setUpateById(B2BWorkcardQtyDaily.B2BWorkcardUpdateBy.B2BCANBO.id)
                                    .build();
                    b2BWorkcardQtyDailyMQSend.send(b2BWorkcardQtyDailyMessage);
                }
            }
            return new MSResponse(MSErrorCode.SUCCESS);
        } catch (Exception e) {
            log.error("工单转换失败", e.getMessage());
            sysLogService.insert(1L,new Gson().toJson(workcardTransferResults),
                    "工单转换失败：" + e.getMessage(),
                    "工单转换失败",CanboUtils.UPDATETRANSFERRESULT, CanboUtils.REQUESTMETHOD);
            return new MSResponse(new MSErrorCode(1000, CanboUtils.cutOutErrorMessage(e.getMessage())));

        }
    }

    public List<CanboOrderConfirm> insertManyOrderInfo(List<GetOrderResponseData.CanboOrder>
                                                       canboOrders,B2BDataSourceEnum dataSource,B2BShopEnum shop) {
        Gson gson = new Gson();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        List<CanboOrderConfirm> canboOrderConfirms = new ArrayList<>();
        for(GetOrderResponseData.CanboOrder canboOrder : canboOrders) {
            canboOrder.setRemark(CanboUtils.cleanHtmlTag(canboOrder.getRemark()));
            //获取工单数据
            String orderNo = canboOrder.getOrderNo();
            String userName = canboOrder.getUserName();
            String userMobile = canboOrder.getUserMobile();
            String userPhone = StringUtils.left(canboOrder.getUserPhone(),20);
            String userProvince = canboOrder.getUserProvince();
            String userCity = canboOrder.getUserCity();
            String userCounty = canboOrder.getUserCounty();
            String userAddress = canboOrder.getUserAddress();
            String serviceTypeName = canboOrder.getServiceTypeName();
            String inOrOut = canboOrder.getInOrOut();
            String buyShop = canboOrder.getBuyShop();
            if(StringUtils.isBlank(userMobile)){
                userMobile = userPhone;
                canboOrder.setUserMobile(userPhone);
            }
            List<GetOrderResponseData.CanboOrder.Product> items = canboOrder.getItems();
            boolean itemFlag = true;
            for(GetOrderResponseData.CanboOrder.Product item : items){
                String itemCode = item.getItemCode();
                String itemName = item.getItemName();
                Integer qty = item.getQty();
                if(StringUtils.isBlank(itemCode) || StringUtils.isBlank(itemName)||
                        qty == null || qty <=0){
                    itemFlag = false;
                }
            }
            CanboOrderConfirm canboOrderConfirm = new CanboOrderConfirm();
            canboOrderConfirm.setDataSource(dataSource.id);
            canboOrderConfirm.setOrderNo(orderNo);
            //判断所给数据是否符合条件
            if(StringUtils.isNotBlank(orderNo) &&
                    StringUtils.isNotBlank(userName) &&
                    StringUtils.isNotBlank(userMobile) &&
                    StringUtils.isNotBlank(userProvince) &&
                    StringUtils.isNotBlank(userCounty) &&
                    StringUtils.isNotBlank(userCity) &&
                    StringUtils.isNotBlank(serviceTypeName) &&
                    StringUtils.isNotBlank(inOrOut) &&
                    StringUtils.isNotBlank(userAddress)&&
                    items != null && items.size()>0 && itemFlag){
                Integer orderCount = orderInfoService.findOrderInfo(canboOrder.getOrderNo(),dataSource.id);
                // 判断是否已加入康宝工单表，没有就添加数据
                if (orderCount == null || orderCount <= 0){
                    MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage b2BWorkcardQtyDailyMessage=
                            MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage.newBuilder()
                                    .setUniqueId(System.currentTimeMillis()+"")
                                    .setDataSource(dataSource.id)
                                    .setObtainMethod(B2BWorkcardQtyDaily.ObtainMethod.PUSH.value)
                                    .setStatisticType(B2BWorkcardQtyDaily.StatisticType.IN_B2B.value)
                                    .setIncreasedQty(1)
                                    .setUpdateDate(System.currentTimeMillis())
                                    .setUpateById(B2BWorkcardQtyDaily.B2BWorkcardUpdateBy.B2BCANBO.id)
                                    .build();
                    b2BWorkcardQtyDailyMQSend.send(b2BWorkcardQtyDailyMessage);
                    //填充数据
                    CanboOrderInfo newOrderInfo = new CanboOrderInfo();
                    newOrderInfo.setDataSource(dataSource.id);
                    newOrderInfo.setShopId(shop.id);
                    newOrderInfo.setOrderNo(orderNo);
                    newOrderInfo.setUserName(userName);
                    newOrderInfo.setUserMobile(userMobile);
                    if(StringUtils.isNotBlank(userPhone)) {
                        newOrderInfo.setUserPhone(userPhone);
                    }
                    newOrderInfo.setUserProvince(userProvince);
                    newOrderInfo.setUserCity(userCity);
                    newOrderInfo.setUserCounty(userCounty);
                    newOrderInfo.setUserAddress(userAddress);
                    newOrderInfo.setServiceTypeName(serviceTypeName);
                    newOrderInfo.setInOrOut(inOrOut);
                    if(canboOrder.getBuyDate()!=null && canboOrder.getBuyDate().length()>0){
                        try{
                            if(canboOrder.getBuyDate().length()==10){
                                newOrderInfo.setBuyDate(sdf2.parse(canboOrder.getBuyDate()).getTime());
                            }else if(canboOrder.getBuyDate().length()>10){
                                newOrderInfo.setBuyDate(sdf.parse(canboOrder.getBuyDate()).getTime());
                            }
                        }catch (Exception e){
                            log.error("日期格式化失败");
                        }
                    }
                    newOrderInfo.setBuyShop(buyShop);
                    newOrderInfo.setOrderTypeName(canboOrder.getOrderTypeName());
                    newOrderInfo.setBrandName(canboOrder.getBrandName());
                    newOrderInfo.setRemark(
                            StringUtils.left(canboOrder.getRemark(), 500));
                    newOrderInfo.setStatus(1);
                    newOrderInfo.setItems(gson.toJson(items));
                    newOrderInfo.setIsSueBy(
                            StringUtils.left(canboOrder.getIsSueBy(), 20));
                    newOrderInfo.preInsert();
                    newOrderInfo.setCreateById(1L);
                    newOrderInfo.setUpdateById(1L);
                    newOrderInfo.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
                    newOrderInfo.setProcessTime(0);
                    newOrderInfo.setUpdateDt(newOrderInfo.getUpdateDate().getTime());
                    newOrderInfo.setCreateDt(newOrderInfo.getCreateDate().getTime());
                    newOrderInfo.setQuarter(QuarterUtils.getQuarter(newOrderInfo.getCreateDt()));
                    try{
                        //保存数据
                        orderInfoService.insert(newOrderInfo);
                        canboOrderConfirm.setB2bOrderId(newOrderInfo.getId());
                        MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage b2BWorkcardQtyDailyMessage2=
                                MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage.newBuilder()
                                        .setUniqueId(System.currentTimeMillis()+"")
                                        .setDataSource(dataSource.id)
                                        .setObtainMethod(B2BWorkcardQtyDaily.ObtainMethod.PUSH.value)
                                        .setStatisticType(B2BWorkcardQtyDaily.StatisticType.IN_B2BDB.value)
                                        .setIncreasedQty(1)
                                        .setUpdateDate(System.currentTimeMillis())
                                        .setUpateById(B2BWorkcardQtyDaily.B2BWorkcardUpdateBy.B2BCANBO.id)
                                        .build();
                        b2BWorkcardQtyDailyMQSend.send(b2BWorkcardQtyDailyMessage2);
                        String issueBy = newOrderInfo.getIsSueBy()!=null?newOrderInfo.getIsSueBy():"";
                        String remark = newOrderInfo.getRemark()!=null?newOrderInfo.getRemark():"";
                        remark = remark.replace("null","");
                        String brandName = newOrderInfo.getBrandName();
                        String shopId = shop.id;
                        if(StringUtils.isNotBlank(buyShop) && dataSource.id == B2BDataSourceEnum.USATON.id){
                            shopId = buyShop;
                        }
                        MQB2BOrderMessage.B2BOrderMessage.Builder builder = MQB2BOrderMessage.B2BOrderMessage.newBuilder()
                                .setId(newOrderInfo.getId())
                                .setDataSource(dataSource.id)
                                .setOrderNo(newOrderInfo.getOrderNo())
                                .setParentBizOrderId(newOrderInfo.getOrderNo())
                                .setShopId(shopId)
                                .setUserName(newOrderInfo.getUserName())
                                .setUserMobile(newOrderInfo.getUserMobile())
                                .setUserPhone(newOrderInfo.getUserPhone())
                                .setUserAddress(newOrderInfo.getUserProvince() + " " + newOrderInfo.getUserCity()
                                        + " " + newOrderInfo.getUserCounty()+ " " + newOrderInfo.getUserAddress())
                                .setBrand(brandName != null ? brandName : "")
                                .setServiceType(newOrderInfo.getServiceTypeName())
                                .setWarrantyType(newOrderInfo.getInOrOut())
                                .setStatus(newOrderInfo.getStatus())
                                .setIssueBy(issueBy)
                                .setDescription(StringUtils.left(issueBy+"，"+remark, 200))
                                .setRemarks(StringUtils.left(remark, 200))
                                .setQuarter(newOrderInfo.getQuarter());
                        for(GetOrderResponseData.CanboOrder.Product product:items){
                            String itemShortName = product.getItemShortName();
                            MQB2BOrderMessage.B2BOrderItem b2BOrderItem = MQB2BOrderMessage.B2BOrderItem.newBuilder()
                                    .setProductCode(product.getItemCode())
                                    .setProductName(product.getItemName())
                                    .setProductSpec(itemShortName != null ? itemShortName : "")
                                    .setServiceType(newOrderInfo.getServiceTypeName())
                                    .setWarrantyType(newOrderInfo.getInOrOut())
                                    .setQty(product.getQty())
                                    .build();
                            builder.addB2BOrderItem(b2BOrderItem);
                        }
                        MQB2BOrderMessage.B2BOrderMessage b2BOrderMessage = builder.build();
                        //调用转单队列
                        b2BOrderMQSender.send(b2BOrderMessage);
                        //数据符合就回传成功
                        canboOrderConfirm.setThirdSendFlag(2);
                    }catch (Exception e){
                        //数据符合就回传成功
                        canboOrderConfirm.setThirdSendMessage("数据存在异常！");
                        canboOrderConfirm.setThirdSendFlag(1);
                        try {
                            B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
                            b2BProcesslog.setDataSource(dataSource.id);
                            b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.GETORDER.apiUrl);
                            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                            b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(e.getMessage()));
                            b2BProcesslog.setProcessTime(0);
                            b2BProcesslog.setCreateById(1L);
                            b2BProcesslog.setUpdateById(1L);
                            b2BProcesslog.preInsert();
                            b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
                            b2BProcesslog.setInfoJson(new Gson().toJson(canboOrder));
                            b2BProcesslogService.insert(b2BProcesslog);
                        }catch (Exception el){
                            sysLogService.insert(dataSource.id,1L,new Gson().toJson(canboOrder),
                                    dataSource.name+"异常工单log添加失败：" + el.getMessage(),
                                    dataSource.name+"异常工单log添加失败",CanboUtils.UPDATETRANSFERRESULT, CanboUtils.REQUESTMETHOD);
                        }
                    }
                }else{
                    canboOrderConfirm.setThirdSendFlag(2);
                }
            }else{
                //数据不符合就回传失败
                canboOrderConfirm.setThirdSendMessage("数据不全！无法解析！");
                canboOrderConfirm.setThirdSendFlag(1);
            }
            canboOrderConfirms.add(canboOrderConfirm);

        }
        return canboOrderConfirms;
    }

    @PostMapping("/cancelOrderTransition")
    public MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult workcardTransferResults) {
        try {
            orderInfoService.cancelledOrder(workcardTransferResults);
            return new MSResponse(MSErrorCode.SUCCESS);
        }catch (Exception e){
            log.error("取消工单失败", e.getMessage());
            sysLogService.insert(1L,new Gson().toJson(workcardTransferResults),"取消工单失败：" + e.getMessage(),
                    "取消工单失败",CanboUtils.CHECKPROCESSFLAG, CanboUtils.REQUESTMETHOD);
            return new MSResponse(new MSErrorCode(1000, CanboUtils.cutOutErrorMessage(e.getMessage())));
        }
    }
    @PostMapping("/testInsert")
    public List<CanboOrderConfirm> testInsert(@RequestBody List<GetOrderResponseData.CanboOrder> canboOrders){
        return this.insertManyOrderInfo(canboOrders,B2BDataSourceEnum.CANBO,B2BShopEnum.CANBO);
    }
}
