package com.company.insurance_request.infrastructure.adapter.output.http;

import com.company.insurance_request.domain.event.OrderTopicEvent;
import com.company.insurance_request.domain.model.Fraud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FraudClient fraudClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fraudClient, "baseUrl", "http://fraud-api");
        ReflectionTestUtils.setField(fraudClient, "path", "/fraud-check/");
    }

    @Test
    void validate_returnsFraud_whenApiReturnsFraud() {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.customerId()).thenReturn(UUID.fromString("75b17569-6b06-417e-a329-054b6389424e"));
        Fraud fraud = mock(Fraud.class);
        when(restTemplate.getForObject("http://fraud-api/fraud-check/75b17569-6b06-417e-a329-054b6389424e", Fraud.class)).thenReturn(fraud);

        Fraud result = fraudClient.validate(event);

        assertThat(result).isEqualTo(fraud);
    }

    @Test
    void validate_returnsNull_whenApiThrowsException() {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.customerId()).thenReturn(UUID.fromString("75b17569-6b06-417e-a329-054b6389424e"));
        when(restTemplate.getForObject("http://fraud-api/fraud-check/75b17569-6b06-417e-a329-054b6389424e", Fraud.class))
                .thenThrow(new RuntimeException("API error"));

        Fraud result = fraudClient.validate(event);

        assertThat(result).isNull();
    }

    @Test
    void validate_returnsNull_whenEventCustomerIdIsNull() {
        OrderTopicEvent event = mock(OrderTopicEvent.class);
        when(event.customerId()).thenReturn(null);
        when(restTemplate.getForObject("http://fraud-api/fraud-check/null", Fraud.class))
                .thenThrow(new RuntimeException("API error"));

        Fraud result = fraudClient.validate(event);

        assertThat(result).isNull();
    }
}
