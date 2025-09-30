package com.company.insurance_request.infrastructure.adapter.output.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "coverages")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoverageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal roubo;
    private BigDecimal perdaTotal;
    private BigDecimal colisaoComTerceiros;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "police_id")
    private PolicieJpaEntity policie;
}
