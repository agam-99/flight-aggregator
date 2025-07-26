#!/bin/bash

# Quick Flight Data Check Script
# Check how many flights are available for popular routes

echo "✈️  Flight Data Verification"
echo "============================"

# Check if services are running
if ! docker-compose ps | grep -q "Up"; then
    echo "❌ Docker Compose services are not running. Please start them first:"
    echo "   docker-compose up -d"
    exit 1
fi

# Function to check flights for a route
check_route() {
    local source=$1
    local destination=$2
    local route_name=$3
    
    echo ""
    echo "📍 $route_name ($source → $destination) - Today ($(date +%Y-%m-%d))"
    echo "================================================================"
    
    # Get flight count and pricing summary
    docker exec airline_postgres psql -U airline_user -d airline_aggregator -c "
    SELECT 
        COUNT(*) as total_flights,
        COUNT(DISTINCT a.name) as airlines,
        MIN(fr.current_price) as min_price,
        MAX(fr.current_price) as max_price,
        ROUND(AVG(fr.current_price), 0) as avg_price,
        SUM(fr.available_seats) as total_seats_available,
        MIN(fr.departure_time) as earliest_flight,
        MAX(fr.departure_time) as latest_flight
    FROM flight_routines fr
    JOIN flights f ON fr.flight_id = f.id
    JOIN airlines a ON f.airline_id = a.id
    WHERE f.source_airport = '$source' 
        AND f.destination_airport = '$destination'
        AND fr.travel_date = CURRENT_DATE
        AND fr.status = 'scheduled';
    " | tail -n +3 | sed '$d' | sed '$d'
    
    # Show sample flights by airline
    echo ""
    echo "Sample flights by airline:"
    docker exec airline_postgres psql -U airline_user -d airline_aggregator -c "
    SELECT 
        a.code as airline,
        a.name,
        COUNT(*) as flights,
        MIN(fr.current_price) as min_price,
        MAX(fr.current_price) as max_price
    FROM flight_routines fr
    JOIN flights f ON fr.flight_id = f.id
    JOIN airlines a ON f.airline_id = a.id
    WHERE f.source_airport = '$source' 
        AND f.destination_airport = '$destination'
        AND fr.travel_date = CURRENT_DATE
        AND fr.status = 'scheduled'
    GROUP BY a.code, a.name
    ORDER BY flights DESC;
    " | tail -n +3 | sed '$d' | sed '$d'
}

# Check popular routes
check_route "DEL" "BLR" "Delhi to Bangalore"
check_route "DEL" "BOM" "Delhi to Mumbai" 
check_route "BOM" "BLR" "Mumbai to Bangalore"
check_route "DEL" "MAA" "Delhi to Chennai"
check_route "BLR" "DEL" "Bangalore to Delhi"

echo ""
echo "🎯 Quick Test Commands:"
echo "======================"
echo ""
echo "# Search DEL to BLR flights:"
echo "curl \"http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=1&sortBy=price\" | jq ."
echo ""
echo "# Search with specific airline (IndiGo):"
echo "curl \"http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=1&airline=6E&sortBy=departureTime\" | jq ."
echo ""
echo "# Check tomorrow's flights:"
echo "curl \"http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date -v+1d +%Y-%m-%d)&passengers=1&sortBy=price\" | jq ."
echo ""

# Total system summary
echo ""
echo "📊 System Overview:"
echo "=================="
docker exec airline_postgres psql -U airline_user -d airline_aggregator -c "
SELECT 
    'Total Airlines' as metric, COUNT(*)::text as value FROM airlines WHERE is_active = true
UNION ALL
SELECT 'Total Airports', COUNT(*)::text FROM airports WHERE is_active = true  
UNION ALL
SELECT 'Total Flight Routes', COUNT(*)::text FROM flights WHERE is_active = true
UNION ALL
SELECT 'Flight Routines (30 days)', COUNT(*)::text FROM flight_routines WHERE status = 'scheduled'
UNION ALL
SELECT 'Today''s Scheduled Flights', COUNT(*)::text FROM flight_routines WHERE travel_date = CURRENT_DATE AND status = 'scheduled'
UNION ALL
SELECT 'Available Seats Today', SUM(available_seats)::text FROM flight_routines WHERE travel_date = CURRENT_DATE AND status = 'scheduled';
" | tail -n +3 | sed '$d' | sed '$d'

echo ""
echo "🚀 Your airline aggregator system is ready with extensive flight data!"
echo "   Run './test-system.sh' for comprehensive testing" 