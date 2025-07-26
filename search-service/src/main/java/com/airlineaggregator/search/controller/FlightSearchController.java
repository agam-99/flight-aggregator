package com.airlineaggregator.search.controller;

import com.airlineaggregator.search.dto.FlightSearchRequest;
import com.airlineaggregator.search.dto.SearchResult;
import com.airlineaggregator.search.service.FlightSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/flights")
@CrossOrigin(origins = "*")
public class FlightSearchController {

    private static final Logger logger = LoggerFactory.getLogger(FlightSearchController.class);

    @Autowired
    private FlightSearchService flightSearchService;

    @GetMapping("/search")
    public ResponseEntity<SearchResult> searchFlights(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
            @RequestParam Integer passengers,
            @RequestParam String sortBy,
            @RequestParam(required = false) String airline,
            @RequestParam(required = false) Integer maxStops,
            @RequestParam(required = false) Integer maxDuration) {
        
        logger.info("Received flight search request: {} -> {}, Date: {}, Passengers: {}, Sort: {}", 
                   source, destination, travelDate, passengers, sortBy);

        try {
            // Validate sort parameter
            if (!isValidSortBy(sortBy)) {
                logger.warn("Invalid sortBy parameter: {}", sortBy);
                return ResponseEntity.badRequest().build();
            }

            // Create search request
            FlightSearchRequest request = new FlightSearchRequest();
            request.setSource(source);
            request.setDestination(destination);
            request.setTravelDate(travelDate);
            request.setPassengers(passengers);
            request.setSortBy(sortBy);
            request.setAirline(airline);
            request.setMaxStops(maxStops);
            request.setMaxDuration(maxDuration);

            // Execute search
            SearchResult result = flightSearchService.searchFlights(request);
            
            logger.info("Flight search completed successfully. Found {} results", 
                       result.getFlights().size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error processing flight search request", e);
            return ResponseEntity.internalServerError().build();
        }
    }



    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Search Service is running");
    }

    @GetMapping("/info")
    public ResponseEntity<SearchServiceInfo> getServiceInfo() {
        SearchServiceInfo info = new SearchServiceInfo();
        info.setServiceName("Flight Search Service");
        info.setVersion("1.0.0");
        info.setDescription("Provides flight search functionality for airline aggregator");
        info.setEndpoints(new String[]{
            "GET /api/v1/flights/search - Search flights",
            "GET /api/v1/flights/health - Health check",
            "GET /api/v1/flights/info - Service information"
        });
        return ResponseEntity.ok(info);
    }

    private boolean isValidSortBy(String sortBy) {
        return "price".equalsIgnoreCase(sortBy) || "duration".equalsIgnoreCase(sortBy);
    }

    // Inner class for service information
    public static class SearchServiceInfo {
        private String serviceName;
        private String version;
        private String description;
        private String[] endpoints;

        // Getters and Setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String[] getEndpoints() { return endpoints; }
        public void setEndpoints(String[] endpoints) { this.endpoints = endpoints; }
    }
} 