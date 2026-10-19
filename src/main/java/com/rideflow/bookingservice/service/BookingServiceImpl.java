package com.rideflow.bookingservice.service;

import com.rideflow.bookingservice.api.LocationServiceApi;
import com.rideflow.bookingservice.api.SocketApi;
import com.rideflow.bookingservice.dto.*;
import com.rideflow.bookingservice.repository.BookingRepository;
import com.rideflow.bookingservice.repository.DriverRepository;
import com.rideflow.bookingservice.repository.PassengerRepository;
import com.rideflow.rideflowentityservice.models.Booking;
import com.rideflow.rideflowentityservice.models.BookingStatus;
import com.rideflow.rideflowentityservice.models.Driver;
import com.rideflow.rideflowentityservice.models.Passenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final LocationServiceApi locationServiceApi;
    private final SocketApi socketApi;
    private final DriverRepository driverRepository;

    public BookingServiceImpl(
            PassengerRepository passengerRepository,
            BookingRepository bookingRepository,
            LocationServiceApi locationServiceApi,
            SocketApi socketApi,
            DriverRepository driverRepository) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.locationServiceApi = locationServiceApi;
        this.socketApi = socketApi;
        this.driverRepository = driverRepository;
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails) {
        if (bookingDetails == null
                || bookingDetails.getPassengerId() == null
                || bookingDetails.getStartLocation() == null
                || bookingDetails.getStartLocation().getLatitude() == null
                || bookingDetails.getStartLocation().getLongitude() == null) {
            throw new IllegalArgumentException("passengerId and startLocation coordinates are required");
        }

        Passenger passenger = passengerRepository.findById(bookingDetails.getPassengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found"));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetails.getStartLocation())
                .endLocation(bookingDetails.getEndLocation())
                .passenger(passenger)
                .build();
        Booking newBooking = bookingRepository.save(booking);

        NearbyDriversRequestDto request = NearbyDriversRequestDto.builder()
                .latitude(bookingDetails.getStartLocation().getLatitude())
                .longitude(bookingDetails.getStartLocation().getLongitude())
                .build();

        processNearbyDriversAsync(request, bookingDetails.getPassengerId(), newBooking.getId());

        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .build();
    }

    @Override
    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingRequestDto, Long bookingId) {
        if (bookingRequestDto == null || bookingRequestDto.getDriverId() == null) {
            throw new IllegalArgumentException("driverId is required");
        }

        Long driverId = bookingRequestDto.getDriverId();
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        bookingRepository.updateBookingStatusAndDriverById(bookingId, BookingStatus.SCHEDULED, driver);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found after update"));

        return UpdateBookingResponseDto.builder()
                .bookingId(bookingId)
                .status(booking.getBookingStatus())
                .driver(java.util.Optional.ofNullable(booking.getDriver())
                        .map(assignedDriver -> DriverSummaryDto.builder()
                                .id(assignedDriver.getId())
                                .driverName(assignedDriver.getDriverName())
                                .build())
                        .orElse(null))
                .build();
    }

    private void processNearbyDriversAsync(
            NearbyDriversRequestDto requestDto,
            Long passengerId,
            Long bookingId) {
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDrivers(requestDto);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.warn("Location lookup failed for booking {}: {}", bookingId, response.message());
                    cancelBooking(bookingId);
                    return;
                }

                List<Long> driverIds = Arrays.stream(response.body())
                        .map(DriverLocationDto::getDriverId)
                        .map(BookingServiceImpl.this::parseDriverId)
                        .filter(java.util.Objects::nonNull)
                        .toList();

                if (driverIds.isEmpty()) {
                    log.info("No nearby drivers found for booking {}", bookingId);
                    cancelBooking(bookingId);
                    return;
                }

                RideRequestDto rideRequest = RideRequestDto.builder()
                        .passengerId(passengerId)
                        .bookingId(bookingId)
                        .driverIds(driverIds)
                        .build();
                raiseRideRequestAsync(rideRequest);
            }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable throwable) {
                log.warn("Location lookup failed for booking {}", bookingId, throwable);
                cancelBooking(bookingId);
            }
        });
    }

    private void raiseRideRequestAsync(RideRequestDto requestDto) {
        Call<Boolean> call = socketApi.raiseRideRequest(requestDto);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (!response.isSuccessful() || !Boolean.TRUE.equals(response.body())) {
                    log.warn("Socket request failed for booking {}: {}", requestDto.getBookingId(), response.message());
                    cancelBooking(requestDto.getBookingId());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable throwable) {
                log.warn("Socket request failed for booking {}", requestDto.getBookingId(), throwable);
                cancelBooking(requestDto.getBookingId());
            }
        });
    }

    private Long parseDriverId(String driverId) {
        try {
            return driverId == null ? null : Long.valueOf(driverId);
        } catch (NumberFormatException ex) {
            log.warn("Ignoring non-numeric driver id returned by Location-Service: {}", driverId);
            return null;
        }
    }

    private void cancelBooking(Long bookingId) {
        bookingRepository.updateBookingStatusById(bookingId, BookingStatus.CANCELED);
    }
}
