package com.waystar.waystar.service.impl;

import com.waystar.waystar.entity.*;
import com.waystar.waystar.entity.dto.*;
import com.waystar.waystar.entity.enums.AvailabilityOperationType;
import com.waystar.waystar.repository.*;
import com.waystar.waystar.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository availabilityRepo;

    private final ModelMapper modelMapper;

    private final DayWiseAvailabilityRepository dayWiseAvailabilityRepo;

    private final BlockDayRepository blockDayRepo;

    private final AvailabilityByDateRepository availabilityByDateRepo;

    private final AvailabilityUtilityService availabilityUtilityService;

    private final BookedSlotsRepository bookedSlotsRepository;

    @Override
    public void addAvailability(AvailabilityDTO availability) {
        Availability existAvailabilityEntity = availabilityRepo.findByLocationIdAndProviderId(availability.getLocationId(), availability.getProviderId());

        if(Objects.nonNull(existAvailabilityEntity))
           return;

        Availability availabilityEntity = modelMapper.map(availability, Availability.class);

        Set<DayWiseAvailability> dayWiseAvailabilityEntitySet = availability.getDayWiseAvailabilitySet().stream()
                .map(dayWiseAvailability -> modelMapper.map(dayWiseAvailability, DayWiseAvailability.class))
                .collect(Collectors.toSet());

        Set<BlockDay> blockDayEntitySet = availability.getBlockDaySet().stream().map(blockDay -> modelMapper.map(blockDay, BlockDay.class)).collect(Collectors.toSet());
        availabilityEntity.setDayWiseAvailabilitySet(dayWiseAvailabilityEntitySet);
        availabilityEntity.setBlockDaySet(blockDayEntitySet);
        availabilityRepo.save(availabilityEntity);
    }

    @Override
    public Availability getByProviderAndLocation(Long locationId, Long providerId) {
        return availabilityRepo.findByLocationIdAndProviderId(locationId, providerId);
    }

    @Override
    public void editAvailability(AvailabilityDTO availability) {
        Availability availabilityEntity = availabilityRepo.findByLocationIdAndProviderId(availability.getLocationId(), availability.getProviderId());

        if (Objects.isNull(availabilityEntity))
            return;

//        setBookingWindowDates(availability, availabilityEntity);
        Set<DayWiseAvailability> dayWiseAvailabilityEntitySet = availability.getDayWiseAvailabilitySet().stream().map(dayWiseAvailability -> modelMapper.map(dayWiseAvailability, DayWiseAvailability.class)).collect(Collectors.toSet());
        Set<BlockDay> blockDayEntitySet = availability.getBlockDaySet().stream().map(blockDay -> modelMapper.map(blockDay, BlockDay.class)).collect(Collectors.toSet());
        availabilityEntity.setDayWiseAvailabilitySet(dayWiseAvailabilityEntitySet);
        availabilityEntity.setBlockDaySet(blockDayEntitySet);
        availabilityRepo.save(availabilityEntity);

        //remove all date wise availability for start and end date
        availabilityUtilityService.deleteInvalidDateWiseAvailabilities(availabilityEntity);
    }

