package com.company.insurance_request.infrastructure.input.controller;

import com.company.insurance_request.application.service.PolicyService;
import com.company.insurance_request.domain.model.Policy;
import com.company.insurance_request.domain.model.PolicyTestFactory;
import com.company.insurance_request.infrastructure.adapter.input.controller.PolicyController;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyRequest;
import com.company.insurance_request.infrastructure.adapter.input.dto.PolicyResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PolicyControllerTest {

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private PolicyController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void create_shouldReturnCreatedAndBody_whenServiceReturnsPolicy() throws JsonProcessingException {
        UUID customerId = UUID.randomUUID();
        PolicyRequest request = new PolicyRequest(
                customerId,
                1L,
                null,
                null,
                List.of("24H_TOWING"),
                BigDecimal.valueOf(250.75),
                BigDecimal.valueOf(50000.0),
                null,
                null
        );

        Policy returned = PolicyTestFactory.createPolicy(customerId);

        when(policyService.create(any(PolicyRequest.class))).thenReturn(returned);

        ResponseEntity<PolicyResponse> response = controller.createPolice(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();

        ArgumentCaptor<PolicyRequest> captor = ArgumentCaptor.forClass(PolicyRequest.class);
        verify(policyService, times(1)).create(captor.capture());
        assertThat(captor.getValue().customerId()).isEqualTo(customerId);
    }

    @Test
    void postEndpoint_shouldReturn201_whenValidRequest() throws Exception {
        UUID customerId = UUID.randomUUID();

        Policy returned = PolicyTestFactory.createPolicy(customerId);

        when(policyService.create(any(PolicyRequest.class))).thenReturn(returned);

        String json = """
                {
                  "customer_id": "75b17569-6b06-417e-a329-054b6389424e",
                  "product_id": 2,
                  "category": "AUTO",
                  "coverages": [],
                  "assistances": ["24H_TOWING"],
                  "total_monthly_premium_amount": 100.0,
                  "insured_amount": 1000.0,
                  "payment_method": "DEBIT_CARD",
                  "sales_channel": "TELEFONE"
                }
                """.formatted(customerId);

        mockMvc.perform(post("/v1/policies")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated());

        verify(policyService, times(1)).create(any(PolicyRequest.class));
    }

    @Test
    void create_shouldReturnServerError_whenServiceThrows() throws Exception {
        UUID customerId = UUID.randomUUID();
        String json = """
                {
                  "customer_id": "%s",
                  "product_id": 2
                }
                """.formatted(customerId);

        when(policyService.create(any())).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/v1/policies")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().is5xxServerError());

        verify(policyService, times(1)).create(any());
    }
}
