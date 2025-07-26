package com.airlineaggregator.search.service;

import com.airlineaggregator.search.dto.FlightSearchRequest;
import com.airlineaggregator.search.dto.SearchResult;
import com.airlineaggregator.search.entity.Airline;
import com.airlineaggregator.search.entity.Flight;
import com.airlineaggregator.search.entity.FlightRoutine;
import com.airlineaggregator.search.repository.FlightRoutineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightSearchServiceTest {

    @Mock
    private FlightRoutineRepository flightRoutineRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private FlightSearchService flightSearchService;

    private FlightSearchRequest validRequest;
    private List<FlightRoutine> mockFlightRoutines;

    @BeforeEach
    void setUp() {
        setupValidRequest();
        setupMockFlightRoutines();
    }

    @Test
    void searchFlights_ValidRequest_ReturnsSearchResults() {
        // Given
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockFlightRoutines));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(2L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getFlights());
        assertEquals(2, result.getFlights().size());
        assertEquals(2, result.getSearchMetadata().getTotalResults());
        assertFalse(result.getSearchMetadata().getCacheHit());
        assertTrue(result.getSearchMetadata().getSearchTimeMs() >= 0);

        verify(flightRoutineRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(flightRoutineRepository).countAvailableFlights("DEL", "BLR", LocalDate.of(2025, 7, 26), 2);
    }

    @Test
    void searchFlights_EmptyResults_ReturnsEmptySearchResult() {
        // Given
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(0L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getFlights());
        assertTrue(result.getFlights().isEmpty());
        assertEquals(0, result.getSearchMetadata().getTotalResults());

        verify(flightRoutineRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(flightRoutineRepository).countAvailableFlights("DEL", "BLR", LocalDate.of(2025, 7, 26), 2);
    }

    @Test
    void searchFlights_WithAirlineFilter_AppliesCorrectSpecification() {
        // Given
        validRequest.setAirline("6E");
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockFlightRoutines.subList(0, 1)));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(1L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getFlights().size());
        assertEquals("6E", validRequest.getAirline());

        verify(flightRoutineRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchFlights_WithMaxDurationFilter_AppliesCorrectSpecification() {
        // Given
        validRequest.setMaxDuration(120);
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockFlightRoutines));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(2L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(120, validRequest.getMaxDuration());

        verify(flightRoutineRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchFlights_SortByPrice_UsesPriceSorting() {
        // Given
        validRequest.setSortBy("price");
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockFlightRoutines));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(2L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        assertNotNull(result);
        assertEquals("price", validRequest.getSortBy());

        verify(flightRoutineRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchFlights_SortByDuration_UsesDurationSorting() {
        // Given
        validRequest.setSortBy("duration");
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockFlightRoutines));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(2L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        assertNotNull(result);
        assertEquals("duration", validRequest.getSortBy());

        verify(flightRoutineRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchFlights_RepositoryThrowsException_ThrowsRuntimeException() {
        // Given
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            flightSearchService.searchFlights(validRequest);
        });

        assertEquals("Flight search failed", exception.getMessage());
        assertEquals("Database connection failed", exception.getCause().getMessage());

        verify(flightRoutineRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(flightRoutineRepository, never()).countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt());
    }

    @Test
    void searchFlights_CountQueryFails_ThrowsRuntimeException() {
        // Given
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockFlightRoutines));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenThrow(new RuntimeException("Count query failed"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            flightSearchService.searchFlights(validRequest);
        });

        assertEquals("Flight search failed", exception.getMessage());
        assertEquals("Count query failed", exception.getCause().getMessage());
    }

    @Test
    void searchFlights_LargeDataset_LimitsToMaxResults() {
        // Given
        List<FlightRoutine> largeDataset = createLargeFlightRoutineList(15); // More than MAX_RESULTS (10)
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(largeDataset.subList(0, 10))); // Repository should limit to 10
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(15L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getFlights().size()); // Should be limited to MAX_RESULTS
        assertEquals(15, result.getSearchMetadata().getTotalResults()); // But total count should reflect actual count

        verify(flightRoutineRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchFlights_AllOptionalParameters_HandlesCorrectly() {
        // Given
        FlightSearchRequest fullRequest = new FlightSearchRequest();
        fullRequest.setSource("DEL");
        fullRequest.setDestination("BLR");
        fullRequest.setTravelDate(LocalDate.of(2025, 7, 26));
        fullRequest.setPassengers(3);
        fullRequest.setSortBy("price");
        fullRequest.setAirline("SG");
        fullRequest.setMaxStops(1);
        fullRequest.setMaxDuration(240);

        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mockFlightRoutines.get(0))));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(1L);

        // When
        SearchResult result = flightSearchService.searchFlights(fullRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getFlights().size());
        assertEquals(1, result.getSearchMetadata().getTotalResults());

        verify(flightRoutineRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(flightRoutineRepository).countAvailableFlights("DEL", "BLR", LocalDate.of(2025, 7, 26), 3);
    }

    @Test
    void searchFlights_SearchMetadata_ContainsCorrectInformation() {
        // Given
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(mockFlightRoutines));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(2L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        SearchResult.SearchMetadata metadata = result.getSearchMetadata();
        assertNotNull(metadata);
        assertEquals(2, metadata.getTotalResults());
        assertNotNull(metadata.getSearchId());
        assertFalse(metadata.getCacheHit()); // Caching not implemented yet
        assertTrue(metadata.getSearchTimeMs() >= 0);
        assertNotNull(metadata.getFiltersApplied());
    }

    @Test
    void searchFlights_ConvertToDTO_MapsAllFields() {
        // Given
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mockFlightRoutines.get(0))));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(1L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        assertNotNull(result.getFlights());
        assertEquals(1, result.getFlights().size());
        
        var flight = result.getFlights().get(0);
        assertNotNull(flight.getFlightRoutineId());
        assertNotNull(flight.getFlightNumber());
        assertNotNull(flight.getAirline());
        assertNotNull(flight.getRoute());
        assertNotNull(flight.getSchedule());
        assertNotNull(flight.getPricing());
        assertNotNull(flight.getAvailability());
    }

    @Test
    void searchFlights_EdgeCaseZeroPassengers_HandledCorrectly() {
        // Given
        validRequest.setPassengers(0);
        when(flightRoutineRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(flightRoutineRepository.countAvailableFlights(anyString(), anyString(), any(LocalDate.class), anyInt()))
                .thenReturn(0L);

        // When
        SearchResult result = flightSearchService.searchFlights(validRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.getFlights().isEmpty());
        assertEquals(0, result.getSearchMetadata().getTotalResults());

        verify(flightRoutineRepository).countAvailableFlights("DEL", "BLR", LocalDate.of(2025, 7, 26), 0);
    }

    private void setupValidRequest() {
        validRequest = new FlightSearchRequest();
        validRequest.setSource("DEL");
        validRequest.setDestination("BLR");
        validRequest.setTravelDate(LocalDate.of(2025, 7, 26));
        validRequest.setPassengers(2);
        validRequest.setSortBy("price");
    }

    private void setupMockFlightRoutines() {
        // Create first flight routine
        FlightRoutine routine1 = createMockFlightRoutine(
                UUID.randomUUID(),
                "6E-112",
                "IndiGo",
                "6E",
                LocalTime.of(14, 30),
                LocalTime.of(17, 30),
                BigDecimal.valueOf(7129.08),
                10
        );

        // Create second flight routine
        FlightRoutine routine2 = createMockFlightRoutine(
                UUID.randomUUID(),
                "SG-113",
                "SpiceJet",
                "SG",
                LocalTime.of(16, 30),
                LocalTime.of(19, 30),
                BigDecimal.valueOf(5115.51),
                15
        );

        mockFlightRoutines = Arrays.asList(routine1, routine2);
    }

    private FlightRoutine createMockFlightRoutine(UUID id, String flightNumber, String airlineName, 
                                                 String airlineCode, LocalTime departure, LocalTime arrival, 
                                                 BigDecimal price, int availableSeats) {
        FlightRoutine routine = new FlightRoutine();
        routine.setId(id);
        routine.setTravelDate(LocalDate.of(2025, 7, 26));
        routine.setDepartureTime(departure);
        routine.setArrivalTime(arrival);
        routine.setCurrentPrice(price);
        routine.setAvailableSeats(availableSeats);
        routine.setTotalSeats(186);
        routine.setStatus("scheduled");

        // Create Flight
        Flight flight = new Flight();
        flight.setId(UUID.randomUUID());
        flight.setFlightNumber(flightNumber);
        flight.setSourceAirport("DEL");
        flight.setDestinationAirport("BLR");
        flight.setRouteDisplay("DEL -> BLR");
        flight.setTotalDurationMinutes(180);
        flight.setMetadata("{\"stops\":0,\"aircraft_type\":\"Airbus A320neo\",\"amenities\":[\"WiFi\",\"Entertainment\"]}");
        flight.setIsActive(true);

        // Create Airline
        Airline airline = new Airline();
        airline.setId(UUID.randomUUID());
        airline.setCode(airlineCode);
        airline.setName(airlineName);
        airline.setLogoUrl("https://logos.textgiraffe.com/logos/logo-name/" + airlineName + "-designstyle-wings-m.png");

        flight.setAirline(airline);
        routine.setFlight(flight);

        return routine;
    }

    private List<FlightRoutine> createLargeFlightRoutineList(int size) {
        List<FlightRoutine> routines = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            routines.add(createMockFlightRoutine(
                    UUID.randomUUID(),
                    "FL-" + (100 + i),
                    "Airline" + i,
                    "A" + i,
                    LocalTime.of(10 + (i % 12), 0),
                    LocalTime.of(12 + (i % 12), 0),
                    BigDecimal.valueOf(5000 + (i * 100)),
                    10 + i
            ));
        }
        return routines;
    }
} 