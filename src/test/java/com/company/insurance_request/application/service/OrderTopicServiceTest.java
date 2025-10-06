package com.company.insurance_request.application.service;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.enums.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTopicServiceTest {

    @Mock
    private FraudService fraudService;

    @InjectMocks
    private OrderTopicService orderTopicService;

    @Test
    void processMessageOrder_whenStatusIsReceived_callsFraudService() throws JsonProcessingException {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.RECEIVED);

        orderTopicService.processMessageOrder(event);

        verify(fraudService).processFraud(event);
    }

    @Test
    void processMessageOrder_whenStatusIsNotReceived_doesNotCallFraudService() throws JsonProcessingException {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.status()).thenReturn(Status.APPROVED);

        orderTopicService.processMessageOrder(event);

        verifyNoInteractions(fraudService);
    }
}
