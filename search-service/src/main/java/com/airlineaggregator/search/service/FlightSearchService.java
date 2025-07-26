package com.airlineaggregator.search.service;

import com.airlineaggregator.search.dto.*;
import com.airlineaggregator.search.entity.FlightRoutine;
import com.airlineaggregator.search.repository.FlightRoutineRepository;
import com.airlineaggregator.search.specification.FlightRoutineSpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FlightSearchService {

    private static final Logger logger = LoggerFactory.getLogger(FlightSearchService.class);
    private static final int MAX_RESULTS = 10;

    @Autowired
    private FlightRoutineRepository flightRoutineRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public SearchResult searchFlights(FlightSearchRequest request) {
        logger.info("Searching flights for request: {}", request);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Build dynamic query specification
            Specification<FlightRoutine> searchSpec = buildSearchSpecification(request);
            
            // Execute search with pagination (limit to top 10)
            Pageable pageable = PageRequest.of(0, MAX_RESULTS);
            List<FlightRoutine> flightRoutines = flightRoutineRepository.findAll(searchSpec, pageable).getContent();
            
            // Convert to DTOs
            List<FlightSearchResponse> flightResponses = flightRoutines.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            // Calculate total available flights for metadata
            Long totalCount = flightRoutineRepository.countAvailableFlights(
                    request.getSource(), 
                    request.getDestination(), 
                    request.getTravelDate(), 
                    request.getPassengers()
            );
            
            long searchTime = System.currentTimeMillis() - startTime;
            
            // Create search metadata
            SearchResult.SearchMetadata metadata = new SearchResult.SearchMetadata(
                    totalCount.intValue(),
                    UUID.randomUUID().toString(),
                    false, // TODO: Implement caching
                    searchTime,
                    request
            );
            
            logger.info("Search completed in {}ms, found {} results (total available: {})", 
                       searchTime, flightResponses.size(), totalCount);
            
            return new SearchResult(flightResponses, metadata);
            
        } catch (Exception e) {
            logger.error("Error during flight search", e);
            throw new RuntimeException("Flight search failed", e);
        }
    }

    private Specification<FlightRoutine> buildSearchSpecification(FlightSearchRequest request) {
        logger.debug("Building search specification for: source={}, destination={}, date={}, passengers={}, sortBy={}, airline={}, maxDuration={}", 
                    request.getSource(), request.getDestination(), request.getTravelDate(), 
                    request.getPassengers(), request.getSortBy(), request.getAirline(), request.getMaxDuration());

        return FlightRoutineSpecification.buildSearchSpecification(
                request.getSource(),
                request.getDestination(),
                request.getTravelDate(),
                request.getPassengers(),
                request.getAirline(),
                request.getMaxDuration(),
                request.getSortBy()
        );
    }

    private FlightSearchResponse convertToDTO(FlightRoutine flightRoutine) {
        FlightSearchResponse response = new FlightSearchResponse();
        
        // Basic flight information
        response.setFlightRoutineId(flightRoutine.getId());
        response.setFlightId(flightRoutine.getFlight().getId());
        response.setFlightNumber(flightRoutine.getFlight().getFlightNumber());
        
        // Airline information
        FlightSearchResponse.AirlineInfo airlineInfo = new FlightSearchResponse.AirlineInfo(
                flightRoutine.getFlight().getAirline().getCode(),
                flightRoutine.getFlight().getAirline().getName(),
                flightRoutine.getFlight().getAirline().getLogoUrl()
        );
        response.setAirline(airlineInfo);
        
        // Route information
        String routeDisplay = flightRoutine.getFlight().getRouteDisplay();
        int stops = calculateStops(routeDisplay);
        FlightSearchResponse.RouteInfo routeInfo = new FlightSearchResponse.RouteInfo(
                flightRoutine.getFlight().getSourceAirport(),
                flightRoutine.getFlight().getDestinationAirport(),
                routeDisplay,
                stops
        );
        response.setRoute(routeInfo);
        
        // Schedule information
        FlightSearchResponse.ScheduleInfo scheduleInfo = new FlightSearchResponse.ScheduleInfo(
                flightRoutine.getDepartureTime(),
                flightRoutine.getArrivalTime(),
                flightRoutine.getTravelDate(),
                flightRoutine.getFlight().getTotalDurationMinutes()
        );
        response.setSchedule(scheduleInfo);
        
        // Pricing information
        FlightSearchResponse.PricingInfo pricingInfo = new FlightSearchResponse.PricingInfo(
                flightRoutine.getCurrentPrice(),
                flightRoutine.getBasePrice(),
                flightRoutine.getCurrency(),
                flightRoutine.getPricingTiers()
        );
        response.setPricing(pricingInfo);
        
        // Availability information
        FlightSearchResponse.AvailabilityInfo availabilityInfo = new FlightSearchResponse.AvailabilityInfo(
                flightRoutine.getTotalSeats(),
                flightRoutine.getAvailableSeats()
        );
        response.setAvailability(availabilityInfo);
        
        // Aircraft information from metadata
        if (flightRoutine.getFlight().getMetadata() != null) {
            try {
                JsonNode metadataNode = objectMapper.readTree(flightRoutine.getFlight().getMetadata());
                String aircraftType = metadataNode.path("aircraft_type").asText();
                String amenities = metadataNode.path("amenities").toString();
                
                FlightSearchResponse.AircraftInfo aircraftInfo = new FlightSearchResponse.AircraftInfo(
                        aircraftType, amenities
                );
                response.setAircraft(aircraftInfo);
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse flight metadata for flight {}", flightRoutine.getFlight().getFlightNumber());
            }
        }
        
        return response;
    }

    private int calculateStops(String routeDisplay) {
        if (routeDisplay == null || routeDisplay.isEmpty()) {
            return 0;
        }
        // Count the number of "->" in the route display to determine stops
        int arrowCount = routeDisplay.split("->").length - 1;
        return Math.max(0, arrowCount - 1); // Number of stops = arrows - 1
    }
} 