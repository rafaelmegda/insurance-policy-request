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
        log.info("Iniciando validacaoo de fraude para a apolice: {}", event.policieId());

        if (event.status() == Status.RECEIVED) {
            fraudService.processFraud(event);
            //log.info("response fraud: {}", validateFraud);
        }
    }
}
