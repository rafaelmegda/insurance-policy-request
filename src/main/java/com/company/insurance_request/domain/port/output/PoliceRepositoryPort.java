package com.company.insurance_request.domain.port.output;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PoliceRepositoryPort {
    Policy save(PolicyRequest policyRequest);

    Policy update(UUID policyId, Status status, LocalDateTime finalizedAt);
}
