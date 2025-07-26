# 🛫 Airline Aggregator System

A comprehensive airline booking platform built with microservices architecture, featuring flight search, booking management with seat control and payment processing.

## 🌟 Key Features

- **🔍 Smart Flight Search**: Dynamic queries with filters (source, destination, date, passengers, airline, duration)
- **🎫 Advanced Seat Management**: Real-time seat allocation with concurrency control and overbooking prevention
- **🔒 Concurrency Control**: Pessimistic locking to prevent race conditions during simultaneous bookings
- **💳 Payment Processing**: Mock payment gateway to simulate real world payment
- **🏗️ Microservices Architecture**: Separate services for Search, Booking, and Payment
- **🗄️ PostgreSQL Database**: Robust data storage with JSONB support and optimized indexing
- **🐳 Containerized**: Docker Compose for easy deployment and scaling

## 📚 Documentation

### Core Documentation
- **[Technical Specification](TECHNICAL_SPECIFICATION.md)**: Complete system architecture and design decisions
- **[Database Schema](DATABASE_SCHEMA.md)**: Comprehensive database design with concurrency control details
- **[API Documentation](API_DOCUMENTATION.md)**: Complete API reference with sample curl commands

### Quick Reference
- **Service Info**: Use `/info` endpoints on each service for live documentation
- **Health Checks**: Use `/health` endpoints for service status monitoring
- **Test Scripts**: Multiple automated testing scripts for different scenarios

## 🚀 Quick Start

### **Option 1: One-Command Setup** ⭐ **(Recommended)**
```bash
git clone <repository>
cd flight-aggregator-service
./run.sh
```

**What this does:**
- ✅ Builds all services
- ✅ Starts the system with Docker Compose
- ✅ **Automatically loads comprehensive flight data** (20-30 flights/day)
- ✅ Runs health checks
- ✅ Shows you how to test the system

### **Option 2: Step-by-Step** *(For customization or troubleshooting)*
```bash
# 1. Build all services
./build.sh

# 2. Start the system
docker-compose up -d

# 3. Wait for services to be ready (10 seconds)
sleep 10

# 4. Load comprehensive flight data (20-30 flights per day)
./load-test-data.sh

# 5. Run end-to-end tests
./test-system.sh
```

**Use this when:**
- 🔧 You want to customize individual steps
- 🐛 You need to troubleshoot specific components
- ⚙️ You want to skip the test data loading
- 📊 You prefer to see each step's output separately

## 📊 Flight Data & Setup

### **Comprehensive Flight Dataset**
The system includes an extensive flight database with realistic Indian airline data:

#### **📈 Flight Coverage**
- **28 flights per day** for ultra-popular routes (DEL-BOM, DEL-BLR, BOM-BLR)
- **20 flights per day** for major city routes (DEL-MAA, BLR-MAA, etc.)
- **12 flights per day** for tier-1 city routes (HYD, CCU connections)
- **8 flights per day** for tier-2 city routes (AMD, PNQ, COK connections)

#### **✈️ Realistic Data Features**
- **15 Airlines**: Air India, IndiGo, SpiceJet, Vistara, GoAir, AirAsia India, etc.
- **30 Airports**: All major Indian cities from DEL, BOM, BLR to tier-2 cities
- **70+ Routes**: Comprehensive domestic connectivity
- **30 Days Coverage**: Complete flight schedules with dynamic pricing
- **Time Slots**: 5:00 AM to 11:30 PM (every 30 minutes for popular routes)

#### **💰 Dynamic Pricing**
- **Route-based pricing**: DEL-BOM (₹4,200-5,000), DEL-BLR (₹3,800-4,500)
- **Airline positioning**: Vistara premium (+25%), GoAir budget (-15%)
- **Peak hour surcharges**: 6-9 AM, 5-9 PM (+20%)
- **Weekend pricing**: Friday (+15%), Saturday/Sunday (+25%)
- **Advance booking**: Last minute (+40%), Month advance (-10%)

