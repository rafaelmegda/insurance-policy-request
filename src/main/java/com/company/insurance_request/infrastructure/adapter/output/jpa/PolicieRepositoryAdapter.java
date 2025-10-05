package com.company.insurance_request.infrastructure.adapter.output.jpa;

import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.PoliceRepositoryPort;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.CoverageJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicyJpaEntity;
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
            log.info("Persisting Policy request for client: {}", policyRequest.customerId());

            PolicyJpaEntity entity = new PolicyJpaEntity();
            entity.setPolicyId(UUID.randomUUID());
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
                    coverageJpaEntity.setPolicy(entity);
                    entity.getCoverages().add(coverageJpaEntity);
                });
            }

            PolicyJpaEntity saved = policieRepository.save(entity);
            log.info("Created policy: {} for client: {}", saved.getPolicyId(), policyRequest.customerId());

            return toDomain(saved);
        }catch (Exception e){
            log.error("Error creating Policy for client: {} - {}", policyRequest.customerId(), e.getMessage());
            throw e;
        }
    }

    @Override
    public Policy update(UUID policyId, Status status, LocalDateTime finishedAt) {


        PolicyJpaEntity updated = new PolicyJpaEntity();

        Optional<PolicyJpaEntity> optional = policieRepository.findById(policyId);
        if (optional.isEmpty()) {
            throw new NoSuchElementException("Policy not found with id: " + policyId);
        }
        PolicyJpaEntity entity = optional.get();
        entity.setStatus(Status.valueOf(String.valueOf(status)));
        entity.setFinishedAt(finishedAt);

        // TODO JOGAR ESSA REGRA PRO SERVICE
        if (entity.getStatus() == Status.CANCELED) {
            entity.setFinishedAt(LocalDateTime.now());
            log.info("Policy with id {} has been cancelled, Cannot be changed", policyId);
            return toDomain(updated = policieRepository.save(entity));
        }

        return toDomain(updated = policieRepository.save(entity));
    }

    private Policy toDomain(PolicyJpaEntity saved) {
        return Policy.builder()
                .policyId(saved.getPolicyId())
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
