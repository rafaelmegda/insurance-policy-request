package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.FraudResult;
import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Fraud;
import com.company.insurance_request.domain.model.enums.Classification;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.FraudPort;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FraudServiceTest {

    @Mock
    private FraudPort fraudPort;

    @Mock
    private PolicyService policyService;

    @Mock
    private OrderTopicPublisherPort publisher;

    @Mock
    private PolicyEventMapper policyEventMapper;

    @Mock
    private FraudResult fraudResult;

    @InjectMocks
    private FraudService fraudService;

    @Test
    void processFraud_doesNothingWhenStatusIsNotReceived() throws Exception {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.APPROVED);
        fraudService.processFraud(event);
        verifyNoInteractions(fraudPort, policyService, publisher, fraudResult);
    }

    @Test
    void processFraud_whenStatusIsReceivedAndFraudClassificationIsNull_logsWarningAndReturns() throws Exception {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.RECEIVED);
        when(event.policyId()).thenReturn(java.util.UUID.randomUUID());

        Fraud fraud = mock(Fraud.class);
        when(fraud.getClassification()).thenReturn(null);
        when(fraudPort.validate(event)).thenReturn(fraud);

        fraudService.processFraud(event);

        verify(fraudPort).validate(event);
        verifyNoMoreInteractions(policyService, publisher, fraudResult);
    }

    @Test
    void processFraud_whenStatusIsReceivedAndFraudIsValidatedAndApproved_updatesStatusAndPublishes() throws Exception {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.RECEIVED);
        when(event.policyId()).thenReturn(java.util.UUID.randomUUID());
        when(event.category()).thenReturn(null);
        when(event.insuredAmount()).thenReturn(null);

        Fraud fraud = mock(Fraud.class);
        when(fraud.getClassification()).thenReturn(Classification.REGULAR);
        when(fraudPort.validate(event)).thenReturn(fraud);

        when(fraudResult.isValidated(any(), any(), any())).thenReturn(true);

        fraudService.processFraud(event);

        InOrder inOrder = inOrder(policyService, publisher);
        inOrder.verify(policyService).updateStatus(event.policyId(), Status.VALIDATED);
        inOrder.verify(publisher).publishReceived(event, Status.VALIDATED.toValue());
        inOrder.verify(policyService).updateStatus(event.policyId(), Status.PENDING);
    }

    @Test
    void processFraud_whenStatusIsReceivedAndFraudIsValidatedAndRejected_updatesStatusToRejected() throws Exception {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.RECEIVED);
        when(event.policyId()).thenReturn(java.util.UUID.randomUUID());
        when(event.category()).thenReturn(null);
        when(event.insuredAmount()).thenReturn(null);

        Fraud fraud = mock(Fraud.class);
        when(fraud.getClassification()).thenReturn(Classification.HIGH_RISK);
        when(fraudPort.validate(event)).thenReturn(fraud);

        when(fraudResult.isValidated(any(), any(), any())).thenReturn(false);

        fraudService.processFraud(event);

        verify(policyService).updateStatus(event.policyId(), Status.REJECTED);
        verifyNoInteractions(publisher);
    }

    @Test
    void processFraud_whenStatusIsReceivedAndClassificationRegularAndIsValidatedTrue_updatesStatusAndPublishes() throws Exception {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.RECEIVED);
        when(event.policyId()).thenReturn(java.util.UUID.randomUUID());
        when(event.category()).thenReturn(null);
        when(event.insuredAmount()).thenReturn(null);

        Fraud fraud = mock(Fraud.class);
        when(fraud.getClassification()).thenReturn(Classification.REGULAR);
        when(fraudPort.validate(event)).thenReturn(fraud);

        when(fraudResult.isValidated(eq(Classification.REGULAR), any(), any())).thenReturn(true);

        fraudService.processFraud(event);

        InOrder inOrder = inOrder(policyService, publisher);
        inOrder.verify(policyService).updateStatus(event.policyId(), Status.VALIDATED);
        inOrder.verify(publisher).publishReceived(event, Status.VALIDATED.toValue());
        inOrder.verify(policyService).updateStatus(event.policyId(), Status.PENDING);
    }

    @Test
    void processFraud_whenStatusIsReceivedAndClassificationHighRiskAndIsValidatedFalse_updatesStatusToRejected() throws Exception {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.RECEIVED);
        when(event.policyId()).thenReturn(java.util.UUID.randomUUID());
        when(event.category()).thenReturn(null);
        when(event.insuredAmount()).thenReturn(null);

        Fraud fraud = mock(Fraud.class);
        when(fraud.getClassification()).thenReturn(Classification.HIGH_RISK);
        when(fraudPort.validate(event)).thenReturn(fraud);

        when(fraudResult.isValidated(eq(Classification.HIGH_RISK), any(), any())).thenReturn(false);

        fraudService.processFraud(event);

        verify(policyService).updateStatus(event.policyId(), Status.REJECTED);
        verifyNoInteractions(publisher);
    }

    @Test
    void processFraud_whenStatusIsReceivedAndClassificationPreferentialAndIsValidatedTrue_updatesStatusAndPublishes() throws Exception {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.RECEIVED);
        when(event.policyId()).thenReturn(java.util.UUID.randomUUID());
        when(event.category()).thenReturn(null);
        when(event.insuredAmount()).thenReturn(null);

        Fraud fraud = mock(Fraud.class);
        when(fraud.getClassification()).thenReturn(Classification.PREFERENTIAL);
        when(fraudPort.validate(event)).thenReturn(fraud);

        when(fraudResult.isValidated(eq(Classification.PREFERENTIAL), any(), any())).thenReturn(true);

        fraudService.processFraud(event);

        InOrder inOrder = inOrder(policyService, publisher);
        inOrder.verify(policyService).updateStatus(event.policyId(), Status.VALIDATED);
        inOrder.verify(publisher).publishReceived(event, Status.VALIDATED.toValue());
        inOrder.verify(policyService).updateStatus(event.policyId(), Status.PENDING);
    }

    @Test
    void processFraud_whenStatusIsReceivedAndClassificationNoInformationAndIsValidatedFalse_updatesStatusToRejected() throws Exception {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.RECEIVED);
        when(event.policyId()).thenReturn(java.util.UUID.randomUUID());
        when(event.category()).thenReturn(null);
        when(event.insuredAmount()).thenReturn(null);

        Fraud fraud = mock(Fraud.class);
        when(fraud.getClassification()).thenReturn(Classification.NO_INFORMATION);
        when(fraudPort.validate(event)).thenReturn(fraud);

        when(fraudResult.isValidated(eq(Classification.NO_INFORMATION), any(), any())).thenReturn(false);

        fraudService.processFraud(event);

        verify(policyService).updateStatus(event.policyId(), Status.REJECTED);
        verifyNoInteractions(publisher);
    }

}
