package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.model.Police;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.input.CreatePoliceUseCase;
import com.company.insurance_request.domain.port.output.HistoryRepositoryPort;
import com.company.insurance_request.domain.port.output.PoliceRepositoryPort;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicieService implements CreatePoliceUseCase {

    private final PoliceRepositoryPort policeRepositoryPort;
    private final HistoryRepositoryPort historyRepositoryPort;

    public PolicieService(PoliceRepositoryPort policeRepositoryPort, HistoryRepositoryPort historyRepositoryPort) {
        this.policeRepositoryPort = policeRepositoryPort;
        this.historyRepositoryPort = historyRepositoryPort;
    }

    @Override
    @Transactional
    public Police create(CreatePoliceRequest createPoliceRequest) {
        Police policeSaved = policeRepositoryPort.save(createPoliceRequest);
        historyRepositoryPort.save(policeSaved.getId(), policeSaved.getStatus());
        return policeSaved;
    }

//    @Transactional
//    public Police updateStatus(Long policieId, Status newStatus) {
//        Police police = policeRepositoryPort.findById(policieId)
//                .orElseThrow(() -> new IllegalArgumentException("Policy não encontrada: " + policieId));
//
//        if (police.getStatus() == newStatus) {
//            return police; // Nada a fazer
//        }
//
//        police.setStatus(newStatus);
//        Police updated = policeRepositoryPort.update(police);
//        historyRepositoryPort.save(updated.getId(), updated.getStatus());
//        return updated;
//    }
}
