package com.company.insurance_request.domain.port.output;

import com.company.insurance_request.domain.model.Police;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceRequest;

public interface PoliceRepositoryPort {
    Police save(CreatePoliceRequest createPoliceRequest);
}
