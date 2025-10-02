package com.company.insurance_request.infrastructure.adapter.output.jpa;

import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.PoliceRepositoryPort;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.CoverageJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicieJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.repository.PolicieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PolicieRepositoryAdapter implements PoliceRepositoryPort {

    private final PolicieRepository policieRepository;

    public PolicieRepositoryAdapter(PolicieRepository policieRepository) {
        this.policieRepository = policieRepository;
    }

    @Override
    public Policy save(PolicyRequest policyRequest) {
        try{
            PolicieJpaEntity entity = new PolicieJpaEntity();
            entity.setCustomerId(policyRequest.customerId());
            entity.setProductId(policyRequest.productId());
            entity.setCategory(policyRequest.category());
            entity.setAssistances(policyRequest.assistances() == null
                    ? Collections.emptySet()
                    : new HashSet<>(policyRequest.assistances()));
            entity.setTotalMonthlyPremiumAmount(policyRequest.totalMonthlyPremiumAmount());
            entity.setInsuredAmount(policyRequest.insuredAmount());
            entity.setPaymentMethod(policyRequest.paymentMethod());
            entity.setStatus(Status.RECEIVED);
            entity.setSalesChannel(policyRequest.salesChannel());
            entity.setCreatedAt(LocalDateTime.now());

            if (policyRequest.coverages() != null) {
                policyRequest.coverages().forEach(coverage -> {
                    CoverageJpaEntity coverageJpaEntity = new CoverageJpaEntity();
                    coverageJpaEntity.setRoubo(coverage.getRoubo());
                    coverageJpaEntity.setPerdaTotal(coverage.getPerdaTotal());
                    coverageJpaEntity.setColisaoComTerceiros(coverage.getColisaoComTerceiros());
                    coverageJpaEntity.setPolicie(entity);
                    entity.getCoverages().add(coverageJpaEntity);
                });
            }

            PolicieJpaEntity saved = policieRepository.save(entity);
            return toDomain(saved);
        }catch (Exception e){
            log.error("Error creating Policy for client: {} - {}", policyRequest.customerId(), e.getMessage());
            throw e;
        }
    }

    @Override
    public Policy update(Long policieId, String newStatus) {
        PolicieJpaEntity updated = new PolicieJpaEntity();

        Optional<PolicieJpaEntity> optional = policieRepository.findById(policieId);
        if (optional.isEmpty()) {
            throw new NoSuchElementException("Policie not found with id: " + policieId);
        }
        PolicieJpaEntity entity = optional.get();
        entity.setStatus(Status.valueOf(newStatus));

        if (entity.getStatus() == Status.CANCELED) {
            entity.setFinishedAt(LocalDateTime.now());
            log.info("Policie with id {} has been cancelled, Cannot be changed", policieId);
            return toDomain(updated = policieRepository.save(entity));
        }

        return toDomain(updated = policieRepository.save(entity));
    }

    private Policy toDomain(PolicieJpaEntity saved) {
        return Policy.builder()
                .id(saved.getId())
                .customerId(saved.getCustomerId())
                .productId(saved.getProductId())
                .category(saved.getCategory())
                .coverages(saved.getCoverages().stream()
                        .map(c -> Coverage.builder()
                                .roubo(c.getRoubo())
                                .perdaTotal(c.getPerdaTotal())
                                .colisaoComTerceiros(c.getColisaoComTerceiros())
                                .build())
                        .collect(Collectors.toList()))
                .assistances(saved.getAssistances() == null
                        ? Collections.emptyList()
                        : new ArrayList<>(saved.getAssistances()))
                .totalMonthlyPremiumAmount(saved.getTotalMonthlyPremiumAmount())
                .insuredAmount(saved.getInsuredAmount())
                .paymentMethod(saved.getPaymentMethod())
                .salesChannel(saved.getSalesChannel())
//                .histories(saved.getHistories().stream().map(history -> new History(
//                        history.getStatus(),
//                        history.getChangedAt()
//                )).collect(Collectors.toList())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .finishedAt(saved.getFinishedAt())
                .build();
    }
}
