package com.company.insurance_request.infrastructure.adapter.output.jpa.entity;

import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.PaymentMethod;
import com.company.insurance_request.domain.model.enums.SalesChannel;
import com.company.insurance_request.domain.model.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "policies")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolicieJpaEntity {

    //TODO Criar tabela History e relacionar aqui

    @Id
    private Long id; // vindo do request

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @OneToMany(mappedBy = "policie", cascade = CascadeType.ALL)
    private List<CoverageJpaEntity> coverages = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "policy_assistances", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "assistance")
    @Enumerated(EnumType.STRING)
    private Set<String> assistances = new HashSet<>();

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalMonthlyPremiumAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal insuredAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesChannel salesChannel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime finishedAt;
}
