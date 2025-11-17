package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.CreatePriceRequest;
import com.shopifake.microservice.dtos.UpdatePriceRequest;
import com.shopifake.microservice.entities.CurrencyCode;
import com.shopifake.microservice.entities.Price;
import com.shopifake.microservice.entities.PriceStatus;
import com.shopifake.microservice.repositories.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PriceService}.
 */
@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private PriceService priceService;

    private CreatePriceRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = CreatePriceRequest.builder()
                .productId(UUID.randomUUID())
                .amount(new BigDecimal("19.99"))
                .currency("usd")
                .build();
    }

    @Test
    @DisplayName("Should create price with normalized currency")
    void shouldCreatePrice() {
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> {
            Price price = invocation.getArgument(0);
            price.setId(UUID.randomUUID());
            price.setCreatedAt(LocalDateTime.now());
            price.setUpdatedAt(LocalDateTime.now());
            return price;
        });
        when(priceRepository.findFirstByProductIdAndStatus(createRequest.getProductId(), PriceStatus.ACTIVE))
                .thenReturn(Optional.empty());

        var response = priceService.createPrice(createRequest);

        assertThat(response.getCurrency()).isEqualTo(CurrencyCode.USD);
        ArgumentCaptor<Price> captor = ArgumentCaptor.forClass(Price.class);
        verify(priceRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PriceStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should reject unsupported currency")
    void shouldRejectUnsupportedCurrency() {
        createRequest.setCurrency("zzz");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> priceService.createPrice(createRequest));

        assertThat(exception.getMessage()).contains("Unsupported currency");
        verify(priceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update price amount and status")
    void shouldUpdatePrice() {
        UUID priceId = UUID.randomUUID();
        Price existing = Price.builder()
                .id(priceId)
                .productId(createRequest.getProductId())
                .amount(new BigDecimal("10.00"))
                .currency(CurrencyCode.USD)
                .effectiveFrom(LocalDateTime.now().minusDays(1))
                .status(PriceStatus.ACTIVE)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        when(priceRepository.findById(priceId)).thenReturn(Optional.of(existing));
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(priceRepository.findFirstByProductIdAndStatus(existing.getProductId(), PriceStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        UpdatePriceRequest request = UpdatePriceRequest.builder()
                .amount(new BigDecimal("12.00"))
                .build();

        var response = priceService.updatePrice(priceId, request);

        assertThat(response.getAmount()).isEqualByComparingTo("12.00");
        verify(priceRepository).save(existing);
    }
}


