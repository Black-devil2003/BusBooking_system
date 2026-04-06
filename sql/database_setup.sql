-- =====================================================
-- BUS TICKET BOOKING SYSTEM - DATABASE SETUP
-- =====================================================
-- Run this script to create the database and tables
-- =====================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS bus_booking_db;
USE bus_booking_db;

-- =====================================================
-- TABLE: routes
-- Stores information about bus routes
-- =====================================================
CREATE TABLE IF NOT EXISTS routes (
    route_id INT PRIMARY KEY AUTO_INCREMENT,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    route_name VARCHAR(200) DEFAULT NULL,
    distance_km DECIMAL(8, 2) DEFAULT NULL,
    fare DECIMAL(10, 2) NOT NULL,
    duration_minutes INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_source (source),
    INDEX idx_destination (destination),
    INDEX idx_source_dest (source, destination),
    INDEX idx_route_name (route_name(100))
);

-- =====================================================
-- TABLE: buses
-- Stores information about buses operating on routes
-- =====================================================
CREATE TABLE IF NOT EXISTS buses (
    bus_id INT PRIMARY KEY AUTO_INCREMENT,
    route_id INT NOT NULL,
    bus_number VARCHAR(20) NOT NULL UNIQUE,
    bus_type ENUM('AC', 'NON_AC', 'SLEEPER', 'SEMI_SLEEPER') NOT NULL,
    total_seats INT NOT NULL DEFAULT 40,
    departure_time TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE CASCADE,
    INDEX idx_route (route_id),
    INDEX idx_bus_number (bus_number)
);

-- =====================================================
-- TABLE: seats
-- Stores individual seat information for each bus
-- =====================================================
CREATE TABLE IF NOT EXISTS seats (
    seat_id INT PRIMARY KEY AUTO_INCREMENT,
    bus_id INT NOT NULL,
    seat_number VARCHAR(5) NOT NULL,
    seat_type ENUM('WINDOW', 'AISLE', 'MIDDLE') NOT NULL,
    is_booked BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE CASCADE,
    UNIQUE KEY uk_bus_seat (bus_id, seat_number),
    INDEX idx_bus (bus_id),
    INDEX idx_booked (is_booked)
);

-- =====================================================
-- TABLE: bookings
-- Stores booking/reservation information
-- =====================================================
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    seat_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_phone VARCHAR(15) NOT NULL,
    passenger_email VARCHAR(100),
    travel_date DATE NOT NULL,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('CONFIRMED', 'CANCELLED', 'PENDING') DEFAULT 'CONFIRMED',
    
    FOREIGN KEY (seat_id) REFERENCES seats(seat_id) ON DELETE CASCADE,
    INDEX idx_seat (seat_id),
    INDEX idx_phone (passenger_phone),
    INDEX idx_travel_date (travel_date),
    INDEX idx_status (status)
);

-- =====================================================
-- View for easy booking queries
-- =====================================================
CREATE OR REPLACE VIEW booking_details AS
SELECT 
    b.booking_id,
    b.passenger_name,
    b.passenger_phone,
    b.travel_date,
    b.booking_time,
    b.status,
    s.seat_number,
    s.seat_type,
    bus.bus_number,
    bus.bus_type,
    bus.departure_time,
    r.source,
    r.destination,
    r.fare
FROM bookings b
JOIN seats s ON b.seat_id = s.seat_id
JOIN buses bus ON s.bus_id = bus.bus_id
JOIN routes r ON bus.route_id = r.route_id;

-- =====================================================
-- Stored Procedure: Get available seats for a bus
-- =====================================================
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS GetAvailableSeats(IN p_bus_id INT, IN p_travel_date DATE)
BEGIN
    SELECT s.seat_id, s.seat_number, s.seat_type
    FROM seats s
    WHERE s.bus_id = p_bus_id
    AND s.seat_id NOT IN (
        SELECT seat_id FROM bookings 
        WHERE travel_date = p_travel_date 
        AND status = 'CONFIRMED'
    )
    ORDER BY s.seat_number;
END //
DELIMITER ;

-- =====================================================
-- Trigger: Update seat status after booking
-- =====================================================
DELIMITER //
CREATE TRIGGER IF NOT EXISTS after_booking_insert
AFTER INSERT ON bookings
FOR EACH ROW
BEGIN
    IF NEW.status = 'CONFIRMED' THEN
        UPDATE seats SET is_booked = TRUE WHERE seat_id = NEW.seat_id;
    END IF;
END //
DELIMITER ;

-- =====================================================
-- Trigger: Update seat status after cancellation
-- =====================================================
DELIMITER //
CREATE TRIGGER IF NOT EXISTS after_booking_update
AFTER UPDATE ON bookings
FOR EACH ROW
BEGIN
    IF NEW.status = 'CANCELLED' AND OLD.status = 'CONFIRMED' THEN
        UPDATE seats SET is_booked = FALSE WHERE seat_id = NEW.seat_id;
    END IF;
END //
DELIMITER ;

-- =====================================================
-- TABLE: appointments
-- Stores standalone appointment bookings
-- =====================================================
CREATE TABLE IF NOT EXISTS appointments (
    id            INT PRIMARY KEY AUTO_INCREMENT,
    booking_id    VARCHAR(12) NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    phone         VARCHAR(15) NOT NULL,
    appt_date     DATE NOT NULL,
    time_slot     VARCHAR(30) NOT NULL,
    status        ENUM('Confirmed','Pending','Cancelled') DEFAULT 'Confirmed',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_date_slot (appt_date, time_slot),
    INDEX idx_email     (email),
    INDEX idx_phone     (phone)
);

-- =====================================================
-- Stored Procedure: Generate seats for all buses
-- =====================================================
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS GenerateSeats()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE b_id INT;
    DECLARE t_seats INT;
    DECLARE cur_seat INT DEFAULT 1;
    DECLARE s_type VARCHAR(20);
    
    -- Cursor to iterate through all buses
    DECLARE bus_cursor CURSOR FOR SELECT bus_id, total_seats FROM buses;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN bus_cursor;

    read_loop: LOOP
        FETCH bus_cursor INTO b_id, t_seats;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- Only generate if seats don't exist for this bus
        IF NOT EXISTS (SELECT 1 FROM seats WHERE bus_id = b_id) THEN
            SET cur_seat = 1;
            WHILE cur_seat <= t_seats DO
                -- Simple logic for seat types (Window, Aisle, Middle)
                IF cur_seat % 4 = 1 OR cur_seat % 4 = 0 THEN
                    SET s_type = 'WINDOW';
                ELSEIF cur_seat % 4 = 2 THEN
                    SET s_type = 'AISLE';
                ELSE
                    SET s_type = 'MIDDLE';
                END IF;

                INSERT INTO seats (bus_id, seat_number, seat_type, is_booked)
                VALUES (b_id, LPAD(cur_seat, 2, '0'), s_type, FALSE);
                
                SET cur_seat = cur_seat + 1;
            END WHILE;
        END IF;
    END LOOP;

    CLOSE bus_cursor;
    SELECT 'Seats generated successfully for all buses!' AS ProcessStatus;
END //
DELIMITER ;

SELECT 'Database setup completed successfully!' AS Status;
