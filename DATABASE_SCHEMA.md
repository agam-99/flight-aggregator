# Database Schema Documentation

## Overview

The Airline Aggregator System uses a PostgreSQL 15 database with a normalized schema designed for high performance and data integrity. The schema supports flight search, booking management, and payment processing with robust concurrency control.

## Schema Architecture

### Database Configuration
- **Database Name**: `airline_aggregator`
- **User**: `airline_user`
- **Port**: `5432`
- **Character Set**: UTF-8
- **Timezone**: UTC

## Core Tables

### 1. Airlines Table
Stores information about airline partners.

```sql
CREATE TABLE airlines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    logo_url VARCHAR(500),
    api_config JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

**Purpose**: Master table for airline information  
**Key Fields**:
- `code`: IATA airline code (e.g., 'AI', '6E', 'SG')
- `api_config`: JSON configuration for airline API integration
- `is_active`: Soft delete flag for enabling/disabling airlines

### 2. Airports Table
Master data for airport information.

```sql
CREATE TABLE airports (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    timezone VARCHAR(50),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    is_active BOOLEAN DEFAULT true
);
```

**Purpose**: Reference table for airport data  
**Key Fields**:
- `code`: IATA airport code (Primary Key)
- `latitude/longitude`: GPS coordinates for distance calculations
- `timezone`: Time zone for schedule calculations

### 3. Flights Table
Static flight route information.

```sql
CREATE TABLE flights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    airline_id UUID REFERENCES airlines(id),
    flight_number VARCHAR(20) NOT NULL,
    source_airport VARCHAR(10) REFERENCES airports(code),
    destination_airport VARCHAR(10) REFERENCES airports(code),
    route_display VARCHAR(255),
    metadata JSONB,
    total_duration_minutes INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

**Purpose**: Defines flight routes and basic flight information  
**Key Fields**:
- `flight_number`: Airline-specific flight identifier
- `route_display`: Human-readable route (e.g., "DEL -> TRI -> HYD -> BLR")
- `metadata`: JSON data for aircraft type, amenities, etc.

### 4. Route Segments Table
Supports multi-stop flights with individual segments.

```sql
CREATE TABLE route_segments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flight_id UUID REFERENCES flights(id),
    segment_order INTEGER NOT NULL,
    from_airport VARCHAR(10) REFERENCES airports(code),
    to_airport VARCHAR(10) REFERENCES airports(code),
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    duration_minutes INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Purpose**: Breaks down multi-stop flights into individual segments  
**Key Fields**:
- `segment_order`: Sequence of segments in the route
- `duration_minutes`: Duration of this specific segment

### 5. Flight Routines Table ⭐ **Core Search Table**
Date-specific flight instances with real-time pricing and availability.

```sql
CREATE TABLE flight_routines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flight_id UUID REFERENCES flights(id),
    travel_date DATE NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    current_price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(20) DEFAULT 'scheduled',
    pricing_tiers JSONB,
    price_updated_at TIMESTAMP DEFAULT NOW(),
    availability_updated_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(flight_id, travel_date, departure_time)
);
```

**Purpose**: Real-time flight availability and pricing  
**Key Fields**:
- `available_seats`: **Critical for seat management** - Updated atomically during bookings
- `current_price`: Dynamic pricing based on demand
- `pricing_tiers`: JSON object with economy/business class prices
- `status`: Flight status ('scheduled', 'delayed', 'cancelled')

**Seat Management**: This table is central to the concurrency control system with pessimistic locking.

### 6. Users Table
Customer information.

```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    preferences JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

**Purpose**: Customer account management  
**Key Fields**:
- `preferences`: JSON for user preferences (seat type, airline preferences, etc.)

### 7. Bookings Table ⭐ **Core Transaction Table**
Flight reservation management with seat control.

