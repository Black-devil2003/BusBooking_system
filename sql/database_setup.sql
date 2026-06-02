CREATE DATABASE IF NOT EXISTS bus_booking_db;
USE bus_booking_db;

DROP VIEW IF EXISTS booking_details;
DROP PROCEDURE IF EXISTS GetAvailableSeats;
DROP PROCEDURE IF EXISTS GenerateSeats;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS buses;
DROP TABLE IF EXISTS routes;
SET FOREIGN_KEY_CHECKS = 1;

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

CREATE TABLE IF NOT EXISTS appointments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id VARCHAR(12) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    appt_date DATE NOT NULL,
    time_slot VARCHAR(30) NOT NULL,
    status ENUM('Confirmed', 'Pending', 'Cancelled') DEFAULT 'Confirmed',
    source VARCHAR(100),
    destination VARCHAR(100),
    fare DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_date_slot (appt_date, time_slot),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
);

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE VIEW booking_details AS
SELECT
    b.booking_id,
    b.passenger_name,
    b.passenger_phone,
    b.passenger_email,
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

DROP PROCEDURE IF EXISTS GetAvailableSeats;

DELIMITER //
CREATE PROCEDURE GetAvailableSeats(IN p_bus_id INT, IN p_travel_date DATE)
BEGIN
    SELECT s.seat_id, s.bus_id, s.seat_number, s.seat_type, FALSE AS is_booked
    FROM seats s
    WHERE s.bus_id = p_bus_id
      AND s.seat_id NOT IN (
          SELECT seat_id
          FROM bookings
          WHERE travel_date = p_travel_date
            AND status = 'CONFIRMED'
      )
    ORDER BY s.seat_number;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS GenerateSeats;

DELIMITER //
CREATE PROCEDURE GenerateSeats()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_bus_id INT;
    DECLARE v_total_seats INT;
    DECLARE i INT;
    DECLARE v_seat_type VARCHAR(10);

    DECLARE bus_cursor CURSOR FOR SELECT bus_id, total_seats FROM buses;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN bus_cursor;

    bus_loop: LOOP
        FETCH bus_cursor INTO v_bus_id, v_total_seats;
        IF done THEN
            LEAVE bus_loop;
        END IF;

        SET i = 1;
        WHILE i <= v_total_seats DO
            IF i % 4 = 1 OR i % 4 = 0 THEN
                SET v_seat_type = 'WINDOW';
            ELSEIF i % 4 = 2 THEN
                SET v_seat_type = 'AISLE';
            ELSE
                SET v_seat_type = 'MIDDLE';
            END IF;

            INSERT IGNORE INTO seats (bus_id, seat_number, seat_type, is_booked)
            VALUES (v_bus_id, LPAD(i, 2, '0'), v_seat_type, FALSE);

            SET i = i + 1;
        END WHILE;
    END LOOP;

    CLOSE bus_cursor;
END //
DELIMITER ;

SELECT 'Database schema setup completed successfully.' AS status;
