package com.company.insurance_request.infrastructure.configuration.messaging;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentMessagingConfig {

    @Bean(name = "paymentExchange")
    public TopicExchange paymentExchange(@Value("${messaging.exchange.payment}") String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    public Queue paymentQueue(@Value("${messaging.queue.payment}") String q) {
        return QueueBuilder.durable(q).build();
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue,
                                  @Qualifier("paymentExchange") TopicExchange exchange,
                                  @Value("${messaging.routing.payment}") String routing) {
        return BindingBuilder.bind(paymentQueue)
                .to(exchange)
                .with(routing);
    }
}