```sql
CREATE TABLE bookings (
    booking_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id),
    flight_routine_id UUID REFERENCES flight_routines(id),
    status VARCHAR(20) DEFAULT 'pending',
    pnr VARCHAR(20),
    total_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    passenger_details JSONB NOT NULL,
    contact_info JSONB NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

**Purpose**: Manages flight reservations with automatic expiration  
**Key Fields**:
- `status`: Booking lifecycle ('pending', 'confirmed', 'cancelled', 'expired')
- `passenger_details`: JSON array of passenger information
- `expires_at`: Automatic booking expiration (15 minutes)
- `pnr`: Passenger Name Record (generated on confirmation)

**Seat Management Integration**: When bookings are created/expired, the `available_seats` in `flight_routines` is updated atomically.

### 8. Payments Table
Payment transaction tracking.

```sql
CREATE TABLE payments (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID REFERENCES bookings(booking_id),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(20) DEFAULT 'pending',
    payment_method VARCHAR(50),
    gateway_provider VARCHAR(50),
    transaction_id VARCHAR(255),
    gateway_response JSONB,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Purpose**: Payment processing and audit trail  
**Key Fields**:
- `transaction_id`: Unique transaction identifier from payment gateway
- `gateway_response`: JSON response from payment gateway
- `payment_method`: Payment type ('UPI', 'netbanking', 'credit_card', 'wallet')

## Performance Optimization

### Indexing Strategy

```sql
-- Core search performance
CREATE INDEX idx_flight_routines_search ON flight_routines(travel_date, available_seats);
CREATE INDEX idx_flight_routines_flight ON flight_routines(flight_id, travel_date);

-- Route-based searches
CREATE INDEX idx_flights_route ON flights(source_airport, destination_airport);
CREATE INDEX idx_flights_airline ON flights(airline_id);

-- Booking and payment queries
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_routine ON bookings(flight_routine_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_payments_booking ON payments(booking_id);

-- Composite index with included columns for search optimization
CREATE INDEX idx_search_optimization ON flight_routines(travel_date, available_seats) 
    INCLUDE (current_price, departure_time, arrival_time);
```

### Query Optimization Features

1. **Composite Indexes**: Multi-column indexes for complex search queries
2. **Included Columns**: Non-key columns in indexes for covering queries
3. **Partial Indexes**: Future enhancement for active records only
4. **JSON Indexing**: GIN indexes on JSONB columns for metadata searches

## Data Integrity & Constraints

### Referential Integrity
- **Cascading Rules**: Proper foreign key relationships
- **Unique Constraints**: Prevent duplicate flights on same date/time
- **Check Constraints**: Data validation at database level

### Concurrency Control

#### Pessimistic Locking
```sql
-- Used in BookingService for seat management
SELECT * FROM flight_routines WHERE id = ? FOR UPDATE;
```

#### Atomic Updates
```sql
-- Seat reservation update
UPDATE flight_routines 
SET available_seats = available_seats - ? 
WHERE id = ? AND available_seats >= ?;
```

### Transaction Isolation
- **Level**: READ_COMMITTED
- **Deadlock Prevention**: Consistent lock ordering
- **Timeout Handling**: Transaction timeout configuration

## Sample Data Structure

### Airlines
- **Air India** (AI): Full-service carrier
- **IndiGo** (6E): Low-cost carrier
- **SpiceJet** (SG): Budget airline

### Airports
- **DEL**: New Delhi (Hub)
- **BLR**: Bengaluru (Tech hub)
- **BOM**: Mumbai (Financial hub)
- **MAA**: Chennai (South India)
- **CCU**: Kolkata (East India)

### Sample Flight Routes
1. **AI-123**: DEL → TRI → HYD → BLR (Multi-stop, 810 min)
2. **6E-101**: DEL → BLR (Direct, 180 min)
3. **SG-301**: DEL → MAA (Direct, 165 min)

## JSON Data Structures

### Flight Metadata
```json
{
  "aircraft_type": "Airbus A320neo",
  "amenities": ["WiFi", "Entertainment", "Meals"]
}
```

### Pricing Tiers
```json
{
  "economy": 4200,
  "business": 12000,
  "premium_economy": 8000
}
```

### Passenger Details
```json
[
  {
    "title": "Mr",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-15",
    "nationality": "Indian",
    "seatPreference": "window"
  }
]
```

### Contact Information
```json
{
  "email": "john.doe@example.com",
  "phone": "+91-9876543210"
}
```

## Backup & Recovery

### Backup Strategy
- **Daily Full Backups**: Complete database backup
- **Continuous WAL Archiving**: Point-in-time recovery
- **Cross-Region Replication**: Disaster recovery

### Data Retention
- **Bookings**: 7 years (compliance)
- **Payments**: 7 years (audit trail)
- **Flight Data**: 1 year (historical analysis)
- **Logs**: 90 days (operational)

## Security Considerations

### Data Protection
- **Encryption at Rest**: PostgreSQL TDE
- **Encryption in Transit**: SSL/TLS connections
- **PII Handling**: Secure storage of passenger data
- **Password Security**: Bcrypt hashing

### Access Control
- **Role-Based Access**: Service-specific database users
- **Connection Limits**: Prevent connection exhaustion
- **Audit Logging**: Database activity monitoring

## Migration & Evolution

### Schema Versioning
- **Flyway Integration**: Database migration management
- **Backward Compatibility**: Non-breaking changes
- **Rollback Strategy**: Safe schema rollbacks

### Future Enhancements
- **Partitioning**: Date-based table partitioning for large datasets
- **Read Replicas**: Separate read/write workloads
- **Caching Layer**: Redis integration for frequently accessed data
- **Analytics Database**: Separate OLAP system for reporting 