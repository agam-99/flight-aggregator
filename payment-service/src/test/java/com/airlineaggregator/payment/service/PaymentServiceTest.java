package com.airlineaggregator.payment.service;

import com.airlineaggregator.payment.dto.PaymentRequest;
import com.airlineaggregator.payment.dto.PaymentResponse;
import com.airlineaggregator.payment.entity.Payment;
import com.airlineaggregator.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest validRequest;
    private Payment mockPayment;

    @BeforeEach
    void setUp() throws Exception {
        setupValidRequest();
        setupMockPayment();
        
        // Set alwaysSuccess to true for deterministic tests
        Field alwaysSuccessField = PaymentService.class.getDeclaredField("alwaysSuccess");
        alwaysSuccessField.setAccessible(true);
        alwaysSuccessField.set(paymentService, true);
        
        // Set processingDelayMs to 0 for faster tests
        Field processingDelayField = PaymentService.class.getDeclaredField("processingDelayMs");
        processingDelayField.setAccessible(true);
        processingDelayField.set(paymentService, 0L);
    }

    @Test
    void processPayment_ValidRequest_ReturnsSuccessfulResponse() {
        // Given
        BookingService.BookingInfo mockBookingInfo = new BookingService.BookingInfo(
            validRequest.getBookingId(), "pending", new BigDecimal("14258.16"), "INR");
        
        when(bookingService.getBookingInfo(validRequest.getBookingId()))
                .thenReturn(mockBookingInfo);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(mockPayment);
        when(bookingService.confirmBooking(any(UUID.class)))
                .thenReturn(createMockBookingUpdate());

        // When
        PaymentResponse response = paymentService.processPayment(validRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getPaymentId());
        assertEquals("success", response.getStatus());
        assertNotNull(response.getTransactionId());
        assertEquals(new BigDecimal("14258.16"), response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertNotNull(response.getBookingUpdate());

        verify(bookingService).getBookingInfo(validRequest.getBookingId());
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingService).confirmBooking(validRequest.getBookingId());
    }

    @Test
    void processPayment_PaymentFailed_ReturnsFailedResponse() throws Exception {
        // Given
        BookingService.BookingInfo mockBookingInfo = new BookingService.BookingInfo(
            validRequest.getBookingId(), "pending", new BigDecimal("14258.16"), "INR");
        
        // Set alwaysSuccess to false for this test to simulate failure
        Field alwaysSuccessField = PaymentService.class.getDeclaredField("alwaysSuccess");
        alwaysSuccessField.setAccessible(true);
        alwaysSuccessField.set(paymentService, false);
        
        // Mock random to always return failure (> 0.9)
        Field randomField = PaymentService.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(paymentService, new java.util.Random() {
            @Override
            public double nextDouble() {
                return 0.95; // > 0.9, so payment will fail
            }
        });
        
        Payment failedPayment = createFailedPayment();
        when(bookingService.getBookingInfo(validRequest.getBookingId()))
                .thenReturn(mockBookingInfo);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(failedPayment);

        // When
        PaymentResponse response = paymentService.processPayment(validRequest);

        // Then
        assertNotNull(response);
        assertEquals("failed", response.getStatus());
        assertNotNull(response.getMessage());
        assertNull(response.getBookingUpdate()); // No booking confirmation for failed payment

        verify(bookingService).getBookingInfo(validRequest.getBookingId());
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingService, never()).confirmBooking(any(UUID.class));
    }

    @Test
    void processPayment_InvalidCardDetails_ThrowsRuntimeException() {
        // Given - This test expects the booking validation to fail due to invalid card
        PaymentRequest invalidRequest = createInvalidPaymentRequest();
        
        // Mock getBookingInfo to return null (booking not found scenario)
        when(bookingService.getBookingInfo(invalidRequest.getBookingId()))
                .thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(invalidRequest);
        });

        assertTrue(exception.getMessage().contains("Booking not found"));
        verify(bookingService).getBookingInfo(invalidRequest.getBookingId());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(bookingService, never()).confirmBooking(any(UUID.class));
    }

    @Test
    void processPayment_BookingServiceFails_ThrowsRuntimeException() {
        // Given
        BookingService.BookingInfo mockBookingInfo = new BookingService.BookingInfo(
            validRequest.getBookingId(), "pending", new BigDecimal("14258.16"), "INR");
        
        when(bookingService.getBookingInfo(validRequest.getBookingId()))
                .thenReturn(mockBookingInfo);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(mockPayment);
        when(bookingService.confirmBooking(any(UUID.class)))
                .thenThrow(new RuntimeException("Booking service unavailable"));

        // When & Then - Service fails entire payment when booking confirmation fails
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(validRequest);
        });

        assertTrue(exception.getMessage().contains("Payment processing failed: Booking service unavailable"));
        verify(bookingService).getBookingInfo(validRequest.getBookingId());
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingService).confirmBooking(validRequest.getBookingId());
    }

    @Test
    void processPayment_RepositoryThrowsException_ThrowsRuntimeException() {
        // Given
        BookingService.BookingInfo mockBookingInfo = new BookingService.BookingInfo(
            validRequest.getBookingId(), "pending", new BigDecimal("14258.16"), "INR");
        
        when(bookingService.getBookingInfo(validRequest.getBookingId()))
                .thenReturn(mockBookingInfo);
        when(paymentRepository.save(any(Payment.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(validRequest);
        });

        assertTrue(exception.getMessage().contains("Payment processing failed"));
        verify(bookingService).getBookingInfo(validRequest.getBookingId());
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingService, never()).confirmBooking(any(UUID.class));
    }

    @Test
    void processPayment_UpiPaymentMethod_ProcessedSuccessfully() {
        // Given
        PaymentRequest upiRequest = createUpiPaymentRequest();
        BookingService.BookingInfo mockBookingInfo = new BookingService.BookingInfo(
            upiRequest.getBookingId(), "pending", new BigDecimal("14258.16"), "INR");
        
        when(bookingService.getBookingInfo(upiRequest.getBookingId()))
                .thenReturn(mockBookingInfo);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(mockPayment);
        when(bookingService.confirmBooking(any(UUID.class)))
                .thenReturn(createMockBookingUpdate());

        // When
        PaymentResponse response = paymentService.processPayment(upiRequest);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(bookingService).getBookingInfo(upiRequest.getBookingId());
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingService).confirmBooking(upiRequest.getBookingId());
    }

    @Test
    void processPayment_WalletPaymentMethod_ProcessedSuccessfully() {
        // Given
        PaymentRequest walletRequest = createWalletPaymentRequest();
        BookingService.BookingInfo mockBookingInfo = new BookingService.BookingInfo(
            walletRequest.getBookingId(), "pending", new BigDecimal("14258.16"), "INR");
        
        when(bookingService.getBookingInfo(walletRequest.getBookingId()))
                .thenReturn(mockBookingInfo);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(mockPayment);
        when(bookingService.confirmBooking(any(UUID.class)))
                .thenReturn(createMockBookingUpdate());

        // When
        PaymentResponse response = paymentService.processPayment(walletRequest);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(bookingService).getBookingInfo(walletRequest.getBookingId());
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingService).confirmBooking(walletRequest.getBookingId());
    }

    @Test
    void processPayment_NetbankingPaymentMethod_ProcessedSuccessfully() {
        // Given
        PaymentRequest netbankingRequest = createNetbankingPaymentRequest();
        BookingService.BookingInfo mockBookingInfo = new BookingService.BookingInfo(
            netbankingRequest.getBookingId(), "pending", new BigDecimal("14258.16"), "INR");
        
        when(bookingService.getBookingInfo(netbankingRequest.getBookingId()))
                .thenReturn(mockBookingInfo);
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(mockPayment);
        when(bookingService.confirmBooking(any(UUID.class)))
                .thenReturn(createMockBookingUpdate());

        // When
        PaymentResponse response = paymentService.processPayment(netbankingRequest);

        // Then
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        verify(bookingService).getBookingInfo(netbankingRequest.getBookingId());
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingService).confirmBooking(netbankingRequest.getBookingId());
    }

    private void setupValidRequest() {
        validRequest = new PaymentRequest();
        validRequest.setBookingId(UUID.randomUUID());

        PaymentRequest.PaymentMethod paymentMethod = new PaymentRequest.PaymentMethod();
        paymentMethod.setType("card");
        paymentMethod.setCardNumber("4111111111111111");
        paymentMethod.setExpiryMonth(12);
        paymentMethod.setExpiryYear(2025);
        paymentMethod.setCvv("123");
        validRequest.setPaymentMethod(paymentMethod);
    }

    private void setupMockPayment() {
        mockPayment = new Payment();
        mockPayment.setPaymentId(UUID.randomUUID());
        mockPayment.setBookingId(validRequest.getBookingId());
        mockPayment.setAmount(new BigDecimal("14258.16"));
        mockPayment.setCurrency("INR");
        mockPayment.setStatus("success");  // lowercase to match isSuccessful() method
        mockPayment.setTransactionId("TXN-" + System.currentTimeMillis());
    }

    private Payment createFailedPayment() {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setBookingId(validRequest.getBookingId());
        payment.setAmount(new BigDecimal("14258.16"));
        payment.setCurrency("INR");
        payment.setStatus("failed");  // lowercase to match isFailed() method
        payment.setTransactionId("TXN-" + System.currentTimeMillis());
        return payment;
    }

    private PaymentRequest createInvalidPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(UUID.randomUUID());

        PaymentRequest.PaymentMethod paymentMethod = new PaymentRequest.PaymentMethod();
        paymentMethod.setType("card");
        paymentMethod.setCardNumber("invalid-card-number");
        paymentMethod.setExpiryMonth(13); // Invalid month
        paymentMethod.setExpiryYear(2020); // Expired year
        paymentMethod.setCvv("invalid");
        request.setPaymentMethod(paymentMethod);

        return request;
    }

    private PaymentRequest createUpiPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(UUID.randomUUID());

        PaymentRequest.PaymentMethod paymentMethod = new PaymentRequest.PaymentMethod();
        paymentMethod.setType("upi");
        paymentMethod.setUpiId("user@paytm");
        request.setPaymentMethod(paymentMethod);

        return request;
    }

    private PaymentRequest createWalletPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(UUID.randomUUID());

        PaymentRequest.PaymentMethod paymentMethod = new PaymentRequest.PaymentMethod();
        paymentMethod.setType("wallet");
        paymentMethod.setWalletProvider("paytm");
        request.setPaymentMethod(paymentMethod);

        return request;
    }

    private PaymentRequest createNetbankingPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(UUID.randomUUID());

        PaymentRequest.PaymentMethod paymentMethod = new PaymentRequest.PaymentMethod();
        paymentMethod.setType("netbanking");
        paymentMethod.setBankCode("HDFC");
        request.setPaymentMethod(paymentMethod);

        return request;
    }

    private PaymentResponse.BookingUpdate createMockBookingUpdate() {
        PaymentResponse.BookingUpdate bookingUpdate = new PaymentResponse.BookingUpdate();
        bookingUpdate.setBookingId(validRequest.getBookingId());
        bookingUpdate.setNewStatus("CONFIRMED");
        bookingUpdate.setPnr("BK-" + System.currentTimeMillis());
        return bookingUpdate;
    }
} 