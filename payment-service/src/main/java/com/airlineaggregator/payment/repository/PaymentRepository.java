package com.airlineaggregator.payment.repository;

import com.airlineaggregator.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentId(UUID paymentId);
    
    List<Payment> findByBookingIdOrderByCreatedAtDesc(UUID bookingId);
    
    List<Payment> findByStatusOrderByCreatedAtDesc(String status);
    
    @Query("SELECT p FROM Payment p WHERE p.bookingId = :bookingId AND p.status = 'success'")
    Optional<Payment> findSuccessfulPaymentByBookingId(@Param("bookingId") UUID bookingId);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'success'")
    Long countSuccessfulPayments();
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'failed'")
    Long countFailedPayments();
    
    boolean existsByTransactionId(String transactionId);
} 