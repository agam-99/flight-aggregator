# API Documentation - Airline Aggregator System

## Overview

The Airline Aggregator System provides three main REST APIs for flight operations:

- **Search Service** (Port 8081): Flight search and filtering
- **Booking Service** (Port 8082): Reservation management
- **Payment Service** (Port 8083): Payment processing

All APIs follow RESTful conventions and return JSON responses. Error responses include appropriate HTTP status codes and descriptive error messages.

## Base URLs

- **Search Service**: `http://localhost:8081/api/v1/flights`
- **Booking Service**: `http://localhost:8082/api/v1/bookings`
- **Payment Service**: `http://localhost:8083/api/v1/payments`

## 📅 Dynamic Examples

**Important**: This documentation uses **dynamic dates and IDs** to ensure examples always work:

- **Dates**: `$(date +%Y-%m-%d)` - Always uses current date
- **Flight Routine IDs**: Use current search results to get valid IDs
- **Booking IDs**: Generated from actual bookings
- **Timestamps**: Use `$(date +%s)` for current Unix timestamp

### Getting Current Valid IDs

1. **Flight Routine ID**: First search for flights, then use `flightRoutineId` from results
2. **Booking ID**: Create a booking, then use the returned `bookingId`
3. **Payment ID**: Process a payment, then use the returned `paymentId`

### Dynamic Script Example

```bash
# Set dynamic values
start_time=$(date +%s)
travel_date=$(date +%Y-%m-%d)

# Search for flights
flight_response=$(curl -s "http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$travel_date&passengers=1")

# Extract first flight routine ID
flight_routine_id=$(echo "$flight_response" | grep -o '"flightRoutineId":"[^"]*"' | head -1 | cut -d'"' -f4)

echo "Using flight routine ID: $flight_routine_id"
echo "Search completed at: $(date)"
```

## Common Response Format

### Success Response
```json
{
  "data": {},
  "status": "success",
  "timestamp": "2025-07-26T10:00:00Z"
}
```

### Error Response
```json
{
  "error": "Error description",
  "status": "error", 
  "timestamp": "2025-07-26T10:00:00Z"
}
```

**Note**: Timestamps in responses reflect current date/time when API is called.

---

# Search Service API

## 1. Search Flights (GET)

**Endpoint**: `GET /api/v1/flights/search`

**Description**: Search for available flights with filtering and sorting options.

### Query Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `source` | String | Yes | Source airport code | `DEL` |
| `destination` | String | Yes | Destination airport code | `BLR` |
| `travelDate` | Date | Yes | Travel date (YYYY-MM-DD) | `$(date +%Y-%m-%d)` |
| `passengers` | Integer | Yes | Number of passengers | `2` |
| `sortBy` | String | No | Sort criteria (`price` or `duration`) | `price` |
| `airline` | String | No | Filter by airline code | `6E` |
| `maxStops` | Integer | No | Maximum number of stops | `1` |
| `maxDuration` | Integer | No | Maximum duration in minutes | `300` |

### Sample Request

```bash
curl -X GET "http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=2&sortBy=price&airline=6E" \
  -H "Content-Type: application/json"
```

### Sample Response

```json
{
  "flights": [
    {
      "flightRoutineId": "dbbe9277-ac15-46a0-8cca-554b00fe8c0e",
      "flightId": "e954012c-54db-466a-a247-9bc35de7e7fe",
      "flightNumber": "6E-112",
      "airline": {
        "code": "6E",
        "name": "IndiGo",
        "logoUrl": "https://logos.textgiraffe.com/logos/logo-name/Indigo-designstyle-wings-m.png"
      },
      "route": {
        "source": "DEL",
        "destination": "BLR",
        "display": "DEL -> BLR",
        "stops": 0
      },
      "schedule": {
        "departureTime": "14:30:00",
        "arrivalTime": "17:30:00",
        "travelDate": "2025-07-26"
        "durationMinutes": 180
      },
      "pricing": {
        "currentPrice": 7129.08,
        "basePrice": 3772.00,
        "currency": "INR",
        "pricingTiers": "{\"first\": 29942, \"economy\": 7129, \"business\": 17823}"
      },
      "availability": {
        "totalSeats": 186,
        "availableSeats": 10
      },
      "aircraft": {
        "type": "Airbus A320neo",
        "amenities": "[\"WiFi\",\"Entertainment\"]"
      }
    }
  ],
  "searchMetadata": {
    "totalResults": 2,
    "searchId": "1f5fefd6-6454-4121-804a-3bb209bfcdf2",
    "cacheHit": false,
    "searchTimeMs": 23,
          "filtersApplied": {
        "source": "DEL",
        "destination": "BLR",
        "travelDate": "2025-07-26"
        "passengers": 2,
        "sortBy": "price",
        "airline": "6E",
        "maxStops": null,
        "maxDuration": null
      }
  }
}
```


  
  ## 2. Health Check

