package com.company.insurance_request.infrastructure.adapter.input.dto;

public record CreatePoliceResponse (Long id, java.time.LocalDateTime LocalDateTime) {
}

//todo entender o record
//TODO implementar o response no service e no controller