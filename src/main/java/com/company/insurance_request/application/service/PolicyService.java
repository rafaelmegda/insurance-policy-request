package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.PolicyUseCase;
import com.company.insurance_request.domain.port.output.HistoryRepositoryPort;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.PolicyRepositoryPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService implements PolicyUseCase {

    private final PolicyRepositoryPort policyRepositoryPort;
    private final HistoryRepositoryPort historyRepositoryPort;
    private final OrderTopicPublisherPort publisher;
    private final PolicyEventMapper eventMapper;

    @Override
    @Transactional
    public Policy create(PolicyRequest policyRequest) throws JsonProcessingException {

        try{
            log.info("Creating the insurance policy request to customer_id: {}", policyRequest.customerId());
            Policy policySaved = policyRepositoryPort.save(policyRequest, Status.RECEIVED);

            log.info("Updating request status to: {} to policy: {}", policySaved.getStatus(), policySaved.getPolicyId());
            historyRepositoryPort.save(policySaved.getPolicyId(), Status.RECEIVED);

            log.info("Publishing event to order topic status {} to policy id: {}", policySaved.getStatus(), policySaved.getPolicyId());
            OrderTopicEvent event = eventMapper.toStatusEvent(policySaved);
            publisher.publishReceived(event, policySaved.getStatus().toString());

            return policySaved;
        }catch (Exception ex){
            log.error("Error creating insurance policy request {}", ex.getMessage());
            throw ex;
        }

    }

    @Override
    public List<Policy> getPolicyById(UUID policyId, UUID customerId) {

        log.info("Finding policy by policyId: {} and customerId: {}", policyId, customerId);
        List<Policy> policy = policyRepositoryPort.getPolicyById(policyId, customerId);

        return policy;
    }

    @Transactional
    public Policy updateStatus(UUID policyId, Status status) {

        log.info("Updating policy: {} to status {}", status, policyId);
        Policy updated = Policy.builder().build();

        if(status == Status.REJECTED || status == Status.APPROVED) {
            updated = policyRepositoryPort.update(policyId, status, LocalDateTime.now());
        } else {
            updated = policyRepositoryPort.update(policyId, status, LocalDateTime.MIN);
        }
        historyRepositoryPort.save(policyId, status);
        return updated;
    }
}
