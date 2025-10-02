package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.port.input.CreatePoliceUseCase;
import com.company.insurance_request.domain.port.output.HistoryRepositoryPort;
import com.company.insurance_request.domain.port.output.OrderTopicBrokerPort;
import com.company.insurance_request.domain.port.output.PoliceRepositoryPort;
import com.company.insurance_request.domain.port.output.mapper.PoliceEventMapper;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyService implements CreatePoliceUseCase {

    private final PoliceRepositoryPort policeRepositoryPort;
    private final HistoryRepositoryPort historyRepositoryPort;
    private final OrderTopicBrokerPort publiser;
    private final PoliceEventMapper eventMapper;

    @Override
    @Transactional
    public Policy create(PolicyRequest policyRequest) throws JsonProcessingException {
        Policy policySaved = policeRepositoryPort.save(policyRequest);
        historyRepositoryPort.save(policySaved.getId(), policySaved.getStatus());
        OrderTopicEvent event = eventMapper.toStatusEvent(policySaved);
        publiser.publish(event, policySaved.getStatus().toString());
        return policySaved;
    }

    @Transactional
    public Policy updateStatus(Long policieId, String newStatus) {
        Policy updated = policeRepositoryPort.update(policieId, newStatus);
        historyRepositoryPort.save(updated.getId(), updated.getStatus());
        return updated;
    }
}
