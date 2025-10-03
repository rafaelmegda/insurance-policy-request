package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.OrderTopicUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTopicService implements OrderTopicUseCase {

    private final FraudService fraudService;

    @Override
    public void processMessageOrder(OrderTopicEvent event) throws JsonProcessingException {

        if (event.status() == Status.RECEIVED) {
            log.info("Initiating Fraud Approval Request to policy: {}", event.policyId());
            fraudService.processFraud(event);
        }
    }
}
