#!/bin/bash

echo "🚀 Starting Airline Aggregator System..."
echo "======================================"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."
if ! command_exists docker; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Prerequisites check passed!"
echo ""

# Build services
echo "🔨 Building services..."
chmod +x build.sh
./build.sh

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo ""

# Start services
echo "🚀 Starting all services with Docker Compose..."
docker-compose down -v  # Clean start
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "❌ Failed to start services!"
    exit 1
fi

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check service health
echo "🏥 Checking service health..."

services=(
    "http://localhost:8081/api/v1/flights/health|Search Service"
    "http://localhost:8082/api/v1/bookings/health|Booking Service"  
    "http://localhost:8083/api/v1/payments/health|Payment Service"
)

all_healthy=true

for service_info in "${services[@]}"; do
    IFS='|' read -r url name <<< "$service_info"
    
    echo -n "Checking $name... "
    
    # Try multiple times
    for i in {1..10}; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo "✅ Healthy"
            break
        elif [ $i -eq 10 ]; then
            echo "❌ Not responding"
            all_healthy=false
        else
            sleep 2
        fi
    done
done

echo ""

if [ "$all_healthy" = true ]; then
    echo "🎉 All services are running successfully!"
    echo ""
    echo "📊 Loading comprehensive flight data..."
    chmod +x load-test-data.sh
    ./load-test-data.sh
    
    if [ $? -eq 0 ]; then
        echo "✅ Flight data loaded successfully!"
        echo "   • 20-30 flights per day for popular routes"
        echo "   • 15 airlines across 30+ Indian airports"
        echo "   • Comprehensive pricing and scheduling"
    else
        echo "⚠️  Failed to load flight data, but system is functional"
    fi
    
    echo ""
    echo "📖 API Endpoints:"
    echo "• Search Service:  http://localhost:8081/api/v1/flights"
    echo "• Booking Service: http://localhost:8082/api/v1/bookings"
    echo "• Payment Service: http://localhost:8083/api/v1/payments"
    echo ""
    echo "🧪 Test the system:"
    echo "1. Search flights (should return 20+ flights for popular routes):"
    echo "   curl \"http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=\$(date +%Y-%m-%d)&passengers=2&sortBy=price\""
    echo ""
    echo "2. Run complete test suite:"
    echo "   ./test-system.sh"
    echo ""
    echo "3. View service info:"
    echo "   curl http://localhost:8081/api/v1/flights/info"
    echo "   curl http://localhost:8082/api/v1/bookings/info"
    echo "   curl http://localhost:8083/api/v1/payments/info"
    echo ""
    echo "📊 View logs:"
    echo "   docker-compose logs -f"
    echo ""
    echo "🛑 Stop system:"
    echo "   docker-compose down"
else
    echo "⚠️  Some services are not healthy. Check logs:"
    echo "   docker-compose logs"
fi 