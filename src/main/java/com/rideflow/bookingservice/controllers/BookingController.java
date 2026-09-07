package com.rideflow.bookingservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.rideflow.bookingservice.dto.CreateBookingDto;
import com.rideflow.bookingservice.dto.CreateBookingResponseDto;
import com.rideflow.bookingservice.dto.UpdateBookingRequestDto;
import com.rideflow.bookingservice.dto.UpdateBookingResponseDto;
import com.rideflow.bookingservice.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Distributed bookings")
@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    @Operation(operationId = "BookingService_createBooking", summary = "Create a distributed booking and start asynchronous dispatch",
            description = "Connects persistence to Location-Service and Socket-Server and demonstrates an immediate response followed by asynchronous work. BookingController.createBooking -> BookingServiceImpl.createBooking -> PassengerRepository.findById(...).get -> BookingRepository.save (ASSIGNING_DRIVER, startLocation, passenger) -> Retrofit LocationServiceApi.getNearbyDrivers.enqueue -> return CreateBookingResponseDto. Successful non-null nearby response triggers SocketApi.raiseRideRequest.enqueue. Returned nearby drivers are printed, not attached to RideRequestDto.driverIds. Even a successful empty array triggers broadcast. No retry/state rollback is implemented for async failures. ExactLocation includes inherited ID/audit properties: omit them in client JSON.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            description = "JSON body is required by Spring MVC. Field descriptions distinguish service requirements from active validation.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.rideflow.bookingservice.dto.CreateBookingDto.class),
                    examples = @ExampleObject(value = "{\n  \"passengerId\": 101,\n  \"startLocation\": {\n    \"latitude\": 17.385,\n    \"longitude\": 78.4867\n  },\n  \"endLocation\": {\n    \"latitude\": 17.4435,\n    \"longitude\": 78.3772\n  }\n}")))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Create a distributed booking and start asynchronous dispatch completed on the controller success branch.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.rideflow.bookingservice.dto.CreateBookingResponseDto.class), examples = @ExampleObject(value = "{\"bookingId\":401,\"bookingStatus\":\"ASSIGNING_DRIVER\",\"driver\":null}"))),
            @ApiResponse(responseCode = "400", description = "Unreadable JSON before controller invocation.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Missing passenger, missing startLocation, persistence or other unhandled failure; no custom error DTO.",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<CreateBookingResponseDto> createBooking(@RequestBody CreateBookingDto createBookingDto) throws IOException {

        return new ResponseEntity<>(bookingService.createBooking(createBookingDto), HttpStatus.CREATED);
    }


    @Operation(operationId = "BookingService_updateBooking", summary = "Assign a driver and force SCHEDULED",
            description = "Tests the callback used by the STOMP ride-response handler and separates it from ReviewService status transitions. BookingController.updateBooking -> BookingServiceImpl.updateBooking -> request.driverId.get -> DriverRepository.findById(...).get -> BookingRepository.updateBookingStatusAndDriverById JPQL bulk UPDATE (SCHEDULED, driver) -> BookingRepository.findById -> UpdateBookingResponseDto. Response driver is Optional<Driver>, exposing an entity graph. The example is only a partial shape: actual fields, cycles/lazy-proxy failures and serialization depend on the resolved shared model and mapper. Do not treat it as DriverResponseDTO or expect exactly two fields.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            description = "JSON body is required by Spring MVC. Field descriptions distinguish service requirements from active validation.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.rideflow.bookingservice.dto.UpdateBookingRequestDto.class),
                    examples = @ExampleObject(value = "{\n  \"status\": \"SCHEDULED\",\n  \"driverId\": 201\n}")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Controller success is 200. Response contains a raw Driver entity; inspect its actual graph and watch for response serialization failure.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.rideflow.bookingservice.dto.UpdateBookingResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Unreadable JSON or path binding error before controller invocation.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Missing Optional driverId value, missing driver/booking, or entity serialization/persistence error.",
                    content = @Content)
    })
    @PostMapping("/{bookingId}")
    public ResponseEntity<UpdateBookingResponseDto> updateBooking(@RequestBody UpdateBookingRequestDto requestDto, @Parameter(description = "Database booking ID, not a review ID.", required = true, example = "1") @PathVariable Long bookingId) {
        return new ResponseEntity<>(bookingService.updateBooking(requestDto, bookingId), HttpStatus.OK);
    }

}
