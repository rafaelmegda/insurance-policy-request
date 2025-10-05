package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.port.input.OrderTopicUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Slf4j
@Component
public class OrderTopicListener {

    private final OrderTopicUseCase orderTopicUseCase;

    public OrderTopicListener(@Qualifier("orderTopicService") OrderTopicUseCase orderTopicUseCase) {
        this.orderTopicUseCase = orderTopicUseCase;
    }

    // A filtragem efetiva ocorre no binding (properties), então o listener só receberá "validado".
    @RabbitListener(
            queues = "${messaging.queue.order.status}"
    )
    public void onMessage(OrderTopicEvent event,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
                          @Header(AmqpHeaders.CONSUMER_QUEUE) String queue
    ) throws JsonProcessingException {
        try{
            log.info("Message received queue={} rk={} status={} policy-id={} payload={}",
                    queue, routingKey, event.status(), event.policyId(), event);
            orderTopicUseCase.processMessageOrder(event);
        }
        catch(NoSuchElementException e) {
            log.warn("Policy not found queue={} rk={} policy-id={} payload={} error={}",
                    queue, routingKey, event.policyId(), event, e.getMessage());
        }
        catch (Exception e){
            log.error("Listener error queue={} rk={} policy_id={} status={} error={}",
                    queue, routingKey, event.policyId(), event.status(), e.getMessage());
            throw e;
        }
    }
}
