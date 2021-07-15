package com.kkl.kklplus.b2b.canbo.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.kkl.kklplus.b2b.canbo.entity.CanboOrderInfo;
import com.kkl.kklplus.b2b.canbo.entity.OrderMessage;
import com.kkl.kklplus.b2b.canbo.http.response.GetOrderChangeResponseData;
import com.kkl.kklplus.b2b.canbo.mapper.OrderInfoMapper;
import com.kkl.kklplus.b2b.canbo.mq.sender.B2BWorkcardQtyDailyMQSend;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderInfoService {

    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private B2BWorkcardQtyDailyMQSend b2BWorkcardQtyDailyMQSend;
    /**
     * 获取某一页的康宝工单信息
     * @param workcardSearchModel
     * @return
     */
    public Page<CanboOrderInfo> getList(B2BOrderSearchModel workcardSearchModel) {
        if (workcardSearchModel.getPage() != null) {
            PageHelper.startPage(workcardSearchModel.getPage().getPageNo(), workcardSearchModel.getPage().getPageSize());
            return orderInfoMapper.getList(workcardSearchModel);
        }else {
            return null;
        }
    }

    public List<CanboOrderInfo> findOrdersProcessFlag(List<B2BOrderTransferResult> orderTransferResults) {
        List<Long> ids = new ArrayList<>();
        for(B2BOrderTransferResult orderTransferResult : orderTransferResults){
            Long id = orderTransferResult.getB2bOrderId();
            //只要一个id无值，就根据b2b工单号去查询
            if(id == null || id <= 0){
                //根据数据源对数据分组
                Map<Integer,List<B2BOrderTransferResult>> orders =
                        orderTransferResults.stream().collect(Collectors.groupingBy(B2BOrderTransferResult::getDataSource));
                return orderInfoMapper.findOrdersProcessFlag(orders);
            }else{
                ids.add(id);
            }
        }
        return orderInfoMapper.findOrdersProcessFlagByIds(ids);
    }

    @Transactional()
    public void updateTransferResult(List<CanboOrderInfo> wis) {
        for(CanboOrderInfo orderInfo:wis){
            orderInfoMapper.updateTransferResult(orderInfo);
        }
    }

    /**
     * 添加数据同时返回需要回传的工单信息
     */
    //@Transactional()


    @Transactional()
    public void insert(CanboOrderInfo canboOrderInfo){
        orderInfoMapper.insert(canboOrderInfo);
    }

    /**
     * 判断订单号是否存在
     * @param orderNo   订单号
     * @param dataSource    数据源
     * @return 1:存在  0:不存在
     */
    public Integer findOrderInfo(String orderNo,Integer dataSource) {
        Long id = orderInfoMapper.findOrderByOrderNoAndDataSource(orderNo,dataSource);
        if(id == null){
            return 0;
        }else{
            return 1;
        }
    }

    public MSResponse validationData(B2BOrder b2BOrder) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(b2BOrder == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("工单资料不能为空！");
            return msResponse;
        }
        String orderNo = b2BOrder.getOrderNo();
        if(StringUtils.isEmpty(orderNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("工单编号orderNo不能为空！");
            return msResponse;
        }
        String shopId = b2BOrder.getShopId();
        if(StringUtils.isEmpty(shopId)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("商铺shopId不能为空！");
            return msResponse;
        }
        String userName = b2BOrder.getUserName();
        if(StringUtils.isEmpty(userName)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("买家姓名userName不能为空！");
            return msResponse;
        }
        String userMobile = b2BOrder.getUserMobile();
        if(StringUtils.isEmpty(userMobile)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("买家电话userMobile不能为空！");
            return msResponse;
        }
        String userProvince = b2BOrder.getUserProvince();
        if(StringUtils.isEmpty(userProvince)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("地址省份userProvince不能为空！");
            return msResponse;
        }
        String userCity = b2BOrder.getUserCity();
        if(StringUtils.isEmpty(userCity)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("地址市userCity不能为空！");
            return msResponse;
        }
        String userCounty = b2BOrder.getUserCounty();
        if(StringUtils.isEmpty(userCounty)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("地址区userCounty不能为空！");
            return msResponse;
        }
        String userAddress = b2BOrder.getUserAddress();
        if(StringUtils.isEmpty(userAddress)){
            msResponse.setMsg("详细地址userAddress不能为空！");
            return msResponse;
        }
        String serviceType = b2BOrder.getServiceType();
        if(StringUtils.isEmpty(serviceType)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("服务类型serviceType不能为空！");
            return msResponse;
        }
        String warrantyType = b2BOrder.getWarrantyType();
        if(StringUtils.isEmpty(warrantyType)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("质保类型warrantyType不能为空！");
            return msResponse;
        }
        List<B2BOrder.B2BOrderItem> items = b2BOrder.getItems();
        if(items == null || items.size() <= 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("产品集合items不能为空！");
        }
        boolean itemFlag = false;
        for(B2BOrder.B2BOrderItem item : items){
            String itemCode = item.getProductCode();
            String itemName = item.getProductName();
            Integer qty = item.getQty();
            if(StringUtils.isEmpty(itemCode)
                    || StringUtils.isEmpty(itemName)||
                    qty == null || qty <=0){
                itemFlag = true;
            }
        }
        if(itemFlag){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("产品数据错误！");
        }
        return msResponse;
    }

    public void cancelledOrder(B2BOrderTransferResult workcardTransferResults) {
        orderInfoMapper.cancelledOrder(workcardTransferResults);
    }

    public String findOrderNoById(Long id){
        return orderInfoMapper.findOrderNoById(id);
    }

    public Map<String, OrderMessage> findOrderInfoByOrderNos
            (List<GetOrderChangeResponseData.CanboOrderChange> orderChanges, int dataSource) {
        List<String> orderNos = orderChanges.stream().map(GetOrderChangeResponseData.CanboOrderChange::getOrderNo)
                .distinct().filter(x -> StringUtils.isNotBlank(x)).collect(Collectors.toList());
        return orderInfoMapper.findOrderByOrderNos(orderNos,dataSource);
    }

    /**
     * 取消工单（B2B取消）
     * @param remark
     * @param id
     * @param processFlag
     * @param updateDt
     */
    public void cancelOrderFormB2B(String remark, Long id, int processFlag,Long updateDt) {
        orderInfoMapper.cancelOrderFormB2B(remark,id,processFlag,updateDt);
    }
}
