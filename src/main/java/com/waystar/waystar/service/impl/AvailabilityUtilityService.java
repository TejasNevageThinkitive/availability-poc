package com.waystar.waystar.service.impl;

import com.waystar.waystar.entity.Availability;
import com.waystar.waystar.entity.AvailabilityByDate;
import com.waystar.waystar.entity.BlockDay;
import com.waystar.waystar.entity.DayWiseAvailability;
import com.waystar.waystar.entity.dto.AvailabilityByDateDTO;
import com.waystar.waystar.entity.enums.AvailabilityOperationType;
import com.waystar.waystar.repository.AvailabilityByDateRepository;
import com.waystar.waystar.repository.AvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class AvailabilityUtilityService {

    @Autowired
    private AvailabilityByDateRepository availabilityByDateRepo;
    @Autowired
    private AvailabilityRepository availabilityRepo;

    public class TimeInterval {
        public LocalTime startTime;
        public LocalTime endTime;
        public TimeInterval(LocalTime startTime, LocalTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public TimeInterval() { }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeInterval that = (TimeInterval) o;
            return (startTime.equals(that.startTime) && endTime.equals(that.endTime));
        }

        @Override
        public int hashCode() {
            return Objects.hash(startTime, endTime);
        }
    }

    private List<TimeInterval> removeTimeBlocks(TimeInterval originalBlock, List<TimeInterval> blocksToRemove) {
        List<TimeInterval> remainingBlocks = new ArrayList<>();
        remainingBlocks.add(originalBlock);
        for (TimeInterval blockToRemove : blocksToRemove) {
            List<TimeInterval> newRemainingBlocks = new ArrayList<>();
            for (TimeInterval currentBlock : remainingBlocks) {
                newRemainingBlocks.addAll(removeTimeBlock(currentBlock, blockToRemove));
            }
            remainingBlocks = newRemainingBlocks;
        }
        return remainingBlocks;
    }

    private List<TimeInterval> removeTimeBlock(TimeInterval originalBlock, TimeInterval blockToRemove) {
        List<TimeInterval> remainingBlocks = new ArrayList<>();
        // Check if the original block is entirely before or after the block to remove.
        if (originalBlock.endTime.isBefore(blockToRemove.startTime) || originalBlock.startTime.isAfter(blockToRemove.endTime)) {
            remainingBlocks.add(originalBlock);
        } else {
            // Split the original block into two parts.
            if (originalBlock.startTime.isBefore(blockToRemove.startTime)) {
                remainingBlocks.add(new TimeInterval(originalBlock.startTime, blockToRemove.startTime));
            }
            if (originalBlock.endTime.isAfter(blockToRemove.endTime)) {
                remainingBlocks.add(new TimeInterval(blockToRemove.endTime, originalBlock.endTime));
            }
        }
        return remainingBlocks;
    }

    // Method to check if two TimeInterval objects overlap
    public static boolean isOverlap(TimeInterval interval1, TimeInterval interval2) {
        return (interval1.startTime.isBefore(interval2.endTime) || interval1.startTime.equals(interval2.endTime))
            && (interval2.startTime.isBefore(interval1.endTime) || interval2.startTime.equals(interval1.endTime));
    }

    // Method to merge two TimeInterval objects
    private TimeInterval mergeIntervals(TimeInterval interval1, TimeInterval interval2) {
        return new TimeInterval(
            interval1.startTime.isBefore(interval2.startTime) ? interval1.startTime : interval2.startTime,
            interval1.endTime.isAfter(interval2.endTime) ? interval1.endTime : interval2.endTime
        );
    }

    // Method to add a new TimeInterval and merge overlapping intervals
    public List<TimeInterval> addAndMergeIntervals(List<TimeInterval> intervals, TimeInterval newInterval) {
        if(newInterval != null) intervals.add(newInterval);
        List<TimeInterval> mergedIntervals = new ArrayList<>();
        intervals.sort(Comparator.comparing(interval -> interval.startTime));
        if(intervals.size()>0) {
            TimeInterval currentInterval = intervals.get(0);
            for (int i = 1; i < intervals.size(); i++) {
                TimeInterval nextInterval = intervals.get(i);

                if (currentInterval.endTime.compareTo(nextInterval.startTime) >= 0) {
                    // Overlapping intervals, merge them
                    currentInterval.endTime = nextInterval.endTime.compareTo(currentInterval.endTime) >= 0 ? nextInterval.endTime : currentInterval.endTime;
                } else {
                    // Non-overlapping intervals, add the current interval to the result list
                    mergedIntervals.add(currentInterval);
                    currentInterval = nextInterval;
                }
            }
            // Add the last interval after the loop
            mergedIntervals.add(currentInterval);
        }
        return mergedIntervals;
    }

    private List<TimeInterval> divideOverlappingIntervals(TimeInterval interval1, TimeInterval interval2) {
        List<TimeInterval> dividedIntervals = new ArrayList<>();
        if (interval1.startTime.isBefore(interval2.startTime)) {
            dividedIntervals.add(new TimeInterval(interval1.startTime, interval2.startTime));
        }
        if (interval1.endTime.isAfter(interval2.endTime)) {
            dividedIntervals.add(new TimeInterval(interval2.endTime, interval1.endTime));
        }
        return dividedIntervals;
    }

    // Method to compare and handle overlapping intervals
    private List<TimeInterval> compareAndDivideIntervals(List<TimeInterval> higherOrderIntervals, List<TimeInterval> lowerOrderIntervals) {
        for (TimeInterval higherOrderInterval : higherOrderIntervals) {
            List<TimeInterval> tempIntervals = new ArrayList<>();
            for (TimeInterval lowerOrderInterval : lowerOrderIntervals) {
                if (isOverlap(lowerOrderInterval, higherOrderInterval)) {
                    tempIntervals.addAll(divideOverlappingIntervals(lowerOrderInterval, higherOrderInterval));
                } else {
                    tempIntervals.add(lowerOrderInterval);
                }
            }
            lowerOrderIntervals = tempIntervals;
        }

        return lowerOrderIntervals;
    }

    public ArrayList<AvailabilityByDate> compareAndDivideIntervals(ArrayList<AvailabilityByDate> newAvailabilities, ArrayList<AvailabilityByDate> removedAvailabilities, AvailabilityByDateDTO availabilityByDate, AvailabilityOperationType operationType) {
        List<TimeInterval> higherOrderList = new ArrayList<>();
        List<TimeInterval> lowerOrderList =  new ArrayList<>();
        for (AvailabilityByDate availabilityByDateEntity : newAvailabilities) higherOrderList.add(new TimeInterval(availabilityByDateEntity.getStartTime(), availabilityByDateEntity.getEndTime()));
        for (AvailabilityByDate availabilityByDateEntity : removedAvailabilities) lowerOrderList.add(new TimeInterval(availabilityByDateEntity.getStartTime(), availabilityByDateEntity.getEndTime()));

        List<TimeInterval> finalIntervals = compareAndDivideIntervals(higherOrderList, lowerOrderList);
        ArrayList<AvailabilityByDate> availabilityByDateEntityArrayList = new ArrayList<>();
        for (TimeInterval interval : finalIntervals) {
            mapToAvailabilityByEntity(availabilityByDate, availabilityByDateEntityArrayList, interval, operationType);
        }
        return availabilityByDateEntityArrayList;
    }

    public ArrayList<AvailabilityByDate> mergeAndGetNewList(List<AvailabilityByDate> availabilityByDateEntityList, AvailabilityByDateDTO availabilityByDate) {
        List<TimeInterval> timeIntervalList = new ArrayList<>();
        for (AvailabilityByDate availabilityByDateEntity : availabilityByDateEntityList) {
            timeIntervalList.add(new TimeInterval(availabilityByDateEntity.getStartTime(), availabilityByDateEntity.getEndTime()));
        }
        TimeInterval currentTimeInterval = new TimeInterval(availabilityByDate.getStartTime(), availabilityByDate.getEndTime());
        timeIntervalList = addAndMergeIntervals(timeIntervalList, currentTimeInterval);

        ArrayList<AvailabilityByDate> availabilityByDateEntityArrayList = new ArrayList<>();
        for (TimeInterval interval : timeIntervalList) {
            mapToAvailabilityByEntity(availabilityByDate, availabilityByDateEntityArrayList, interval, availabilityByDate.getAvailabilityOperationType());
        }
        return availabilityByDateEntityArrayList;
    }

    private static void mapToAvailabilityByEntity(AvailabilityByDateDTO availabilityByDate, ArrayList<AvailabilityByDate> availabilityByDateEntityArrayList, TimeInterval interval, AvailabilityOperationType operationType) {
        AvailabilityByDate availabilityByDateEntity = new AvailabilityByDate();
        availabilityByDateEntity.setLocationId(availabilityByDate.getLocationEntity());
        availabilityByDateEntity.setProviderId(availabilityByDate.getProviderEntity());
        availabilityByDateEntity.setDate(availabilityByDate.getDate());
        availabilityByDateEntity.setAvailabilityOperationType(operationType);
        availabilityByDateEntity.setStartTime(interval.startTime);
        availabilityByDateEntity.setEndTime(interval.endTime);
        availabilityByDateEntityArrayList.add(availabilityByDateEntity);
    }


    // create method that validates date wise validations with all the other locations
    public boolean validateDateWiseAvailabilitiesWithOtherLocations(Availability currentTimeConfig, AvailabilityByDate currentTimeSlot) {
        List<AvailabilityByDate> availabilityByDateEntityList = availabilityByDateRepo.findAllByProviderIdAndLocationIdNot(currentTimeConfig.getProviderId(), currentTimeConfig.getLocationId());
        boolean isValid = true;
        for(AvailabilityByDate l1: availabilityByDateEntityList) {
            Availability g1 = availabilityRepo.findByLocationIdAndProviderId(l1.getLocationId(), l1.getProviderId());
            //convert time zones
            LocalDateTime currentTimeSlotStartTime = LocalDateTime.of(currentTimeSlot.getDate(), currentTimeSlot.getStartTime());
            LocalDateTime currentTimeSlotEndTime = LocalDateTime.of(currentTimeSlot.getDate(), currentTimeSlot.getEndTime());

            // Convert the LocalDateTime objects to the same time zone
            LocalDateTime L1ConvertedStartTime = currentTimeSlotStartTime.atZone(ZoneId.of(currentTimeConfig.getBookingWindowTimeZone().getValue())).withZoneSameInstant(ZoneId.of(g1.getBookingWindowTimeZone().getValue())).toLocalDateTime();
            LocalDateTime L1ConvertedEndTime = currentTimeSlotEndTime.atZone(ZoneId.of(currentTimeConfig.getBookingWindowTimeZone().getValue())).withZoneSameInstant(ZoneId.of(g1.getBookingWindowTimeZone().getValue())).toLocalDateTime();
            LocalDateTime L2ConvertedStartTime = LocalDateTime.of(l1.getDate(), l1.getStartTime());
            LocalDateTime L2ConvertedEndTime = LocalDateTime.of(l1.getDate(), l1.getEndTime());

            if(L1ConvertedStartTime.isBefore(L2ConvertedEndTime) && L1ConvertedEndTime.isAfter(L2ConvertedStartTime)) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    // create methods that validates with daywise validation with all other locations
    public boolean validateDayWiseAvailabilitiesWithOtherLocations(Availability currentTimeConfig, AvailabilityByDate currentTimeSlot) {
        boolean isValid = true;
        List<Availability> availabilityEntityList = availabilityRepo.findAllByProviderIdAndLocationIdNot(currentTimeConfig.getProviderId(), currentTimeConfig.getLocationId());

        for (Availability g1 : availabilityEntityList) {
            //convert time zones
            LocalDateTime currentTimeSlotStartTime = LocalDateTime.of(currentTimeSlot.getDate(), currentTimeSlot.getStartTime());
            LocalDateTime currentTimeSlotEndTime = LocalDateTime.of(currentTimeSlot.getDate(), currentTimeSlot.getEndTime());

            LocalDateTime L1ConvertedStartTime = currentTimeSlotStartTime.atZone(ZoneId.of(currentTimeConfig.getBookingWindowTimeZone().getValue())).withZoneSameInstant(ZoneId.of(g1.getBookingWindowTimeZone().getValue())).toLocalDateTime();
            LocalDateTime L1ConvertedEndTime = currentTimeSlotEndTime.atZone(ZoneId.of(currentTimeConfig.getBookingWindowTimeZone().getValue())).withZoneSameInstant(ZoneId.of(g1.getBookingWindowTimeZone().getValue())).toLocalDateTime();

            ArrayList<LocalDate> currentLocalDates = new ArrayList<>();
            currentLocalDates.add(L1ConvertedStartTime.toLocalDate());
            currentLocalDates.add(L1ConvertedEndTime.toLocalDate());
            //check if this dates falls in start and end date of g1 and has day wise for both day of week time
            boolean checkDayIsPresent = false;
            for(LocalDate localDate: currentLocalDates) {
                if(localDate.isAfter(g1.getBookingWindowStart()) && (localDate.isBefore(g1.getBookingWindowEnd()) || localDate.isEqual(g1.getBookingWindowEnd()))) {
                    Optional<DayWiseAvailability> dayWiseAvailabilityEntityOptional = g1.getDayWiseAvailabilitySet().stream().filter(day -> day.getDayOfWeek().equals(localDate.getDayOfWeek())).findAny();
                    if(dayWiseAvailabilityEntityOptional.isPresent()) {
                        checkDayIsPresent = true;
                        break;
                    }
                }
            }
            if(!checkDayIsPresent) continue;

            //if yes then check if both comes in block days array
            int blockDayCount = 0;
            for (LocalDate localDate: currentLocalDates) {
                for (BlockDay blockDayEntity : g1.getBlockDaySet()) {
                    if((localDate.isAfter(blockDayEntity.getFromDate()) || localDate.isEqual(blockDayEntity.getFromDate())) && (localDate.isBefore(blockDayEntity.getToDate()) || localDate.isEqual(blockDayEntity.getToDate()))) {
                        blockDayCount++;
                        break;
                    }
                }
            }
            if(blockDayCount == 2) continue;

            //if no then create loop for this 2 days and pick date check with both the day by removing block hours
            for (LocalDate localDate : currentLocalDates) {
                DayWiseAvailability dayWiseAvailabilityEntity = g1.getDayWiseAvailabilitySet().stream().filter(day -> localDate.getDayOfWeek().equals(day.getDayOfWeek())).findFirst().orElseThrow();
                TimeInterval originalBlock = new TimeInterval(dayWiseAvailabilityEntity.getStartTime(), dayWiseAvailabilityEntity.getEndTime());
                List<TimeInterval> blocksToRemove = new ArrayList<>();

                List<AvailabilityByDate> availabilityByDateEntityList = availabilityByDateRepo.findAllByLocationIdAndProviderId(g1.getLocationId(), g1.getProviderId()).stream().filter(availabilityByDate -> availabilityByDate.getAvailabilityOperationType().equals(AvailabilityOperationType.REMOVE)).toList();
                for (AvailabilityByDate availabilityByDate : availabilityByDateEntityList) {
                    blocksToRemove.add(new TimeInterval(availabilityByDate.getStartTime(), availabilityByDate.getEndTime()));
                }

                List<TimeInterval> remainingBlocks = removeTimeBlocks(originalBlock, blocksToRemove);

                for (TimeInterval unAvailableSlot : remainingBlocks) {
                    LocalDateTime L2ConvertedStartTime = LocalDateTime.of(localDate, unAvailableSlot.startTime);
                    LocalDateTime L2ConvertedEndTime = LocalDateTime.of(localDate, unAvailableSlot.endTime);

                    if(L1ConvertedStartTime.isBefore(L2ConvertedEndTime) && L1ConvertedEndTime.isAfter(L2ConvertedStartTime)) {
                        isValid = false;
                        break;
                    }
                }
                if(!isValid) break;
            }
            if(!isValid) break;
        }
        return isValid;
    }

    private boolean isOverlapDays(BlockDay blockDayEntity, LocalDate localDate) {
        return (localDate.isAfter(blockDayEntity.getFromDate()) || localDate.isEqual(blockDayEntity.getFromDate())) &&
            (localDate.isBefore(blockDayEntity.getToDate()) || localDate.isEqual(blockDayEntity.getToDate()));
    }

    private Set<BlockDay> compareAndAddBlockDays(BlockDay blockDayEntity, LocalDate localDate) {
        Set<BlockDay> blockDayEntitySet = new HashSet<>();
        if(isOverlapDays(blockDayEntity, localDate)) {
            if(blockDayEntity.getFromDate().equals(localDate) && !blockDayEntity.getFromDate().equals(blockDayEntity.getToDate())) {
                BlockDay blockDay = new BlockDay();
                blockDay.setFromDate(localDate.plusDays(1));
                blockDay.setToDate(blockDayEntity.getToDate());
                blockDayEntitySet.add(blockDay);
            }
            else if(blockDayEntity.getToDate().equals(localDate) && !blockDayEntity.getFromDate().equals(blockDayEntity.getToDate())) {
                BlockDay blockDay = new BlockDay();
                blockDay.setFromDate(blockDayEntity.getFromDate());
                blockDay.setToDate(blockDayEntity.getToDate().minusDays(1));
                blockDayEntitySet.add(blockDay);
            }
            else if(!blockDayEntity.getFromDate().equals(blockDayEntity.getToDate())){
                BlockDay blockDayEntity1 = new BlockDay();
                blockDayEntity1.setFromDate(blockDayEntity.getFromDate());
                blockDayEntity1.setToDate(localDate.minusDays(1));
                blockDayEntitySet.add(blockDayEntity1);

                BlockDay blockDayEntity2 = new BlockDay();
                blockDayEntity2.setFromDate(localDate.plusDays(1));
                blockDayEntity2.setToDate(blockDayEntity.getToDate());
                blockDayEntitySet.add(blockDayEntity2);
            }
        }
        else {
            blockDayEntitySet.add(blockDayEntity);
        }
        return blockDayEntitySet;
    }

    public Set<BlockDay> createNewBlockDays(Set<BlockDay> blockDayEntitySet, AvailabilityByDateDTO availability) {
        Set<BlockDay> newBlockDays = new HashSet<>();
        for (BlockDay blockDayEntity : blockDayEntitySet) {
            newBlockDays.addAll(compareAndAddBlockDays(blockDayEntity, availability.getDate()));
        }
        return newBlockDays;
    }

    public void deleteInvalidDateWiseAvailabilities(Availability availabilityEntity) {
        List<AvailabilityByDate> availabilityByDateEntityList = availabilityByDateRepo.findAllByLocationIdAndProviderId(availabilityEntity.getLocationId(), availabilityEntity.getProviderId());
        List<AvailabilityByDate> invalidEntityList = new ArrayList<>();
        for (AvailabilityByDate availabilityByDateEntity : availabilityByDateEntityList) {
            if((availabilityByDateEntity.getDate().isAfter(availabilityEntity.getBookingWindowStart())
                && availabilityByDateEntity.getDate().isBefore(availabilityEntity.getBookingWindowEnd()))
                || availabilityByDateEntity.getDate().equals(availabilityEntity.getBookingWindowStart())
                || availabilityByDateEntity.getDate().equals(availabilityEntity.getBookingWindowEnd())
            ) {
                invalidEntityList.add(availabilityByDateEntity);
            }
        }

        if(!invalidEntityList.isEmpty())
            availabilityByDateRepo.deleteAll(invalidEntityList);
    }

    public LocalDate getFromDate(Month month, int year, Availability availabilityEntity) {
        LocalDate utcLocalDate = LocalDate.of(year, month, 1);
        ZonedDateTime utcDateTime = utcLocalDate.atStartOfDay(ZoneOffset.UTC);
        ZoneId targetTimeZone = ZoneId.of(availabilityEntity.getBookingWindowTimeZone().getValue());
        return utcDateTime.withZoneSameInstant(targetTimeZone).toLocalDate();
    }

    public LocalDate getToDate(LocalDate fromDate) {
        return fromDate.with(TemporalAdjusters.lastDayOfMonth());
    }

    public TimeInterval createTimeIntervalFromAvailabilityConfiguration(Availability availabilityEntity, LocalDate date) {
        Optional<DayWiseAvailability> dayWiseAvailabilityEntityOptional = availabilityEntity.getDayWiseAvailabilitySet().stream().filter(day -> day.getDayOfWeek()
            .equals(date.getDayOfWeek())).findAny();

        if(dayWiseAvailabilityEntityOptional.isPresent() && (date.compareTo(availabilityEntity.getBookingWindowStart()) >= 0) && (date.compareTo(availabilityEntity.getBookingWindowEnd()) <= 0)) {
            boolean isBlockDay = false;
            for (BlockDay blockDayEntity : availabilityEntity.getBlockDaySet()) {
                if((date.isAfter(blockDayEntity.getFromDate()) || date.isEqual(blockDayEntity.getFromDate())) && (date.isBefore(blockDayEntity.getToDate()) || date.isEqual(blockDayEntity.getToDate()))) {
                    isBlockDay = true;
                    break;
                }
            }
            if(!isBlockDay)
                return new TimeInterval(dayWiseAvailabilityEntityOptional.get().getStartTime(), dayWiseAvailabilityEntityOptional.get().getEndTime());
        }
        return null;
    }

    public List<TimeInterval> mergeDateWiseAvailabilityWithCurrentInterval(List<AvailabilityByDate> availabilityByDateEntityList, TimeInterval currentTimeInterval) {
        List<TimeInterval> addTimeIntervals = new ArrayList<>();
        availabilityByDateEntityList.stream()
            .filter(availabilityByDate -> availabilityByDate.getAvailabilityOperationType().equals(AvailabilityOperationType.ADD))
            .forEach(availabilityByDateEntity -> addTimeIntervals.add(new TimeInterval(availabilityByDateEntity.getStartTime(), availabilityByDateEntity.getEndTime())));
        addAndMergeIntervals(addTimeIntervals, currentTimeInterval);
        return compareAndDivideIntervals(availabilityByDateEntityList.stream()
                .filter(availabilityByDate -> availabilityByDate.getAvailabilityOperationType().equals(AvailabilityOperationType.REMOVE))
                .map(availabilityByDateEntity -> new TimeInterval(availabilityByDateEntity.getStartTime(), availabilityByDateEntity.getEndTime())).toList(),
            addTimeIntervals);
    }
}
