package com.shopifake.microservice.repositories;

import com.shopifake.microservice.entities.Price;
import com.shopifake.microservice.entities.PriceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for price records.
 */
public interface PriceRepository extends JpaRepository<Price, UUID> {

    List<Price> findByProductIdOrderByEffectiveFromDesc(UUID productId);

    Optional<Price> findFirstByProductIdAndStatus(UUID productId, PriceStatus status);
}


