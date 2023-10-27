package com.waystar.waystar.repository;

import com.waystar.waystar.entity.DayWiseAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayWiseAvailabilityRepository extends JpaRepository<DayWiseAvailability, Long> {
}
