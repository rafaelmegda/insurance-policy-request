package com.company.insurance_request.infrastructure.adapter.execption;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class GlobalError {
    LocalDateTime timestamp;
    int status;
    String error;
    String message;
    String path;

    public static GlobalError of(int status, String error, String message, String path){
        return GlobalError.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}
