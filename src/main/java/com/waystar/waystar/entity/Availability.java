package com.waystar.waystar.entity;

import com.waystar.waystar.entity.enums.BookingWindow;
import com.waystar.waystar.entity.enums.BookingWindowTimeZone;
import com.waystar.waystar.entity.enums.SchedulingNoticeInputType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "availability")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "availability_id", referencedColumnName = "id")
    private Set<DayWiseAvailability> dayWiseAvailabilitySet;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "availability_id", referencedColumnName = "id")
    private Set<BlockDay> blockDaySet;

    private Long providerId;

    private Long locationId;

    private UUID uuid;

    private int initialConsultTime;

    private int followUpConsultTime;

    private int minScheduleNoticeInput;

    private SchedulingNoticeInputType schedulingNoticeInputType;

    private int eventBuffer;

    private int bookingWindow;

    private BookingWindowTimeZone bookingWindowTimeZone;

    private LocalDate bookingWindowStart;

    private LocalDate bookingWindowEnd;

    @PrePersist
    public void prePersist(){
        this.uuid = UUID.randomUUID();
    }

}
