package com.shopifake.microservice.dtos;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Error body for pricing endpoints.
 */
@Value
@Builder
public class ErrorResponse {

    LocalDateTime timestamp;
    int status;
    String error;
    String message;
    String path;
}


