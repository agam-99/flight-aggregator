package com.airlineaggregator.search.dto;

import java.util.List;

public class SearchResult {

    private List<FlightSearchResponse> flights;
    private SearchMetadata searchMetadata;

    // Constructors
    public SearchResult() {}

    public SearchResult(List<FlightSearchResponse> flights, SearchMetadata searchMetadata) {
        this.flights = flights;
        this.searchMetadata = searchMetadata;
    }

    // Nested class for search metadata
    public static class SearchMetadata {
        private Integer totalResults;
        private String searchId;
        private Boolean cacheHit;
        private Long searchTimeMs;
        private FlightSearchRequest filtersApplied;

        public SearchMetadata() {}

        public SearchMetadata(Integer totalResults, String searchId, Boolean cacheHit, 
                             Long searchTimeMs, FlightSearchRequest filtersApplied) {
            this.totalResults = totalResults;
            this.searchId = searchId;
            this.cacheHit = cacheHit;
            this.searchTimeMs = searchTimeMs;
            this.filtersApplied = filtersApplied;
        }

        // Getters and Setters
        public Integer getTotalResults() { return totalResults; }
        public void setTotalResults(Integer totalResults) { this.totalResults = totalResults; }
        public String getSearchId() { return searchId; }
        public void setSearchId(String searchId) { this.searchId = searchId; }
        public Boolean getCacheHit() { return cacheHit; }
        public void setCacheHit(Boolean cacheHit) { this.cacheHit = cacheHit; }
        public Long getSearchTimeMs() { return searchTimeMs; }
        public void setSearchTimeMs(Long searchTimeMs) { this.searchTimeMs = searchTimeMs; }
        public FlightSearchRequest getFiltersApplied() { return filtersApplied; }
        public void setFiltersApplied(FlightSearchRequest filtersApplied) { this.filtersApplied = filtersApplied; }
    }

    // Getters and Setters
    public List<FlightSearchResponse> getFlights() {
        return flights;
    }

    public void setFlights(List<FlightSearchResponse> flights) {
        this.flights = flights;
    }

    public SearchMetadata getSearchMetadata() {
        return searchMetadata;
    }

    public void setSearchMetadata(SearchMetadata searchMetadata) {
        this.searchMetadata = searchMetadata;
    }
} 