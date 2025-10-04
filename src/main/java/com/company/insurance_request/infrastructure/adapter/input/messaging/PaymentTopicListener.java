package com.company.insurance_request.infrastructure.adapter.input.messaging;

import com.company.insurance_request.domain.event.PaymentTopicEvent;
import com.company.insurance_request.domain.port.input.PaymentTopicUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentTopicListener {

    private final PaymentTopicUseCase paymentTopicUseCase;

    public PaymentTopicListener(@Qualifier("paymentTopicService") PaymentTopicUseCase paymentTopicUseCase) {
        this.paymentTopicUseCase = paymentTopicUseCase;
    }

    @RabbitListener(queues = "${messaging.queue.payment}" )
    public void onMessage(PaymentTopicEvent event,
                          @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws JsonProcessingException {
        try{
            log.info("Message received status: {} queue.payment to customer_id: {} - Event: {}", event.status(), event.customerId(), event);
            paymentTopicUseCase.processMessagePayment(event);
        }catch (Exception e){
            log.error("Error listener message queue.payment to policy_id: {} - status: {} - error: {}", event.policyId(), event.status(), e.getMessage());
            throw e;
        }
    }
}
