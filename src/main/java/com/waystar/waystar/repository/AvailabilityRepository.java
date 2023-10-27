package com.waystar.waystar.repository;

import com.waystar.waystar.entity.Availability;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Availability entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long>, JpaSpecificationExecutor<Availability> {

    Optional<Availability> findByProviderId(Long Provider);

    Optional<Availability> findByLocationId(Long location);

    List<Availability> findAllByProviderIdAndLocationIdNot(Long provider, Long location);

    Availability findByLocationIdAndProviderId(Long locationEntity, Long providerEntity);

    Optional<Availability> findByUuid(UUID uuid);

    List<Availability> findAllByProviderId(Long providerEntity);




}
