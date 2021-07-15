package com.kkl.kklplus.b2b.canbo.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 *工单物流到货队列
 * @author chenxj
 * @date 2020/05/29
 */
@Configuration
public class B2BCenterOrderExpressArrivalMQConfig {

    @Bean
    public Queue b2bCenterOrderExpressArrivalQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_ORDER_EXPRESS_ARRIVAL, true);
    }

    @Bean
    DirectExchange b2bCenterOrderExpressArrivalExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2BCENTER_ORDER_EXPRESS_ARRIVAL);
    }

    @Bean
    Binding bindingB2bCenterOrderExpressArrivalMessage(Queue b2bCenterOrderExpressArrivalQueue, DirectExchange b2bCenterOrderExpressArrivalExchange) {
        return BindingBuilder.bind(b2bCenterOrderExpressArrivalQueue)
                .to(b2bCenterOrderExpressArrivalExchange)
                .with(B2BMQConstant.MQ_B2BCENTER_ORDER_EXPRESS_ARRIVAL);
    }

}
