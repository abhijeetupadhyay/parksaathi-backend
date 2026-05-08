package com.parkar.parksaathi.dto.request;

import com.parkar.parksaathi.enums.DayOfWeekEnum;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class AvailabilityDto {
    private boolean isOpen24Hours;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate adStartDate;
    private LocalDate adEndDate;
    private List<DayOfWeekEnum> days;
}