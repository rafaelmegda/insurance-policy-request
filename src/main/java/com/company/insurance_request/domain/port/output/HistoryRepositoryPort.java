package com.company.insurance_request.domain.port.output;

import com.company.insurance_request.domain.model.Police;
import com.company.insurance_request.domain.model.enums.Status;

public interface HistoryRepositoryPort {
    void save(Long id, Status status);
}
