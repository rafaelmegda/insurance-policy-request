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

    private static final String ROUTING_KEY_INTERNAL = "insurance.order.status.";
    private static final String ROUTING_KEY_EXTERNAL = "insurance.order.external.status.";

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange orderExchange;

    public OrderTopicPublisher(RabbitTemplate rabbitTemplate,
                               @Qualifier("orderExchange") TopicExchange orderExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderExchange = orderExchange;
    }

    @Override
    public void publishReceived(OrderTopicEvent event, String routingKey) throws JsonProcessingException {
        String resolvedRoutingKey = ROUTING_KEY_INTERNAL + routingKey;
        try{
            rabbitTemplate.convertAndSend(orderExchange.getName(), resolvedRoutingKey, event);
            log.info("Published to exchange={} router_key={} policy_id={} status={} payload={}",
                    orderExchange.getName(), resolvedRoutingKey, event.policyId(), event.status(), event);
        }catch (Exception e){
            log.error("Publish error exchange={} rk={} policy_id={} status={} error={}",
                    orderExchange.getName(), resolvedRoutingKey, event.policyId(), event.status(), e.getMessage());
            throw e;
        }
    }

    public void publishFinishStatus(OrderTopicEvent event, String routingKey) {
        String resolvedRoutingKey = ROUTING_KEY_EXTERNAL + routingKey;
        try {
            rabbitTemplate.convertAndSend(orderExchange.getName(), resolvedRoutingKey, event);
            log.info("Published (external) to exchange={} rk={} customer_id={} status={} payload={}",
                    orderExchange.getName(), resolvedRoutingKey, event.customerId(), event.status(), event);
        } catch (Exception e) {
            log.error("Publish (external) error exchange={} rk={} customer_id={} status={} error={}",
                    orderExchange.getName(), resolvedRoutingKey, event.customerId(), event.status(), e.getMessage());
            throw e;
        }
    }
}
