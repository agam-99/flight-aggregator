package com.airlineaggregator.search.specification;

import com.airlineaggregator.search.entity.FlightRoutine;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;

public class FlightRoutineSpecification {

    public static Specification<FlightRoutine> hasSource(String source) {
        return (root, query, criteriaBuilder) -> {
            if (source == null || source.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> flightJoin = root.join("flight", JoinType.INNER);
            return criteriaBuilder.equal(
                criteriaBuilder.upper(flightJoin.get("sourceAirport")), 
                source.toUpperCase()
            );
        };
    }

    public static Specification<FlightRoutine> hasDestination(String destination) {
        return (root, query, criteriaBuilder) -> {
            if (destination == null || destination.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> flightJoin = root.join("flight", JoinType.INNER);
            return criteriaBuilder.equal(
                criteriaBuilder.upper(flightJoin.get("destinationAirport")), 
                destination.toUpperCase()
            );
        };
    }

    public static Specification<FlightRoutine> hasTravelDate(LocalDate travelDate) {
        return (root, query, criteriaBuilder) -> {
            if (travelDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("travelDate"), travelDate);
        };
    }

    public static Specification<FlightRoutine> hasMinimumSeats(Integer minSeats) {
        return (root, query, criteriaBuilder) -> {
            if (minSeats == null || minSeats <= 0) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("availableSeats"), minSeats);
        };
    }

    public static Specification<FlightRoutine> hasAirline(String airlineCode) {
        return (root, query, criteriaBuilder) -> {
            if (airlineCode == null || airlineCode.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> flightJoin = root.join("flight", JoinType.INNER);
            Join<Object, Object> airlineJoin = flightJoin.join("airline", JoinType.INNER);
            return criteriaBuilder.equal(
                criteriaBuilder.upper(airlineJoin.get("code")), 
                airlineCode.toUpperCase()
            );
        };
    }

    public static Specification<FlightRoutine> hasMaxDuration(Integer maxDurationMinutes) {
        return (root, query, criteriaBuilder) -> {
            if (maxDurationMinutes == null || maxDurationMinutes <= 0) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> flightJoin = root.join("flight", JoinType.INNER);
            return criteriaBuilder.lessThanOrEqualTo(
                flightJoin.get("totalDurationMinutes"), 
                maxDurationMinutes
            );
        };
    }

    public static Specification<FlightRoutine> isScheduled() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("status"), "scheduled");
    }

    public static Specification<FlightRoutine> isActive() {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> flightJoin = root.join("flight", JoinType.INNER);
            Join<Object, Object> airlineJoin = flightJoin.join("airline", JoinType.INNER);
            
            Predicate flightActive = criteriaBuilder.equal(flightJoin.get("isActive"), true);
            Predicate airlineActive = criteriaBuilder.equal(airlineJoin.get("isActive"), true);
            
            return criteriaBuilder.and(flightActive, airlineActive);
        };
    }

    public static Specification<FlightRoutine> sortByPrice() {
        return (root, query, criteriaBuilder) -> {
            if (query != null) {
                query.orderBy(criteriaBuilder.asc(root.get("currentPrice")));
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<FlightRoutine> sortByDuration() {
        return (root, query, criteriaBuilder) -> {
            if (query != null) {
                Join<Object, Object> flightJoin = root.join("flight", JoinType.INNER);
                query.orderBy(criteriaBuilder.asc(flightJoin.get("totalDurationMinutes")));
            }
            return criteriaBuilder.conjunction();
        };
    }

    // Combined specification builder method
    public static Specification<FlightRoutine> buildSearchSpecification(
            String source, 
            String destination, 
            LocalDate travelDate, 
            Integer minSeats,
            String airlineCode,
            Integer maxDurationMinutes,
            String sortBy) {
        
        Specification<FlightRoutine> spec = Specification.where(hasSource(source))
                .and(hasDestination(destination))
                .and(hasTravelDate(travelDate))
                .and(hasMinimumSeats(minSeats))
                .and(hasAirline(airlineCode))
                .and(hasMaxDuration(maxDurationMinutes))
                .and(isScheduled())
                .and(isActive());

        // Add sorting
        if ("duration".equalsIgnoreCase(sortBy)) {
            spec = spec.and(sortByDuration());
        } else {
            spec = spec.and(sortByPrice());
        }

        return spec;
    }
} 