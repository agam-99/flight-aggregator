package com.airlineaggregator.payment.controller;

import com.airlineaggregator.payment.dto.PaymentRequest;
import com.airlineaggregator.payment.dto.PaymentResponse;
import com.airlineaggregator.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        
        logger.info("Received payment request for booking: {}", request.getBookingId());

        try {
            PaymentResponse response = paymentService.processPayment(request);
            
            logger.info("Payment processed successfully: {} - Status: {}", 
                       response.getPaymentId(), response.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing payment request", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable UUID paymentId) {
        
        logger.info("Received payment status request for: {}", paymentId);

        try {
            // For demo purposes, return a mock status response
            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", paymentId);
            response.put("status", "success");
            response.put("message", "Payment completed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting payment status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Service is running");
    }

    @GetMapping("/info")
    public ResponseEntity<PaymentServiceInfo> getServiceInfo() {
        PaymentServiceInfo info = new PaymentServiceInfo();
        info.setServiceName("Payment Service");
        info.setVersion("1.0.0");
        info.setDescription("Provides payment processing functionality for airline aggregator");
        info.setMockMode(true);
        info.setAlwaysSuccess(true);
        info.setEndpoints(new String[]{
            "POST /api/v1/payments - Process payment",
            "GET /api/v1/payments/{id} - Get payment status", 
            "GET /api/v1/payments/health - Health check",
            "GET /api/v1/payments/info - Service information"
        });
        return ResponseEntity.ok(info);
    }

    // Test endpoint for easy testing
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testPayment() {
        
        logger.info("Received test payment request");

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("paymentId", UUID.randomUUID());
            response.put("status", "success");
            response.put("transactionId", "TXN_TEST_" + System.currentTimeMillis());
            response.put("message", "Test payment processed successfully");
            response.put("amount", 1000.00);
            response.put("currency", "INR");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing test payment", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Inner class for service information
    public static class PaymentServiceInfo {
        private String serviceName;
        private String version;
        private String description;
        private boolean mockMode;
        private boolean alwaysSuccess;
        private String[] endpoints;

        // Getters and Setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isMockMode() { return mockMode; }
        public void setMockMode(boolean mockMode) { this.mockMode = mockMode; }
        public boolean isAlwaysSuccess() { return alwaysSuccess; }
        public void setAlwaysSuccess(boolean alwaysSuccess) { this.alwaysSuccess = alwaysSuccess; }
        public String[] getEndpoints() { return endpoints; }
        public void setEndpoints(String[] endpoints) { this.endpoints = endpoints; }
    }
} 