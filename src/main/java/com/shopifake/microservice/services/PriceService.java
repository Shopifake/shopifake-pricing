package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.CreatePriceRequest;
import com.shopifake.microservice.dtos.PriceResponse;
import com.shopifake.microservice.dtos.UpdatePriceRequest;
import com.shopifake.microservice.entities.CurrencyCode;
import com.shopifake.microservice.entities.Price;
import com.shopifake.microservice.entities.PriceStatus;
import com.shopifake.microservice.repositories.PriceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for pricing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService {

    private final PriceRepository priceRepository;
    private final Clock clock = Clock.systemUTC();

    /**
     * Create a new price entry.
     */
    @Transactional
    public PriceResponse createPrice(final CreatePriceRequest request) {
        CurrencyCode currency = parseCurrency(request.getCurrency());
        LocalDateTime effectiveFrom = request.getEffectiveFrom() != null
                ? request.getEffectiveFrom()
                : LocalDateTime.now(clock);
        validateEffectiveWindow(effectiveFrom, request.getEffectiveTo());

        Price price = Price.builder()
                .productId(request.getProductId())
                .amount(request.getAmount())
                .currency(currency)
                .effectiveFrom(effectiveFrom)
                .effectiveTo(request.getEffectiveTo())
                .status(resolveStatus(effectiveFrom, request.getEffectiveTo()))
                .build();

        Price saved = priceRepository.save(price);

        if (saved.getStatus() == PriceStatus.ACTIVE) {
            deactivateExistingActive(saved.getProductId(), saved.getId());
        }

        return mapToResponse(saved);
    }

    /**
     * Update an existing price.
     */
    @Transactional
    public PriceResponse updatePrice(final UUID priceId, final UpdatePriceRequest request) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new IllegalArgumentException("Price not found " + priceId));

        if (request.getAmount() != null) {
            price.setAmount(request.getAmount());
        }
        if (StringUtils.hasText(request.getCurrency())) {
            price.setCurrency(parseCurrency(request.getCurrency()));
        }
        LocalDateTime effectiveFrom = request.getEffectiveFrom() != null
                ? request.getEffectiveFrom()
                : price.getEffectiveFrom();
        LocalDateTime effectiveTo = request.getEffectiveTo() != null
                ? request.getEffectiveTo()
                : price.getEffectiveTo();
        validateEffectiveWindow(effectiveFrom, effectiveTo);
        price.setEffectiveFrom(effectiveFrom);
        price.setEffectiveTo(effectiveTo);
        price.setStatus(resolveStatus(effectiveFrom, effectiveTo));

        Price saved = priceRepository.save(price);
        if (saved.getStatus() == PriceStatus.ACTIVE) {
            deactivateExistingActive(saved.getProductId(), saved.getId());
        }

        return mapToResponse(saved);
    }

    /**
     * Get currently active price for a product.
     */
    public PriceResponse getActivePrice(final UUID productId) {
        Price price = priceRepository.findFirstByProductIdAndStatus(productId, PriceStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Active price not found for product " + productId));
        return mapToResponse(price);
    }

    /**
     * List price history for a product.
     */
    public List<PriceResponse> listPrices(final UUID productId) {
        return priceRepository.findByProductIdOrderByEffectiveFromDesc(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void deactivateExistingActive(final UUID productId, final UUID excludePriceId) {
        priceRepository.findFirstByProductIdAndStatus(productId, PriceStatus.ACTIVE)
                .filter(existing -> !existing.getId().equals(excludePriceId))
                .ifPresent(existing -> {
                    existing.setStatus(PriceStatus.EXPIRED);
                    existing.setEffectiveTo(LocalDateTime.now(clock));
                    priceRepository.save(existing);
                });
    }

    private CurrencyCode parseCurrency(final String currency) {
        try {
            return CurrencyCode.valueOf(currency.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }
    }

    private void validateEffectiveWindow(final LocalDateTime from, final LocalDateTime to) {
        if (to != null && !to.isAfter(from)) {
            throw new IllegalArgumentException("effectiveTo must be after effectiveFrom");
        }
    }

    private PriceStatus resolveStatus(final LocalDateTime from, final LocalDateTime to) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (from.isAfter(now)) {
            return PriceStatus.FUTURE;
        }
        if (to != null && !to.isAfter(now)) {
            return PriceStatus.EXPIRED;
        }
        return PriceStatus.ACTIVE;
    }

    private PriceResponse mapToResponse(final Price price) {
        return PriceResponse.builder()
                .id(price.getId())
                .productId(price.getProductId())
                .amount(price.getAmount())
                .currency(price.getCurrency())
                .status(price.getStatus())
                .effectiveFrom(price.getEffectiveFrom())
                .effectiveTo(price.getEffectiveTo())
                .createdAt(price.getCreatedAt())
                .updatedAt(price.getUpdatedAt())
                .build();
    }
}


