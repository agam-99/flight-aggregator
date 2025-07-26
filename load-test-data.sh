#!/bin/bash

# Load Test Data Script
# This script loads comprehensive test data including 20-30 flights per day for popular routes

echo "🗄️  Loading Comprehensive Test Data for Airline Aggregator System"
echo "================================================================="

# Check if Docker Compose is running
if ! docker-compose ps | grep -q "Up"; then
    echo "❌ Docker Compose services are not running. Please start them first:"
    echo "   docker-compose up -d"
    exit 1
fi

# Wait for PostgreSQL to be ready
echo "🔄 Waiting for PostgreSQL to be ready..."
for i in {1..30}; do
    if docker exec airline_postgres pg_isready -U airline_user > /dev/null 2>&1; then
        echo "✅ PostgreSQL is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ PostgreSQL is not responding after 30 seconds"
        exit 1
    fi
    sleep 1
done

# Load comprehensive data
echo ""
echo "📊 Loading comprehensive dataset..."
echo "   - 15 airlines with realistic configurations"
echo "   - 30 major Indian airports"
echo "   - 70+ popular flight routes"
echo "   - 20-30 flights per day for popular routes (DEL-BLR, DEL-BOM, etc.)"
echo "   - 30 days of flight schedules with dynamic pricing"
echo "   - 50 sample users and realistic booking data"
echo ""

start_time=$(date +%s)

# Execute the comprehensive data script
if docker exec -i airline_postgres psql -U airline_user -d airline_aggregator < database/comprehensive_data.sql > /dev/null; then
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    
    echo "✅ Comprehensive data loaded successfully in ${duration} seconds!"
    echo ""
    
    # Get data summary
    echo "📈 Data Summary:"
    echo "================"
    
    # Query the database for summary statistics
    docker exec airline_postgres psql -U airline_user -d airline_aggregator -c "
    SELECT 
        (SELECT COUNT(*) FROM airlines WHERE is_active = true) as active_airlines,
        (SELECT COUNT(*) FROM airports WHERE is_active = true) as active_airports,
        (SELECT COUNT(*) FROM flights WHERE is_active = true) as total_flights,
        (SELECT COUNT(*) FROM flight_routines WHERE status = 'scheduled') as scheduled_routines,
        (SELECT COUNT(*) FROM users) as total_users,
        (SELECT COUNT(*) FROM bookings) as total_bookings;
    " | tail -n +3 | sed '$d' | sed '$d'
    
    echo ""
    echo "📍 Popular Route Analysis (DEL-BLR for today):"
    echo "=============================================="
    
    docker exec airline_postgres psql -U airline_user -d airline_aggregator -c "
    SELECT 
        COUNT(*) as flights_today,
        MIN(fr.current_price) as min_price,
        MAX(fr.current_price) as max_price,
        ROUND(AVG(fr.current_price), 2) as avg_price,
        SUM(fr.available_seats) as total_available_seats
    FROM flight_routines fr
    JOIN flights f ON fr.flight_id = f.id
    WHERE f.source_airport = 'DEL' 
        AND f.destination_airport = 'BLR'
        AND fr.travel_date = CURRENT_DATE
        AND fr.status = 'scheduled';
    " | tail -n +3 | sed '$d' | sed '$d'
    
    echo ""
    echo "🎯 Sample Search Commands:"
    echo "=========================="
    echo "# Search DEL to BLR flights for today:"
    echo "curl \"http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=1&sortBy=price\""
    echo ""
    echo "# Search with airline filter:"
    echo "curl \"http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=1&airline=6E&sortBy=departureTime\""
    echo ""
    echo "# Search BOM to BLR flights:"
    echo "curl \"http://localhost:8081/api/v1/flights/search?source=BOM&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=2&sortBy=price\""
    echo ""
    
    echo "🚀 System is ready with comprehensive flight data!"
    echo "   Total flight routines: $(docker exec airline_postgres psql -U airline_user -d airline_aggregator -t -c "SELECT COUNT(*) FROM flight_routines;" | tr -d ' ')"
    echo "   Data covers: $(date +%Y-%m-%d) to $(date -v+29d +%Y-%m-%d)"
    echo ""
    echo "✨ You can now run the test suite:"
    echo "   ./test-system.sh"
    echo "   ./test-seat-management.sh"
    
else
    echo "❌ Failed to load comprehensive data"
    echo "   Check Docker logs: docker-compose logs postgres"
    exit 1
fi 