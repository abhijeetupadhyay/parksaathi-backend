package com.parkar.parksaathi.model;

import com.parkar.parksaathi.constant.DayOfWeekEnum;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class ParkingAvailabilityDayId implements Serializable {

    private Long parkingId;

    @Enumerated(EnumType.STRING)
    private DayOfWeekEnum dayOfWeek;
}
