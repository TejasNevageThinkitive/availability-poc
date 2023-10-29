package com.waystar.waystar.controller;


import com.waystar.waystar.entity.BookedSlots;
import com.waystar.waystar.entity.dto.AppointmentSlotBookRequest;
import com.waystar.waystar.entity.dto.AvailabilityRequestResponse;
import com.waystar.waystar.entity.dto.AvailabilitySlots;
import com.waystar.waystar.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping("/provider/availability")
    public Map<String, Long> createSlots(@RequestBody AvailabilityRequestResponse availabilityRequestResponse)  {
        availabilityService.saveProviderAvailability(availabilityRequestResponse);
        return Map.of("Slot has been created successfully for provider : ", availabilityRequestResponse.getProviderId());
    }

    @GetMapping("/provider/{providerId}/location/{locationId}/availability/{date}")
    public ResponseEntity<Page<AvailabilitySlots>> getProviderAvailability(@PathVariable Long providerId, @PathVariable Long locationId, @PathVariable LocalDate date,
                                                                           @RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok().body(availabilityService.getAvailabilitySlots(locationId, providerId, date, page-1, size));
    }

    @PostMapping("/book/slot")
    public BookedSlots bookAppointmentSlot(@RequestBody AppointmentSlotBookRequest appointmentSlotBookRequest) {
        return availabilityService.bookSlots(appointmentSlotBookRequest);
    }


}
