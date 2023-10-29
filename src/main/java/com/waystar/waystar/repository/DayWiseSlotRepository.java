package com.waystar.waystar.repository;

import com.waystar.waystar.entity.DayWiseSlotCreation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DayWiseSlotRepository extends JpaRepository<DayWiseSlotCreation,Long> {
    Optional<DayWiseSlotCreation> findByProviderIdAndDate(Long providerId, LocalDate slotDate);

    @Query(value = "select dws.* from day_wise_slot dws inner join availability a \n" +
            "on a.id = dws.availability_id where a.provider_id = :providerId and a.location_id = :locationId and dws.\"date\" = :date", nativeQuery = true)
    Optional<DayWiseSlotCreation> findDaySlotsByProviderIdLocationIdAndDateNative(@Param("providerId") Long providerId, @Param("locationId") Long locationId, @Param("date") LocalDate date);



}
