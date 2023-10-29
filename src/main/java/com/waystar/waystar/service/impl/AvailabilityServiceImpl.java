package com.waystar.waystar.service.impl;

import com.waystar.waystar.entity.Availability;
import com.waystar.waystar.entity.BookedSlots;
import com.waystar.waystar.entity.DayWiseSlotCreation;
import com.waystar.waystar.entity.dto.AppointmentSlotBookRequest;
import com.waystar.waystar.entity.dto.AvailabilityRequestResponse;
import com.waystar.waystar.entity.dto.AvailabilitySlots;
import com.waystar.waystar.entity.dto.SlotStatus;
import com.waystar.waystar.repository.AvailabilityRepository;
import com.waystar.waystar.repository.BlockDaysRepository;
import com.waystar.waystar.repository.BookedSlotRepository;
import com.waystar.waystar.repository.DayWiseSlotRepository;
import com.waystar.waystar.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    private final BlockDaysRepository blockDaysRepository;

    private final DayWiseSlotRepository dayWiseSlotRepository;

    private final BookedSlotRepository bookedSlotsRepository;

    private final ModelMapper modelMapper;

    @Override
    public void saveProviderAvailability(AvailabilityRequestResponse availabilityRequestResponse)  {

        Optional<Availability> alreadyPresentAvailability = availabilityRepository.findByProviderIdAndLocationId(availabilityRequestResponse.getProviderId(), availabilityRequestResponse.getLocationId());
        Availability availability2 = modelMapper.map(availabilityRequestResponse, Availability.class);
        if (alreadyPresentAvailability.isEmpty()) {

            Availability availabilityVersion21 = availabilityRepository.save(availability2);

            //Creating provider availabilities blocked days
            saveBlockedDays(availability2, availabilityVersion21.getId(), availabilityRequestResponse.getProviderId());

            //Creating Day Wise slots
            saveDaySlots(availability2, availabilityVersion21.getId(), availabilityRequestResponse.getProviderId());
        } else{
            Availability alreadyAvailability = alreadyPresentAvailability.get();

            //Creating provider availabilities blocked days
            saveBlockedDays(availability2, alreadyAvailability.getId(), availabilityRequestResponse.getProviderId());

            //Creating Day Wise slots
            saveDaySlots(availability2,alreadyAvailability.getId(), availabilityRequestResponse.getProviderId());
        }
    }


    private void saveBlockedDays(Availability availabilityVersion2, Long availabilityId, Long providerId) {
        availabilityVersion2.getBlockDays().forEach(blockDays -> {
            if (blockDaysRepository.findByProviderIdAndDate(providerId, blockDays.getDate()).isEmpty()) {
                blockDays.setAvailabilityId(availabilityId);
                blockDays.setProviderId(providerId);
                blockDaysRepository.save(blockDays);
            }
        });
    }

    private void saveDaySlots(Availability availabilityVersion2, Long availabilityId, Long providerId) {

        for (DayWiseSlotCreation daySlots : availabilityVersion2.getDaySlotCreations()) {
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(daySlots.getDay());
            LocalDate firstDate = LocalDate.now().with(TemporalAdjusters.next(dayOfWeek));
            LocalDate lastDate = firstDate.plusWeeks(availabilityVersion2.getBookingWindow());
            LocalDate slotDate = firstDate;
            Optional<DayWiseSlotCreation> alreadySlot = dayWiseSlotRepository.findByProviderIdAndDate(providerId, slotDate);

            if (alreadySlot.isEmpty()) {
                while (slotDate.isBefore(lastDate)) {
                    log.info("Date : {} and Day : {}", slotDate , slotDate.getDayOfWeek());
                    DayWiseSlotCreation dayWiseSlotCreation = new DayWiseSlotCreation();

                    dayWiseSlotCreation.setDay(slotDate.getDayOfWeek().toString());
                    dayWiseSlotCreation.setDate(slotDate);
                    dayWiseSlotCreation.setStartTime(daySlots.getStartTime());
                    dayWiseSlotCreation.setEndTime(daySlots.getEndTime());
                    dayWiseSlotCreation.setAvailabilityId(availabilityId);
                    dayWiseSlotCreation.setProviderId(providerId);
                    dayWiseSlotRepository.save(dayWiseSlotCreation);
                    slotDate = slotDate.plusWeeks(1);
                }
            }
        }
    }

    @Override
    public Page<AvailabilitySlots> getAvailabilitySlots(Long locationId, Long providerId, LocalDate date, int page, int size) {
        List<AvailabilitySlots> availabilitySlots = new ArrayList<>();

        Optional<Availability> existAvailability = availabilityRepository.findByProviderIdAndLocationId(providerId, locationId);
        Optional<DayWiseSlotCreation> existDaySlots = dayWiseSlotRepository.findDaySlotsByProviderIdLocationIdAndDateNative(providerId, locationId, date);

        if (existDaySlots.isPresent() && existAvailability.isPresent()) {
            DayWiseSlotCreation dayWiseSlotCreation = existDaySlots.get();
            Availability availability = existAvailability.get();

            LocalTime currentSlotTime = dayWiseSlotCreation.getStartTime();
            while (currentSlotTime.isBefore(dayWiseSlotCreation.getEndTime())) {

                if (bookedSlotsRepository.findByDateAndProviderIdAndLocationIdAndStartTimeAndEndTime(date, providerId, locationId, currentSlotTime, currentSlotTime.plusMinutes(availability.getInPersonInitialConsultTime())).isEmpty()) {
                    availabilitySlots.add(AvailabilitySlots.builder()
                            .duration(availability.getInPersonInitialConsultTime())
                            .date(date)
                            .status(SlotStatus.AVAILABLE.name())
                            .startTime(currentSlotTime)
                            .endTime(currentSlotTime.plusMinutes(availability.getInPersonInitialConsultTime()))
                            .providerId(providerId)
                            .build());
                }
                currentSlotTime = currentSlotTime.plusMinutes(availability.getInPersonInitialConsultTime()).plusMinutes(availability.getBufferTime());
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), availabilitySlots.size());

        return new PageImpl<>(availabilitySlots.subList(start, end), pageable, availabilitySlots.size());
    }

    @Override
    public BookedSlots bookSlots(AppointmentSlotBookRequest appointmentSlotBookRequest){
        return bookedSlotsRepository.save(BookedSlots.builder()
                .appointmentId(appointmentSlotBookRequest.getAppointmentId())
                .date(appointmentSlotBookRequest.getDate())
                .startTime(appointmentSlotBookRequest.getStartTime())
                .endTime(appointmentSlotBookRequest.getEndTime())
                .providerId(appointmentSlotBookRequest.getProviderId())
                .locationId(appointmentSlotBookRequest.getLocationId())
                .build());
    }

    @Override
    public DayWiseSlotCreation getProviderDaySlot(Long providerId, Long locationId, LocalDate date){
        return dayWiseSlotRepository.findDaySlotsByProviderIdLocationIdAndDateNative(providerId, locationId, date).orElse(null);
    }

    @Override
    public DayWiseSlotCreation updateDayWiseSlots(DayWiseSlotCreation dayWiseSlotCreation){
        DayWiseSlotCreation dayWiseSlotCreation2 =  modelMapper.map(dayWiseSlotCreation, DayWiseSlotCreation.class);
        return dayWiseSlotRepository.save(dayWiseSlotCreation2);
    }



}
