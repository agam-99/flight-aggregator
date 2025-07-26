package com.airlineaggregator.booking.repository;

import com.airlineaggregator.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingId(UUID bookingId);

    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Booking> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT b FROM Booking b WHERE b.status = 'pending' AND b.expiresAt < :currentTime")
    List<Booking> findExpiredPendingBookings(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Modifying
    @Query("UPDATE Booking b SET b.status = 'expired' WHERE b.status = 'pending' AND b.expiresAt < :currentTime")
    int expireOldBookings(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.flightRoutine.id = :flightRoutineId AND b.status IN ('pending', 'confirmed')")
    Long countActiveBookingsForFlightRoutine(@Param("flightRoutineId") UUID flightRoutineId);
} 