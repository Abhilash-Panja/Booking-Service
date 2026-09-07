package com.rideflow.bookingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.rideflow.rideflowentityservice.models.BookingStatus;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingResponseDto {

    private Long bookingId;
    private BookingStatus status;
    @Schema(description = "Raw shared Driver entity wrapped in Optional, not DriverResponseDTO. Nested relationships and sensitive properties may be exposed; serialization can fail on cycles or proxies. See separate issue report.")
    private Optional<DriverSummaryDto> driver;

}
