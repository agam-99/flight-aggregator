package com.airlineaggregator.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public class BookingResponse {

    private UUID bookingId;
    private String status;
    private FlightDetails flightDetails;
    private PricingDetails pricing;
    private LocalDateTime expiryTime;
    private String paymentUrl;
    private Integer seatsHeld;
    private String bookingReference;

    // Constructors
    public BookingResponse() {}

    // Nested classes
    public static class FlightDetails {
        private String flightNumber;
        private String route;
        private LocalTime departureTime;
        private LocalDate travelDate;
        private LocalTime arrivalTime;

        public FlightDetails() {}

        public FlightDetails(String flightNumber, String route, LocalTime departureTime, 
                           LocalDate travelDate, LocalTime arrivalTime) {
            this.flightNumber = flightNumber;
            this.route = route;
            this.departureTime = departureTime;
            this.travelDate = travelDate;
            this.arrivalTime = arrivalTime;
        }

        // Getters and Setters
        public String getFlightNumber() { return flightNumber; }
        public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }
        public LocalTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
        public LocalDate getTravelDate() { return travelDate; }
        public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
        public LocalTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    }

    public static class PricingDetails {
        private BigDecimal totalAmount;
        private String currency;
        private PriceBreakdown breakdown;

        public PricingDetails() {}

        public PricingDetails(BigDecimal totalAmount, String currency, PriceBreakdown breakdown) {
            this.totalAmount = totalAmount;
            this.currency = currency;
            this.breakdown = breakdown;
        }

        // Getters and Setters
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public PriceBreakdown getBreakdown() { return breakdown; }
        public void setBreakdown(PriceBreakdown breakdown) { this.breakdown = breakdown; }
    }

    public static class PriceBreakdown {
        private BigDecimal baseFare;
        private BigDecimal taxes;
        private BigDecimal fees;

        public PriceBreakdown() {}

        public PriceBreakdown(BigDecimal baseFare, BigDecimal taxes, BigDecimal fees) {
            this.baseFare = baseFare;
            this.taxes = taxes;
            this.fees = fees;
        }

        // Getters and Setters
        public BigDecimal getBaseFare() { return baseFare; }
        public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }
        public BigDecimal getTaxes() { return taxes; }
        public void setTaxes(BigDecimal taxes) { this.taxes = taxes; }
        public BigDecimal getFees() { return fees; }
        public void setFees(BigDecimal fees) { this.fees = fees; }
    }

    // Main class getters and setters
    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public FlightDetails getFlightDetails() { return flightDetails; }
    public void setFlightDetails(FlightDetails flightDetails) { this.flightDetails = flightDetails; }
    public PricingDetails getPricing() { return pricing; }
    public void setPricing(PricingDetails pricing) { this.pricing = pricing; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
    public Integer getSeatsHeld() { return seatsHeld; }
    public void setSeatsHeld(Integer seatsHeld) { this.seatsHeld = seatsHeld; }
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
} 