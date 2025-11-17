package com.shopifake.microservice.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Partial price update payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceRequest {

    @DecimalMin(value = "0.01", message = "amount must be positive")
    private BigDecimal amount;

    @Size(max = 3, message = "currency must be ISO code")
    private String currency;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
}


