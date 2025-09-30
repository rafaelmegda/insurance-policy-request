package com.company.insurance_request.infrastructure.adapter.output.jpa;

import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.History;
import com.company.insurance_request.domain.model.Police;
import com.company.insurance_request.domain.port.output.PoliceRepositoryPort;
import com.company.insurance_request.infrastructure.adapter.input.dto.CreatePoliceRequest;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.CoverageJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicieJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.repository.PolicieRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PolicieRepositoryAdapter implements PoliceRepositoryPort {

    private final PolicieRepository policieRepository;

    public PolicieRepositoryAdapter(PolicieRepository policieRepository) {
        this.policieRepository = policieRepository;
    }

    @Override
    public Police save(CreatePoliceRequest createPoliceRequest) {
        PolicieJpaEntity entity = new PolicieJpaEntity();
        entity.setCustomerId(createPoliceRequest.customerId());
        entity.setProductId(createPoliceRequest.productId());
        entity.setCategory(createPoliceRequest.category());
        entity.setAssistances(createPoliceRequest.assistances() == null
                ? Collections.emptySet()
                : new HashSet<>(createPoliceRequest.assistances()));
        entity.setTotalMonthlyPremiumAmount(createPoliceRequest.totalMonthlyPremiumAmount());
        entity.setInsuredAmount(createPoliceRequest.insuredAmount());
        entity.setPaymentMethod(createPoliceRequest.paymentMethod());
        entity.setSalesChannel(createPoliceRequest.salesChannel());

        if (createPoliceRequest.coverages() != null) {
            createPoliceRequest.coverages().forEach(coverage -> {
                CoverageJpaEntity coverageJpaEntity = new CoverageJpaEntity();
                coverageJpaEntity.setRoubo(coverage.getRoubo());
                coverageJpaEntity.setPerdaTotal(coverage.getPerdaTotal());
                coverageJpaEntity.setColisaoComTerceiros(coverage.getColisaoComTerceiros());
                entity.getCoverages().add(coverageJpaEntity);
            });
        }

        PolicieJpaEntity saved = policieRepository.save(entity);
        return toDomain(saved);
    }

    private Police toDomain(PolicieJpaEntity saved) {
        return Police.builder()
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
                .build();
    }
}
