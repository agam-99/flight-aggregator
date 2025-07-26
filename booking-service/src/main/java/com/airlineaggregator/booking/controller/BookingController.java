package com.airlineaggregator.booking.controller;

import com.airlineaggregator.booking.dto.BookingRequest;
import com.airlineaggregator.booking.dto.BookingResponse;
import com.airlineaggregator.booking.entity.Booking;
import com.airlineaggregator.booking.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    // Error response class for API errors
    public static class ErrorResponse {
        private String errorCode;
        private String message;

        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getMessage() {
            return message;
        }
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request) {
        
        logger.info("Received booking request for flight routine: {}", request.getFlightRoutineId());

        try {
            BookingResponse response = bookingService.createBooking(request);
            
            logger.info("Booking created successfully: {}", response.getBookingId());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error processing booking request: {}", e.getMessage());
            
            // Check if it's a seat availability issue
            if (e.getMessage().contains("Insufficient seats")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("INSUFFICIENT_SEATS", e.getMessage()));
            } else if (e.getMessage().contains("Booking creation failed: Flight routine not found")) {
                return ResponseEntity.status(404)
                    .body(new ErrorResponse("FLIGHT_ROUTINE_NOT_FOUND", 
                          "The specified flight routine does not exist. Please search for available flights and use a valid flight routine ID."));
            } else if (e.getMessage().contains("Flight routine not found")) {
                return ResponseEntity.status(404)
                    .body(new ErrorResponse("FLIGHT_ROUTINE_NOT_FOUND", 
                          "The specified flight routine does not exist. Please search for available flights and use a valid flight routine ID."));
            } else if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404)
                    .body(new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage()));
            } else if (e.getMessage().contains("not available for booking")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("FLIGHT_NOT_AVAILABLE", e.getMessage()));
            } else {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("BOOKING_ERROR", e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("Unexpected error processing booking request", e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable UUID bookingId) {
        
        logger.info("Received booking status request for: {}", bookingId);

        try {
            Optional<BookingResponse> booking = bookingService.getBookingResponse(bookingId);
            
            if (booking.isPresent()) {
                return ResponseEntity.ok(booking.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            logger.error("Error getting booking", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Booking Service is running");
    }

    @GetMapping("/info")
    public ResponseEntity<BookingServiceInfo> getServiceInfo() {
        BookingServiceInfo info = new BookingServiceInfo();
        info.setServiceName("Booking Service");
        info.setVersion("1.0.0");
        info.setDescription("Provides flight booking functionality for airline aggregator");
        info.setBookingExpiryMinutes(15);
        info.setEndpoints(new String[]{
            "POST /api/v1/bookings - Create booking",
            "GET /api/v1/bookings/{id} - Get booking details",
            "GET /api/v1/bookings/health - Health check",
            "GET /api/v1/bookings/info - Service information"
        });
        return ResponseEntity.ok(info);
    }

    // Inner class for service information
    public static class BookingServiceInfo {
        private String serviceName;
        private String version;
        private String description;
        private int bookingExpiryMinutes;
        private String[] endpoints;

        // Getters and Setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getBookingExpiryMinutes() { return bookingExpiryMinutes; }
        public void setBookingExpiryMinutes(int bookingExpiryMinutes) { this.bookingExpiryMinutes = bookingExpiryMinutes; }
        public String[] getEndpoints() { return endpoints; }
        public void setEndpoints(String[] endpoints) { this.endpoints = endpoints; }
    }
} 