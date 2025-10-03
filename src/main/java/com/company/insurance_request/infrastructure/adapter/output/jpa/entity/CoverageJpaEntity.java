package com.company.insurance_request.infrastructure.adapter.output.jpa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    // TODO - IMAGINAR COBERTURAS E INCLUIR COMO NULLABLE NA TABELA

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 15, scale = 2)
    private BigDecimal roubo;

    @Column(precision = 15, scale = 2)
    private BigDecimal perdaTotal;

    @Column(precision = 15, scale = 2)
    private BigDecimal colisaoComTerceiros;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    @JsonBackReference
    private PolicyJpaEntity policy;
}
