package com.waystar.waystar.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "block_days")
public class BlockDays implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uuid;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private Long availabilityId;

    private Long providerId;

    @PrePersist
    public void prePersist() {
        uuid = UUID.randomUUID();
    }

}
