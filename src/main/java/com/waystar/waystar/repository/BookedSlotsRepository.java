package com.waystar.waystar.repository;

import com.waystar.waystar.entity.BookedSlots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface BookedSlotsRepository extends JpaRepository<BookedSlots, Long> {

    Optional<BookedSlots> findByDateAndProviderIdAndLocationIdAndStartTimeAndEndTime(LocalDate date, Long providerId, Long locationId, LocalTime startTime, LocalTime endTime);

}
