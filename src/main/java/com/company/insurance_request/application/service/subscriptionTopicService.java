package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.SubscriptionTopicEvent;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.SubscriptionTopicUseCase;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class subscriptionTopicService implements SubscriptionTopicUseCase {

    private final PolicyService policyService;
    private final OrderTopicPublisherPort publisher;
    private final PolicyEventMapper policyEventMapper;

    @Override
    public void processMessageSubscription(SubscriptionTopicEvent subscriptionTopicEvent) throws JsonProcessingException {

        // TODO - VALIDA SE O STATUS ESTA VAZIO ?
        // TODO - SE O LISTNER OBTER A MENSAGEM E DER ERRO, O QUE FAZEMOS? RETORNAMOS PARA A FILA?

        if (subscriptionTopicEvent.status() == Status.REJECTED || subscriptionTopicEvent.status() == Status.APPROVED){

            log.info("Subscription status {} for policy: {}", subscriptionTopicEvent.status(), subscriptionTopicEvent.policyId());

//            Policy policy = policyService.updateStatus(subscriptionTopicEvent.policyId(), subscriptionTopicEvent.status());
//            publiser.publish(policyEventMapper.toStatusEvent(policy), subscriptionTopicEvent.status().toValue());
//            log.info("policy: {} subscription was: {}", subscriptionTopicEvent.policyId(), subscriptionTopicEvent.status());
        }
    }
}
