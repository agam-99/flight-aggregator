package com.airlineaggregator.search.controller;

import com.airlineaggregator.search.dto.FlightSearchRequest;
import com.airlineaggregator.search.dto.FlightSearchResponse;
import com.airlineaggregator.search.dto.SearchResult;
import com.airlineaggregator.search.service.FlightSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FlightSearchControllerTest {

    @Mock
    private FlightSearchService flightSearchService;

    @InjectMocks
    private FlightSearchController flightSearchController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(flightSearchController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void searchFlights_ValidRequest_ReturnsSearchResults() throws Exception {
        // Given
        LocalDate travelDate = LocalDate.of(2025, 7, 26);
        SearchResult mockResult = createMockSearchResult();
        
        when(flightSearchService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "BLR")
                .param("travelDate", "2025-07-26")
                .param("passengers", "2")
                .param("sortBy", "price")
                .param("airline", "6E")
                .param("maxStops", "1")
                .param("maxDuration", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flights").isArray())
                .andExpect(jsonPath("$.flights[0].flightNumber").value("6E-112"))
                .andExpect(jsonPath("$.searchMetadata.totalResults").value(1));

        // Verify service was called with correct parameters
        ArgumentCaptor<FlightSearchRequest> requestCaptor = ArgumentCaptor.forClass(FlightSearchRequest.class);
        verify(flightSearchService).searchFlights(requestCaptor.capture());
        
        FlightSearchRequest capturedRequest = requestCaptor.getValue();
        assertEquals("DEL", capturedRequest.getSource());
        assertEquals("BLR", capturedRequest.getDestination());
        assertEquals(travelDate, capturedRequest.getTravelDate());
        assertEquals(2, capturedRequest.getPassengers());
        assertEquals("price", capturedRequest.getSortBy());
        assertEquals("6E", capturedRequest.getAirline());
        assertEquals(1, capturedRequest.getMaxStops());
        assertEquals(300, capturedRequest.getMaxDuration());
    }

    @Test
    void searchFlights_MinimalRequiredParams_ReturnsSearchResults() throws Exception {
        // Given
        SearchResult mockResult = createMockSearchResult();
        when(flightSearchService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(mockResult);

        // When & Then
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "BLR")
                .param("travelDate", "2025-07-26")
                .param("passengers", "1")
                .param("sortBy", "duration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flights").isArray());

        // Verify optional parameters are null
        ArgumentCaptor<FlightSearchRequest> requestCaptor = ArgumentCaptor.forClass(FlightSearchRequest.class);
        verify(flightSearchService).searchFlights(requestCaptor.capture());
        
        FlightSearchRequest capturedRequest = requestCaptor.getValue();
        assertNull(capturedRequest.getAirline());
        assertNull(capturedRequest.getMaxStops());
        assertNull(capturedRequest.getMaxDuration());
    }

    @Test
    void searchFlights_InvalidSortBy_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "BLR")
                .param("travelDate", "2025-07-26")
                .param("passengers", "2")
                .param("sortBy", "invalid"))
                .andExpect(status().isBadRequest());

        // Verify service was not called
        verify(flightSearchService, never()).searchFlights(any());
    }

    @Test
    void searchFlights_MissingRequiredParams_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "BLR"))
                .andExpect(status().isBadRequest());

        verify(flightSearchService, never()).searchFlights(any());
    }

    @Test
    void searchFlights_InvalidDateFormat_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "BLR")
                .param("travelDate", "invalid-date")
                .param("passengers", "2")
                .param("sortBy", "price"))
                .andExpect(status().isBadRequest());

        verify(flightSearchService, never()).searchFlights(any());
    }

    @Test
    void searchFlights_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Given
        when(flightSearchService.searchFlights(any(FlightSearchRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "BLR")
                .param("travelDate", "2025-07-26")
                .param("passengers", "2")
                .param("sortBy", "price"))
                .andExpect(status().isInternalServerError());

        verify(flightSearchService).searchFlights(any());
    }

    @Test
    void searchFlights_EmptyResults_ReturnsOkWithEmptyArray() throws Exception {
        // Given
        SearchResult emptyResult = new SearchResult(
                Collections.emptyList(),
                new SearchResult.SearchMetadata(0, UUID.randomUUID().toString(), false, 10L, new FlightSearchRequest())
        );
        when(flightSearchService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(emptyResult);

        // When & Then
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "XYZ")
                .param("travelDate", "2025-07-26")
                .param("passengers", "2")
                .param("sortBy", "price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flights").isArray())
                .andExpect(jsonPath("$.flights").isEmpty())
                .andExpect(jsonPath("$.searchMetadata.totalResults").value(0));
    }

    @Test
    void searchFlights_ValidSortByValues_AcceptsPriceAndDuration() throws Exception {
        // Given
        SearchResult mockResult = createMockSearchResult();
        when(flightSearchService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(mockResult);

        // Test "price" sortBy
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "BLR")
                .param("travelDate", "2025-07-26")
                .param("passengers", "1")
                .param("sortBy", "PRICE"))
                .andExpect(status().isOk());

        // Test "duration" sortBy
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "BLR")
                .param("travelDate", "2025-07-26")
                .param("passengers", "1")
                .param("sortBy", "Duration"))
                .andExpect(status().isOk());

        verify(flightSearchService, times(2)).searchFlights(any());
    }

    @Test
    void healthCheck_ReturnsOk() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/flights/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Search Service is running"));
    }

    @Test
    void getServiceInfo_ReturnsServiceInformation() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/flights/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("Flight Search Service"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.description").value("Provides flight search functionality for airline aggregator"))
                .andExpect(jsonPath("$.endpoints").isArray())
                .andExpect(jsonPath("$.endpoints[0]").value("GET /api/v1/flights/search - Search flights"))
                .andExpect(jsonPath("$.endpoints[1]").value("GET /api/v1/flights/health - Health check"))
                .andExpect(jsonPath("$.endpoints[2]").value("GET /api/v1/flights/info - Service information"));
    }

    @Test
    void searchFlights_EdgeCaseParameters_HandledCorrectly() throws Exception {
        // Given
        SearchResult mockResult = createMockSearchResult();
        when(flightSearchService.searchFlights(any(FlightSearchRequest.class)))
                .thenReturn(mockResult);

        // Test with edge case values
        mockMvc.perform(get("/api/v1/flights/search")
                .param("source", "DEL")
                .param("destination", "BLR")
                .param("travelDate", "2025-07-26")
                .param("passengers", "1")
                .param("sortBy", "price")
                .param("maxStops", "0")
                .param("maxDuration", "60"))
                .andExpect(status().isOk());

        ArgumentCaptor<FlightSearchRequest> requestCaptor = ArgumentCaptor.forClass(FlightSearchRequest.class);
        verify(flightSearchService).searchFlights(requestCaptor.capture());
        
        FlightSearchRequest capturedRequest = requestCaptor.getValue();
        assertEquals(0, capturedRequest.getMaxStops());
        assertEquals(60, capturedRequest.getMaxDuration());
    }

    private SearchResult createMockSearchResult() {
        FlightSearchResponse flight = new FlightSearchResponse();
        flight.setFlightRoutineId(UUID.randomUUID());
        flight.setFlightNumber("6E-112");
        
        FlightSearchResponse.AirlineInfo airline = new FlightSearchResponse.AirlineInfo();
        airline.setCode("6E");
        airline.setName("IndiGo");
        flight.setAirline(airline);
        
        FlightSearchResponse.RouteInfo route = new FlightSearchResponse.RouteInfo();
        route.setSource("DEL");
        route.setDestination("BLR");
        route.setDisplay("DEL -> BLR");
        route.setStops(0);
        flight.setRoute(route);
        
        FlightSearchResponse.ScheduleInfo schedule = new FlightSearchResponse.ScheduleInfo();
        schedule.setDepartureTime(java.time.LocalTime.of(14, 30));
        schedule.setArrivalTime(java.time.LocalTime.of(17, 30));
        schedule.setTravelDate(java.time.LocalDate.of(2025, 7, 26));
        schedule.setDurationMinutes(180);
        flight.setSchedule(schedule);
        
        FlightSearchResponse.PricingInfo pricing = new FlightSearchResponse.PricingInfo();
        pricing.setCurrentPrice(java.math.BigDecimal.valueOf(7129.08));
        pricing.setBasePrice(java.math.BigDecimal.valueOf(3772.00));
        pricing.setCurrency("INR");
        flight.setPricing(pricing);
        
        FlightSearchResponse.AvailabilityInfo availability = new FlightSearchResponse.AvailabilityInfo();
        availability.setTotalSeats(186);
        availability.setAvailableSeats(10);
        flight.setAvailability(availability);

        SearchResult.SearchMetadata metadata = new SearchResult.SearchMetadata(
                1, UUID.randomUUID().toString(), false, 25L, new FlightSearchRequest()
        );
        
        return new SearchResult(Arrays.asList(flight), metadata);
    }
} 