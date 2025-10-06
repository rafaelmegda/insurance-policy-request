
package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.HistoryRepositoryPort;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.PolicyRepositoryPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import com.company.insurance_request.infrastructure.adapter.execption.PolicyIdNotExists;
import com.company.insurance_request.infrastructure.adapter.execption.PolicyStatusUpdateException;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepositoryPort policyRepositoryPort;
    @Mock
    private HistoryRepositoryPort historyRepositoryPort;
    @Mock
    private OrderTopicPublisherPort publisher;
    @Mock
    private PolicyEventMapper eventMapper;

    @InjectMocks
    private PolicyService service;

    @Test
    void create_returnsSavedPolicyAndPublishesEvent() throws Exception {
        PolicyRequest request = mock(PolicyRequest.class);
        UUID customerId = UUID.randomUUID();
        when(request.customerId()).thenReturn(customerId);

        Policy saved = mock(Policy.class);
        UUID policyId = UUID.randomUUID();
        when(saved.getPolicyId()).thenReturn(policyId);
        when(saved.getStatus()).thenReturn(Status.RECEIVED);

        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(policyRepositoryPort.save(request, Status.RECEIVED)).thenReturn(saved);
        when(eventMapper.toStatusEvent(saved)).thenReturn(event);

        Policy result = service.create(request);

        assertSame(saved, result);
        verify(historyRepositoryPort).save(policyId, Status.RECEIVED);
        verify(publisher).publishReceived(event, Status.RECEIVED.toString());
    }

    @Test
    void create_propagatesExceptionWhenPublisherFails() throws Exception {
        PolicyRequest request = mock(PolicyRequest.class);

        Policy saved = mock(Policy.class);
        when(saved.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.save(request, Status.RECEIVED)).thenReturn(saved);

        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(eventMapper.toStatusEvent(saved)).thenReturn(event);
        doThrow(new RuntimeException("publish-fail")).when(publisher).publishReceived(any(), anyString());

        assertThrows(RuntimeException.class, () -> service.create(request));
    }

    @Test
    void getPolicyById_delegatesToRepository() {
        UUID policyId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Policy p = mock(Policy.class);
        List<Policy> list = List.of(p);
        when(policyRepositoryPort.getPolicyById(policyId, customerId)).thenReturn(list);

        List<Policy> result = service.getPolicyById(policyId, customerId);

        assertSame(list, result);
    }

    @Test
    void updateStatus_throwsWhenPolicyNotFound() {
        UUID policyId = UUID.randomUUID();
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of());

        assertThrows(PolicyIdNotExists.class, () -> service.updateStatus(policyId, Status.APPROVED));
    }

    @Test
    void updateStatus_throwsWhenCurrentIsCancelled() {
        UUID policyId = UUID.randomUUID();
        Policy policy = mock(Policy.class);
        when(policy.getStatus()).thenReturn(Status.CANCELLED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(policy));

        assertThrows(PolicyStatusUpdateException.class, () -> service.updateStatus(policyId, Status.APPROVED));
    }

    @Test
    void updateStatus_approving_savesHistoryAndUpdatesWithNow() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        Policy updated = mock(Policy.class);
        when(policyRepositoryPort.update(eq(policyId), eq(Status.APPROVED), any(LocalDateTime.class))).thenReturn(updated);

        Policy result = service.updateStatus(policyId, Status.APPROVED);

        assertSame(updated, result);
        verify(historyRepositoryPort).save(policyId, Status.APPROVED);
        verify(policyRepositoryPort).update(eq(policyId), eq(Status.APPROVED), any(LocalDateTime.class));
    }

    @Test
    void updateStatus_receivedValidations_updatesWithMinTime() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        Policy updated = mock(Policy.class);
        when(policyRepositoryPort.update(eq(policyId), eq(Status.RECEIVED), eq(LocalDateTime.MIN))).thenReturn(updated);

        Policy result = service.updateStatus(policyId, Status.RECEIVED);

        assertSame(updated, result);
        verify(historyRepositoryPort).save(policyId, Status.RECEIVED);
        verify(policyRepositoryPort).update(policyId, Status.RECEIVED, LocalDateTime.MIN);
    }

    @Test
    void updateStatus_cancelling_whenCurrentApproved_throws() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.APPROVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        assertThrows(PolicyStatusUpdateException.class, () -> service.updateStatus(policyId, Status.CANCELLED));
    }

    @Test
    void updateStatus_cancelling_whenCurrentRecevied_updatesWithNow() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        Policy updated = mock(Policy.class);
        when(policyRepositoryPort.update(eq(policyId), eq(Status.CANCELLED), any(LocalDateTime.class))).thenReturn(updated);

        Policy result = service.updateStatus(policyId, Status.CANCELLED);

        assertSame(updated, result);
        verify(historyRepositoryPort).save(policyId, Status.CANCELLED);
        verify(policyRepositoryPort).update(eq(policyId), eq(Status.CANCELLED), any(LocalDateTime.class));
    }

    @Test
    void updateStatus_received_savesHistoryAndUpdatesWithMinTime() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        Policy updated = mock(Policy.class);
        when(policyRepositoryPort.update(eq(policyId), eq(Status.RECEIVED), eq(LocalDateTime.MIN))).thenReturn(updated);

        Policy result = service.updateStatus(policyId, Status.RECEIVED);

        assertSame(updated, result);
        verify(historyRepositoryPort).save(policyId, Status.RECEIVED);
        verify(policyRepositoryPort).update(policyId, Status.RECEIVED, LocalDateTime.MIN);
    }

    @Test
    void updateStatus_validated_savesHistoryAndUpdatesWithMinTime() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        Policy updated = mock(Policy.class);
        when(policyRepositoryPort.update(eq(policyId), eq(Status.VALIDATED), eq(LocalDateTime.MIN))).thenReturn(updated);

        Policy result = service.updateStatus(policyId, Status.VALIDATED);

        assertSame(updated, result);
        verify(historyRepositoryPort).save(policyId, Status.VALIDATED);
        verify(policyRepositoryPort).update(policyId, Status.VALIDATED, LocalDateTime.MIN);
    }

    @Test
    void updateStatus_pending_savesHistoryAndUpdatesWithMinTime() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        Policy updated = mock(Policy.class);
        when(policyRepositoryPort.update(eq(policyId), eq(Status.PENDING), eq(LocalDateTime.MIN))).thenReturn(updated);

        Policy result = service.updateStatus(policyId, Status.PENDING);

        assertSame(updated, result);
        verify(historyRepositoryPort).save(policyId, Status.PENDING);
        verify(policyRepositoryPort).update(policyId, Status.PENDING, LocalDateTime.MIN);
    }

    @Test
    void updateStatus_rejected_savesHistoryAndUpdatesWithNow() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        Policy updated = mock(Policy.class);
        when(policyRepositoryPort.update(eq(policyId), eq(Status.REJECTED), any(LocalDateTime.class))).thenReturn(updated);

        Policy result = service.updateStatus(policyId, Status.REJECTED);

        assertSame(updated, result);
        verify(historyRepositoryPort).save(policyId, Status.REJECTED);
        verify(policyRepositoryPort).update(eq(policyId), eq(Status.REJECTED), any(LocalDateTime.class));
    }

    @Test
    void updateStatus_approved_savesHistoryAndUpdatesWithNow() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        Policy updated = mock(Policy.class);
        when(policyRepositoryPort.update(eq(policyId), eq(Status.APPROVED), any(LocalDateTime.class))).thenReturn(updated);

        Policy result = service.updateStatus(policyId, Status.APPROVED);

        assertSame(updated, result);
        verify(historyRepositoryPort).save(policyId, Status.APPROVED);
        verify(policyRepositoryPort).update(eq(policyId), eq(Status.APPROVED), any(LocalDateTime.class));
    }

    @Test
    void updateStatus_cancelled_whenCurrentIsReceived_savesHistoryAndUpdatesWithNow() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.RECEIVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        Policy updated = mock(Policy.class);
        when(policyRepositoryPort.update(eq(policyId), eq(Status.CANCELLED), any(LocalDateTime.class))).thenReturn(updated);

        Policy result = service.updateStatus(policyId, Status.CANCELLED);

        assertSame(updated, result);
        verify(historyRepositoryPort).save(policyId, Status.CANCELLED);
        verify(policyRepositoryPort).update(eq(policyId), eq(Status.CANCELLED), any(LocalDateTime.class));
    }

    @Test
    void updateStatus_cancelled_whenCurrentIsApproved_throwsException() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.APPROVED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        assertThrows(PolicyStatusUpdateException.class, () -> service.updateStatus(policyId, Status.CANCELLED));
    }

    @Test
    void updateStatus_cancelled_whenCurrentIsRejected_throwsException() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.REJECTED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        assertThrows(PolicyStatusUpdateException.class, () -> service.updateStatus(policyId, Status.CANCELLED));
    }

    @Test
    void updateStatus_cancelled_whenCurrentIsCancelled_throwsException() {
        UUID policyId = UUID.randomUUID();
        Policy existing = mock(Policy.class);
        when(existing.getStatus()).thenReturn(Status.CANCELLED);
        when(policyRepositoryPort.findByPolicyId(policyId)).thenReturn(List.of(existing));

        assertThrows(PolicyStatusUpdateException.class, () -> service.updateStatus(policyId, Status.CANCELLED));
    }

}
