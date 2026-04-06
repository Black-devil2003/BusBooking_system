package dao;

import model.Bus;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Bus Data Access Object
 * Handles all database operations for buses
 */
public class BusDAO {

    private Connection connection;

    public BusDAO() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            // Silently fallback to mock mode
        }
    }

    /**
     * Get all buses for a specific route
     */
    public List<Bus> getBusesByRoute(int routeId) {
        List<Bus> buses = new ArrayList<>();
        if (connection == null) {
            buses.add(new Bus(1, routeId, "MH-12-AB-1234", "Sleeper AC", 30, Time.valueOf("10:00:00")));
            buses.add(new Bus(2, routeId, "MH-12-CD-5678", "Seater Non-AC", 40, Time.valueOf("14:30:00")));
            return buses;
        }
        String sql = "SELECT b.bus_id, b.route_id, b.bus_number, b.bus_type, b.total_seats, b.departure_time, " +
                "r.source, r.destination, r.fare " +
                "FROM buses b " +
                "JOIN routes r ON b.route_id = r.route_id " +
                "WHERE b.route_id = ? " +
                "ORDER BY b.departure_time";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, routeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Bus bus = mapResultSetToBus(rs);
                    buses.add(bus);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching buses by route: " + e.getMessage());
        }

        return buses;
    }

    /**
     * Get bus by ID
     */
    public Bus getBusById(int busId) {
        if (connection == null) {
            if (busId == 1)
                return new Bus(1, 1, "MH-12-AB-1234", "Sleeper AC", 30, Time.valueOf("10:00:00"));
            if (busId == 2)
                return new Bus(2, 1, "MH-12-CD-5678", "Seater Non-AC", 40, Time.valueOf("14:30:00"));
            return null;
        }
        String sql = "SELECT b.bus_id, b.route_id, b.bus_number, b.bus_type, b.total_seats, b.departure_time, " +
                "r.source, r.destination, r.fare " +
                "FROM buses b " +
                "JOIN routes r ON b.route_id = r.route_id " +
                "WHERE b.bus_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, busId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBus(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bus by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get all buses
     */
    public List<Bus> getAllBuses() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT b.bus_id, b.route_id, b.bus_number, b.bus_type, b.total_seats, b.departure_time, " +
                "r.source, r.destination, r.fare " +
                "FROM buses b " +
                "JOIN routes r ON b.route_id = r.route_id " +
                "ORDER BY r.source, r.destination, b.departure_time";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Bus bus = mapResultSetToBus(rs);
                buses.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all buses: " + e.getMessage());
        }

        return buses;
    }

    /**
     * Get available seat count for a bus on a specific date
     */
    public int getAvailableSeatCount(int busId, Date travelDate) {
        if (connection == null) {
            return 25; // Dummy available count
        }
        String sql = "SELECT COUNT(*) as available " +
                "FROM seats s " +
                "WHERE s.bus_id = ? " +
                "AND s.seat_id NOT IN (" +
                "    SELECT seat_id FROM bookings " +
                "    WHERE travel_date = ? AND status = 'CONFIRMED'" +
                ")";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, busId);
            stmt.setDate(2, travelDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available seat count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Add a new bus
     */
    public boolean addBus(Bus bus) {
        String sql = "INSERT INTO buses (route_id, bus_number, bus_type, total_seats, departure_time) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, bus.getRouteId());
            stmt.setString(2, bus.getBusNumber());
            stmt.setString(3, bus.getBusType());
            stmt.setInt(4, bus.getTotalSeats());
            stmt.setTime(5, bus.getDepartureTime());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        bus.setBusId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding bus: " + e.getMessage());
        }

        return false;
    }

    /**
     * Map ResultSet to Bus object
     */
    private Bus mapResultSetToBus(ResultSet rs) throws SQLException {
        Bus bus = new Bus();
        bus.setBusId(rs.getInt("bus_id"));
        bus.setRouteId(rs.getInt("route_id"));
        bus.setBusNumber(rs.getString("bus_number"));
        bus.setBusType(rs.getString("bus_type"));
        bus.setTotalSeats(rs.getInt("total_seats"));
        bus.setDepartureTime(rs.getTime("departure_time"));
        bus.setSource(rs.getString("source"));
        bus.setDestination(rs.getString("destination"));
        bus.setFare(rs.getDouble("fare"));
        return bus;
    }
}
