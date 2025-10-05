package com.company.insurance_request.domain.port.input;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.UUID;

public interface PolicyUseCase {
    Policy create(PolicyRequest policyRequest) throws JsonProcessingException;
    List<Policy> getPolicyById(UUID policyId, UUID customerId);
}
