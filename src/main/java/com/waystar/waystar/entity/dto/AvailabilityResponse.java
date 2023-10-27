package com.waystar.waystar.entity.dto;

import com.waystar.waystar.entity.enums.BookingWindowTimeZone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    private Long provider;

    private Long location;

    private BookingWindowTimeZone timeZone;

    private Set<TimeDateInterval> timeDateIntervalSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvailabilityResponse that)) return false;
        return Objects.equals(getProvider(), that.getProvider()) && Objects.equals(getLocation(), that.getLocation()) && getTimeZone() == that.getTimeZone() && Objects.equals(getTimeDateIntervalSet(), that.getTimeDateIntervalSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProvider(), getLocation(), getTimeZone(), getTimeDateIntervalSet());
    }
}
