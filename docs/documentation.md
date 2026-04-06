# Bus Ticket Booking System - Documentation

---

## Table of Contents
1. [Objective](#1-objective)
2. [System Overview](#2-system-overview)
3. [Database Design](#3-database-design)
4. [Seat Allocation Flowchart](#4-seat-allocation-flowchart)
5. [Module Descriptions](#5-module-descriptions)
6. [SQL Queries](#6-sql-queries)
7. [Screenshots](#7-screenshots)
8. [How to Run](#8-how-to-run)

---

## 1. Objective

The **Bus Ticket Booking System** is a Java console-based application designed to manage bus ticket reservations efficiently. The system allows users to:

- **Book tickets** for various bus routes
- **Cancel bookings** when travel plans change
- **View seat availability** with a visual seat map
- **Prevent double booking** of the same seat using transaction management
- **Search and manage bookings** by booking ID or phone number

### Key Features:
- ✅ User-friendly console interface with ANSI color support
- ✅ Multiple routes and buses with different types (AC, Non-AC, Sleeper, Semi-Sleeper)
- ✅ Visual seat layout display
- ✅ Transaction-based booking to prevent race conditions
- ✅ Row-level locking for double-booking prevention
- ✅ Ticket generation with booking details
- ✅ Comprehensive search functionality

---

## 2. System Overview

### 2.1 Architecture

The system follows a **3-tier architecture**:

```
┌─────────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                      │
│              (BusTicketApp.java - Console UI)            │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                 BUSINESS LOGIC LAYER                     │
│    (BookingService, RouteService, SeatService, etc.)     │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                 DATA ACCESS LAYER                        │
│        (RouteDAO, BusDAO, SeatDAO, BookingDAO)           │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│                   DATABASE LAYER                         │
│                  (MySQL Database)                        │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Project Structure

```
Java Project/
├── sql/
│   ├── database_setup.sql      # Database and table creation
│   └── sample_data.sql         # Sample data for testing
├── src/
│   ├── config/
│   │   └── db.properties       # Database configuration
│   ├── model/
│   │   ├── Route.java          # Route entity
│   │   ├── Bus.java            # Bus entity
│   │   ├── Seat.java           # Seat entity
│   │   └── Booking.java        # Booking entity
│   ├── dao/
│   │   ├── RouteDAO.java       # Route data access
│   │   ├── BusDAO.java         # Bus data access
│   │   ├── SeatDAO.java        # Seat data access
│   │   └── BookingDAO.java     # Booking data access
│   ├── service/
│   │   ├── RouteService.java   # Route business logic
│   │   ├── BusService.java     # Bus business logic
│   │   ├── SeatService.java    # Seat business logic
│   │   └── BookingService.java # Booking business logic
│   ├── util/
│   │   ├── DatabaseConnection.java  # DB connection utility
│   │   └── ConsoleUI.java      # Console UI utilities
│   └── BusTicketApp.java       # Main application
├── docs/
│   └── documentation.md        # This documentation
└── lib/
    └── mysql-connector-java-x.x.jar  # JDBC driver
```

---

## 3. Database Design

### 3.1 Entity Relationship Diagram

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   ROUTES    │       │    BUSES    │       │    SEATS    │       │  BOOKINGS   │
├─────────────┤       ├─────────────┤       ├─────────────┤       ├─────────────┤
│ route_id PK │◄──────┤ route_id FK │       │ seat_id PK  │◄──────┤ seat_id FK  │
│ source      │   1:N │ bus_id PK   │◄──────┤ bus_id FK   │   1:N │ booking_id  │
│ destination │       │ bus_number  │   1:N │ seat_number │       │ passenger_  │
│ fare        │       │ bus_type    │       │ seat_type   │       │   name      │
│ duration_   │       │ total_seats │       │ is_booked   │       │ passenger_  │
│   minutes   │       │ departure_  │       └─────────────┘       │   phone     │
└─────────────┘       │   time      │                             │ travel_date │
                      └─────────────┘                             │ status      │
                                                                  └─────────────┘
```

### 3.2 Route Table

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| route_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique route identifier |
| source | VARCHAR(100) | NOT NULL | Starting city |
| destination | VARCHAR(100) | NOT NULL | Ending city |
| fare | DECIMAL(10,2) | NOT NULL | Ticket price in INR |
| duration_minutes | INT | NOT NULL | Travel time in minutes |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation time |

**Sample Data:**
| route_id | source | destination | fare | duration_minutes |
|----------|--------|-------------|------|------------------|
| 1 | Delhi | Mumbai | 1500.00 | 720 |
| 2 | Delhi | Jaipur | 600.00 | 300 |
| 3 | Mumbai | Pune | 400.00 | 180 |
| 4 | Bangalore | Chennai | 800.00 | 360 |
| 5 | Hyderabad | Bangalore | 900.00 | 420 |

### 3.3 Bus Table

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| bus_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique bus identifier |
| route_id | INT | FOREIGN KEY | Reference to routes table |
| bus_number | VARCHAR(20) | UNIQUE, NOT NULL | Bus registration number |
| bus_type | ENUM | NOT NULL | 'AC', 'NON_AC', 'SLEEPER', 'SEMI_SLEEPER' |
| total_seats | INT | DEFAULT 40 | Number of seats in bus |
| departure_time | TIME | NOT NULL | Scheduled departure time |

### 3.4 Seat Table

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| seat_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique seat identifier |
| bus_id | INT | FOREIGN KEY | Reference to buses table |
| seat_number | VARCHAR(5) | NOT NULL | Seat number (e.g., "01", "15") |
| seat_type | ENUM | NOT NULL | 'WINDOW', 'AISLE', 'MIDDLE' |
| is_booked | BOOLEAN | DEFAULT FALSE | Current booking status |

### 3.5 Booking Table

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| booking_id | INT | PRIMARY KEY, AUTO_INCREMENT | Unique booking identifier |
| seat_id | INT | FOREIGN KEY | Reference to seats table |
| passenger_name | VARCHAR(100) | NOT NULL | Name of passenger |
| passenger_phone | VARCHAR(15) | NOT NULL | Contact number |
| passenger_email | VARCHAR(100) | - | Email address (optional) |
| travel_date | DATE | NOT NULL | Date of travel |
| booking_time | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | When booking was made |
| status | ENUM | DEFAULT 'CONFIRMED' | 'CONFIRMED', 'CANCELLED', 'PENDING' |

---

## 4. Seat Allocation Flowchart

### 4.1 Booking Process Flow

```
                              ┌──────────────────┐
                              │   START BOOKING  │
                              └────────┬─────────┘
                                       │
                              ┌────────▼─────────┐
                              │   Select Route   │
                              └────────┬─────────┘
                                       │
                              ┌────────▼─────────┐
                              │    Select Bus    │
                              └────────┬─────────┘
                                       │
                              ┌────────▼─────────┐
                              │ Enter Travel Date│
                              └────────┬─────────┘
                                       │
                              ┌────────▼─────────┐
                              │ Display Available│
                              │     Seats        │
                              └────────┬─────────┘
                                       │
                         ┌─────────────▼─────────────┐
                         │    Seats Available?       │
                         └────┬─────────────────┬────┘
                              │ NO              │ YES
                    ┌─────────▼────────┐ ┌──────▼──────┐
                    │ Show No Seats    │ │ Select Seat │
                    │ Message          │ │ Number      │
                    └─────────┬────────┘ └──────┬──────┘
                              │                  │
                              │          ┌───────▼───────┐
                              │          │ START         │
                              │          │ TRANSACTION   │
                              │          └───────┬───────┘
                              │                  │
                              │          ┌───────▼────────────┐
                              │          │ Lock Seat Row      │
                              │          │ (SELECT FOR UPDATE)│
                              │          └───────┬────────────┘
                              │                  │
                              │          ┌───────▼───────┐
                              │          │ Still         │
                              │          │ Available?    │
                              │          └──┬─────────┬──┘
                              │          NO │         │ YES
                              │    ┌───────▼──────┐   │
                              │    │ ROLLBACK     │   │
                              │    │ Transaction  │   │
                              │    └───────┬──────┘   │
                              │            │          │
                              │    ┌───────▼──────┐   │
                              │    │ Show Double  │   │
                              │    │ Booking Error│   │
                              │    └───────┬──────┘   │
                              │            │    ┌─────▼─────────┐
                              │            │    │ Get Passenger │
                              │            │    │ Details       │
                              │            │    └───────┬───────┘
                              │            │            │
                              │            │    ┌───────▼───────┐
                              │            │    │ Insert Booking│
                              │            │    │ Record        │
                              │            │    └───────┬───────┘
                              │            │            │
                              │            │    ┌───────▼───────┐
                              │            │    │ COMMIT        │
                              │            │    │ Transaction   │
                              │            │    └───────┬───────┘
                              │            │            │
                              │            │    ┌───────▼───────┐
                              │            │    │ Display Ticket│
                              │            │    └───────┬───────┘
                              │            │            │
                              └────────────┴────────────┘
                                           │
                                    ┌──────▼──────┐
                                    │     END     │
                                    └─────────────┘
```

### 4.2 Double Booking Prevention Mechanism

```
┌─────────────────────────────────────────────────────────────────┐
│                 DOUBLE BOOKING PREVENTION                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. BEGIN TRANSACTION                                            │
│     ↓                                                            │
│  2. SELECT seat FROM bookings                                    │
│     WHERE seat_id = ? AND travel_date = ?                        │
│     FOR UPDATE  ← Lock the row                                   │
│     ↓                                                            │
│  3. CHECK if any row returned                                    │
│     ├── YES → Seat already booked → ROLLBACK → Show Error       │
│     └── NO  → Seat available → Continue                         │
│     ↓                                                            │
│  4. INSERT INTO bookings (...)                                   │
│     ↓                                                            │
│  5. COMMIT TRANSACTION                                           │
│     ↓                                                            │
│  6. SUCCESS - Booking Confirmed                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. Module Descriptions

### 5.1 Model Layer

| Class | Description |
|-------|-------------|
| **Route.java** | Represents a bus route between two cities. Contains route ID, source, destination, fare, and duration. |
| **Bus.java** | Represents a bus operating on a route. Contains bus ID, number, type, total seats, and departure time. |
| **Seat.java** | Represents an individual seat in a bus. Contains seat ID, number, type (Window/Aisle), and booking status. |
| **Booking.java** | Represents a ticket booking. Contains booking ID, passenger details, travel date, and status. |

### 5.2 Data Access Layer (DAO)

| Class | Key Methods | Description |
|-------|-------------|-------------|
| **RouteDAO.java** | `getAllRoutes()`, `getRouteById()`, `searchRoutes()` | Handles CRUD operations for routes |
| **BusDAO.java** | `getBusesByRoute()`, `getBusById()`, `getAvailableSeatCount()` | Handles bus-related database operations |
| **SeatDAO.java** | `getAvailableSeats()`, `isSeatAvailable()`, `getSeatByBusAndNumber()` | Manages seat availability and status |
| **BookingDAO.java** | `createBooking()`, `cancelBooking()`, `getBookingById()`, `getBookingsByPhone()` | Handles booking transactions |

### 5.3 Service Layer

| Class | Responsibility |
|-------|----------------|
| **RouteService.java** | Business logic for route display, search, and selection |
| **BusService.java** | Business logic for bus listing and selection |
| **SeatService.java** | Visual seat map generation, availability checking |
| **BookingService.java** | **Core booking logic** with transaction management and double-booking prevention |

### 5.4 Utility Layer

| Class | Description |
|-------|-------------|
| **DatabaseConnection.java** | Singleton pattern for database connection management. Loads configuration from `db.properties`. |
| **ConsoleUI.java** | Console formatting utilities with ANSI colors, box drawing, table formatting, and ticket display. |

### 5.5 Main Application

| Class | Description |
|-------|-------------|
| **BusTicketApp.java** | Entry point with main menu. Handles user interaction and coordinates all services. |

---

## 6. SQL Queries

### 6.1 Table Creation Queries

```sql
-- Create Routes Table
CREATE TABLE routes (
    route_id INT PRIMARY KEY AUTO_INCREMENT,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    fare DECIMAL(10, 2) NOT NULL,
    duration_minutes INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Buses Table
CREATE TABLE buses (
    bus_id INT PRIMARY KEY AUTO_INCREMENT,
    route_id INT NOT NULL,
    bus_number VARCHAR(20) NOT NULL UNIQUE,
    bus_type ENUM('AC', 'NON_AC', 'SLEEPER', 'SEMI_SLEEPER') NOT NULL,
    total_seats INT NOT NULL DEFAULT 40,
    departure_time TIME NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE
);

-- Create Seats Table
CREATE TABLE seats (
    seat_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_id INT NOT NULL,
    seat_number VARCHAR(5) NOT NULL,
    seat_type ENUM('WINDOW', 'AISLE', 'MIDDLE') NOT NULL,
    is_booked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE CASCADE,
    UNIQUE KEY uk_bus_seat (bus_id, seat_number)
);

-- Create Bookings Table
CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    seat_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_phone VARCHAR(15) NOT NULL,
    passenger_email VARCHAR(100),
    travel_date DATE NOT NULL,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('CONFIRMED', 'CANCELLED', 'PENDING') DEFAULT 'CONFIRMED',
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE CASCADE
);
```

### 6.2 Data Retrieval Queries

```sql
-- Get all routes
SELECT route_id, source, destination, fare, duration_minutes 
FROM routes ORDER BY source, destination;

-- Search routes by source/destination
SELECT * FROM routes 
WHERE LOWER(source) LIKE LOWER('%Delhi%') 
  AND LOWER(destination) LIKE LOWER('%Mumbai%');

-- Get buses for a route
SELECT b.*, r.source, r.destination, r.fare 
FROM buses b
JOIN routes r ON b.route_id = r.route_id
WHERE b.route_id = 1
ORDER BY b.departure_time;

-- Get available seats for a bus on a date
SELECT s.seat_id, s.seat_number, s.seat_type,
       CASE WHEN b.booking_id IS NOT NULL THEN TRUE ELSE FALSE END as is_booked
FROM seats s
LEFT JOIN bookings b ON s.seat_id = b.seat_id 
    AND b.travel_date = '2026-01-15' 
    AND b.status = 'CONFIRMED'
WHERE s.bus_id = 1
ORDER BY s.seat_number;

-- Get booking details with full information
SELECT b.*, s.seat_number, s.seat_type,
       bus.bus_number, bus.bus_type, bus.departure_time,
       r.source, r.destination, r.fare
FROM bookings b
JOIN seats s ON b.seat_id = s.seat_id
JOIN buses bus ON s.bus_id = bus.bus_id
JOIN routes r ON bus.route_id = r.route_id
WHERE b.booking_id = 1;
```

### 6.3 Data Modification Queries

```sql
-- Insert new booking (with transaction)
INSERT INTO bookings (seat_id, passenger_name, passenger_phone, 
                      passenger_email, travel_date, status)
VALUES (?, ?, ?, ?, ?, 'CONFIRMED');

-- Cancel booking
UPDATE bookings 
SET status = 'CANCELLED' 
WHERE booking_id = ? AND status = 'CONFIRMED';

-- Check seat availability with row lock (for double-booking prevention)
SELECT COUNT(*) FROM bookings 
WHERE seat_id = ? AND travel_date = ? AND status = 'CONFIRMED'
FOR UPDATE;
```

### 6.4 Aggregate Queries

```sql
-- Get available seat count for a bus on a date
SELECT COUNT(*) as available
FROM seats s
WHERE s.bus_id = ?
  AND s.seat_id NOT IN (
      SELECT seat_id FROM bookings 
      WHERE travel_date = ? AND status = 'CONFIRMED'
  );

-- Get booking statistics
SELECT 
    COUNT(*) as total_bookings,
    SUM(CASE WHEN status = 'CONFIRMED' THEN 1 ELSE 0 END) as confirmed,
    SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled
FROM bookings;
```

---

## 7. Screenshots

### 7.1 Main Menu
```
╔══════════════════════════════════════════════════════════╗
║         🚌  BUS TICKET BOOKING SYSTEM  🚌                ║
╚══════════════════════════════════════════════════════════╝

  MAIN MENU
  ────────────────────────────────────────

  [1] 🗺️  View All Routes
  [2] 🚌 Search Buses by Route
  [3] 💺 View Seat Availability
  [4] 🎫 Book a Ticket
  [5] ❌ Cancel Booking
  [6] 🔍 View Booking Details
  [7] 📱 Search Bookings by Phone
  [8] 📋 View All Bookings

  ────────────────────────────────────────
  [0] 🚪 Exit

  » Enter your choice: 
```

### 7.2 View All Routes
```
  Available Routes (10)
  ─────────────────────
  ┌───────┬─────────────────┬─────────────────┬────────────┬────────────┐
  │  ID   │      From       │       To        │    Fare    │  Duration  │
  ├───────┼─────────────────┼─────────────────┼────────────┼────────────┤
  │ 1     │ Delhi           │ Mumbai          │ ₹1500      │ 12h 00m    │
  │ 2     │ Delhi           │ Jaipur          │ ₹600       │ 5h 00m     │
  │ 3     │ Mumbai          │ Pune            │ ₹400       │ 3h 00m     │
  │ 4     │ Bangalore       │ Chennai         │ ₹800       │ 6h 00m     │
  │ 5     │ Hyderabad       │ Bangalore       │ ₹900       │ 7h 00m     │
  └───────┴─────────────────┴─────────────────┴────────────┴────────────┘
```

### 7.3 Seat Availability View
```
  Seat Layout - DL-MH-001
  Route: Delhi → Mumbai
  Type: AC | Departure: 06:00
  Date: 2026-01-15

  Seat Legend:
    [  ] Available    [XX] Booked    [W] Window    [A] Aisle

  ┌─────────────────────────────────┐
  │        🚌 FRONT                 │
  ├─────────────────────────────────┤
  │ [01] [02]   [03] [04] │
  │ [05] [XX]   [07] [XX] │
  │ [09] [10]   [11] [12] │
  │ [13] [14]   [XX] [16] │
  │ [17] [18]   [19] [20] │
  ├─────────────────────────────────┤
  │         🚌 REAR                 │
  └─────────────────────────────────┘

  Available: 36 | Booked: 4 | Total: 40
```

### 7.4 Booking Confirmation Ticket
```
  ╔════════════════════════════════════════════════════╗
  ║              🎫 BUS TICKET 🎫                      ║
  ╠════════════════════════════════════════════════════╣
  ║  Booking ID  : 1001                                ║
  ║  Passenger   : John Doe                            ║
  ║  Phone       : 9876543210                          ║
  ╠════════════════════════════════════════════════════╣
  ║  Bus         : DL-MH-001 (AC)                      ║
  ║  Route       : Delhi → Mumbai                      ║
  ║  Seat No.    : 15                                  ║
  ╠════════════════════════════════════════════════════╣
  ║  Travel Date : 2026-01-15                          ║
  ║  Departure   : 06:00                               ║
  ║  Fare        : ₹1500.00                            ║
  ╠════════════════════════════════════════════════════╣
  ║           Status: CONFIRMED                        ║
  ╚════════════════════════════════════════════════════╝
```

### 7.5 Booking Cancellation
```
  ╔══════════════════════════════════════════════════════╗
  ║             CANCEL BOOKING                           ║
  ╚══════════════════════════════════════════════════════╝

  » Enter Booking ID: 1001

  Booking Details
  ───────────────
  [Ticket Display]

  » Are you sure you want to cancel this booking? (yes/no): yes

  ✓ Booking #1001 has been cancelled successfully!
  ℹ Seat 15 is now available for booking.
```

---

## 8. How to Run

### 8.1 Prerequisites

1. **Java JDK 8+** installed
2. **MySQL Server 5.7+** installed and running
3. **MySQL Connector/J** (JDBC driver) JAR file

### 8.2 Database Setup

1. Open MySQL command line or workbench
2. Run the database setup script:
   ```sql
   source /path/to/sql/database_setup.sql
   ```
3. Run the sample data script:
   ```sql
   source /path/to/sql/sample_data.sql
   ```

### 8.3 Configuration

Edit `src/config/db.properties` with your MySQL credentials:
```properties
db.url=jdbc:mysql://localhost:3306/bus_booking_db
db.username=root
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

### 8.4 Compilation

```bash
# Navigate to project directory
cd "Java Project"

# Create output directory
mkdir -p bin

# Compile all Java files
javac -cp "lib/*" -d bin src/**/*.java src/*.java
```

### 8.5 Execution

```bash
# Run the application
java -cp "bin;lib/*" BusTicketApp
```

On Linux/Mac:
```bash
java -cp "bin:lib/*" BusTicketApp
```

---

## 9. Conclusion

The Bus Ticket Booking System demonstrates a well-structured Java application with:

- **Clean Architecture**: Separation of concerns with Model-DAO-Service-UI layers
- **Robust Database Design**: Normalized tables with proper relationships
- **Transaction Safety**: Double-booking prevention using database transactions
- **User-Friendly Interface**: Beautiful console UI with colors and formatting
- **Comprehensive Functionality**: Complete booking lifecycle management

---

**Author:** Student  
**Version:** 1.0  
**Date:** January 2026
