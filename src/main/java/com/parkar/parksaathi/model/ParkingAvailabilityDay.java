package com.parkar.parksaathi.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Objects;

@Entity
@Table(name = "parking_availability_days")
@IdClass(ParkingAvailabilityDay.ParkingAvailabilityDayId.class)
public class ParkingAvailabilityDay {

    @Id
    @Column(name = "parking_id")
    private Long parkingId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id", insertable = false, updatable = false)
    private ParkingListing parkingListing;

    // Constructors
    public ParkingAvailabilityDay() {}

    public ParkingAvailabilityDay(Long parkingId, DayOfWeek dayOfWeek) {
        this.parkingId = parkingId;
        this.dayOfWeek = dayOfWeek;
    }

    // Getters and Setters
    public Long getParkingId() {
        return parkingId;
    }

    public void setParkingId(Long parkingId) {
        this.parkingId = parkingId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public ParkingListing getParkingListing() {
        return parkingListing;
    }

    public void setParkingListing(ParkingListing parkingListing) {
        this.parkingListing = parkingListing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingAvailabilityDay that = (ParkingAvailabilityDay) o;
        return Objects.equals(parkingId, that.parkingId) && dayOfWeek == that.dayOfWeek;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parkingId, dayOfWeek);
    }

    // Composite Primary Key Class
    public static class ParkingAvailabilityDayId implements Serializable {
        private Long parkingId;
        private DayOfWeek dayOfWeek;

        public ParkingAvailabilityDayId() {}

        public ParkingAvailabilityDayId(Long parkingId, DayOfWeek dayOfWeek) {
            this.parkingId = parkingId;
            this.dayOfWeek = dayOfWeek;
        }

        public Long getParkingId() {
            return parkingId;
        }

        public void setParkingId(Long parkingId) {
            this.parkingId = parkingId;
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParkingAvailabilityDayId that = (ParkingAvailabilityDayId) o;
            return Objects.equals(parkingId, that.parkingId) && dayOfWeek == that.dayOfWeek;
        }

        @Override
        public int hashCode() {
            return Objects.hash(parkingId, dayOfWeek);
        }
    }
}
