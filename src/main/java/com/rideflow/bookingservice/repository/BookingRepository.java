package com.rideflow.bookingservice.repository;

import com.rideflow.rideflowentityservice.models.Booking;
import com.rideflow.rideflowentityservice.models.BookingStatus;
import com.rideflow.rideflowentityservice.models.Driver;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.bookingStatus = :status, b.driver = :driver WHERE b.id = :id")
    void updateBookingStatusAndDriverById(
            @Param("id") Long id,
            @Param("status") BookingStatus status,
            @Param("driver") Driver driver);

    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.bookingStatus = :status WHERE b.id = :id")
    void updateBookingStatusById(
            @Param("id") Long id,
            @Param("status") BookingStatus status);
}
