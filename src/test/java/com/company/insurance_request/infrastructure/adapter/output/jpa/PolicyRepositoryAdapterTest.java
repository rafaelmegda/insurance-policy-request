package com.company.insurance_request.infrastructure.adapter.output.jpa;

import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.Coverage;
import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.PaymentMethod;
import com.company.insurance_request.domain.model.enums.SalesChannel;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.PolicyJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.entity.CoverageJpaEntity;
import com.company.insurance_request.infrastructure.adapter.output.jpa.repository.PolicyRepository;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyRepositoryAdapterTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private PolicyRepositoryAdapter adapter;

    @Mock
    private PolicyJpaEntity policyJpaEntity;

    @Mock
    private CoverageJpaEntity coverageJpaEntity;

    @BeforeEach
    void injectEntityManager() {
        org.springframework.test.util.ReflectionTestUtils.setField(adapter, "em", entityManager);
    }

    @Test
    void save_persistsPolicyWithAllFieldsAndReturnsDomainObject() {
        PolicyRequest request = mock(PolicyRequest.class);
        when(request.customerId()).thenReturn(UUID.randomUUID());
        when(request.productId()).thenReturn(1L);
        when(request.category()).thenReturn(Category.AUTO);
        when(request.assistances()).thenReturn(List.of("GUINCHO"));
        when(request.totalMonthlyPremiumAmount()).thenReturn(BigDecimal.valueOf(100.0));
        when(request.insuredAmount()).thenReturn(BigDecimal.valueOf(10000.0));
        when(request.paymentMethod()).thenReturn(PaymentMethod.valueOf(String.valueOf(PaymentMethod.BOLETO)));
        when(request.salesChannel()).thenReturn(SalesChannel.valueOf(String.valueOf(SalesChannel.MOBILE)));
        when(request.coverages()).thenReturn(List.of(mock(Coverage.class)));

        when(policyRepository.save(any(PolicyJpaEntity.class))).thenAnswer(invocation -> {
            PolicyJpaEntity entity = invocation.getArgument(0);
            entity.setPolicyId(UUID.randomUUID());
            return entity;
        });

        Policy result = adapter.save(request, Status.APPROVED);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(result.getAssistances()).contains("GUINCHO");
    }


    @Test
    void save_logsErrorAndThrowsWhenRepositoryFails() {
        PolicyRequest request = mock(PolicyRequest.class);
        when(request.customerId()).thenReturn(UUID.randomUUID());
        when(policyRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> adapter.save(request, Status.APPROVED))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    @Test
    void update_updatesStatusAndFinishedAtWhenPolicyExists() {
        UUID policyId = UUID.randomUUID();
        PolicyJpaEntity entity = new PolicyJpaEntity();
        entity.setPolicyId(policyId);
        entity.setStatus(Status.PENDING);

        when(policyRepository.findByPolicyId(policyId)).thenReturn(Optional.of(entity));
        when(policyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Policy result = adapter.update(policyId, Status.APPROVED, LocalDateTime.now());

        assertThat(result.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(result.getPolicyId()).isEqualTo(policyId);
    }

    @Test
    void update_throwsExceptionWhenPolicyNotFound() {
        UUID policyId = UUID.randomUUID();
        when(policyRepository.findByPolicyId(policyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.update(policyId, Status.APPROVED, LocalDateTime.now()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Policy not found");
    }

    @Test
    void findByPolicyId_returnsListWithDomainPolicyWhenFound() {
        UUID policyId = UUID.randomUUID();
        PolicyJpaEntity entity = new PolicyJpaEntity();
        entity.setPolicyId(policyId);

        when(policyRepository.findByPolicyId(policyId)).thenReturn(Optional.of(entity));

        List<Policy> result = adapter.findByPolicyId(policyId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPolicyId()).isEqualTo(policyId);
    }

    @Test
    void findByPolicyId_returnsEmptyListWhenNotFound() {
        UUID policyId = UUID.randomUUID();
        when(policyRepository.findByPolicyId(policyId)).thenReturn(Optional.empty());

        List<Policy> result = adapter.findByPolicyId(policyId);

        assertThat(result).isEmpty();
    }

    @Test
    void getPolicyById_buildsQueryWithBothParameters() {
        UUID policyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        List<PolicyJpaEntity> entities = List.of(new PolicyJpaEntity());

        var query = mock(jakarta.persistence.TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(PolicyJpaEntity.class))).thenReturn(query);
        when(query.setParameter(eq("policyId"), any())).thenReturn(query);
        when(query.setParameter(eq("customerId"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(entities);

        List<Policy> result = adapter.getPolicyById(policyId, customerId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getPolicyById_buildsQueryWithOnlyPolicyId() {
        UUID policyId = UUID.randomUUID();
        List<PolicyJpaEntity> entities = List.of(new PolicyJpaEntity());

        var query = mock(jakarta.persistence.TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(PolicyJpaEntity.class))).thenReturn(query);
        when(query.setParameter(eq("policyId"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(entities);

        List<Policy> result = adapter.getPolicyById(policyId, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getPolicyById_buildsQueryWithOnlyCustomerId() {
        UUID customerId = UUID.randomUUID();
        List<PolicyJpaEntity> entities = List.of(new PolicyJpaEntity());

        var query = mock(jakarta.persistence.TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(PolicyJpaEntity.class))).thenReturn(query);
        when(query.setParameter(eq("customerId"), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(entities);

        List<Policy> result = adapter.getPolicyById(null, customerId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getPolicyById_returnsEmptyListWhenNoResults() {
        var query = mock(jakarta.persistence.TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(PolicyJpaEntity.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        List<Policy> result = adapter.getPolicyById(null, null);

        assertThat(result).isEmpty();
    }
}
