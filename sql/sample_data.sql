-- =====================================================
-- BUS TICKET BOOKING SYSTEM - SAMPLE DATA
-- =====================================================
-- Run this script after database_setup.sql
-- =====================================================

USE bus_booking_db;

-- =====================================================
-- Clean Existing Data (Safe Re-run)
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE bookings;
TRUNCATE TABLE seats;
TRUNCATE TABLE buses;
TRUNCATE TABLE routes;
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- Insert Sample Routes
-- =====================================================
INSERT INTO routes (source, destination, route_name, distance_km, fare, duration_minutes) VALUES
('Delhi', 'Mumbai', 'Delhi – Mumbai Express Route', NULL, 1500.00, 720),
('Delhi', 'Jaipur', 'Delhi – Jaipur Express Route', NULL, 600.00, 300),
('Mumbai', 'Pune', 'Mumbai – Pune Express Route', NULL, 400.00, 180),
('Bangalore', 'Chennai', 'Bangalore – Chennai Express Route', NULL, 800.00, 360),
('Hyderabad', 'Bangalore', 'Hyderabad – Bangalore Express Route', NULL, 900.00, 420),
('Kolkata', 'Patna', 'Kolkata – Patna Express Route', NULL, 550.00, 480),
('Chennai', 'Coimbatore', 'Chennai – Coimbatore Express Route', NULL, 500.00, 300),
('Ahmedabad', 'Mumbai', 'Ahmedabad – Mumbai Express Route', NULL, 700.00, 360),
('Lucknow', 'Delhi', 'Lucknow – Delhi Express Route', NULL, 650.00, 420),
('Pune', 'Goa', 'Pune – Goa Express Route', NULL, 850.00, 480);

-- =====================================================
-- Insert Sample Buses
-- =====================================================
INSERT INTO buses (route_id, bus_number, bus_type, total_seats, departure_time) VALUES
-- Delhi to Mumbai
(1, 'DL-MH-001', 'AC', 40, '06:00:00'),
(1, 'DL-MH-002', 'SLEEPER', 30, '20:00:00'),
(1, 'DL-MH-003', 'NON_AC', 40, '08:00:00'),

-- Delhi to Jaipur
(2, 'DL-RJ-001', 'AC', 40, '07:00:00'),
(2, 'DL-RJ-002', 'SEMI_SLEEPER', 36, '14:00:00'),

-- Mumbai to Pune
(3, 'MH-PN-001', 'AC', 40, '06:30:00'),
(3, 'MH-PN-002', 'NON_AC', 40, '09:00:00'),

-- Bangalore to Chennai
(4, 'KA-TN-001', 'AC', 40, '05:30:00'),
(4, 'KA-TN-002', 'SLEEPER', 30, '22:00:00'),

-- Hyderabad to Bangalore
(5, 'TS-KA-001', 'AC', 40, '21:00:00'),

-- Kolkata to Patna
(6, 'WB-BR-001', 'SEMI_SLEEPER', 36, '19:00:00'),

-- Chennai to Coimbatore
(7, 'TN-CB-001', 'AC', 40, '06:00:00'),

-- Ahmedabad to Mumbai
(8, 'GJ-MH-001', 'SLEEPER', 30, '21:30:00'),

-- Lucknow to Delhi
(9, 'UP-DL-001', 'AC', 40, '05:00:00'),

-- Pune to Goa
(10, 'MH-GA-001', 'SEMI_SLEEPER', 36, '18:00:00');

-- =====================================================
-- Generate Seats for Each Bus
-- =====================================================
-- Procedure to generate seats
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS GenerateSeats()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_bus_id INT;
    DECLARE v_total_seats INT;
    DECLARE v_bus_type VARCHAR(20);
    DECLARE i INT;
    DECLARE v_seat_type VARCHAR(10);
    DECLARE v_seat_number VARCHAR(5);
    
    DECLARE bus_cursor CURSOR FOR SELECT bus_id, total_seats, bus_type FROM buses;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN bus_cursor;
    
    bus_loop: LOOP
        FETCH bus_cursor INTO v_bus_id, v_total_seats, v_bus_type;
        IF done THEN
            LEAVE bus_loop;
        END IF;
        
        SET i = 1;
        WHILE i <= v_total_seats DO
            -- Determine seat number format based on bus type
            IF v_bus_type = 'SLEEPER' THEN
                SET v_seat_number = CONCAT('S', LPAD(i, 2, '0'));
            ELSE
                SET v_seat_number = LPAD(i, 2, '0');
            END IF;
            
            -- Determine seat type (Window, Aisle, Middle)
            IF i % 4 = 1 OR i % 4 = 0 THEN
                SET v_seat_type = 'WINDOW';
            ELSEIF i % 4 = 2 OR i % 4 = 3 THEN
                SET v_seat_type = 'AISLE';
            ELSE
                SET v_seat_type = 'MIDDLE';
            END IF;
            
            -- Insert seat (ignore if already exists)
            INSERT IGNORE INTO seats (bus_id, seat_number, seat_type, is_booked) 
            VALUES (v_bus_id, v_seat_number, v_seat_type, FALSE);
            
            SET i = i + 1;
        END WHILE;
    END LOOP;
    
    CLOSE bus_cursor;
END //
DELIMITER ;

-- Generate all seats
CALL GenerateSeats();

-- Clean up procedure
DROP PROCEDURE IF EXISTS GenerateSeats;

-- =====================================================
-- Verify Data Insertion
-- =====================================================
SELECT 'Sample data inserted successfully!' AS Status;
SELECT COUNT(*) AS 'Total Routes' FROM routes;
SELECT COUNT(*) AS 'Total Buses' FROM buses;
SELECT COUNT(*) AS 'Total Seats' FROM seats;
