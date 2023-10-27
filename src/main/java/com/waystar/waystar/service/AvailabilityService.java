package com.waystar.waystar.service;

import com.waystar.waystar.entity.Availability;
import com.waystar.waystar.entity.BookedSlots;
import com.waystar.waystar.entity.dto.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AvailabilityService {

    void addAvailability(AvailabilityDTO availability) ;

    Availability getByProviderAndLocation(Long providerId, Long locationId);

    void editAvailability(AvailabilityDTO availability);

//    void addAvailabilityByDate(AvailabilityByDateDTO availability);

    Set<AvailabilityResponse> getAvailability(AvailabilityRequest availabilityRequest);


    List<AvailabilitySlots> createAvailabilitySlots(Long locationId, Long providerId);

    BookedSlots bookSlots(AppointmentSlotBookRequest appointmentSlotBookRequest);
}