**Endpoint**: `GET /api/v1/flights/health`

### Sample Request

```bash
curl -X GET "http://localhost:8081/api/v1/flights/health"
```

### Sample Response

```
Search Service is running
```

## 3. Service Information

**Endpoint**: `GET /api/v1/flights/info`

### Sample Request

```bash
curl -X GET "http://localhost:8081/api/v1/flights/info"
```

### Sample Response

```json
{
  "serviceName": "Flight Search Service",
  "version": "1.0.0",
  "description": "Provides flight search functionality for airline aggregator",
  "endpoints": [
    "GET /api/v1/flights/search - Search flights",
    "GET /api/v1/flights/health - Health check",
    "GET /api/v1/flights/info - Service information"
  ]
}
```

---

# Booking Service API

## 1. Create Booking

**Endpoint**: `POST /api/v1/bookings`

**Description**: Create a new flight booking with seat reservation and concurrency control.

### Request Body

```json
{
  "flightRoutineId": "dbbe9277-ac15-46a0-8cca-554b00fe8c0e",
  "passengers": [
    {
      "title": "Mr",
      "firstName": "John",
      "lastName": "Doe",
      "dateOfBirth": "1990-01-15",
      "nationality": "Indian",
      "seatPreference": "window"
    },
    {
      "title": "Ms",
      "firstName": "Jane",
      "lastName": "Smith",
      "dateOfBirth": "1992-05-20",
      "nationality": "Indian",
      "seatPreference": "aisle"
    }
  ],
  "contactInfo": {
    "email": "john.doe@example.com",
    "phone": "+91-9876543210"
  }
}
```

### Sample Request

