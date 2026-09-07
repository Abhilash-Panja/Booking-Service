package com.rideflow.bookingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rideflow.rideflowentityservice.models.ExactLocation;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBookingDto {

    @Schema(description = "Existing Passenger ID. Null or missing row is not translated to 400/404 by the service.", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long passengerId;

    @Schema(description = "Entity-valued input: send latitude and longitude only; omit inherited id/audit fields. Saved through Booking.startLocation cascade, then read for nearby-driver lookup. No @Valid.", requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"latitude\":17.3850,\"longitude\":78.4867}")
    private ExactLocation startLocation;

    @Schema(description = "Accepted in DTO but assignment to Booking is commented out; it is not saved by createBooking.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "{\"latitude\":17.4435,\"longitude\":78.3772}")
    private ExactLocation endLocation;
}
