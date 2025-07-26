package com.airlineaggregator.booking.repository;

import com.airlineaggregator.booking.entity.FlightRoutine;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlightRoutineRepository extends JpaRepository<FlightRoutine, UUID> {

    Optional<FlightRoutine> findById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT fr FROM FlightRoutine fr JOIN FETCH fr.flight f JOIN FETCH f.airline WHERE fr.id = :id")
    Optional<FlightRoutine> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT fr FROM FlightRoutine fr JOIN FETCH fr.flight f JOIN FETCH f.airline WHERE fr.id = :id")
    Optional<FlightRoutine> findByIdWithFlightAndAirline(@Param("id") UUID id);

    @Query("SELECT fr FROM FlightRoutine fr WHERE fr.id = :id AND fr.availableSeats >= :requiredSeats AND fr.status = 'scheduled'")
    Optional<FlightRoutine> findAvailableFlightRoutine(@Param("id") UUID id, @Param("requiredSeats") Integer requiredSeats);

    @Modifying
    @Query("UPDATE FlightRoutine fr SET fr.availableSeats = fr.availableSeats - :seatsToBook WHERE fr.id = :id AND fr.availableSeats >= :seatsToBook")
    int updateAvailableSeats(@Param("id") UUID id, @Param("seatsToBook") Integer seatsToBook);

    @Modifying
    @Query("UPDATE FlightRoutine fr SET fr.availableSeats = fr.availableSeats + :seatsToRelease WHERE fr.id = :id")
    int releaseSeats(@Param("id") UUID id, @Param("seatsToRelease") Integer seatsToRelease);
} 