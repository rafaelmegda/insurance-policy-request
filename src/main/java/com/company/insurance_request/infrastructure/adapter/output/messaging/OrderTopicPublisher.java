package com.company.insurance_request.infrastructure.adapter.output.messaging;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTopicPublisher implements OrderTopicPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange orderExchange;

    public OrderTopicPublisher(RabbitTemplate rabbitTemplate,
                               @Qualifier("orderExchange") TopicExchange orderExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderExchange = orderExchange;
    }

    @Override
    public void publish(OrderTopicEvent event, String routingKey) throws JsonProcessingException {
        try{
            rabbitTemplate.convertAndSend(orderExchange.getName(), routingKey, event);
            log.info("Message published status: {} in order topic to customer_id : {} with routingKey: {} - Message: {}", event.status(), event.customerId(), routingKey, event);
        }catch (Exception e){
            log.error("Error publish message order topic to customer_id : {} - {}", event.customerId(), e.getMessage());
            throw e;
        }
    }
}
