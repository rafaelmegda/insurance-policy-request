package com.company.insurance_request.domain.port.output;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;

public interface PoliceRepositoryPort {
    Policy save(PolicyRequest policyRequest);

    Policy update(Long policieId, String newStatus);
}
