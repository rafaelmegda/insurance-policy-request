package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.PaymentTopicEvent;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.PaymentTopicUseCase;
import com.company.insurance_request.domain.port.output.OrderTopicBrokerPort;
import com.company.insurance_request.domain.port.output.mapper.PoliceEventMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentTopicService implements PaymentTopicUseCase {

    private final PolicyService policyService;
    private final OrderTopicBrokerPort publiser;
    private final PoliceEventMapper policeEventMapper;

    @Override
    public void processMessagePayment(PaymentTopicEvent paymentTopicEvent) throws JsonProcessingException {

        if (paymentTopicEvent.status() == Status.REJECTED || paymentTopicEvent.status() == Status.APPROVED){
            Policy policy = policyService.updateStatus(paymentTopicEvent.policieId(), paymentTopicEvent.status().toValue());
            publiser.publish(policeEventMapper.toStatusEvent(policy), paymentTopicEvent.status().toValue());
            log.info("policy: {} payment was: {}", paymentTopicEvent.policieId(), paymentTopicEvent.status());
        }
    }
}