### **Loading Flight Data**

> **📝 Note:** If you used **Option 1 (./run.sh)**, flight data is automatically loaded! Skip to verification.

```bash
# Load comprehensive dataset manually (only needed for Option 2)
./load-test-data.sh

# Verify flight data for popular routes
./check-flight-data.sh

# Check specific route (example: 20+ flights DEL-BLR today)
curl "http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=1&sortBy=price"
```

## 🧪 Testing & Validation

### **Comprehensive Test Suite**

#### **1. End-to-End System Testing**
```bash
# Complete workflow testing: Search → Book → Pay → Confirm
./test-system.sh
```

#### **2. Seat Management & Concurrency Testing**
```bash
# Test seat allocation, insufficient seats scenarios, and concurrent booking prevention
./test-seat-management.sh
```

### **Test Coverage**
- ✅ **Search functionality** with filtering and sorting
- ✅ **Booking creation** with passenger validation
- ✅ **Seat management** with atomic updates
- ✅ **Concurrency control** preventing overbooking
- ✅ **Payment processing** with confirmation
- ✅ **Service health checks** and info endpoints
- ✅ **Error handling** for edge cases

### **Sample Test Results**
```
🧪 Testing Airline Aggregator System
====================================
[SUCCESS] ✅ Search Service: Find flights with filters and sorting
[SUCCESS] ✅ Booking Service: Create bookings with passenger details
[SUCCESS] ✅ Payment Service: Process mock payments and confirm bookings
[SUCCESS] ✅ Database Integration: All services connected to PostgreSQL
[SUCCESS] ✅ End-to-End Flow: Search → Book → Pay → Confirm

🎫 Seat Management & Concurrency Control
========================================
[SUCCESS] ✅ Seat count properly reduced after booking
[SUCCESS] ✅ Insufficient seats scenario handled correctly
[SUCCESS] ✅ Concurrency control prevents race conditions
[SUCCESS] ✅ Database integrity maintained under concurrent load
```

## 📊 Advanced Features

### **🎫 Seat Management System**

The booking service implements seat management with:

#### **Concurrency Control**
- **Pessimistic Row-Level Locking**: Prevents race conditions during booking
- **Atomic Seat Updates**: Database-level operations ensure consistency
- **Transaction Isolation**: READ_COMMITTED isolation for reliable operations

#### **Real-Time Seat Tracking**
- **Immediate Updates**: Available seats decremented on booking creation
- **Automatic Release**: Expired bookings release seats back to inventory
- **Overbooking Prevention**: Validates availability before confirming bookings