//    @Override
//    public void addAvailabilityByDate(AvailabilityByDateDTO availability) {
//        List<AvailabilityByDate> availabilityByDateEntityList = availabilityByDateRepo.findAllByLocationIdAndProviderId(availability.getLocationEntity(), availability.getProviderEntity());
//        ArrayList<AvailabilityByDate> availableAvailabilityList = new ArrayList<>(availabilityByDateEntityList.stream().
//                filter(availabilityByDate -> (availabilityByDate.getAvailabilityOperationType().equals(AvailabilityOperationType.ADD) && availabilityByDate.getDate().equals(availability.getDate()))).toList());
//
//        ArrayList<AvailabilityByDate> disableAvailabilityList = new ArrayList<>(availabilityByDateEntityList.stream().
//                filter(availabilityByDate -> (availabilityByDate.getAvailabilityOperationType().equals(AvailabilityOperationType.REMOVE)&& availabilityByDate.getDate().equals(availability.getDate()))).toList());
//
//        //refactor stored date wise availability
//        switch (availability.getAvailabilityOperationType()) {
//            case ADD -> {
//                ArrayList<AvailabilityByDate> higherOrderList = availabilityUtilityService.mergeAndGetNewList(availableAvailabilityList, availability);
//                ArrayList<AvailabilityByDate> lowerOrderList = availabilityUtilityService.compareAndDivideIntervals(higherOrderList, disableAvailabilityList, availability, AvailabilityOperationType.REMOVE);
//                availabilityByDateRepo.deleteAll(availableAvailabilityList);
//                availabilityByDateRepo.deleteAll(disableAvailabilityList);
//                availabilityByDateRepo.saveAll(higherOrderList);
//                availabilityByDateRepo.saveAll(lowerOrderList);
//
//                Availability availabilityEntity = availabilityRepo.findByLocationIdAndProviderId(availability.getLocationEntity(), availability.getProviderEntity());
//                Set<BlockDay> newBlockDayEntitySet = availabilityUtilityService.createNewBlockDays(availabilityEntity.getBlockDaySet(), availability);
//                availabilityEntity.setBlockDaySet(newBlockDayEntitySet);
//                availabilityRepo.save(availabilityEntity);
//            }
//            case REMOVE -> {
//                ArrayList<AvailabilityByDate> higherOrderList = availabilityUtilityService.mergeAndGetNewList(disableAvailabilityList, availability);
//                ArrayList<AvailabilityByDate> lowerOrderList = availabilityUtilityService.compareAndDivideIntervals(higherOrderList, availableAvailabilityList, availability, AvailabilityOperationType.ADD);
//                availabilityByDateRepo.deleteAll(availableAvailabilityList);
//                availabilityByDateRepo.deleteAll(disableAvailabilityList);
//                availabilityByDateRepo.saveAll(higherOrderList);
//                availabilityByDateRepo.saveAll(lowerOrderList);
//            }
//        }
//    }

    @Override
    public Set<AvailabilityResponse> getAvailability(AvailabilityRequest availabilityRequest) {
        List<Long> providerEntityList = new ArrayList<>(availabilityRequest.getProviderUuidSet());
        List<Long> locationEntityList = new ArrayList<>(availabilityRequest.getLocationUuidSet());

        Set<AvailabilityResponse> availabilityResponseSet = new HashSet<>();
        for (Long providerEntity : providerEntityList) {
            for (Long locationEntity : locationEntityList) {
                Availability availabilityEntity = availabilityRepo.findByLocationIdAndProviderId(locationEntity, providerEntity);
                List<AvailabilityByDate> availabilityByDateEntity = availabilityByDateRepo.findAllByLocationIdAndProviderId(locationEntity, providerEntity);

                AvailabilityResponse availabilityResponse = new AvailabilityResponse();

                availabilityResponse.setProvider(providerEntity);
                availabilityResponse.setLocation(locationEntity);
                if(availabilityEntity != null) {
                    availabilityResponse.setTimeZone(availabilityEntity.getBookingWindowTimeZone());
                    Set<TimeDateInterval> timeDateIntervalSet = new HashSet<>();
                    LocalDate fromDate = availabilityUtilityService.getFromDate(availabilityRequest.getMonth(), availabilityRequest.getYear(), availabilityEntity);
                    LocalDate toDate = availabilityUtilityService.getToDate(fromDate);
                    while(!fromDate.isAfter(toDate)) {
                        AvailabilityUtilityService.TimeInterval currentTimeInterval = availabilityUtilityService.createTimeIntervalFromAvailabilityConfiguration(availabilityEntity, fromDate);
                        LocalDate compareDate = fromDate;
                        List<AvailabilityByDate> availabilityByDateEntityList = availabilityByDateRepo.findAllByLocationIdAndProviderId(locationEntity, providerEntity).stream().filter(availabilityByDate -> availabilityByDate.getDate().equals(compareDate)).toList();
                        List<AvailabilityUtilityService.TimeInterval> finalIntervals = availabilityUtilityService.mergeDateWiseAvailabilityWithCurrentInterval(availabilityByDateEntityList, currentTimeInterval);
                        for (AvailabilityUtilityService.TimeInterval timeInterval : finalIntervals) {
                            LocalDateTime currentTimeSlotStartTime = LocalDateTime.of(fromDate, timeInterval.startTime);
                            LocalDateTime currentTimeSlotEndTime = LocalDateTime.of(fromDate, timeInterval.endTime);

                            // Convert the LocalDateTime objects to the same time zone
                            LocalDateTime L1ConvertedStartTime = currentTimeSlotStartTime.atZone(ZoneId.of(availabilityEntity.getBookingWindowTimeZone().getValue())).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
                            LocalDateTime L1ConvertedEndTime = currentTimeSlotEndTime.atZone(ZoneId.of(availabilityEntity.getBookingWindowTimeZone().getValue())).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
                            timeDateIntervalSet.add(new TimeDateInterval(L1ConvertedStartTime, L1ConvertedEndTime));
                        }
                        //add them in timDateIntervalSet
                        fromDate = fromDate.plusDays(1);
                    }
                    availabilityResponse.setTimeDateIntervalSet(timeDateIntervalSet);
                }
                availabilityResponseSet.add(availabilityResponse);
            }
        }
        return availabilityResponseSet;
    }


    @Override
    public List<AvailabilitySlots> createAvailabilitySlots(Long locationId, Long providerId) {
        Availability availability = getByProviderAndLocation(locationId, providerId);
        List<AvailabilitySlots> availabilitySlots = new ArrayList<>();
        if (Objects.nonNull(availability)) {

            for (DayWiseAvailability daySlotCreation : availability.getDayWiseAvailabilitySet()) {
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(daySlotCreation.getDayOfWeek().toString());
                LocalDate firstDate = LocalDate.now().with(TemporalAdjusters.next(dayOfWeek));
                LocalDate lastDate = firstDate.plusWeeks(availability.getBookingWindow());
                LocalDate slotDate = firstDate;

                while (slotDate.isBefore(lastDate)) {
                    LocalTime currentSlotTime = daySlotCreation.getStartTime();
                    while (currentSlotTime.isBefore(daySlotCreation.getEndTime())) {

                        if (bookedSlotsRepository.findByDateAndProviderIdAndLocationIdAndStartTimeAndEndTime(slotDate, providerId, locationId, currentSlotTime, currentSlotTime.plusMinutes(availability.getInitialConsultTime())).isEmpty()) {
                            availabilitySlots.add(AvailabilitySlots.builder()
                                    .duration(availability.getInitialConsultTime())
                                    .date(slotDate)
                                    .status(SlotStatus.AVAILABLE.name())
                                    .startTime(currentSlotTime)
                                    .endTime(currentSlotTime.plusMinutes(availability.getInitialConsultTime()))
                                    .providerId(providerId)
                                    .build());
                        }
                        currentSlotTime = currentSlotTime.plusMinutes(availability.getInitialConsultTime()).plusMinutes(availability.getEventBuffer());
                    }
                    slotDate = slotDate.plusWeeks(1);
                }
            }
        }
        return availabilitySlots;
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











}
