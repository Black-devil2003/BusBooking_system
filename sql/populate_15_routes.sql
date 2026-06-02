USE bus_booking_db;

-- Clear existing data to ensure clean populate
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE bookings;
TRUNCATE TABLE seats;
TRUNCATE TABLE buses;
TRUNCATE TABLE routes;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO routes (
    source,
    destination,
    route_name,
    fare,
    duration_minutes
)
VALUES
('Delhi', 'Jaipur', 'Delhi - Jaipur AC Service', 600.00, 300),
('Delhi', 'Chandigarh', 'Delhi - Chandigarh Express', 500.00, 240),
('Mumbai', 'Pune', 'Mumbai - Pune Shivneri', 450.00, 180),
('Mumbai', 'Goa', 'Mumbai - Goa Sleeper', 1200.00, 720),
('Bangalore', 'Chennai', 'Bangalore - Chennai Express', 800.00, 360),
('Bangalore', 'Hyderabad', 'Bangalore - Hyderabad Super Luxury', 950.00, 480),
('Chennai', 'Coimbatore', 'Chennai - Coimbatore Premium', 650.00, 420),
('Hyderabad', 'Vijayawada', 'Hyderabad - Vijayawada AC', 400.00, 300),
('Kolkata', 'Durgapur', 'Kolkata - Durgapur Express', 350.00, 180),
('Ahmedabad', 'Surat', 'Ahmedabad - Surat Express', 300.00, 240),
('Jaipur', 'Udaipur', 'Jaipur - Udaipur Sleeper', 700.00, 480),
('Lucknow', 'Kanpur', 'Lucknow - Kanpur Express', 150.00, 90),
('Patna', 'Gaya', 'Patna - Gaya Service', 200.00, 120),
('Bhopal', 'Indore', 'Bhopal - Indore AC', 350.00, 210),
('Kochi', 'Trivandrum', 'Kochi - Trivandrum Express', 450.00, 300);


INSERT INTO buses (
    route_id,
    bus_number,
    bus_type,
    total_seats,
    departure_time
)
VALUES
(1, 'DL-RJ-001', 'AC', 40, '06:00:00'),
(2, 'DL-CH-001', 'AC', 40, '07:30:00'),
(3, 'MH-PN-001', 'AC', 40, '08:00:00'),
(4, 'MH-GA-001', 'SLEEPER', 30, '21:00:00'),
(5, 'KA-TN-001', 'AC', 40, '06:30:00'),
(6, 'KA-TS-001', 'SLEEPER', 30, '22:00:00'),
(7, 'TN-CB-001', 'AC', 40, '05:00:00'),
(8, 'TS-AP-001', 'NON_AC', 40, '14:00:00'),
(9, 'WB-DG-001', 'AC', 40, '10:00:00'),
(10, 'GJ-SR-001', 'NON_AC', 40, '16:00:00'),
(11, 'RJ-UD-001', 'SLEEPER', 30, '22:30:00'),
(12, 'UP-KN-001', 'AC', 40, '09:00:00'),
(13, 'BR-GY-001', 'NON_AC', 40, '11:00:00'),
(14, 'MP-IN-001', 'AC', 40, '08:30:00'),
(15, 'KL-TV-001', 'NON_AC', 40, '13:00:00');

CALL GenerateSeats();

SELECT 'Route data inserted successfully' AS status;

SELECT COUNT(*) AS total_routes
FROM routes;

SELECT COUNT(*) AS total_buses
FROM buses;

SELECT COUNT(*) AS total_seats
FROM seats;
SELECT * FROM routes;

SELECT * FROM buses;

SELECT * FROM seats LIMIT 20;