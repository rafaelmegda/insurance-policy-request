package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.FraudResult;
import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Fraud;
import com.company.insurance_request.domain.model.enums.Category;
import com.company.insurance_request.domain.model.enums.Classification;
import com.company.insurance_request.domain.model.enums.Status;
import com.company.insurance_request.domain.port.output.FraudPort;
import com.company.insurance_request.domain.port.output.OrderTopicPublisherPort;
import com.company.insurance_request.domain.port.output.mapper.PolicyEventMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

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

    @Test
    void isValidated_returnsTrue_forRegularLifeAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.REGULAR, Category.LIFE, new BigDecimal("500000")));
    }

    @Test
    void isValidated_returnsFalse_forRegularLifeAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.REGULAR, Category.LIFE, new BigDecimal("500001")));
    }

    @Test
    void isValidated_returnsTrue_forRegularAutoAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.REGULAR, Category.AUTO, new BigDecimal("350000")));
    }

    @Test
    void isValidated_returnsFalse_forRegularAutoAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.REGULAR, Category.AUTO, new BigDecimal("350001")));
    }

    @Test
    void isValidated_returnsTrue_forRegularOtherAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.REGULAR, Category.TRAVEL, new BigDecimal("255000")));
    }

    @Test
    void isValidated_returnsFalse_forRegularOtherAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.REGULAR, Category.TRAVEL, new BigDecimal("255001")));
    }

    @Test
    void isValidated_returnsTrue_forHighRiskAutoAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.HIGH_RISK, Category.AUTO, new BigDecimal("250000")));
    }

    @Test
    void isValidated_returnsFalse_forHighRiskAutoAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.HIGH_RISK, Category.AUTO, new BigDecimal("250001")));
    }

    @Test
    void isValidated_returnsTrue_forHighRiskResidentialAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.HIGH_RISK, Category.RESIDENTIAL, new BigDecimal("150000")));
    }

    @Test
    void isValidated_returnsFalse_forHighRiskResidentialAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.HIGH_RISK, Category.RESIDENTIAL, new BigDecimal("150001")));
    }

    @Test
    void isValidated_returnsTrue_forHighRiskOtherAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.HIGH_RISK, Category.TRAVEL, new BigDecimal("125000")));
    }

    @Test
    void isValidated_returnsFalse_forHighRiskOtherAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.HIGH_RISK, Category.TRAVEL, new BigDecimal("125001")));
    }

    @Test
    void isValidated_returnsTrue_forPreferentialLifeBelowLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.PREFERENTIAL, Category.LIFE, new BigDecimal("799999")));
    }

    @Test
    void isValidated_returnsFalse_forPreferentialLifeAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.PREFERENTIAL, Category.LIFE, new BigDecimal("800000")));
    }

    @Test
    void isValidated_returnsTrue_forPreferentialAutoBelowLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.PREFERENTIAL, Category.AUTO, new BigDecimal("449999")));
    }

    @Test
    void isValidated_returnsFalse_forPreferentialAutoAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.PREFERENTIAL, Category.AUTO, new BigDecimal("450000")));
    }

    @Test
    void isValidated_returnsTrue_forPreferentialResidentialBelowLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.PREFERENTIAL, Category.RESIDENTIAL, new BigDecimal("449999")));
    }

    @Test
    void isValidated_returnsFalse_forPreferentialResidentialAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.PREFERENTIAL, Category.RESIDENTIAL, new BigDecimal("450000")));
    }

    @Test
    void isValidated_returnsTrue_forPreferentialOtherAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.PREFERENTIAL, Category.TRAVEL, new BigDecimal("375000")));
    }

    @Test
    void isValidated_returnsFalse_forPreferentialOtherAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.PREFERENTIAL, Category.TRAVEL, new BigDecimal("375001")));
    }

    @Test
    void isValidated_returnsTrue_forNoInfoLifeAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.NO_INFORMATION, Category.LIFE, new BigDecimal("200000")));
    }

    @Test
    void isValidated_returnsFalse_forNoInfoLifeAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.NO_INFORMATION, Category.LIFE, new BigDecimal("200001")));
    }

    @Test
    void isValidated_returnsTrue_forNoInfoResidentialAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.NO_INFORMATION, Category.RESIDENTIAL, new BigDecimal("200000")));
    }

    @Test
    void isValidated_returnsFalse_forNoInfoResidentialAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.NO_INFORMATION, Category.RESIDENTIAL, new BigDecimal("200001")));
    }

    @Test
    void isValidated_returnsTrue_forNoInfoAutoAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.NO_INFORMATION, Category.AUTO, new BigDecimal("75000")));
    }

    @Test
    void isValidated_returnsFalse_forNoInfoAutoAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.NO_INFORMATION, Category.AUTO, new BigDecimal("75001")));
    }

    @Test
    void isValidated_returnsTrue_forNoInfoOtherAtLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertTrue(fraudResult.isValidated(Classification.NO_INFORMATION, Category.TRAVEL, new BigDecimal("55000")));
    }

    @Test
    void isValidated_returnsFalse_forNoInfoOtherAboveLimit() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(Classification.NO_INFORMATION, Category.TRAVEL, new BigDecimal("55001")));
    }

    @Test
    void isValidated_returnsFalse_forUnknownClassification() {
        FraudResult fraudResult = new FraudResult();
        Assertions.assertFalse(fraudResult.isValidated(null, Category.LIFE, new BigDecimal("1")));
    }
}
