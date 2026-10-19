package com.rideflow.bookingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.rideflow.rideflowentityservice.models.BookingStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingResponseDto {

    private Long bookingId;
    private BookingStatus status;

    @Schema(description = "Small public summary of the assigned driver.")
    private DriverSummaryDto driver;
}
