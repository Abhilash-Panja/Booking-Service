package com.rideflow.bookingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
    @Schema(description = "Not populated by createBooking; normally null in its immediate response.")
    private Optional<Driver> driver;

}
