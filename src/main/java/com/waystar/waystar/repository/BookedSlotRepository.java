package com.waystar.waystar.repository;

import com.waystar.waystar.entity.BookedSlots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookedSlotRepository extends JpaRepository<BookedSlots,Long> {

    List<Object> findByDateAndProviderIdAndLocationIdAndStartTimeAndEndTime(LocalDate date, Long providerId, Long locationId, LocalTime currentSlotTime, LocalTime localTime);

}
