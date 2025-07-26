package com.airlineaggregator.booking.controller;

import com.airlineaggregator.booking.dto.BookingRequest;
import com.airlineaggregator.booking.dto.BookingResponse;
import com.airlineaggregator.booking.entity.Booking;
import com.airlineaggregator.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createBooking_ValidRequest_ReturnsBookingResponse() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        BookingResponse mockResponse = createMockBookingResponse();
        
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).createBooking(any(BookingRequest.class));
    }

    @Test
    void createBooking_InsufficientSeats_ReturnsBadRequestWithStructuredError() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new RuntimeException("Insufficient seats available. Requested: 2, Available: 1"));

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_SEATS"))
                .andExpect(jsonPath("$.message").value("Insufficient seats available. Requested: 2, Available: 1"));

        verify(bookingService).createBooking(any(BookingRequest.class));
    }

    @Test
    void createBooking_FlightRoutineNotFound_ReturnsNotFoundWithStructuredError() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new RuntimeException("Booking creation failed: Flight routine not found: " + request.getFlightRoutineId()));

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("FLIGHT_ROUTINE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("The specified flight routine does not exist. Please search for available flights and use a valid flight routine ID."));

        verify(bookingService).createBooking(any(BookingRequest.class));
    }

    @Test
    void createBooking_UnexpectedException_ReturnsBadRequest() throws Exception {
        // Given
        BookingRequest request = createValidBookingRequest();
        
        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new IllegalStateException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BOOKING_ERROR"));

        verify(bookingService).createBooking(any(BookingRequest.class));
    }

    @Test
    void createBooking_InvalidJson_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).createBooking(any());
    }

    @Test
    void getBooking_ExistingBooking_ReturnsOk() throws Exception {
        // Given
        UUID bookingId = UUID.randomUUID();
        BookingResponse mockResponse = createMockBookingResponse();
        
        when(bookingService.getBookingResponse(bookingId))
                .thenReturn(Optional.of(mockResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/{bookingId}", bookingId))
                .andExpect(status().isOk());

        verify(bookingService).getBookingResponse(bookingId);
    }

    @Test
    void getBooking_NonExistentBooking_ReturnsNotFound() throws Exception {
        // Given
        UUID bookingId = UUID.randomUUID();
        
        when(bookingService.getBookingResponse(bookingId))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/{bookingId}", bookingId))
                .andExpect(status().isNotFound());

        verify(bookingService).getBookingResponse(bookingId);
    }

    @Test
    void getBooking_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Given
        UUID bookingId = UUID.randomUUID();
        
        when(bookingService.getBookingResponse(bookingId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/v1/bookings/{bookingId}", bookingId))
                .andExpect(status().isInternalServerError());

        verify(bookingService).getBookingResponse(bookingId);
    }

    @Test
    void healthCheck_ReturnsOk() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bookings/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking Service is running"));
    }

    @Test
    void getServiceInfo_ReturnsServiceInformation() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/bookings/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("Booking Service"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    private BookingRequest createValidBookingRequest() {
        BookingRequest request = new BookingRequest();
        request.setFlightRoutineId(UUID.randomUUID());

        BookingRequest.PassengerInfo passenger1 = new BookingRequest.PassengerInfo();
        passenger1.setTitle("Mr");
        passenger1.setFirstName("John");
        passenger1.setLastName("Doe");
        passenger1.setDateOfBirth("1990-01-15");
        passenger1.setNationality("Indian");
        passenger1.setSeatPreference("window");

        BookingRequest.PassengerInfo passenger2 = new BookingRequest.PassengerInfo();
        passenger2.setTitle("Ms");
        passenger2.setFirstName("Jane");
        passenger2.setLastName("Smith");
        passenger2.setDateOfBirth("1992-05-20");
        passenger2.setNationality("Indian");
        passenger2.setSeatPreference("aisle");

        request.setPassengers(Arrays.asList(passenger1, passenger2));

        BookingRequest.ContactInfo contact = new BookingRequest.ContactInfo();
        contact.setEmail("john.doe@example.com");
        contact.setPhone("+91-9876543210");
        request.setContactInfo(contact);

        return request;
    }

    private BookingResponse createMockBookingResponse() {
        BookingResponse response = new BookingResponse();
        response.setBookingId(UUID.randomUUID());
        response.setStatus("CONFIRMED");
        response.setBookingReference("BK-" + System.currentTimeMillis());
        response.setExpiryTime(LocalDateTime.now().plusMinutes(15));

        return response;
    }

    private Booking createMockBooking(UUID bookingId) {
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setPnr("BK-" + System.currentTimeMillis());
        booking.setStatus("CONFIRMED");
        booking.setTotalAmount(BigDecimal.valueOf(14258.16));
        booking.setCreatedAt(LocalDateTime.now());
        return booking;
    }
} 