#### **Code Example: Seat Management**
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public BookingResponse createBooking(BookingRequest request) {
    // Lock the flight routine row to prevent concurrent modifications
    FlightRoutine flightRoutine = flightRoutineRepository.findByIdWithLock(flightRoutineId);
    
    // Atomically update available seats
    int updatedRows = flightRoutineRepository.updateAvailableSeats(flightRoutineId, requestedSeats);
    if (updatedRows == 0) {
        throw new RuntimeException("Unable to reserve seats. Please try again.");
    }
    
    // Create booking with reserved seats
    return createBookingResponse(booking, flightRoutine, requestedSeats);
}
```

### **🔍 Dynamic Search Engine**
- **JPA Specifications**: Type-safe dynamic query building
- **Performance Optimized**: Strategic database indexing
- **Flexible Filtering**: Multiple search criteria combinations
- **Real-time Results**: Sub-second response times

### **💳 Payment Integration**
- **Mock Gateway**: Simulates real payment processing
- **Transaction Tracking**: Unique transaction IDs
- **Booking Confirmation**: Automatic status updates and PNR generation

## 🏗️ Architecture

### **Microservices Overview**
- **Search Service** (Port 8081): Flight search with advanced filtering
- **Booking Service** (Port 8082): Reservation management with seat control
- **Payment Service** (Port 8083): Payment processing and confirmation
- **PostgreSQL** (Port 5432): Primary database with concurrency control

### **Technology Stack**
- **Backend**: Java 17 + Spring Boot 3.2.0
- **Database**: PostgreSQL 15 with advanced indexing
- **ORM**: Hibernate JPA with pessimistic locking
- **Containerization**: Docker + Docker Compose
- **Build**: Maven with multi-stage Docker builds

### **Database Design Highlights**
- **Normalized Schema**: Optimized for data integrity
- **JSONB Support**: Flexible metadata storage
- **Concurrency Control**: Pessimistic locking for seat management
- **Performance Indexes**: Optimized for search operations

## 📁 Project Structure

```
flight-aggregator-service/
├── 📋 Documentation
│   ├── README.md                      # This comprehensive guide
│   ├── TECHNICAL_SPECIFICATION.md    # System architecture details
│   ├── DATABASE_SCHEMA.md            # Database design documentation
│   └── API_DOCUMENTATION.md          # Complete API reference
├── 🔍 search-service/                # Flight search microservice
│   ├── src/main/java/...            # JPA Specifications & search logic
│   ├── Dockerfile                    # Multi-stage build
│   └── pom.xml
├── 📋 booking-service/               # Booking management with seat control
│   ├── src/main/java/...            # Pessimistic locking & transactions
│   ├── Dockerfile
│   └── pom.xml
├── 💳 payment-service/               # Payment processing
│   ├── src/main/java/...            # Mock payment gateway
│   ├── Dockerfile
│   └── pom.xml
├── 🗄️ database/
│   ├── init.sql                     # Schema + basic test data
│   └── comprehensive_data.sql       # Extensive test dataset
├── 🐳 Docker Configuration
│   └── docker-compose.yml           # Multi-service orchestration
└── 🧪 Scripts & Automation
    ├── build.sh                     # Build all services
    ├── run.sh                       # One-command startup
    ├── load-test-data.sh            # Load comprehensive flight data (20-30 flights/day)
    ├── check-flight-data.sh         # Verify flight data for popular routes
    ├── test-system.sh               # End-to-end testing
    └── test-seat-management.sh      # Seat management testing
```

## 🛠️ API Reference

### **Quick API Overview**

| Service | Endpoint | Purpose | Key Features |
|---------|----------|---------|--------------|
| **Search** | `GET /api/v1/flights/search` | Find flights | Dynamic filtering, sorting, pagination |
| **Booking** | `POST /api/v1/bookings` | Create reservation | Seat management, concurrency control |
| **Payment** | `POST /api/v1/payments` | Process payment | Multiple methods, booking confirmation |

### **Complete API Documentation**
For detailed API documentation with request/response examples and curl commands, see:
📖 **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)**

### **Service Information Endpoints**
Each service provides live documentation:
```bash
# Get service information and available endpoints
curl http://localhost:8081/api/v1/flights/info
curl http://localhost:8082/api/v1/bookings/info  
curl http://localhost:8083/api/v1/payments/info
```

## 🗄️ Database Schema

### **Core Tables with Seat Management**

#### **Flight Routines** (Core Search & Booking Table)
```sql
CREATE TABLE flight_routines (
    id UUID PRIMARY KEY,
    flight_id UUID REFERENCES flights(id),
    travel_date DATE NOT NULL,
    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,    -- ⭐ Critical for seat management
    current_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'scheduled',
    -- ... other fields
);
```

#### **Bookings** (Transaction Management)
```sql
CREATE TABLE bookings (
    booking_id UUID PRIMARY KEY,
    flight_routine_id UUID REFERENCES flight_routines(id),
    status VARCHAR(20) DEFAULT 'pending',  -- pending → confirmed → expired
    passenger_details JSONB NOT NULL,      -- Flexible passenger info
    expires_at TIMESTAMP NOT NULL,         -- 15-minute expiration
    -- ... other fields
);
```

### **Concurrency Control Features**
- **Pessimistic Locking**: `SELECT ... FOR UPDATE` on flight routines
- **Atomic Updates**: Single-query seat modifications
- **Transaction Isolation**: READ_COMMITTED for consistency
- **Automatic Cleanup**: Expired booking seat release

For complete schema documentation, see: 📖 **[DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)**

## 🧪 Manual Testing Examples

### **1. Search Flights**
```bash
# Basic search (DEL-BLR: expect 20-28 flights today)
curl "http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=2&sortBy=price"

