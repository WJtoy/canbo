package com.kkl.kklplus.b2b.canbo.controller;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.canbo.entity.CanboOrderInfo;
import com.kkl.kklplus.b2b.canbo.http.config.B2BFeiyuProperties;
import com.kkl.kklplus.b2b.canbo.http.response.GetOrderResponseData;
import com.kkl.kklplus.b2b.canbo.mq.sender.B2BOrderMQSender;
import com.kkl.kklplus.b2b.canbo.mq.sender.B2BWorkcardQtyDailyMQSend;
import com.kkl.kklplus.b2b.canbo.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.canbo.service.OrderInfoService;
import com.kkl.kklplus.b2b.canbo.service.SysLogService;
import com.kkl.kklplus.b2b.canbo.utils.CanboUtils;
import com.kkl.kklplus.b2b.canbo.utils.QuarterUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2b.order.B2BWorkcardQtyDaily;
import com.kkl.kklplus.entity.b2b.pb.MQB2BWorkcardQtyDailyMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderMessage;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
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
@RequestMapping("/feiyu")
public class FeiyuController {
    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private B2BFeiyuProperties b2BFeiyuProperties;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private B2BWorkcardQtyDailyMQSend b2BWorkcardQtyDailyMQSend;

    @Autowired
    private B2BOrderMQSender b2BOrderMQSender;

