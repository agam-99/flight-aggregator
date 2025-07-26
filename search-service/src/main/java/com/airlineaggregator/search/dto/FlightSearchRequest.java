package com.airlineaggregator.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class FlightSearchRequest {

    @NotBlank(message = "Source airport is required")
    private String source;

    @NotBlank(message = "Destination airport is required")
    private String destination;

    @NotNull(message = "Travel date is required")
    private LocalDate travelDate;

    @NotNull(message = "Number of passengers is required")
    @Positive(message = "Number of passengers must be positive")
    private Integer passengers;

    @NotBlank(message = "Sort by is required")
    private String sortBy; // "price" or "duration"

    private String airline; // Optional filter by airline code

    private Integer maxStops; // Optional max stops filter

    private Integer maxDuration; // Optional max duration in minutes

    // Constructors
    public FlightSearchRequest() {}

    public FlightSearchRequest(String source, String destination, LocalDate travelDate, 
                              Integer passengers, String sortBy) {
        this.source = source;
        this.destination = destination;
        this.travelDate = travelDate;
        this.passengers = passengers;
        this.sortBy = sortBy;
    }

    // Getters and Setters
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public Integer getPassengers() {
        return passengers;
    }

    public void setPassengers(Integer passengers) {
        this.passengers = passengers;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public Integer getMaxStops() {
        return maxStops;
    }

    public void setMaxStops(Integer maxStops) {
        this.maxStops = maxStops;
    }

    public Integer getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Integer maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Override
    public String toString() {
        return "FlightSearchRequest{" +
                "source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", travelDate=" + travelDate +
                ", passengers=" + passengers +
                ", sortBy='" + sortBy + '\'' +
                ", airline='" + airline + '\'' +
                ", maxStops=" + maxStops +
                ", maxDuration=" + maxDuration +
                '}';
    }
} 