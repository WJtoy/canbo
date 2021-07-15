package com.kkl.kklplus.b2b.canbo.mq.config;

import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class B2BCenterOrderRetryMQConfig{


    @Bean
    public Queue b2bCenterOrderRetryQueue() {
        return new Queue(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER_RETRY, true);
    }

    @Bean
    DirectExchange b2bCenterOrderRetryExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER_RETRY).
                delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingB2BCenterOrderRetryExchangeMessage(Queue b2bCenterOrderRetryQueue, DirectExchange b2bCenterOrderRetryExchange) {
        return BindingBuilder.bind(b2bCenterOrderRetryQueue).
                to(b2bCenterOrderRetryExchange).
                with(B2BMQConstant.MQ_B2BCENTER_RECEIVE_NEW_B2BORDER_RETRY);
    }

}
