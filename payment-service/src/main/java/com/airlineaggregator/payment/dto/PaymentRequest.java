package com.airlineaggregator.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class PaymentRequest {

    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    @NotNull(message = "Payment method is required")
    @Valid
    private PaymentMethod paymentMethod;

    // Constructors
    public PaymentRequest() {}

    public PaymentRequest(UUID bookingId, PaymentMethod paymentMethod) {
        this.bookingId = bookingId;
        this.paymentMethod = paymentMethod;
    }

    // Nested class for payment method
    public static class PaymentMethod {
        private String type; // card, upi, netbanking, wallet
        private String cardNumber;
        private Integer expiryMonth;
        private Integer expiryYear;
        private String cvv;
        private String upiId;
        private String bankCode;
        private String walletProvider;

        // Constructors
        public PaymentMethod() {}

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
        public Integer getExpiryMonth() { return expiryMonth; }
        public void setExpiryMonth(Integer expiryMonth) { this.expiryMonth = expiryMonth; }
        public Integer getExpiryYear() { return expiryYear; }
        public void setExpiryYear(Integer expiryYear) { this.expiryYear = expiryYear; }
        public String getCvv() { return cvv; }
        public void setCvv(String cvv) { this.cvv = cvv; }
        public String getUpiId() { return upiId; }
        public void setUpiId(String upiId) { this.upiId = upiId; }
        public String getBankCode() { return bankCode; }
        public void setBankCode(String bankCode) { this.bankCode = bankCode; }
        public String getWalletProvider() { return walletProvider; }
        public void setWalletProvider(String walletProvider) { this.walletProvider = walletProvider; }
    }

    // Main class getters and setters
    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "bookingId=" + bookingId +
                ", paymentMethod=" + paymentMethod.getType() +
                '}';
    }
} 