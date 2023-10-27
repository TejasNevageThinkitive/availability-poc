package com.waystar.waystar.repository;

import com.waystar.waystar.entity.AvailabilityByDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityByDateRepository extends JpaRepository<AvailabilityByDate, Long> {


//    List<AvailabilityByDate> findAllByLocation(Location location);

    List<AvailabilityByDate> findAllByLocationIdAndProviderId(Long location, Long provider);

    List<AvailabilityByDate> findAllByProviderIdAndLocationIdNot(Long provider, Long location);


}
