# Bus Ticket Booking System

A Java console-based bus ticket booking system with JDBC and MySQL, plus an Express web API and static web UI.

## Features

- Book bus tickets for available routes
- Cancel bookings
- View seat availability with a visual seat map
- Prevent double booking with database transactions
- Search bookings by ID, phone number, or contact details
- Manage routes through the web API

## Prerequisites

- Java JDK 8 or higher
- MySQL Server 5.7 or higher
- Node.js 18 or higher
- MySQL Connector/J in `lib/`

## Database Setup

Start MySQL, then run:

```sql
source sql/database_setup.sql
source sql/populate_15_routes.sql
```

## Configuration

Edit `src/config/db.properties`, or set environment variables before running the app:

```properties
db.url=jdbc:mysql://localhost:3306/bus_booking_db
db.username=root
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

Supported environment variable overrides:

```text
BUS_DB_URL
BUS_DB_USERNAME
BUS_DB_PASSWORD
BUS_DB_DRIVER
```

## Compile And Run

Windows:

```cmd
npm --prefix json run compile
npm --prefix json run java
```

Or run the batch files:

```cmd
compile.bat
run.bat
```

Start the web server:

```cmd
npm --prefix json start
```

Then open `http://localhost:3000`.

## Manual Java Commands

Windows:

```cmd
javac -d bin -cp "lib/*" src/model/*.java src/util/*.java src/dao/*.java src/service/*.java src/main/*.java
java -cp "bin;lib/*" main.BusTicketApp
```

Linux/Mac:

```bash
javac -d bin -cp "lib/*" src/model/*.java src/util/*.java src/dao/*.java src/service/*.java src/main/*.java
java -cp "bin:lib/*" main.BusTicketApp
```

## Project Structure

```text
sql/                  SQL scripts
src/
  config/             Database configuration
  dao/                Data access objects
  main/               Console app and Java API handler
  model/              Entity classes
  service/            Business logic
  util/               Utilities
js/client/            Browser JavaScript
js/server/            Node server and Java process connection
js/build/             Build helper scripts
js/config/            JavaScript config files
json/                 JSON configuration files
web/                  Static web UI
js/tests/             Node-based smoke scripts
lib/                  Java dependencies
docs/                 Documentation
```

## Checks

```cmd
npm --prefix json test
```

Because npm files are stored in `json/`, run npm commands from the project root with `npm --prefix json`:

```cmd
npm --prefix json start
npm --prefix json test
```

This compiles the Java sources and checks the Node server files for syntax errors.
