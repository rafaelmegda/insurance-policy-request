package com.company.insurance_request.domain.port.input;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceRequest;

public interface CreatePoliceUseCase {
    Policy create(CreatePoliceRequest createPoliceRequest);
}