```bash
curl -X POST "http://localhost:8082/api/v1/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "flightRoutineId": "dbbe9277-ac15-46a0-8cca-554b00fe8c0e",
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

### Sample Response

```json
{
  "bookingId": "5e6d1d80-bef9-4e22-8395-5dc08d8d40a7",
  "status": "pending",
  "flightDetails": {
    "flightNumber": "6E-112",
    "route": "14:30 - 17:30",
    "departureTime": "14:30:00",
    "travelDate": "2025-07-26",
    "arrivalTime": "17:30:00"
  },
  "pricing": {
    "totalAmount": 7129.08,
    "currency": "INR",
    "breakdown": {
      "baseFare": 7129.08,
      "taxes": 855.49,
      "fees": 213.87
    }
  },
  "expiryTime": "2025-07-26T09:58:21.992866833",
  "paymentUrl": "http://localhost:8083/api/v1/payments",
  "seatsHeld": 1,
  "bookingReference": "BK1753523001994600"
}
```

### Error Responses

#### Insufficient Seats
**Status Code**: `400 Bad Request`

```json
{
  "error": "Insufficient seats available. Requested: 5, Available: 3"
}
```

#### Flight Not Found
**Status Code**: `404 Not Found`

```json
{
  "errorCode": "FLIGHT_ROUTINE_NOT_FOUND",
  "message": "The specified flight routine does not exist. Please search for available flights and use a valid flight routine ID."
}
```

#### Flight Not Available
**Status Code**: `400 Bad Request`

```json
{
  "error": "Flight is not available for booking. Status: cancelled"
}
```

## 2. Get Booking Details

**Endpoint**: `GET /api/v1/bookings/{bookingId}`

### Sample Request

```bash
curl -X GET "http://localhost:8082/api/v1/bookings/5e6d1d80-bef9-4e22-8395-5dc08d8d40a7"
```

### Sample Response

```json
{
  "bookingId": "880bad58-590a-4425-a5b6-b1c5acddf35e",
  "userId": null,
  "flightRoutine": {
    "id": "aa7e6b1e-70fe-4d4e-a70f-bf1260056916",
    "travelDate": "2025-07-25",
    "departureTime": "06:00:00",
    "arrivalTime": "09:00:00"
  },
  "status": "pending",
  "pnr": null,
  "totalAmount": 9660.00,
  "currency": "INR",
  "passengerDetails": "[{\"title\":\"Mr\",\"firstName\":\"John\",\"lastName\":\"Doe\"}]",
  "contactInfo": "{\"email\":\"john.doe@example.com\",\"phone\":\"+91-9876543210\"}",
  "expiresAt": "2025-07-25T10:15:00Z",
  "createdAt": "2025-07-25T10:00:00Z",
  "updatedAt": "2025-07-25T10:00:00Z"
}
```

## 3. Health Check

**Endpoint**: `GET /api/v1/bookings/health`

### Sample Request

```bash
curl -X GET "http://localhost:8082/api/v1/bookings/health"
```

## 4. Service Information

**Endpoint**: `GET /api/v1/bookings/info`

### Sample Request

```bash
curl -X GET "http://localhost:8082/api/v1/bookings/info"
```

### Sample Response

```json
{
  "serviceName": "Booking Service",
  "version": "1.0.0",
  "description": "Provides flight booking functionality for airline aggregator",
  "bookingExpiryMinutes": 15,
  "endpoints": [
    "POST /api/v1/bookings - Create booking",
    "GET /api/v1/bookings/{id} - Get booking details",
    "GET /api/v1/bookings/health - Health check",
    "GET /api/v1/bookings/info - Service information"
  ]
}
```

---

# Payment Service API

## 1. Process Payment

**Endpoint**: `POST /api/v1/payments`

**Description**: Process payment for a booking and confirm the reservation.

### Request Body

```json
{
  "bookingId": "5e6d1d80-bef9-4e22-8395-5dc08d8d40a7",
  "paymentMethod": {
    "type": "upi",
    "upiId": "john.doe@okaxis"
  }
}
```

### Payment Method Types

#### UPI Payment
```json
{
  "type": "upi",
  "upiId": "user@okaxis"
}
```

#### Credit Card Payment
```json
{
  "type": "card",
  "cardNumber": "4111111111111111",
  "expiryMonth": 12,
  "expiryYear": 2025,
  "cvv": "123"
}
```

#### Net Banking
```json
{
  "type": "netbanking",
  "bankCode": "HDFC"
}
```

#### Wallet Payment
```json
{
  "type": "wallet",
  "walletProvider": "Paytm"
}
```

### Sample Request

```bash
curl -X POST "http://localhost:8083/api/v1/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": "5e6d1d80-bef9-4e22-8395-5dc08d8d40a7",
    "paymentMethod": {
      "type": "upi",
      "upiId": "john.doe@okaxis"
    }
  }'
