package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.PolicieStatusEvent;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.port.input.CreatePoliceUseCase;
import com.company.insurance_request.domain.port.output.HistoryRepositoryPort;
import com.company.insurance_request.domain.port.output.MessageBrokerPort;
import com.company.insurance_request.domain.port.output.PoliceRepositoryPort;
import com.company.insurance_request.domain.port.output.mapper.PoliceEventMapper;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyService implements CreatePoliceUseCase {

    private final PoliceRepositoryPort policeRepositoryPort;
    private final HistoryRepositoryPort historyRepositoryPort;
    private final MessageBrokerPort publiser;
    private final PoliceEventMapper eventMapper;

    public PolicyService(PoliceRepositoryPort policeRepositoryPort, HistoryRepositoryPort historyRepositoryPort, MessageBrokerPort publiser, PoliceEventMapper eventMapper) {
        this.policeRepositoryPort = policeRepositoryPort;
        this.historyRepositoryPort = historyRepositoryPort;
        this.publiser = publiser;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public Policy create(CreatePoliceRequest createPoliceRequest) {
        Policy policySaved = policeRepositoryPort.save(createPoliceRequest);
        historyRepositoryPort.save(policySaved.getId(), policySaved.getStatus());
        PolicieStatusEvent event = eventMapper.toStatusEvent(policySaved);
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
