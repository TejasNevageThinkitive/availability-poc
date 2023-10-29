package com.waystar.waystar.service;

import com.waystar.waystar.entity.BookedSlots;
import com.waystar.waystar.entity.dto.AppointmentSlotBookRequest;
import com.waystar.waystar.entity.dto.AvailabilityRequestResponse;
import com.waystar.waystar.entity.dto.AvailabilitySlots;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityService {

    void saveProviderAvailability(AvailabilityRequestResponse availabilityRequestResponse);

    Page<AvailabilitySlots> getAvailabilitySlots(Long locationId, Long providerId, LocalDate date, int page, int size);

    BookedSlots bookSlots(AppointmentSlotBookRequest appointmentSlotBookRequest);
}