```

### Sample Response

```json
{
  "paymentId": "b4d2da27-5906-4ef6-b9e8-d49426352b6c",
  "status": "success",
  "transactionId": "TXN_1753523016902_955",
  "amount": 5882.84,
  "currency": "INR",
  "processedAt": "2025-07-26T09:43:36.951950635",
  "message": "Payment processed successfully",
  "bookingUpdate": {
    "bookingId": "5e6d1d80-bef9-4e22-8395-5dc08d8d40a7",
    "newStatus": "confirmed",
    "pnr": "RTN3XX",
    "confirmationMessage": "Your booking has been confirmed successfully. PNR: RTN3XX"
  }
}
```

### Error Responses

#### Invalid Booking
**Status Code**: `400 Bad Request`

```json
{
  "error": "Invalid booking ID or booking not found"
}
```

#### Payment Failed
**Status Code**: `400 Bad Request`

```json
{
  "error": "Payment processing failed"
}
```

## 2. Get Payment Status

**Endpoint**: `GET /api/v1/payments/{paymentId}`

### Sample Request

```bash
curl -X GET "http://localhost:8083/api/v1/payments/b4d2da27-5906-4ef6-b9e8-d49426352b6c"
```

### Sample Response

```json
{
  "paymentId": "b4d2da27-5906-4ef6-b9e8-d49426352b6c",
  "status": "success",
  "message": "Payment completed successfully"
}
```

## 3. Health Check

**Endpoint**: `GET /api/v1/payments/health`

### Sample Request

```bash
curl -X GET "http://localhost:8083/api/v1/payments/health"
```

## 4. Service Information

**Endpoint**: `GET /api/v1/payments/info`

### Sample Request

```bash
curl -X GET "http://localhost:8083/api/v1/payments/info"
```

### Sample Response

```json
{
  "serviceName": "Payment Service",
  "version": "1.0.0",
  "description": "Provides payment processing functionality for airline aggregator",
  "mockMode": true,
  "alwaysSuccess": true,
  "endpoints": [
    "POST /api/v1/payments - Process payment",
    "GET /api/v1/payments/{id} - Get payment status",
    "GET /api/v1/payments/health - Health check",
    "GET /api/v1/payments/info - Service information"
  ]
}
```

---

# Complete End-to-End Example

## Scenario: Book a flight from Delhi to Bangalore

### Step 1: Search for flights

```bash
curl -X GET "http://localhost:8081/api/v1/flights/search?source=DEL&destination=BLR&travelDate=$(date +%Y-%m-%d)&passengers=2&sortBy=price"
```

### Step 2: Create booking (using flight ID from search)

```bash
curl -X POST "http://localhost:8082/api/v1/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "flightRoutineId": "dbbe9277-ac15-46a0-8cca-554b00fe8c0e",
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

### Step 3: Process payment (using booking ID from booking response)

```bash
curl -X POST "http://localhost:8083/api/v1/payments" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": "5e6d1d80-bef9-4e22-8395-5dc08d8d40a7",
    "paymentMethod": {
      "type": "upi",
      "upiId": "john.doe@okaxis"
    }
  }'
```

### Step 4: Verify booking confirmation

```bash
curl -X GET "http://localhost:8082/api/v1/bookings/5e6d1d80-bef9-4e22-8395-5dc08d8d40a7"
```

### Sample Response

```json
{
  "bookingId": "5e6d1d80-bef9-4e22-8395-5dc08d8d40a7",
  "status": "confirmed",
  "flightDetails": {
    "flightNumber": "6E-112",
    "route": "14:30 - 17:30",
    "departureTime": "14:30:00",
    "travelDate": "2025-07-26",
    "arrivalTime": "17:30:00"
  },
  "pricing": {
    "totalAmount": 5882.84,
    "currency": "INR",
    "breakdown": {
      "baseFare": 5115.51,
      "taxes": 613.86,
      "fees": 153.47
    }
  },
  "expiryTime": "2025-07-26T09:58:21.992867",
  "paymentUrl": "http://localhost:8083/api/v1/payments",
  "seatsHeld": 1,
  "bookingReference": "RTN3XX"
}
```

---

# Error Handling

## HTTP Status Codes

| Status Code | Description | Example |
|-------------|-------------|---------|
| `200` | Success | Successful search/booking/payment |
| `400` | Bad Request | Invalid input, insufficient seats |
| `404` | Not Found | Booking/flight not found |
| `500` | Internal Server Error | Database connection issues |

## Common Error Scenarios

### 1. Seat Management Errors

**Insufficient Seats**:
```json
{
  "error": "Insufficient seats available. Requested: 5, Available: 3"
}
```

**Concurrent Booking**:
```json
{
  "error": "Unable to reserve seats. Please try again or choose a different flight."
}
```

### 2. Validation Errors

**Invalid Date Format**:
```json
{
  "error": "Invalid date format. Expected: YYYY-MM-DD"
}
```

**Missing Required Fields**:
```json
{
  "error": "Missing required field: passengers"
}
```

### 3. Business Logic Errors

**Expired Booking**:
```json
{
  "error": "Booking has expired. Please create a new booking."
}
```

**Payment Amount Mismatch**:
```json
{
  "error": "Payment amount does not match booking total"
}
```

---
