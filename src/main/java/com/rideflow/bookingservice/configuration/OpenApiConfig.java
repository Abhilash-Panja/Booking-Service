package com.rideflow.bookingservice.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "RideFlow Distributed Booking API",
        version = "0.0.1-SNAPSHOT",
        description = "Creates bookings and dispatches asynchronously through Location-Service and Socket-Server. The assignment endpoint always sets SCHEDULED. A 201 response does not confirm driver assignment or asynchronous delivery."
))
public class OpenApiConfig {
}
