package com.company.insurance_request.infrastructure.adapter.output.jpa.repository;

import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicieJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicieRepository extends JpaRepository<PolicieJpaEntity, Long> {
}
