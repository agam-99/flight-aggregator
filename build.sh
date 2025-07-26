#!/bin/bash

echo "🔨 Building Airline Aggregator Services"
echo "======================================="

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if Docker is available
if ! command_exists docker; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

echo "✅ Docker is available"
echo ""

# Services to build
services=("search-service" "booking-service" "payment-service")

echo "🏗️  Building services with Docker multi-stage builds..."
echo ""

# Build each service
for service in "${services[@]}"; do
    echo "📦 Building $service..."
    
    # Change to service directory and build
    cd "$service"
    
    # Build Docker image (this will compile Java code inside Docker)
    if docker build -t "airline-aggregator-$service:latest" .; then
        echo "✅ $service built successfully"
    else
        echo "❌ Failed to build $service"
        cd ..
        exit 1
    fi
    
    # Go back to root directory
    cd ..
    echo ""
done

echo "🎉 All services built successfully!"
echo ""
echo "📋 Built images:"
docker images | grep airline-aggregator

echo ""
echo "🚀 Ready to start with: docker-compose up -d" 