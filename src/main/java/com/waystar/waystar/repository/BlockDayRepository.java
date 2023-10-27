package com.waystar.waystar.repository;

import com.waystar.waystar.entity.BlockDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockDayRepository extends JpaRepository<BlockDay, Long> {
}
