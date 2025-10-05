package com.company.insurance_request.infrastructure.adapter.output.jpa.entity;

import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.PaymentMethod;
import com.company.insurance_request.domain.model.enums.SalesChannel;
import com.company.insurance_request.domain.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "policies")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"coverages"})
public class PolicyJpaEntity {

    @Id
    private UUID policyId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonManagedReference
    private Set<CoverageJpaEntity> coverages = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "assistances", joinColumns = @JoinColumn(name = "policy_id"))
    @Column(name = "assistance")
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
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL)
    private List<HistoryEntity> history = new ArrayList<>();
}
