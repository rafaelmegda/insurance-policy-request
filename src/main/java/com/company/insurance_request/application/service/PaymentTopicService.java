package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.PaymentTopicEvent;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.PaymentTopicUseCase;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentTopicService implements PaymentTopicUseCase {

    private final PolicyService policyService;
    private final OrderTopicPublisherPort publisher;
    private final PolicyEventMapper policyEventMapper;

    @Override
    public void processMessagePayment(PaymentTopicEvent paymentTopicEvent) throws JsonProcessingException {

        // TODO : Achar uma forma de orquestrar o event de payment e subs para nao depender da ordem de chegada e definir se a solicitação foi aprovada ou rejeitada

        if (paymentTopicEvent.status() == Status.REJECTED || paymentTopicEvent.status() == Status.APPROVED){

            log.info("Payment status {} for policy: {}", paymentTopicEvent.status(), paymentTopicEvent.policyId());

//            Policy policy = policyService.updateStatus(paymentTopicEvent.policyId(), paymentTopicEvent.status());
//            publisher.publish(policyEventMapper.toStatusEvent(policy), paymentTopicEvent.status().toValue());
//            log.info("policy: {} payment was: {}", paymentTopicEvent.policyId(), paymentTopicEvent.status());
        }
    }
}
