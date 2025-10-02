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

    //TODO talvez não precise tipar, senão se inserirem novas ocorrencias, teria que validar sempre aqui
    // Pensar que o sistema é para multiplas corretoras e cada uma pode ter suas proprias ocorrencias
    @JsonProperty("type")
    private OccurrencesType type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
