package com.company.insurance_request.infrastructure.adapter.output.jpa;

import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.History;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.PolicyRepositoryPort;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.CoverageJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicyJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.repository.PolicyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PolicyRepositoryAdapter implements PolicyRepositoryPort {

    @PersistenceContext
    private EntityManager em;
    private final PolicyRepository policyRepository;

    public PolicyRepositoryAdapter(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Override
    public Policy save(PolicyRequest policyRequest, Status status) {
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
            entity.setStatus(status);
            entity.setSalesChannel(policyRequest.salesChannel());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setFinishedAt(LocalDateTime.MIN);

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

            PolicyJpaEntity saved = policyRepository.save(entity);
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

        Optional<PolicyJpaEntity> optional = policyRepository.findByPolicyId(policyId);
        if (optional.isEmpty()) {
            throw new NoSuchElementException("Policy not found with id: " + policyId);
        }
        PolicyJpaEntity entity = optional.get();
        entity.setStatus(Status.valueOf(String.valueOf(status)));
        entity.setFinishedAt(finishedAt);

        if (entity.getStatus() == Status.CANCELLED) {
            log.info("Policy id {} has been cancelled, Cannot be changed", policyId);
            return toDomain(updated = policyRepository.save(entity));
        }
        return toDomain(updated = policyRepository.save(entity));
    }

    @Override
    public List<Policy> findByPolicyId(UUID policyId) {
        Optional<PolicyJpaEntity> entities = policyRepository.findByPolicyId(policyId);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Policy> getPolicyById(UUID policyId, UUID customerId) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT p FROM PolicyJpaEntity p WHERE 1=1 ");
        if (policyId != null) {
            query.append("AND policyId = :policyId ");
        }
        if (customerId != null) {
            query.append("AND customerId = :customerId ");
        }
        var result = em.createQuery(query.toString(), PolicyJpaEntity.class);
        if (policyId != null) {
            result.setParameter("policyId", policyId);
        }
        if (customerId != null) {
            result.setParameter("customerId", customerId);
        }
        List<PolicyJpaEntity> entities = result.getResultList();
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
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
                .history(saved.getHistory().stream()
                        .map(h -> History.builder()
                                .timestamp(h.getTimestamp())
                                .status(h.getStatus())
                                .build())
                        .collect(Collectors.toList()))
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .finishedAt(saved.getFinishedAt())
                .build();
    }
}
