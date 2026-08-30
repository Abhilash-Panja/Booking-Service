package com.rideflow.bookingservice.dto;

import com.rideflow.rideflowentityservice.models.Driver;
import com.rideflow.rideflowentityservice.models.ExactLocation;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBookingResponseDto {
    private long bookingId;
    private String bookingStatus;
    private Optional<Driver> driver;

}
