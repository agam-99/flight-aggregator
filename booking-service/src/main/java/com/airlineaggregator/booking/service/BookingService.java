package com.airlineaggregator.booking.service;

import com.airlineaggregator.booking.dto.BookingRequest;
import com.airlineaggregator.booking.dto.BookingResponse;
import com.airlineaggregator.booking.entity.Booking;
import com.airlineaggregator.booking.entity.FlightRoutine;
import com.airlineaggregator.booking.repository.BookingRepository;
import com.airlineaggregator.booking.repository.FlightRoutineRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightRoutineRepository flightRoutineRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final Random random = new Random();

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingResponse createBooking(BookingRequest request) {
        logger.info("Creating booking for flight routine: {}, passengers: {}", 
                   request.getFlightRoutineId(), request.getPassengers().size());

        try {
            int requestedSeats = request.getPassengers().size();
            UUID flightRoutineId = request.getFlightRoutineId();

            // Lock the flight routine row to prevent concurrent modifications and fetch flight/airline data
            FlightRoutine flightRoutine = flightRoutineRepository.findByIdWithLock(flightRoutineId)
                    .orElseThrow(() -> new RuntimeException("Flight routine not found: " + flightRoutineId));

            // Check availability after locking
            if (flightRoutine.getAvailableSeats() < requestedSeats) {
                logger.warn("Insufficient seats for flight routine: {}. Requested: {}, Available: {}", 
                           flightRoutineId, requestedSeats, flightRoutine.getAvailableSeats());
                throw new RuntimeException("Insufficient seats available. Requested: " + requestedSeats + 
                                         ", Available: " + flightRoutine.getAvailableSeats());
            }

            // Check if flight is in valid status for booking
            if (!"scheduled".equalsIgnoreCase(flightRoutine.getStatus())) {
                throw new RuntimeException("Flight is not available for booking. Status: " + flightRoutine.getStatus());
            }

            // Atomically update available seats
            int updatedRows = flightRoutineRepository.updateAvailableSeats(flightRoutineId, requestedSeats);
            if (updatedRows == 0) {
                // This means the seat update failed due to insufficient seats or concurrent modification
                logger.warn("Failed to reserve seats for flight routine: {}. Seats may have been booked by another user.", flightRoutineId);
                throw new RuntimeException("Unable to reserve seats. Please try again or choose a different flight.");
            }

            logger.info("Successfully reserved {} seats for flight routine: {}", requestedSeats, flightRoutineId);

            // Calculate total amount
            BigDecimal totalAmount = calculateTotalAmount(flightRoutine, requestedSeats);

            // Calculate expiry time (15 minutes from now)
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

            // Convert passenger details and contact info to JSON
            String passengerDetails = objectMapper.writeValueAsString(request.getPassengers());
            String contactInfo = objectMapper.writeValueAsString(request.getContactInfo());

            // For demo purposes, use null user_id (anonymous booking)
            UUID userId = null;

            // Create booking entity
            Booking booking = new Booking(
                    userId,
                    flightRoutine,
                    totalAmount,
                    passengerDetails,
                    contactInfo,
                    expiresAt
            );

            // Save booking
            booking = bookingRepository.save(booking);

            // Create response
            BookingResponse response = createBookingResponse(booking, flightRoutine, requestedSeats);

            logger.info("Booking created successfully: {} for flight routine: {}. Seats reserved: {}", 
                       booking.getBookingId(), flightRoutine.getId(), requestedSeats);

            return response;

        } catch (Exception e) {
            logger.error("Failed to create booking for flight routine: {}", request.getFlightRoutineId(), e);
            throw new RuntimeException("Booking creation failed: " + e.getMessage(), e);
        }
    }

    private BigDecimal calculateTotalAmount(FlightRoutine flightRoutine, int passengers) {
        BigDecimal baseAmount = flightRoutine.getCurrentPrice().multiply(BigDecimal.valueOf(passengers));
        
        // Calculate taxes (12% of base fare)
        BigDecimal taxes = baseAmount.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
        
        // Calculate fees (3% of base fare)
        BigDecimal fees = baseAmount.multiply(BigDecimal.valueOf(0.03)).setScale(2, RoundingMode.HALF_UP);
        
        return baseAmount.add(taxes).add(fees);
    }

    private BookingResponse createBookingResponse(Booking booking, FlightRoutine flightRoutine, int seatsHeld) {
        BookingResponse response = new BookingResponse();
        
        response.setBookingId(booking.getBookingId());
        response.setStatus(booking.getStatus());
        response.setExpiryTime(booking.getExpiresAt());
        response.setSeatsHeld(seatsHeld);
        response.setBookingReference(generateBookingReference());
        response.setPaymentUrl("http://localhost:8083/api/v1/payments");

        // Flight details - now using actual flight data
        String flightNumber = flightRoutine.getFlight().getFlightNumber();
        String route = flightRoutine.getFlight().getRouteDisplay() != null ? 
                      flightRoutine.getFlight().getRouteDisplay() :
                      flightRoutine.getFlight().getSourceAirport() + " - " + flightRoutine.getFlight().getDestinationAirport();
        String timeRange = flightRoutine.getDepartureTime() + " - " + flightRoutine.getArrivalTime();
        
        BookingResponse.FlightDetails flightDetails = new BookingResponse.FlightDetails(
                flightNumber,
                timeRange,
                flightRoutine.getDepartureTime(),
                flightRoutine.getTravelDate(),
                flightRoutine.getArrivalTime()
        );
        response.setFlightDetails(flightDetails);

        // Pricing details
        BigDecimal baseFare = booking.getTotalAmount().divide(BigDecimal.valueOf(1.15), 2, RoundingMode.HALF_UP);
        BigDecimal taxes = baseFare.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fees = baseFare.multiply(BigDecimal.valueOf(0.03)).setScale(2, RoundingMode.HALF_UP);

        BookingResponse.PriceBreakdown breakdown = new BookingResponse.PriceBreakdown(baseFare, taxes, fees);
        BookingResponse.PricingDetails pricing = new BookingResponse.PricingDetails(
                booking.getTotalAmount(),
                booking.getCurrency(),
                breakdown
        );
        response.setPricing(pricing);

        return response;
    }

    private String generateBookingReference() {
        return "BK" + System.currentTimeMillis() + random.nextInt(1000);
    }

    @Transactional(readOnly = true)
    public Optional<Booking> getBooking(UUID bookingId) {
        return bookingRepository.findByBookingId(bookingId);
    }

    @Transactional(readOnly = true)
    public Optional<BookingResponse> getBookingResponse(UUID bookingId) {
        try {
            // Get the booking with flight routine data
            Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
            if (bookingOpt.isEmpty()) {
                return Optional.empty();
            }
            
            Booking booking = bookingOpt.get();
            
            // Get flight routine with flight and airline data
            Optional<FlightRoutine> flightRoutineOpt = flightRoutineRepository
                    .findByIdWithFlightAndAirline(booking.getFlightRoutine().getId());
            
            if (flightRoutineOpt.isEmpty()) {
                logger.warn("Flight routine not found for booking: {}", bookingId);
                return Optional.empty();
            }
            
            FlightRoutine flightRoutine = flightRoutineOpt.get();
            
            // Count passengers from JSON
            int passengerCount = countPassengersFromBooking(booking);
            
            // Create response with actual flight data
            BookingResponse response = new BookingResponse();
            response.setBookingId(booking.getBookingId());
            response.setStatus(booking.getStatus());
            response.setBookingReference(booking.getPnr());
            response.setExpiryTime(booking.getExpiresAt());
            response.setSeatsHeld(passengerCount);
            
            // Create flight details with actual data
            String flightNumber = flightRoutine.getFlight().getFlightNumber();
            String route = flightRoutine.getFlight().getRouteDisplay() != null ? 
                          flightRoutine.getFlight().getRouteDisplay() :
                          flightRoutine.getFlight().getSourceAirport() + " - " + flightRoutine.getFlight().getDestinationAirport();
            String timeRange = flightRoutine.getDepartureTime() + " - " + flightRoutine.getArrivalTime();
            
            BookingResponse.FlightDetails flightDetails = new BookingResponse.FlightDetails(
                    flightNumber,
                    timeRange,
                    flightRoutine.getDepartureTime(),
                    flightRoutine.getTravelDate(),
                    flightRoutine.getArrivalTime()
            );
            response.setFlightDetails(flightDetails);
            
            // Create pricing details
            BigDecimal totalAmount = booking.getTotalAmount();
            String currency = booking.getCurrency();
            
            // Calculate breakdown (same logic as in creation)
            BigDecimal baseFare = totalAmount.divide(BigDecimal.valueOf(1.15), 2, RoundingMode.HALF_UP);
            BigDecimal taxes = baseFare.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal fees = baseFare.multiply(BigDecimal.valueOf(0.03)).setScale(2, RoundingMode.HALF_UP);
            
            BookingResponse.PriceBreakdown breakdown = new BookingResponse.PriceBreakdown(baseFare, taxes, fees);
            BookingResponse.PricingDetails pricing = new BookingResponse.PricingDetails(
                    totalAmount,
                    currency,
                    breakdown
            );
            response.setPricing(pricing);
            response.setPaymentUrl("http://localhost:8083/api/v1/payments");
            
            return Optional.of(response);
            
        } catch (Exception e) {
            logger.error("Error getting booking response for: {}", bookingId, e);
            return Optional.empty();
        }
    }

    @Transactional
    public void expireOldBookings() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find all bookings that are about to expire to release their seats
        List<Booking> expiringBookings = bookingRepository.findExpiredPendingBookings(now);
        
        // Release seats for each expiring booking
        for (Booking booking : expiringBookings) {
            try {
                // Count passengers to determine seats to release
                int seatsToRelease = countPassengersFromBooking(booking);
                if (seatsToRelease > 0) {
                    int releasedRows = flightRoutineRepository.releaseSeats(
                            booking.getFlightRoutine().getId(), 
                            seatsToRelease
                    );
                    if (releasedRows > 0) {
                        logger.info("Released {} seats for expired booking: {}", 
                                   seatsToRelease, booking.getBookingId());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to release seats for expired booking: {}", 
                           booking.getBookingId(), e);
            }
        }
        
        // Now expire the bookings
        int expiredBookings = bookingRepository.expireOldBookings(now);
        if (expiredBookings > 0) {
            logger.info("Expired {} old bookings and released their seats", expiredBookings);
        }
    }

    @Transactional
    public void releaseSeatsForBooking(UUID bookingId, String reason) {
        logger.info("Releasing seats for booking: {} due to: {}", bookingId, reason);
        
        try {
            Optional<Booking> bookingOpt = bookingRepository.findByBookingId(bookingId);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                
                // Only release seats for bookings that are pending or confirmed
                if ("pending".equals(booking.getStatus()) || "confirmed".equals(booking.getStatus())) {
                    int seatsToRelease = countPassengersFromBooking(booking);
                    if (seatsToRelease > 0) {
                        int releasedRows = flightRoutineRepository.releaseSeats(
                                booking.getFlightRoutine().getId(), 
                                seatsToRelease
                        );
                        if (releasedRows > 0) {
                            logger.info("Released {} seats for booking: {} due to: {}", 
                                       seatsToRelease, bookingId, reason);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to release seats for booking: {} due to: {}", bookingId, reason, e);
        }
    }

    private int countPassengersFromBooking(Booking booking) {
        try {
            if (booking.getPassengerDetails() != null) {
                // Parse the JSON to count passengers
                Object[] passengers = objectMapper.readValue(booking.getPassengerDetails(), Object[].class);
                return passengers.length;
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse passenger details for booking: {}", booking.getBookingId(), e);
        }
        return 0;
    }
} 