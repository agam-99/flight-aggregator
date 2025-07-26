package com.airlineaggregator.payment.service;

import com.airlineaggregator.payment.dto.PaymentRequest;
import com.airlineaggregator.payment.dto.PaymentResponse;
import com.airlineaggregator.payment.entity.Payment;
import com.airlineaggregator.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${payment.mock-mode:true}")
    private boolean mockMode;

    @Value("${payment.always-success:true}")
    private boolean alwaysSuccess;

    @Value("${payment.processing-delay-ms:2000}")
    private long processingDelayMs;

    private final Random random = new Random();

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        logger.info("Processing payment for booking: {}", request.getBookingId());

        try {
            // Validate booking exists and get amount
            BookingService.BookingInfo bookingInfo = bookingService.getBookingInfo(request.getBookingId());
            if (bookingInfo == null) {
                throw new RuntimeException("Booking not found: " + request.getBookingId());
            }

            if (!"pending".equals(bookingInfo.getStatus())) {
                throw new RuntimeException("Booking is not in pending status: " + bookingInfo.getStatus());
            }

            // Create payment record
            Payment payment = new Payment(
                    request.getBookingId(),
                    bookingInfo.getTotalAmount(),
                    request.getPaymentMethod().getType()
            );

            // Simulate payment processing delay
            if (processingDelayMs > 0) {
                try {
                    Thread.sleep(processingDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Mock payment processing
            PaymentResult paymentResult = mockPaymentProcessing(request, payment);

            // Update payment with result
            payment.setStatus(paymentResult.getStatus());
            payment.setTransactionId(paymentResult.getTransactionId());
            payment.setGatewayResponse(paymentResult.getGatewayResponse());
            payment.setProcessedAt(LocalDateTime.now());

            // Save payment
            payment = paymentRepository.save(payment);

            // Update booking status if payment successful
            PaymentResponse.BookingUpdate bookingUpdate = null;
            if (payment.isSuccessful()) {
                bookingUpdate = bookingService.confirmBooking(request.getBookingId());
                logger.info("Booking confirmed: {} with PNR: {}", request.getBookingId(), 
                           bookingUpdate != null ? bookingUpdate.getPnr() : "N/A");
            }

            // Create response
            PaymentResponse response = new PaymentResponse(
                    payment.getPaymentId(),
                    payment.getStatus(),
                    payment.getTransactionId(),
                    payment.getAmount(),
                    payment.getCurrency()
            );

            response.setMessage(paymentResult.getMessage());
            response.setBookingUpdate(bookingUpdate);

            logger.info("Payment processed successfully: {} - Status: {}", 
                       payment.getPaymentId(), payment.getStatus());

            return response;

        } catch (Exception e) {
            logger.error("Payment processing failed for booking: {}", request.getBookingId(), e);
            throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    private PaymentResult mockPaymentProcessing(PaymentRequest request, Payment payment) {
        PaymentResult result = new PaymentResult();

        if (mockMode) {
            logger.info("Mock payment processing enabled");

            if (alwaysSuccess) {
                // Always return success
                result.setStatus("success");
                result.setTransactionId("TXN_" + System.currentTimeMillis() + "_" + random.nextInt(1000));
                result.setMessage("Payment processed successfully");
                result.setGatewayResponse(createMockSuccessResponse(request));
            } else {
                // Random success/failure (90% success rate)
                boolean success = random.nextDouble() < 0.9;

                if (success) {
                    result.setStatus("success");
                    result.setTransactionId("TXN_" + System.currentTimeMillis() + "_" + random.nextInt(1000));
                    result.setMessage("Payment processed successfully");
                    result.setGatewayResponse(createMockSuccessResponse(request));
                } else {
                    result.setStatus("failed");
                    result.setTransactionId("TXN_FAIL_" + System.currentTimeMillis());
                    result.setMessage("Payment failed - Insufficient funds");
                    result.setGatewayResponse(createMockFailureResponse(request));
                }
            }
        } else {
            // In real mode, you would integrate with actual payment gateways
            // For now, we'll still mock it
            result.setStatus("success");
            result.setTransactionId("TXN_REAL_" + System.currentTimeMillis());
            result.setMessage("Payment processed via real gateway");
            result.setGatewayResponse(createMockSuccessResponse(request));
        }

        return result;
    }

    private String createMockSuccessResponse(PaymentRequest request) {
        try {
            return objectMapper.writeValueAsString(new MockGatewayResponse(
                    "SUCCESS",
                    "Payment completed successfully",
                    request.getPaymentMethod().getType(),
                    "AUTHORIZED",
                    System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return "{\"status\":\"SUCCESS\",\"message\":\"Payment completed\"}";
        }
    }

    private String createMockFailureResponse(PaymentRequest request) {
        try {
            return objectMapper.writeValueAsString(new MockGatewayResponse(
                    "FAILED",
                    "Insufficient funds in account",
                    request.getPaymentMethod().getType(),
                    "DECLINED",
                    System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return "{\"status\":\"FAILED\",\"message\":\"Payment failed\"}";
        }
    }

    // Inner classes for structured responses
    private static class PaymentResult {
        private String status;
        private String transactionId;
        private String message;
        private String gatewayResponse;

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getGatewayResponse() { return gatewayResponse; }
        public void setGatewayResponse(String gatewayResponse) { this.gatewayResponse = gatewayResponse; }
    }

    private static class MockGatewayResponse {
        private String status;
        private String message;
        private String paymentMethod;
        private String authCode;
        private Long timestamp;

        public MockGatewayResponse(String status, String message, String paymentMethod, String authCode, Long timestamp) {
            this.status = status;
            this.message = message;
            this.paymentMethod = paymentMethod;
            this.authCode = authCode;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getAuthCode() { return authCode; }
        public void setAuthCode(String authCode) { this.authCode = authCode; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
} 