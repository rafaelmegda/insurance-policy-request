package com.company.insurance_request.infrastructure.adapter.output.jpa.repository;

import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PolicieRepository extends JpaRepository<PolicyJpaEntity, UUID> {
}
