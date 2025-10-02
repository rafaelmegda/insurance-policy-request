package com.company.insurance_request.infrastructure.adapter.output.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "coverages")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CoverageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal roubo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal perdaTotal;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal colisaoComTerceiros;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "police_id", nullable = false)
    private PolicieJpaEntity policie;
}
