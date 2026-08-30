package com.rideflow.bookingservice.service;


import com.rideflow.bookingservice.dto.CreateBookingDto;
import com.rideflow.bookingservice.dto.CreateBookingResponseDto;
import com.rideflow.bookingservice.dto.UpdateBookingRequestDto;
import com.rideflow.bookingservice.dto.UpdateBookingResponseDto;

public interface BookingService {

    CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails);

    UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingRequestDto, Long bookingId);
}