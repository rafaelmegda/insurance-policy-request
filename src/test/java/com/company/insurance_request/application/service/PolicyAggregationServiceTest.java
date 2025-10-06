package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.AggregationResult;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PolicyAggregationServiceTest {

    @Mock
    private PolicyService policyService;
    @Mock
    private OrderTopicPublisherPort publisher;
    @Mock
    private PolicyEventMapper policyEventMapper;
    @InjectMocks
    private PolicyAggregationService aggregationService;

    @Test
    void finalizeAggregation_whenResultIsNull_logsAndReturns() {
        aggregationService.finalizeAggregation(null);
        verifyNoInteractions(policyService, publisher, policyEventMapper);
    }

    @Test
    void finalizeAggregation_whenAnyStatusRejected_updatesStatusToRejectedAndPublishes() {
        UUID policyId = UUID.randomUUID();
        AggregationResult result = mock(AggregationResult.class);
        when(result.policyId()).thenReturn(policyId);
        when(result.paymentStatus()).thenReturn(Status.REJECTED.toValue());
        when(result.subscriptionStatus()).thenReturn(Status.APPROVED.toValue());

        Policy policy = mock(Policy.class);
        when(policyService.updateStatus(policyId, Status.REJECTED)).thenReturn(policy);
        when(policy.getStatus()).thenReturn(Status.REJECTED);
        when(policy.getPolicyId()).thenReturn(policyId);

        aggregationService.finalizeAggregation(result);

        verify(policyService).updateStatus(policyId, Status.REJECTED);
        verify(publisher).publishFinishStatus(any(), eq(Status.REJECTED.toValue()));
    }

    @Test
    void finalizeAggregation_whenBothStatusesPresentAndNotRejected_updatesStatusToApprovedAndPublishes() {
        UUID policyId = UUID.randomUUID();
        AggregationResult result = mock(AggregationResult.class);
        when(result.policyId()).thenReturn(policyId);
        when(result.paymentStatus()).thenReturn(Status.APPROVED.toValue());
        when(result.subscriptionStatus()).thenReturn(Status.APPROVED.toValue());

        Policy policy = mock(Policy.class);
        when(policyService.updateStatus(policyId, Status.APPROVED)).thenReturn(policy);
        when(policy.getStatus()).thenReturn(Status.APPROVED);
        when(policy.getPolicyId()).thenReturn(policyId);

        aggregationService.finalizeAggregation(result);

        verify(policyService).updateStatus(policyId, Status.APPROVED);
        verify(publisher).publishFinishStatus(any(), eq(Status.APPROVED.toValue()));
    }

    @Test
    void finalizeAggregation_whenAnyStatusIsNullAndNotRejected_updatesStatusToPendingAndPublishes() {
        UUID policyId = UUID.randomUUID();
        AggregationResult result = mock(AggregationResult.class);
        when(result.policyId()).thenReturn(policyId);
        when(result.paymentStatus()).thenReturn(null);
        when(result.subscriptionStatus()).thenReturn(Status.APPROVED.toValue());

        Policy policy = mock(Policy.class);
        when(policyService.updateStatus(policyId, Status.PENDING)).thenReturn(policy);
        when(policy.getStatus()).thenReturn(Status.PENDING);
        when(policy.getPolicyId()).thenReturn(policyId);

        aggregationService.finalizeAggregation(result);

        verify(policyService).updateStatus(policyId, Status.PENDING);
        verify(publisher).publishFinishStatus(any(), eq(Status.PENDING.toValue()));
    }

    @Test
    void finalizeAggregation_whenUpdateStatusThrowsException_logsError() {
        UUID policyId = UUID.randomUUID();
        AggregationResult result = mock(AggregationResult.class);
        when(result.policyId()).thenReturn(policyId);
        when(result.paymentStatus()).thenReturn(Status.APPROVED.toValue());
        when(result.subscriptionStatus()).thenReturn(Status.APPROVED.toValue());

        when(policyService.updateStatus(policyId, Status.APPROVED)).thenThrow(new RuntimeException("fail"));

        aggregationService.finalizeAggregation(result);

        verify(policyService).updateStatus(policyId, Status.APPROVED);
        verifyNoInteractions(publisher, policyEventMapper);
    }
}
