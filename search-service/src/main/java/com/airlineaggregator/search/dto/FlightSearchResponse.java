package com.airlineaggregator.search.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class FlightSearchResponse {

    private UUID flightRoutineId;
    private UUID flightId;
    private String flightNumber;
    private AirlineInfo airline;
    private RouteInfo route;
    private ScheduleInfo schedule;
    private PricingInfo pricing;
    private AvailabilityInfo availability;
    private AircraftInfo aircraft;

    // Constructors
    public FlightSearchResponse() {}

    // Nested classes for structured response
    public static class AirlineInfo {
        private String code;
        private String name;
        private String logoUrl;

        public AirlineInfo() {}

        public AirlineInfo(String code, String name, String logoUrl) {
            this.code = code;
            this.name = name;
            this.logoUrl = logoUrl;
        }

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    }

    public static class RouteInfo {
        private String source;
        private String destination;
        private String display;
        private Integer stops;

        public RouteInfo() {}

        public RouteInfo(String source, String destination, String display, Integer stops) {
            this.source = source;
            this.destination = destination;
            this.display = display;
            this.stops = stops;
        }

        // Getters and Setters
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public String getDisplay() { return display; }
        public void setDisplay(String display) { this.display = display; }
        public Integer getStops() { return stops; }
        public void setStops(Integer stops) { this.stops = stops; }
    }

    public static class ScheduleInfo {
        private LocalTime departureTime;
        private LocalTime arrivalTime;
        private LocalDate travelDate;
        private Integer durationMinutes;

        public ScheduleInfo() {}

        public ScheduleInfo(LocalTime departureTime, LocalTime arrivalTime, 
                           LocalDate travelDate, Integer durationMinutes) {
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.travelDate = travelDate;
            this.durationMinutes = durationMinutes;
        }

        // Getters and Setters
        public LocalTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
        public LocalTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
        public LocalDate getTravelDate() { return travelDate; }
        public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    }

    public static class PricingInfo {
        private BigDecimal currentPrice;
        private BigDecimal basePrice;
        private String currency;
        private String pricingTiers;

        public PricingInfo() {}

        public PricingInfo(BigDecimal currentPrice, BigDecimal basePrice, String currency, String pricingTiers) {
            this.currentPrice = currentPrice;
            this.basePrice = basePrice;
            this.currency = currency;
            this.pricingTiers = pricingTiers;
        }

        // Getters and Setters
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getPricingTiers() { return pricingTiers; }
        public void setPricingTiers(String pricingTiers) { this.pricingTiers = pricingTiers; }
    }

    public static class AvailabilityInfo {
        private Integer totalSeats;
        private Integer availableSeats;

        public AvailabilityInfo() {}

        public AvailabilityInfo(Integer totalSeats, Integer availableSeats) {
            this.totalSeats = totalSeats;
            this.availableSeats = availableSeats;
        }

        // Getters and Setters
        public Integer getTotalSeats() { return totalSeats; }
        public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
        public Integer getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    }

    public static class AircraftInfo {
        private String type;
        private String amenities;

        public AircraftInfo() {}

        public AircraftInfo(String type, String amenities) {
            this.type = type;
            this.amenities = amenities;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getAmenities() { return amenities; }
        public void setAmenities(String amenities) { this.amenities = amenities; }
    }

    // Main class getters and setters
    public UUID getFlightRoutineId() { return flightRoutineId; }
    public void setFlightRoutineId(UUID flightRoutineId) { this.flightRoutineId = flightRoutineId; }
    public UUID getFlightId() { return flightId; }
    public void setFlightId(UUID flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public AirlineInfo getAirline() { return airline; }
    public void setAirline(AirlineInfo airline) { this.airline = airline; }
    public RouteInfo getRoute() { return route; }
    public void setRoute(RouteInfo route) { this.route = route; }
    public ScheduleInfo getSchedule() { return schedule; }
    public void setSchedule(ScheduleInfo schedule) { this.schedule = schedule; }
    public PricingInfo getPricing() { return pricing; }
    public void setPricing(PricingInfo pricing) { this.pricing = pricing; }
    public AvailabilityInfo getAvailability() { return availability; }
    public void setAvailability(AvailabilityInfo availability) { this.availability = availability; }
    public AircraftInfo getAircraft() { return aircraft; }
    public void setAircraft(AircraftInfo aircraft) { this.aircraft = aircraft; }
} 