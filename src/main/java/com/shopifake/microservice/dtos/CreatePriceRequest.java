package com.shopifake.microservice.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request body to set a price for a product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePriceRequest {

    @NotNull(message = "productId is required")
    private UUID productId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    @Size(max = 3, message = "currency must be ISO 4217 code")
    private String currency;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
}


