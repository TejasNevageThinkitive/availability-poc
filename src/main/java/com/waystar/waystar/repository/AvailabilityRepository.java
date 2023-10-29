package com.waystar.waystar.repository;

import com.waystar.waystar.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    Optional<Availability> findByProviderIdAndLocationId(Long providerId, Long locationId);
}
