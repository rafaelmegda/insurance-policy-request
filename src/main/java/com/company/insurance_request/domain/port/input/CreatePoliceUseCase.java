package com.company.insurance_request.domain.port.input;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface CreatePoliceUseCase {
    Policy create(PolicyRequest policyRequest) throws JsonProcessingException;
}
