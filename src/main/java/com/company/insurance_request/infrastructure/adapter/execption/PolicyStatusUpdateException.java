package com.company.insurance_request.infrastructure.adapter.execption;

public class PolicyStatusUpdateException extends RuntimeException{
    public PolicyStatusUpdateException(String message) {
        super(message);
    }
}
