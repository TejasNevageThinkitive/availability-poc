package com.waystar.waystar.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "availability")
public class Availability implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private UUID uuid;

    @NotNull
    private int bookingWindow;

    @NotNull
    private String timezone;

    private String availabilitySettingType;

    private int inPersonInitialConsultTime;

    private Long inPersonFollowupConsultTime;

    private int bufferTime;

    private Long inPersonBookingIntervalTime;

    private Long providerId;

    private Long locationId;

    @Transient
    private List<BlockDays> blockDays;

    @Transient
    private List<DayWiseSlotCreation> daySlotCreations;

    @PrePersist
    public void prePersist(){
        uuid = UUID.randomUUID();
    }




}
