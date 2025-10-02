package com.company.insurance_request.infrastructure.configuration.messaging;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderTopicConfig {

    @Bean
    public TopicExchange requestExchange(
            @Value("${messaging.exchange.policy:insurance.policy.exchange}") String exchangeName
    ) {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Queue policieStatusQueue(
            @Value("${messaging.queue.policy-status:insurance.policy.status.q}") String queueName
    ) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding policieStatusBinding(
            Queue policieStatusQueue,
            TopicExchange policieExchange
    ) {
        // Captura qualquer routing key (ex: CREATED, APPROVED, REJECTED)
        return BindingBuilder.bind(policieStatusQueue)
                .to(policieExchange)
                .with("#");
    }
}
