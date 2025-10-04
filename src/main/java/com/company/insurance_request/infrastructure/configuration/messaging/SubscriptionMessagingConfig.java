package com.company.insurance_request.infrastructure.configuration.messaging;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SubscriptionMessagingConfig {

    @Bean(name = "subscriptionExchange")
    public TopicExchange subscriptionExchange(@Value("${messaging.exchange.subscription}") String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    public Queue subscriptionQueue(@Value("${messaging.queue.subscription}") String q) {
        return QueueBuilder.durable(q).build();
    }

    @Bean
    public Binding subscriptionBinding(Queue subscriptionQueue,
                                       @Qualifier("subscriptionExchange") TopicExchange exchange,
                                       @Value("${messaging.routing.subscription}") String routing) {
        return BindingBuilder.bind(subscriptionQueue)
                .to(exchange)
                .with(routing);
    }
}
