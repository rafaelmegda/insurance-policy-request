package com.company.insurance_request.infrastructure.configuration.messaging;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderMessageConfig {

    @Bean(name = "orderExchange")
    public TopicExchange requestExchange(
            @Value("${messaging.exchange.order}") String exchangeName) {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Queue orderStatusQueue(
            @Value("${messaging.queue.order.status}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding orderStatusBinding(Queue orderStatusQueue,
                                      @Value("${messaging.routing.order.status}") String routing,
                                      @Qualifier("orderExchange") TopicExchange orderExchange) {
        return BindingBuilder.bind(orderStatusQueue)
                .to(orderExchange)
                .with(routing);
    }
}
