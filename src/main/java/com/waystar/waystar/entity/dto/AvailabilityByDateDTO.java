package com.waystar.waystar.entity.dto;


import com.waystar.waystar.entity.enums.AvailabilityOperationType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@ValidateAvailabilityByDate
public class AvailabilityByDateDTO {

    @NotNull
    private UUID locationUuid;

    @NotNull
    private AvailabilityOperationType availabilityOperationType;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @Transient
    private Long locationEntity;

    @Transient
    private Long providerEntity;
}