# With airline filter (IndiGo flights only)
curl "http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=1&airline=6E&sortBy=departureTime"

# Popular route with many options (BOM-BLR: expect 20+ flights)
curl "http://localhost:8081/api/v1/flights/search?source=BOM&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=1&sortBy=price"
```

### **2. Create Booking**
```bash
curl -X POST "http://localhost:8082/api/v1/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "flightRoutineId": "FLIGHT_ID_FROM_SEARCH",
    "passengers": [
      {
        "title": "Mr",
        "firstName": "John",
        "lastName": "Doe",
        "dateOfBirth": "1990-01-15",
        "nationality": "Indian",
        "seatPreference": "window"
      }
    ],
    "contactInfo": {
      "email": "john.doe@example.com",
      "phone": "+91-9876543210"
    }
  }'
```

### **3. Process Payment**
```bash
curl -X POST "http://localhost:8083/api/v1/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": "BOOKING_ID_FROM_BOOKING",
    "paymentMethod": {
      "type": "upi",
      "upiId": "user@okaxis"
    }
  }'
```

## 📊 System Capabilities

### **Scalability Features**
- **Stateless Services**: Horizontal scaling ready
- **Connection Pooling**: Efficient database resource usage
- **Container Ready**: Docker orchestration support
- **Load Balancer Compatible**: Health check endpoints

### **Data Integrity**
- **ACID Transactions**: All-or-nothing booking operations
- **Referential Integrity**: Foreign key constraints
- **Seat Consistency**: Atomic seat count updates

## 🚨 Error Handling & Edge Cases

### **Seat Management Error Scenarios**
```bash
# Test insufficient seats
curl -X POST "http://localhost:8082/api/v1/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "flightRoutineId": "FLIGHT_ID",
    "passengers": [/* 50 passengers when only 5 seats available */]
  }'

# Response: "Insufficient seats available. Requested: 50, Available: 5"
```

### **Concurrency Control Testing**
The system prevents race conditions when multiple users book simultaneously:
```bash
# Start multiple concurrent bookings (handled safely)
./test-seat-management.sh
```

### **Common Error Responses**
- **400 Bad Request**: Invalid input, insufficient seats
- **404 Not Found**: Flight/booking not found
- **500 Internal Error**: Database connection issues

## 📊 Monitoring & Operations

### **Health Monitoring**
```bash
# Check all services
curl http://localhost:8081/api/v1/flights/health   # Search Service
curl http://localhost:8082/api/v1/bookings/health  # Booking Service  
curl http://localhost:8083/api/v1/payments/health  # Payment Service
```

### **Service Information**
```bash
# Get live service documentation
curl http://localhost:8081/api/v1/flights/info
curl http://localhost:8082/api/v1/bookings/info
curl http://localhost:8083/api/v1/payments/info
```

### **Log Monitoring**
```bash
# View all logs
docker-compose logs -f

# Service-specific logs
docker-compose logs -f search-service
docker-compose logs -f booking-service
docker-compose logs -f payment-service
```

### **Database Operations**
```bash
# Connect to database
docker exec -it airline_postgres psql -U airline_user -d airline_aggregator

# Check seat availability
SELECT id, available_seats, total_seats FROM flight_routines 
WHERE travel_date = CURRENT_DATE;

