package com.waystar.waystar.repository;

import com.waystar.waystar.entity.BlockDays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BlockDaysRepository extends JpaRepository<BlockDays,Long> {
    List<Object> findByProviderIdAndDate(Long providerId, LocalDate date);
}
