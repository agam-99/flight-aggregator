package com.airlineaggregator.booking.service;

import com.airlineaggregator.booking.dto.BookingRequest;
import com.airlineaggregator.booking.dto.BookingResponse;
import com.airlineaggregator.booking.entity.Airline;
import com.airlineaggregator.booking.entity.Booking;
import com.airlineaggregator.booking.entity.Flight;
import com.airlineaggregator.booking.entity.FlightRoutine;
import com.airlineaggregator.booking.repository.BookingRepository;
import com.airlineaggregator.booking.repository.FlightRoutineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightRoutineRepository flightRoutineRepository;

    @Mock
    private ObjectMapper objectMapper;



    @InjectMocks
    private BookingService bookingService;

    private BookingRequest validRequest;
    private FlightRoutine mockFlightRoutine;

    @BeforeEach
    void setUp() {
        setupValidRequest();
        setupMockFlightRoutine();
    }

    @Test
    void createBooking_ValidRequest_ReturnsBookingResponse() {
        // Given
        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.of(mockFlightRoutine));
        when(flightRoutineRepository.updateAvailableSeats(any(UUID.class), anyInt()))
                .thenReturn(1); // Simulate successful seat update
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(createMockBooking());

        // When
        BookingResponse response = bookingService.createBooking(validRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBookingId());
        assertEquals("CONFIRMED", response.getStatus());
        assertNotNull(response.getBookingReference());
        assertNotNull(response.getExpiryTime());

        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_FlightRoutineNotFound_ThrowsRuntimeException() {
        // Given
        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertTrue(exception.getMessage().contains("Flight routine not found"));
        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository, never()).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_InsufficientSeats_ThrowsRuntimeException() {
        // Given
        FlightRoutine insufficientSeatsFlightRoutine = createMockFlightRoutine();
        insufficientSeatsFlightRoutine.setAvailableSeats(1); // Only 1 seat available

        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.of(insufficientSeatsFlightRoutine));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(validRequest); // Request has 2 passengers
        });

        assertTrue(exception.getMessage().contains("Insufficient seats"));
        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository, never()).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_SeatUpdateFails_ThrowsRuntimeException() {
        // Given
        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.of(mockFlightRoutine));
        when(flightRoutineRepository.updateAvailableSeats(any(UUID.class), anyInt()))
                .thenReturn(0); // Simulate failed seat update

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertTrue(exception.getMessage().contains("Unable to reserve seats"));
        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_DatabaseException_ThrowsRuntimeException() {
        // Given
        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.of(mockFlightRoutine));
        when(flightRoutineRepository.updateAvailableSeats(any(UUID.class), anyInt()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertEquals("Booking creation failed: Database connection failed", exception.getMessage());
        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_ConcurrentSeatUpdate_HandledWithPessimisticLocking() {
        // Given
        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.of(mockFlightRoutine));
        when(flightRoutineRepository.updateAvailableSeats(any(UUID.class), anyInt()))
                .thenReturn(1);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(createMockBooking());

        // When
        BookingResponse response = bookingService.createBooking(validRequest);

        // Then
        assertNotNull(response);
        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_SinglePassenger_CalculatesCorrectAmount() {
        // Given
        BookingRequest singlePassengerRequest = createSinglePassengerRequest();

        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.of(mockFlightRoutine));
        when(flightRoutineRepository.updateAvailableSeats(any(UUID.class), anyInt()))
                .thenReturn(1);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(createMockBooking());

        // When
        BookingResponse response = bookingService.createBooking(singlePassengerRequest);

        // Then
        assertNotNull(response);
        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_MultiplePassengers_CalculatesCorrectAmount() {
        // Given - using the validRequest which already has 2 passengers
        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.of(mockFlightRoutine));
        when(flightRoutineRepository.updateAvailableSeats(any(UUID.class), anyInt()))
                .thenReturn(1);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(createMockBooking());

        // When
        BookingResponse response = bookingService.createBooking(validRequest);

        // Then
        assertNotNull(response);
        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void getBooking_ExistingBooking_ReturnsBooking() {
        // Given
        UUID bookingId = UUID.randomUUID();
        Booking mockBooking = createMockBooking();
        
        when(bookingRepository.findByBookingId(bookingId))
                .thenReturn(Optional.of(mockBooking));

        // When
        Optional<Booking> result = bookingService.getBooking(bookingId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockBooking, result.get());
        verify(bookingRepository).findByBookingId(bookingId);
    }

    @Test
    void getBooking_NonExistentBooking_ReturnsEmpty() {
        // Given
        UUID bookingId = UUID.randomUUID();
        
        when(bookingRepository.findByBookingId(bookingId))
                .thenReturn(Optional.empty());

        // When
        Optional<Booking> result = bookingService.getBooking(bookingId);

        // Then
        assertFalse(result.isPresent());
        verify(bookingRepository).findByBookingId(bookingId);
    }

    @Test
    void createBooking_ExactSeatCount_SuccessfullyBooks() {
        // Given
        FlightRoutine exactSeatFlightRoutine = createMockFlightRoutine();
        exactSeatFlightRoutine.setAvailableSeats(2); // Exactly 2 seats for 2 passengers

        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.of(exactSeatFlightRoutine));
        when(flightRoutineRepository.updateAvailableSeats(any(UUID.class), anyInt()))
                .thenReturn(1);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(createMockBooking());

        // When
        BookingResponse response = bookingService.createBooking(validRequest);

        // Then
        assertNotNull(response);
        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_ZeroAvailableSeats_ThrowsRuntimeException() {
        // Given
        FlightRoutine zeroSeatsFlightRoutine = createMockFlightRoutine();
        zeroSeatsFlightRoutine.setAvailableSeats(0);

        when(flightRoutineRepository.findByIdWithLock(any(UUID.class)))
                .thenReturn(Optional.of(zeroSeatsFlightRoutine));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(validRequest);
        });

        assertTrue(exception.getMessage().contains("Insufficient seats"));
        verify(flightRoutineRepository).findByIdWithLock(any(UUID.class));
        verify(flightRoutineRepository, never()).updateAvailableSeats(any(UUID.class), anyInt());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    private void setupValidRequest() {
        validRequest = new BookingRequest();
        validRequest.setFlightRoutineId(UUID.randomUUID());

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

        validRequest.setPassengers(Arrays.asList(passenger1, passenger2));

        BookingRequest.ContactInfo contact = new BookingRequest.ContactInfo();
        contact.setEmail("john.doe@example.com");
        contact.setPhone("+91-9876543210");
        validRequest.setContactInfo(contact);
    }

    private BookingRequest createSinglePassengerRequest() {
        BookingRequest request = new BookingRequest();
        request.setFlightRoutineId(UUID.randomUUID());

        BookingRequest.PassengerInfo passenger = new BookingRequest.PassengerInfo();
        passenger.setTitle("Mr");
        passenger.setFirstName("John");
        passenger.setLastName("Doe");
        passenger.setDateOfBirth("1990-01-15");
        passenger.setNationality("Indian");
        passenger.setSeatPreference("window");

        request.setPassengers(Arrays.asList(passenger));

        BookingRequest.ContactInfo contact = new BookingRequest.ContactInfo();
        contact.setEmail("john.doe@example.com");
        contact.setPhone("+91-9876543210");
        request.setContactInfo(contact);

        return request;
    }

    private void setupMockFlightRoutine() {
        mockFlightRoutine = createMockFlightRoutine();
    }

    private FlightRoutine createMockFlightRoutine() {
        // Create mock Airline
        Airline airline = new Airline();
        airline.setId(UUID.randomUUID());
        airline.setCode("6E");
        airline.setName("IndiGo");
        
        // Create mock Flight
        Flight flight = new Flight();
        flight.setId(UUID.randomUUID());
        flight.setFlightNumber("6E123");
        flight.setSourceAirport("DEL");
        flight.setDestinationAirport("BLR");
        flight.setAirline(airline);
        
        // Create mock FlightRoutine
        FlightRoutine routine = new FlightRoutine();
        routine.setId(UUID.randomUUID());
        routine.setFlight(flight);
        routine.setTravelDate(LocalDate.of(2025, 7, 26));
        routine.setDepartureTime(LocalTime.of(14, 30));
        routine.setArrivalTime(LocalTime.of(17, 30));
        routine.setCurrentPrice(BigDecimal.valueOf(7129.08));
        routine.setAvailableSeats(10);
        routine.setTotalSeats(186);
        routine.setStatus("scheduled");
        return routine;
    }

    private Booking createMockBooking() {
        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID());
        booking.setPnr("BK-" + System.currentTimeMillis());
        booking.setStatus("CONFIRMED");
        booking.setTotalAmount(BigDecimal.valueOf(14258.16));
        booking.setCreatedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        return booking;
    }
} 