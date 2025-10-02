package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.port.input.OrderTopicUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTopicListener {

    private final OrderTopicUseCase orderTopicUseCase;

    public OrderTopicListener(OrderTopicUseCase orderTopicUseCase) {
        this.orderTopicUseCase = orderTopicUseCase;
    }

    @RabbitListener(queues = "${messaging.queue.policy-status:insurance.policy.status.q}" )
    public void onMessage(OrderTopicEvent event,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws JsonProcessingException {
        try{
            log.info("Received message with routing key {}: message: {}", routingKey, event);
            orderTopicUseCase.processMessageOrder(event);
        }catch (Exception e){
            log.error("Error listener message to policy_id: {} - status: {} - error: {}", event.policieId(), event.status(), e.getMessage());
            throw e;
        }
    }
}
