package com.waystar.waystar.repository;

import com.waystar.waystar.entity.BlockDays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BlockDaysRepository extends JpaRepository<BlockDays,Long> {
    List<Object> findByProviderIdAndDate(Long providerId, LocalDate date);

    @Query(value = "select * from block_days bd where :startTime > bd.start_time and :startTime < bd.end_time or :endTime > bd.start_time and :endTime < bd.end_time and provider_id = :providerId", nativeQuery = true)
    List<BlockDays> existByStartTimeEndTimeAndProvider(@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime, @Param("providerId") Long providerId);


}
