package com.company.insurance_request.domain.port.output;

import com.company.insurance_request.domain.model.enums.Status;

import java.util.UUID;

public interface HistoryRepositoryPort {
    void save(UUID policyId, Status status);
}
