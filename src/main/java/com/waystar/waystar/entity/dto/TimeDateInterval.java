package com.waystar.waystar.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeDateInterval {

    public LocalDateTime from;

    public LocalDateTime to;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeDateInterval that = (TimeDateInterval) o;
        return (from.equals(that.from) && to.equals(that.to));
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
