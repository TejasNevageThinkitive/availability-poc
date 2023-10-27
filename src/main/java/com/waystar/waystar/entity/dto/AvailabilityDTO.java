package com.waystar.waystar.entity.dto;

import com.waystar.waystar.entity.BlockDay;
import com.waystar.waystar.entity.DayWiseAvailability;
import com.waystar.waystar.entity.enums.BookingWindow;
import com.waystar.waystar.entity.enums.BookingWindowTimeZone;
import com.waystar.waystar.entity.enums.SchedulingNoticeInputType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@ValidateAvailability
public class AvailabilityDTO {

    private Long id;

    @NotNull
    private Set<DayWiseAvailability> dayWiseAvailabilitySet;

    private Set<BlockDay> blockDaySet;

    private Integer initialConsultTime;

    private Integer followUpConsultTime;

    private Integer minScheduleNoticeInput;

    private SchedulingNoticeInputType schedulingNoticeInputType;

    private int eventBuffer;

    @NotNull
    private int bookingWindow;

    @NotNull
    private BookingWindowTimeZone bookingWindowTimeZone;

    @NotNull
    private Long providerId;

    @NotNull
    private Long locationId;

}
