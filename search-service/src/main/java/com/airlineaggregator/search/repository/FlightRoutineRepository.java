package com.airlineaggregator.search.repository;

import com.airlineaggregator.search.entity.FlightRoutine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface FlightRoutineRepository extends JpaRepository<FlightRoutine, UUID>, JpaSpecificationExecutor<FlightRoutine> {

    @Query("""
        SELECT COUNT(fr) FROM FlightRoutine fr
        JOIN fr.flight f
        WHERE f.sourceAirport = :source
        AND f.destinationAirport = :destination
        AND fr.travelDate = :travelDate
        AND fr.availableSeats >= :minSeats
        AND fr.status = 'scheduled'
        AND f.isActive = true
        """)
    Long countAvailableFlights(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("travelDate") LocalDate travelDate,
            @Param("minSeats") Integer minSeats
    );
} 