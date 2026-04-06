# 🚌 Bus Ticket Booking System

A Java console-based bus ticket booking system with JDBC and MySQL.

## Features

- ✅ Book bus tickets for various routes
- ✅ Cancel bookings
- ✅ View seat availability with visual seat map
- ✅ Prevent double booking using transactions
- ✅ Search bookings by ID or phone number
- ✅ Beautiful console UI with colors

## Quick Start

### Prerequisites

- Java JDK 8 or higher
- MySQL Server 5.7 or higher
- MySQL Connector/J (JDBC driver)

### Database Setup

1. Start MySQL and run:
```sql
source sql/database_setup.sql
source sql/sample_data.sql
```

### Configuration

Edit `src/config/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/bus_booking_db
db.username=root
db.password=your_password
```

### Compile & Run

**Windows:**
```cmd
javac -cp "lib/*" -d bin src/config/*.java src/model/*.java src/util/*.java src/dao/*.java src/service/*.java src/*.java
java -cp "bin;lib/*" BusTicketApp
```

**Linux/Mac:**
```bash
javac -cp "lib/*" -d bin src/**/*.java src/*.java
java -cp "bin:lib/*" BusTicketApp
```

## Project Structure

```
├── sql/                  # SQL scripts
├── src/
│   ├── model/           # Entity classes
│   ├── dao/             # Data Access Objects
│   ├── service/         # Business logic
│   ├── util/            # Utilities
│   └── BusTicketApp.java
├── docs/                # Documentation
└── lib/                 # JDBC driver JAR
```

## Documentation

See [docs/documentation.md](docs/documentation.md) for complete documentation.

## Author

Student - January 2026
