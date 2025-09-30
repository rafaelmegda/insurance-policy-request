package com.company.insurance_request.infrastructure.adapter.output.jpa.repository;

import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {
}
