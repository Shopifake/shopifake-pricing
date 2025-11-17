package com.shopifake.microservice.dtos;

import com.shopifake.microservice.entities.CurrencyCode;
import com.shopifake.microservice.entities.PriceStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO returned for price reads.
 */
@Value
@Builder
public class PriceResponse {

    UUID id;

    UUID productId;

    BigDecimal amount;

    CurrencyCode currency;

    PriceStatus status;

    LocalDateTime effectiveFrom;

    LocalDateTime effectiveTo;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}


