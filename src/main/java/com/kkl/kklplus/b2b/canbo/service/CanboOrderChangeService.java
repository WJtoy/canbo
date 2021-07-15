package com.kkl.kklplus.b2b.canbo.service;

import com.kkl.kklplus.b2b.canbo.entity.OrderChangeTypeEnum;
import com.kkl.kklplus.b2b.canbo.entity.OrderMessage;
import com.kkl.kklplus.b2b.canbo.http.response.GetOrderChangeResponseData;
import com.kkl.kklplus.b2b.canbo.mq.sender.B2BCenterOrderExpressArrivalMQSender;
import com.kkl.kklplus.b2b.canbo.mq.sender.B2BCenterOrderProcessMQSend;
import com.kkl.kklplus.entity.b2b.common.TwoTuple;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BCenterOrderExpressArrivalMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 *工单变更通知
 * @author chenxj
 * @date 2020/06/11
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CanboOrderChangeService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private B2BCenterOrderProcessMQSend orderProcessMQSend;

    @Autowired
    private B2BCenterOrderExpressArrivalMQSender expressArrivalMQSender;

    public void processChangeMsg(List<GetOrderChangeResponseData.CanboOrderChange> orderChanges,
                                 B2BDataSourceEnum dataSource) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, OrderMessage> orderMap =  orderInfoService.findOrderInfoByOrderNos(orderChanges,dataSource.id);
        for(GetOrderChangeResponseData.CanboOrderChange orderChange : orderChanges){
            String orderNo = orderChange.getOrderNo();
            String changeType = orderChange.getChangeType();
            OrderMessage orderMessage = orderMap.get(orderNo);
            if (orderMessage != null) {
                Long orderId = orderMessage.getOrderId();
                Long id = orderMessage.getId();
                if (orderId != null && orderId > 0) {
                    if(OrderChangeTypeEnum.DELIEVERED.name.equals(changeType)) {
                        Long arrivalTime;
                        try {
                            Date arrivalDate = sdf.parse(orderChange.getChangeDate());
                            arrivalTime = arrivalDate.getTime();
                        } catch (ParseException e) {
                            log.error("操作时间格式化异常:{}={}",orderNo,orderChange.getChangeDate());
                            continue;
                        }
                        MQB2BCenterOrderExpressArrivalMessage.B2BCenterOrderExpressArrivalMessage arrivalMessage =
                                MQB2BCenterOrderExpressArrivalMessage.B2BCenterOrderExpressArrivalMessage.newBuilder()
                                        .setMessageId(System.currentTimeMillis())
                                        .setDataSource(dataSource.id)
                                        .setKklOrderId(orderId)
                                        .setArrivalTime(arrivalTime).build();
                        expressArrivalMQSender.send(arrivalMessage);
                    }else if(OrderChangeTypeEnum.CANCELED.name.equals(changeType)) {
                        MQB2BOrderProcessMessage.B2BOrderProcessMessage processMessage =
                                MQB2BOrderProcessMessage.B2BOrderProcessMessage.newBuilder()
                                        .setMessageId(System.currentTimeMillis())
                                        .setB2BOrderNo(orderNo)
                                        .setKklOrderId(orderId)
                                        .setB2BOrderId(id)
                                        .setDataSource(dataSource.id)
                                        .setActionType(B2BOrderActionEnum.CONVERTED_CANCEL.value).build();
                        orderProcessMQSend.send(processMessage);
                    }
                } else {
                    if (OrderChangeTypeEnum.CANCELED.name.equals(changeType)) {
                        orderInfoService.cancelOrderFormB2B
                                ("B2B取消:" + orderChange.getInfo(), id, 5, System.currentTimeMillis());
                    }
                }
            }
        }
    }
}
