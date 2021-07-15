package com.kkl.kklplus.b2b.canbo.mq.config;

import com.kkl.kklplus.b2b.canbo.config.B2BExceptionProperties;
import com.kkl.kklplus.entity.b2b.mq.B2BMQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@EnableConfigurationProperties({B2BExceptionProperties.class})
@EnableRabbit
@Configuration
public class B2BExceptionCollectMQConfig {

    @Bean
    public Queue exceptionCollectQueue() {
        return new Queue(B2BMQConstant.MQ_B2B_EXCEPTION_COLLECT, true);
    }

    @Bean
    DirectExchange exceptionCollectExchange() {
        return new DirectExchange(B2BMQConstant.MQ_B2B_EXCEPTION_COLLECT);
    }

    @Bean
    Binding bindingB2BWorkcardQtyDailyExchangeMessage(Queue exceptionCollectQueue, DirectExchange exceptionCollectExchange) {
        return BindingBuilder.bind(exceptionCollectQueue).to(exceptionCollectExchange).
                with(B2BMQConstant.MQ_B2B_EXCEPTION_COLLECT);
    }
}
