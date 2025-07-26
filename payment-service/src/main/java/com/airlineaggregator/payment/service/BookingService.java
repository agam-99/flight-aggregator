package com.airlineaggregator.payment.service;

import com.airlineaggregator.payment.dto.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Random random = new Random();

    public BookingInfo getBookingInfo(UUID bookingId) {
        try {
            String sql = """
                SELECT b.booking_id, b.status, b.total_amount, b.currency 
                FROM bookings b 
                WHERE b.booking_id = ?
                """;

            return jdbcTemplate.queryForObject(sql, 
                (rs, rowNum) -> new BookingInfo(
                    UUID.fromString(rs.getString("booking_id")),
                    rs.getString("status"),
                    rs.getBigDecimal("total_amount"),
                    rs.getString("currency")
                ), 
                bookingId
            );
        } catch (Exception e) {
            logger.error("Failed to get booking info for ID: {}", bookingId, e);
            return null;
        }
    }

    @Transactional
    public PaymentResponse.BookingUpdate confirmBooking(UUID bookingId) {
        try {
            // Generate a mock PNR
            String pnr = generatePNR();

            // Update booking status to confirmed and set PNR
            String updateSql = """
                UPDATE bookings 
                SET status = 'confirmed', pnr = ?, updated_at = NOW() 
                WHERE booking_id = ? AND status = 'pending'
                """;

            int rowsUpdated = jdbcTemplate.update(updateSql, pnr, bookingId);

            if (rowsUpdated > 0) {
                logger.info("Booking confirmed: {} with PNR: {}", bookingId, pnr);
                
                return new PaymentResponse.BookingUpdate(
                    bookingId,
                    "confirmed",
                    pnr,
                    "Your booking has been confirmed successfully. PNR: " + pnr
                );
            } else {
                logger.warn("No booking found to confirm for ID: {}", bookingId);
                return null;
            }

        } catch (Exception e) {
            logger.error("Failed to confirm booking: {}", bookingId, e);
            throw new RuntimeException("Failed to confirm booking", e);
        }
    }

    private String generatePNR() {
        // Generate a 6-character alphanumeric PNR
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder pnr = new StringBuilder();
        
        for (int i = 0; i < 6; i++) {
            pnr.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return pnr.toString();
    }

    // Inner class for booking information
    public static class BookingInfo {
        private UUID bookingId;
        private String status;
        private BigDecimal totalAmount;
        private String currency;

        public BookingInfo(UUID bookingId, String status, BigDecimal totalAmount, String currency) {
            this.bookingId = bookingId;
            this.status = status;
            this.totalAmount = totalAmount;
            this.currency = currency;
        }

        // Getters and Setters
        public UUID getBookingId() { return bookingId; }
        public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
} 