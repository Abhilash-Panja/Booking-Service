package com.rideflow.bookingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingRequestDto {

    @Schema(description = "The accepted driver's database ID.", requiredMode = Schema.RequiredMode.REQUIRED, example = "201")
    @NotNull(message = "driverId is required")
    private Long driverId;
}
