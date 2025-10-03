package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.SubscriptionTopicEvent;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.SubscriptionTopicUseCase;
import com.company.insurance_request.domain.port.output.OrderTopicBrokerPort;
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
    private final OrderTopicBrokerPort publiser;
    private final PolicyEventMapper policyEventMapper;

    @Override
    public void processMessageSubscription(SubscriptionTopicEvent subscriptionTopicEvent) throws JsonProcessingException {

        if (subscriptionTopicEvent.status() == Status.REJECTED || subscriptionTopicEvent.status() == Status.APPROVED){
            Policy policy = policyService.updateStatus(subscriptionTopicEvent.policieId(), subscriptionTopicEvent.status().toValue());
            publiser.publish(policyEventMapper.toStatusEvent(policy), subscriptionTopicEvent.status().toValue());
            log.info("policy: {} subscription was: {}", subscriptionTopicEvent.policieId(), subscriptionTopicEvent.status());
        }
    }
}
