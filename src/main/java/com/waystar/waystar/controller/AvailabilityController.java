package com.waystar.waystar.controller;

import com.waystar.waystar.entity.Availability;
import com.waystar.waystar.entity.BookedSlots;
import com.waystar.waystar.entity.dto.*;
import com.waystar.waystar.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/provider/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping
    public String save(@RequestBody @Valid AvailabilityDTO availability) {
        availabilityService.addAvailability(availability);
        return "Availability added successfully !";
    }

    @GetMapping("/config")
    public Availability getByLocationUuid(@RequestParam Long providerUuid, @RequestParam Long locationUuid)  {
        return availabilityService.getByProviderAndLocation(providerUuid, locationUuid);
//        return data(ResponseCode.OK, "Data fetched successfully!", availabilityService.getByProviderAndLocation(providerUuid, locationUuid));
    }

    @PutMapping()
    public String editAvailability(@RequestBody @Valid AvailabilityDTO availability) {
        availabilityService.editAvailability(availability);
        return "Availability updated successfully !";
    }

//    @PostMapping("/by_date")
//    public void addAvailabilityByDate(@RequestBody @Valid AvailabilityByDateDTO availability){
//        availabilityService.addAvailabilityByDate(availability);
////        return success(ResponseCode.CREATED, "Successfully Added availability for given date");
//    }

    @GetMapping
    public Set<AvailabilityResponse> getAvailabilities(@RequestBody AvailabilityRequest availabilityRequest){
        return availabilityService.getAvailability(availabilityRequest);
//        return data(availabilityService.getAvailability(availabilityRequest));
    }

    @GetMapping("/slots")
    public List<AvailabilitySlots> getProviderAvailabilitySlots(@RequestParam Long providerId, @RequestParam Long locationId){
        return availabilityService.createAvailabilitySlots(locationId, providerId);
    }

    @PostMapping("/slots/book")
    public BookedSlots bookAppointmentSlot(@RequestBody AppointmentSlotBookRequest appointmentSlotBookRequest) {
        return availabilityService.bookSlots(appointmentSlotBookRequest);
    }

}

