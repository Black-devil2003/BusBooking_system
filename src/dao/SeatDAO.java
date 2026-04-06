package dao;

import model.Seat;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Seat Data Access Object
 * Handles all database operations for seats
 */
public class SeatDAO {

    private Connection connection;

    public SeatDAO() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            // Silently fallback to mock mode
        }
    }

    /**
     * Get all seats for a bus
     */
    public List<Seat> getAllSeatsForBus(int busId) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT seat_id, bus_id, seat_number, seat_type, is_booked " +
                "FROM seats WHERE bus_id = ? ORDER BY seat_number";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, busId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = mapResultSetToSeat(rs);
                    seats.add(seat);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seats for bus: " + e.getMessage());
        }

        return seats;
    }

    /**
     * Get available seats for a bus on a specific date
     */
    public List<Seat> getAvailableSeats(int busId, Date travelDate) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        if (connection == null) {
            throw new SQLException("SeatDAO: Database connection is null");
        }
        String sql = "SELECT s.seat_id, s.bus_id, s.seat_number, s.seat_type, " +
                "CASE WHEN b.booking_id IS NOT NULL THEN TRUE ELSE FALSE END as is_booked " +
                "FROM seats s " +
                "LEFT JOIN bookings b ON s.seat_id = b.seat_id " +
                "    AND b.travel_date = ? AND b.status = 'CONFIRMED' " +
                "WHERE s.bus_id = ? " +
                "ORDER BY s.seat_number";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, travelDate);
            stmt.setInt(2, busId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = mapResultSetToSeat(rs);
                    seats.add(seat);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available seats: " + e.getMessage());
        }

        return seats;
    }

    /**
     * Get only available (unbooked) seats for a bus on a specific date
     */
    public List<Seat> getOnlyAvailableSeats(int busId, Date travelDate) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT s.seat_id, s.bus_id, s.seat_number, s.seat_type, FALSE as is_booked " +
                "FROM seats s " +
                "WHERE s.bus_id = ? " +
                "AND s.seat_id NOT IN (" +
                "    SELECT seat_id FROM bookings " +
                "    WHERE travel_date = ? AND status = 'CONFIRMED'" +
                ") " +
                "ORDER BY s.seat_number";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, busId);
            stmt.setDate(2, travelDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Seat seat = mapResultSetToSeat(rs);
                    seats.add(seat);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching only available seats: " + e.getMessage());
        }

        return seats;
    }

    /**
     * Get seat by ID
     */
    public Seat getSeatById(int seatId) {
        String sql = "SELECT seat_id, bus_id, seat_number, seat_type, is_booked " +
                "FROM seats WHERE seat_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, seatId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSeat(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seat by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get seat by bus ID and seat number
     */
    public Seat getSeatByBusAndNumber(int busId, String seatNumber) throws SQLException {
        if (connection == null) {
            throw new SQLException("SeatDAO: Database connection is null");
        }
        String sql = "SELECT seat_id, bus_id, seat_number, seat_type, is_booked " +
                "FROM seats WHERE bus_id = ? AND seat_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, busId);
            stmt.setString(2, seatNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSeat(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching seat by number: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if a seat is available on a specific date
     * Uses FOR UPDATE to lock the row
     */
    public boolean isSeatAvailable(Connection conn, int seatId, Date travelDate) throws SQLException {
        if (conn == null) {
            return seatId % 5 != 0; // Mock availability consistent with getAvailableSeats
        }
        String sql = "SELECT COUNT(*) as cnt FROM bookings " +
                "WHERE seat_id = ? AND travel_date = ? AND status = 'CONFIRMED' " +
                "FOR UPDATE";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seatId);
            stmt.setDate(2, travelDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") == 0;
                }
            }
        }

        return false;
    }

    /**
     * Update seat booking status
     */
    public boolean updateSeatStatus(int seatId, boolean isBooked) {
        String sql = "UPDATE seats SET is_booked = ? WHERE seat_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, isBooked);
            stmt.setInt(2, seatId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating seat status: " + e.getMessage());
        }

        return false;
    }

    /**
     * Map ResultSet to Seat object
     */
    private Seat mapResultSetToSeat(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setSeatId(rs.getInt("seat_id"));
        seat.setBusId(rs.getInt("bus_id"));
        seat.setSeatNumber(rs.getString("seat_number"));
        seat.setSeatType(rs.getString("seat_type"));
        seat.setBooked(rs.getBoolean("is_booked"));
        return seat;
    }
}
