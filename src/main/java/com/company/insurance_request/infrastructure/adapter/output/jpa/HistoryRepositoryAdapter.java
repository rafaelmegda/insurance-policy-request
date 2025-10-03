package com.company.insurance_request.infrastructure.adapter.output.jpa;

import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.HistoryRepositoryPort;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.HistoryEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicieJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.repository.HistoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class HistoryRepositoryAdapter implements HistoryRepositoryPort {

    private final HistoryRepository historyRepository;

    @PersistenceContext
    private EntityManager em;

    public HistoryRepositoryAdapter(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public void save(Long policieId, Status status) {
        try {
            HistoryEntity entity = new HistoryEntity();
            entity.setStatus(status);
            entity.setTimestamp(LocalDateTime.now());
            PolicieJpaEntity policy = em.getReference(PolicieJpaEntity.class, policieId);
            entity.setPolicie(policy);
            historyRepository.save(entity);
        }catch (Exception e){
            log.error("Error creating history policy_id: {} - {}", policieId, e.getMessage());
            throw e;
        }
    }
}