    @PostMapping("/pushOrder")
    public MSResponse pushOrder(@RequestBody B2BOrder b2BOrder){
        b2BOrder.setDataSource(b2BFeiyuProperties.getDataSource());
        b2BOrder.setStatus(1);
        MSResponse msResponse = orderInfoService.validationData(b2BOrder);
        if(msResponse.getCode() != MSErrorCode.CODE_VALUE_SUCCESS){
            return msResponse;
        }
        MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage bWorkcardQtyDailyMessage =
                MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage.newBuilder()
                        .setUniqueId(System.currentTimeMillis()+"")
                        .setDataSource(b2BOrder.getDataSource())
                        .setObtainMethod(B2BWorkcardQtyDaily.ObtainMethod.PUSH.value)
                        .setStatisticType(B2BWorkcardQtyDaily.StatisticType.IN_B2B_MULTI.value)
                        .setIncreasedQty(1)
                        .setUpdateDate(System.currentTimeMillis())
                        .setUpateById(B2BWorkcardQtyDaily.B2BWorkcardUpdateBy.B2BCANBO.id)
                        .build();
        b2BWorkcardQtyDailyMQSend.send(bWorkcardQtyDailyMessage);
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setDataSource(b2BOrder.getDataSource());
        b2BProcesslog.setInterfaceName("pushOrder");
        b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
        b2BProcesslog.setProcessTime(0);
        b2BProcesslog.setCreateById(1L);
        b2BProcesslog.setUpdateById(1L);
        b2BProcesslog.preInsert();
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDate()));
        b2BProcesslog.setInfoJson(new Gson().toJson(b2BOrder));
        b2BProcesslogService.insert(b2BProcesslog);
        Integer orderCount = orderInfoService.findOrderInfo(b2BOrder.getOrderNo(),b2BOrder.getDataSource());
        if(orderCount > 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg(b2BOrder.getOrderNo()+"订单已经存在！不能重复下单！");
            b2BProcesslog.setProcessComment(msResponse.getMsg());
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            return msResponse;
        }
        MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage b2BWorkcardQtyDailyMessage=
                MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage.newBuilder()
                        .setUniqueId(System.currentTimeMillis()+"")
                        .setDataSource(b2BOrder.getDataSource())
                        .setObtainMethod(B2BWorkcardQtyDaily.ObtainMethod.PUSH.value)
                        .setStatisticType(B2BWorkcardQtyDaily.StatisticType.IN_B2B.value)
                        .setIncreasedQty(1)
                        .setUpdateDate(System.currentTimeMillis())
                        .setUpateById(B2BWorkcardQtyDaily.B2BWorkcardUpdateBy.B2BCANBO.id)
                        .build();
        b2BWorkcardQtyDailyMQSend.send(b2BWorkcardQtyDailyMessage);
        CanboOrderInfo newOrderInfo = new CanboOrderInfo();
        newOrderInfo.setDataSource(b2BOrder.getDataSource());
        newOrderInfo.setShopId(b2BOrder.getShopId());
        newOrderInfo.setBuyShop(b2BOrder.getShopId());
        newOrderInfo.setOrderNo(b2BOrder.getOrderNo());
        newOrderInfo.setUserName(b2BOrder.getUserName());
        newOrderInfo.setUserMobile(b2BOrder.getUserMobile());
        String userPhone = b2BOrder.getUserPhone();
        if(StringUtils.isNotBlank(userPhone)){
            newOrderInfo.setUserPhone(userPhone);
        }
        newOrderInfo.setUserProvince(b2BOrder.getUserProvince());
        newOrderInfo.setUserCity(b2BOrder.getUserCity());
        newOrderInfo.setUserCounty(b2BOrder.getUserCounty());
        newOrderInfo.setUserAddress(b2BOrder.getUserAddress());
        newOrderInfo.setServiceTypeName(b2BOrder.getServiceType());
        newOrderInfo.setInOrOut(b2BOrder.getWarrantyType());
        newOrderInfo.setBrandName(b2BOrder.getBrand());
        newOrderInfo.setRemark(
                org.apache.commons.lang.StringUtils.left(b2BOrder.getDescription(), 200));
        newOrderInfo.setStatus(b2BOrder.getStatus());
        newOrderInfo.setIsSueBy(
                org.apache.commons.lang.StringUtils.left(b2BOrder.getIssueBy(), 20));
        List<GetOrderResponseData.CanboOrder.Product> products = new ArrayList<>();
        for(B2BOrder.B2BOrderItem item : b2BOrder.getItems()){
            GetOrderResponseData.CanboOrder.Product product = new GetOrderResponseData.CanboOrder.Product();
            product.setItemCode(item.getProductCode());
            product.setItemName(item.getProductName());
            product.setItemShortName(item.getProductSpec());
            product.setQty(item.getQty());
            products.add(product);
        }
        newOrderInfo.setItems(new Gson().toJson(products));
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
            MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage b2BWorkcardQtyDailyMessage2=
                    MQB2BWorkcardQtyDailyMessage.B2BWorkcardQtyDailyMessage.newBuilder()
                            .setUniqueId(System.currentTimeMillis()+"")
                            .setDataSource(newOrderInfo.getDataSource())
                            .setObtainMethod(B2BWorkcardQtyDaily.ObtainMethod.PUSH.value)
                            .setStatisticType(B2BWorkcardQtyDaily.StatisticType.IN_B2BDB.value)
                            .setIncreasedQty(1)
                            .setUpdateDate(System.currentTimeMillis())
                            .setUpateById(B2BWorkcardQtyDaily.B2BWorkcardUpdateBy.B2BCANBO.id)
                            .build();
            b2BWorkcardQtyDailyMQSend.send(b2BWorkcardQtyDailyMessage2);
            String issueBy = newOrderInfo.getIsSueBy()!=null?newOrderInfo.getIsSueBy():"";
            String remark = newOrderInfo.getRemark()!=null?newOrderInfo.getRemark():"";
            MQB2BOrderMessage.B2BOrderMessage.Builder builder = MQB2BOrderMessage.B2BOrderMessage.newBuilder()
                    .setId(newOrderInfo.getId())
                    .setDataSource(newOrderInfo.getDataSource())
                    .setOrderNo(newOrderInfo.getOrderNo())
                    .setShopId(newOrderInfo.getShopId())
                    .setUserName(newOrderInfo.getUserName())
                    .setUserMobile(newOrderInfo.getUserMobile())
                    .setUserAddress(newOrderInfo.getUserProvince() + " " + newOrderInfo.getUserCity()
                            + " " + newOrderInfo.getUserCounty()+ " " + newOrderInfo.getUserAddress())
                    .setServiceType(newOrderInfo.getServiceTypeName())
                    .setWarrantyType(newOrderInfo.getInOrOut())
                    .setStatus(newOrderInfo.getStatus())
                    .setIssueBy(issueBy)
                    .setDescription(StringUtils.left(issueBy+"，"+remark, 200))
                    .setRemarks(StringUtils.left(remark, 200))
                    .setQuarter(newOrderInfo.getQuarter());
            for(B2BOrder.B2BOrderItem product : b2BOrder.getItems()){
                MQB2BOrderMessage.B2BOrderItem b2BOrderItem = MQB2BOrderMessage.B2BOrderItem.newBuilder()
                        .setProductCode(product.getProductCode())
                        .setProductName(product.getProductName()!=null?product.getProductName():"")
                        .setProductSpec(product.getProductSpec()!=null?product.getProductSpec():"")
                        .setWarrantyType(newOrderInfo.getInOrOut())
                        .setServiceType(newOrderInfo.getServiceTypeName())
                        .setQty(product.getQty())
                        .build();
                builder.addB2BOrderItem(b2BOrderItem);
            }
            MQB2BOrderMessage.B2BOrderMessage b2BOrderMessage = builder.build();
            //调用转单队列
            b2BOrderMQSender.send(b2BOrderMessage);
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
        }catch (Exception e){
            b2BProcesslog.setProcessComment(CanboUtils.cutOutErrorMessage(e.getMessage()));
            b2BProcesslog.preUpdate();
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);
            sysLogService.insert(b2BOrder.getDataSource(),1L,new Gson().toJson(b2BOrder),
                    "工单添加失败：" + e.getMessage(),
                    "工单添加失败",CanboUtils.UPDATETRANSFERRESULT, CanboUtils.REQUESTMETHOD);
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("订单操作异常！添加失败！");
            return msResponse;
        }
        return msResponse;
    }
}
