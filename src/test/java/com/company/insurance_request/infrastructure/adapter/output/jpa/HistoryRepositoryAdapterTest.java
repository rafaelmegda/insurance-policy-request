package com.company.insurance_request.infrastructure.adapter.output.jpa;

import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicyJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.repository.HistoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class HistoryRepositoryAdapterTest {

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PolicyJpaEntity policyJpaEntity;

    @InjectMocks
    private HistoryRepositoryAdapter adapter;

    @Test
    void save_persistsHistoryEntityWithCorrectStatusAndPolicy() {
        UUID policyId = UUID.randomUUID();
        Status status = Status.APPROVED;
        ReflectionTestUtils.setField(adapter, "em", entityManager);

        when(entityManager.getReference(PolicyJpaEntity.class, policyId)).thenReturn(policyJpaEntity);

        adapter.save(policyId, status);

        verify(entityManager).getReference(PolicyJpaEntity.class, policyId);
        verify(historyRepository).save(argThat(entity ->
                entity.getStatus() == status &&
                        entity.getPolicy() == policyJpaEntity &&
                        entity.getTimestamp() != null
        ));
    }

    @Test
    void save_logsAndThrowsExceptionWhenEntityManagerFails() {
        UUID policyId = UUID.randomUUID();
        Status status = Status.REJECTED;
        RuntimeException ex = new RuntimeException("DB error");
        ReflectionTestUtils.setField(adapter, "em", entityManager);

        when(entityManager.getReference(PolicyJpaEntity.class, policyId)).thenThrow(ex);

        assertThatThrownBy(() -> adapter.save(policyId, status))
                .isSameAs(ex);

        verify(historyRepository, never()).save(any());
    }

}
