package com.company.insurance_request.domain.model;

import com.company.insurance_request.domain.model.enums.OccurrencesType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Occurrence {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("type")
    private OccurrencesType type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
