package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.model.Police;
import com.company.insurance_request.domain.port.input.CreatePoliceUseCase;
import com.company.insurance_request.domain.port.output.PoliceRepositoryPort;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicieService implements CreatePoliceUseCase {

    private final PoliceRepositoryPort policeRepositoryPort;

    public PolicieService(PoliceRepositoryPort policeRepositoryPort) {
        this.policeRepositoryPort = policeRepositoryPort;
    }

    @Override
    @Transactional
    public Police create(CreatePoliceRequest createPoliceRequest) {
        return policeRepositoryPort.save(createPoliceRequest);
    }
}
