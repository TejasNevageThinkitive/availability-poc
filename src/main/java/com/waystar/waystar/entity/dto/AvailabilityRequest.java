package com.waystar.waystar.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Month;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityRequest {

    private Set<Long> providerUuidSet;

    private Set<Long> locationUuidSet;

    private Month month;

    private int year;
}
