package com.rideflow.bookingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;


import lombok.*;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingRequestDto {

    @Schema(description = "Ignored. Implementation always writes SCHEDULED, regardless of this string.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "SCHEDULED")
    private String status;
    @Schema(description = "Send a JSON number, not an Optional wrapper object. Service calls get() without checking presence and requires an existing driver.", requiredMode = Schema.RequiredMode.REQUIRED, example = "201", implementation = Long.class)
    private Optional<Long> driverId;

}