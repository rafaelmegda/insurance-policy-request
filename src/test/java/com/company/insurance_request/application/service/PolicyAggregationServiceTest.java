package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.AggregationResult;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import org.junit.jupiter.api.Assertions;
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

    @Test
    void completed_returnsTrue_whenBothStatusesAreNotNull() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), "APPROVED", "APPROVED");
        Assertions.assertTrue(result.completed());
    }

    @Test
    void completed_returnsFalse_whenPaymentStatusIsNull() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), null, "APPROVED");
        Assertions.assertFalse(result.completed());
    }

    @Test
    void completed_returnsFalse_whenSubscriptionStatusIsNull() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), "APPROVED", null);
        Assertions.assertFalse(result.completed());
    }

    @Test
    void anyRejected_returnsTrue_whenPaymentStatusIsRejected_caseInsensitive() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), "rejected", "APPROVED");
        Assertions.assertTrue(result.anyRejected());
    }

    @Test
    void anyRejected_returnsTrue_whenSubscriptionStatusIsRejected_caseInsensitive() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), "APPROVED", "REJECTED");
        Assertions.assertTrue(result.anyRejected());
    }

    @Test
    void anyRejected_returnsFalse_whenNeitherStatusIsRejected() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), "APPROVED", "APPROVED");
        Assertions.assertFalse(result.anyRejected());
    }

    @Test
    void decision_returnsRejected_whenAnyStatusIsRejected() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), "APPROVED", "REJECTED");
        Assertions.assertEquals(Status.REJECTED, result.decision());
    }

    @Test
    void decision_returnsApproved_whenBothStatusesPresentAndNotRejected() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), "APPROVED", "APPROVED");
        Assertions.assertEquals(Status.APPROVED, result.decision());
    }

    @Test
    void decision_returnsPending_whenAnyStatusIsNullAndNotRejected() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), null, "APPROVED");
        Assertions.assertEquals(Status.PENDING, result.decision());
    }

    @Test
    void decision_returnsPending_whenBothStatusesAreNull() {
        AggregationResult result = new AggregationResult(UUID.randomUUID(), null, null);
        Assertions.assertEquals(Status.PENDING, result.decision());
    }

}
