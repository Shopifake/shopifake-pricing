package com.shopifake.microservice.controllers;

import com.shopifake.microservice.dtos.CreatePriceRequest;
import com.shopifake.microservice.dtos.PriceResponse;
import com.shopifake.microservice.dtos.UpdatePriceRequest;
import com.shopifake.microservice.services.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST API for product pricing.
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "Pricing")
public class PriceController {

    private final PriceService priceService;

    @PostMapping
    @Operation(summary = "Create new price")
    public ResponseEntity<PriceResponse> createPrice(
            @Valid @RequestBody final CreatePriceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(priceService.createPrice(request));
    }

    @PatchMapping("/{priceId}")
    @Operation(summary = "Update price entry")
    public ResponseEntity<PriceResponse> updatePrice(
            @PathVariable final UUID priceId,
            @Valid @RequestBody final UpdatePriceRequest request) {
        return ResponseEntity.ok(priceService.updatePrice(priceId, request));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "List price history for product")
    public ResponseEntity<List<PriceResponse>> listPrices(
            @PathVariable final UUID productId) {
        return ResponseEntity.ok(priceService.listPrices(productId));
    }

    @GetMapping("/product/{productId}/active")
    @Operation(summary = "Get active price for product")
    public ResponseEntity<PriceResponse> getActivePrice(
            @PathVariable final UUID productId) {
        return ResponseEntity.ok(priceService.getActivePrice(productId));
    }
}


