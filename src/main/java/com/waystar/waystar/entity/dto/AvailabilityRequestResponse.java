package com.waystar.waystar.entity.dto;

import com.waystar.waystar.entity.BlockDays;
import com.waystar.waystar.entity.DayWiseSlotCreation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AvailabilityRequestResponse {

    private Long id;

    @NotNull
    private int bookingWindow;

    @NotNull
    private String timezone;

    @NotNull
    private String availabilitySettingType;


    private int inPersonInitialConsultTime;

    private Long inPersonFollowupConsultTime;

    private int bufferTime;

    private Long inPersonBookingIntervalTime;

    @NotNull
    private Long providerId;

    @NotNull
    private Long locationId;

    private List<BlockDays> blockDays;

    private List<AvailabilitySlots> availabilitySlots;

    private List<DayWiseSlotCreation> daySlotCreations;


}
