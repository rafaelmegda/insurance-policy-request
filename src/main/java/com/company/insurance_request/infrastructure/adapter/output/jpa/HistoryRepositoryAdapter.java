package com.company.insurance_request.infrastructure.adapter.output.jpa;

import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.HistoryRepositoryPort;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.HistoryEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicyJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.repository.HistoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

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
    public void save(UUID policyId, Status status) {
        try {
            log.info("Persisting request status: {} to policy: {}", status, policyId);
            HistoryEntity entity = new HistoryEntity();
            entity.setStatus(status);
            entity.setTimestamp(LocalDateTime.now());
            PolicyJpaEntity policy = em.getReference(PolicyJpaEntity.class, policyId);
            entity.setPolicy(policy);
            historyRepository.save(entity);

        }catch (Exception e){
            log.error("Error updated status to policy: {} - {}", policyId, e.getMessage());
            throw e;
        }
    }
}
