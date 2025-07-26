package com.airlineaggregator.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentResponse {

    private UUID paymentId;
    private String status;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime processedAt;
    private String message;
    private BookingUpdate bookingUpdate;

    // Constructors
    public PaymentResponse() {}

    public PaymentResponse(UUID paymentId, String status, String transactionId, 
                          BigDecimal amount, String currency) {
        this.paymentId = paymentId;
        this.status = status;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.processedAt = LocalDateTime.now();
    }

    // Nested class for booking update information
    public static class BookingUpdate {
        private UUID bookingId;
        private String newStatus;
        private String pnr;
        private String confirmationMessage;

        public BookingUpdate() {}

        public BookingUpdate(UUID bookingId, String newStatus, String pnr, String confirmationMessage) {
            this.bookingId = bookingId;
            this.newStatus = newStatus;
            this.pnr = pnr;
            this.confirmationMessage = confirmationMessage;
        }

        // Getters and Setters
        public UUID getBookingId() { return bookingId; }
        public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
        public String getPnr() { return pnr; }
        public void setPnr(String pnr) { this.pnr = pnr; }
        public String getConfirmationMessage() { return confirmationMessage; }
        public void setConfirmationMessage(String confirmationMessage) { this.confirmationMessage = confirmationMessage; }
    }

    // Getters and Setters
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public BookingUpdate getBookingUpdate() { return bookingUpdate; }
    public void setBookingUpdate(BookingUpdate bookingUpdate) { this.bookingUpdate = bookingUpdate; }
} 