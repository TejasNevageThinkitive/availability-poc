package com.waystar.waystar.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booked_slots")
public class BookedSlots {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID uuid;

    private LocalDate date;

    private Long providerId;

    private Long locationId;

    private LocalTime startTime;

    private LocalTime endTime;

    private Long appointmentId;

    @PrePersist
    public void prePersist(){
        uuid = UUID.randomUUID();
    }

}
