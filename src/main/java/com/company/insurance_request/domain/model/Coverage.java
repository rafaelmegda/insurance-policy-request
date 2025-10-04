package com.company.insurance_request.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Coverage {

    @JsonProperty("roubo")
    private BigDecimal roubo;

    @JsonProperty("perda_total")
    private BigDecimal perdaTotal;

    @JsonProperty("colisao_com_terceiros")
    private BigDecimal colisaoComTerceiros;
}