# Monitor booking activity
SELECT status, COUNT(*) FROM bookings GROUP BY status;
```

## 🔧 Configuration

### **Service Ports**
- **Search Service**: 8081
- **Booking Service**: 8082  
- **Payment Service**: 8083
- **PostgreSQL**: 5432

### **Environment Variables**
```yaml
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/airline_aggregator
SPRING_DATASOURCE_USERNAME=airline_user
SPRING_DATASOURCE_PASSWORD=airline_pass

# Service URLs
SEARCH_SERVICE_URL=http://search-service:8080
BOOKING_SERVICE_URL=http://booking-service:8080
PAYMENT_SERVICE_URL=http://payment-service:8080
```

### **Booking Configuration**
```yaml
# Booking Service Settings
booking:
  expiry-minutes: 15
  max-passengers: 9
  seat-hold-timeout: 300
```

## 🛑 Cleanup & Maintenance

### **Stop Services**
```bash
# Graceful shutdown
docker-compose down

# Remove all data (complete reset)
docker-compose down -v

# Remove images and rebuild
docker-compose down -v --rmi all
./build.sh
docker-compose up -d
```

### **Reset Test Data**
```bash
# Complete reset to basic data
docker-compose down -v
docker-compose up -d

# Load comprehensive flight data (20-30 flights per popular route)
./load-test-data.sh

# Verify the loaded data
./check-flight-data.sh
```

## 🚨 Troubleshooting

### **Common Issues & Solutions**

#### **Service Startup Issues**
```bash
# Check service status
docker-compose ps

# View startup logs
docker-compose logs search-service
docker-compose logs booking-service
docker-compose logs payment-service

# Restart specific service
docker-compose restart booking-service
```

#### **Database Connection Problems**
```bash
# Check database status
docker exec airline_postgres pg_isready -U airline_user

# View database logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

#### **Port Conflicts**
```bash
# Check port usage
netstat -tlnp | grep :8081
netstat -tlnp | grep :8082
netstat -tlnp | grep :8083

# Kill conflicting processes
sudo kill -9 <process-id>
```

#### **Seat Management Issues**
```bash
# Check current seat counts
docker exec -it airline_postgres psql -U airline_user -d airline_aggregator -c "
SELECT fr.id, f.flight_number, fr.travel_date, fr.total_seats, fr.available_seats 
FROM flight_routines fr 
JOIN flights f ON fr.flight_id = f.id 
WHERE fr.travel_date >= CURRENT_DATE 
ORDER BY fr.available_seats;
"

# Reset seat counts if needed
docker-compose restart booking-service
```

## 🔮 Future Enhancements

### **Production Readiness**
- **Redis Caching**: Search result caching for performance
- **API Gateway**: Rate limiting, authentication, and routing
- **Real Payment Integration**: Razorpay, Stripe, PayU gateways
- **Message Queues**: Async processing with RabbitMQ/Kafka

### **Advanced Features**
- **Seat Selection**: Visual seat maps and specific seat booking
- **Multi-city Booking**: Complex itinerary support
- **Dynamic Pricing**: ML-based demand prediction
- **Notification Service**: Email/SMS confirmations

### **Operational Improvements**
- **Monitoring**: APM integration (New Relic, Datadog)
- **Analytics**: Booking conversion and user behavior tracking
- **Admin Dashboard**: Flight management and pricing controls
- **Mobile APIs**: Optimized mobile application endpoints

## 🤝 Contributing

### **Development Workflow**
1. Fork the repository
2. Create feature branch (`git checkout -b feature/seat-selection`)
3. Implement changes with tests
4. Run test suite (`./test-system.sh && ./test-seat-management.sh`)
5. Commit changes (`git commit -m 'Add seat selection feature'`)
6. Push to branch (`git push origin feature/seat-selection`)
7. Open Pull Request

### **Code Standards**
- **Java Conventions**: Follow Spring Boot best practices
- **Documentation**: Update relevant .md files
- **Testing**: Add tests for new features
- **Database**: Include migration scripts for schema changes

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---
