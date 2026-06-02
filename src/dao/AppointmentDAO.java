package dao;

import model.Appointment;
import util.DatabaseConnection;




import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    private Connection connection;

    public AppointmentDAO() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
            alterTableToAddFields();
        } catch (SQLException e) {
            this.connection = null;
        }
    }

    private void alterTableToAddFields() {
        String[] sqls = {
            "ALTER TABLE appointments ADD COLUMN source VARCHAR(100)",
            "ALTER TABLE appointments ADD COLUMN destination VARCHAR(100)",
            "ALTER TABLE appointments ADD COLUMN fare DECIMAL(10,2)"
        };
        try {
            checkConnection();
            try (Statement stmt = connection.createStatement()) {
                for (String sql : sqls) {
                    try { stmt.execute(sql); } catch (SQLException ignored) {}
                }
            }
        } catch (SQLException ignored) {}
    }

    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DatabaseConnection.getInstance().getConnection();
        }
    }

    public boolean bookAppointment(Appointment appt) {
        String sql = "INSERT INTO appointments (booking_id, name, email, phone, appt_date, time_slot, status, source, destination, fare) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, appt.getBookingId());
                stmt.setString(2, appt.getName());
                stmt.setString(3, appt.getEmail());
                stmt.setString(4, appt.getPhone());
                stmt.setDate(5, appt.getApptDate());
                stmt.setString(6, appt.getTimeSlot());
                stmt.setString(7, appt.getStatus() != null ? appt.getStatus() : "Confirmed");
                stmt.setString(8, appt.getSource());
                stmt.setString(9, appt.getDestination());
                if (appt.getFare() != null) stmt.setDouble(10, appt.getFare());
                else stmt.setNull(10, java.sql.Types.DECIMAL);
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            appt.setId(generatedKeys.getInt(1));
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error booking appointment: " + e.getMessage());
        }
        return false;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY created_at DESC";
        try {
            checkConnection();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointments: " + e.getMessage());
        }
        return list;
    }

    public List<Appointment> getAppointmentsByContact(String phone, String email) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE phone = ? OR email = ? ORDER BY created_at DESC";
        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, phone);
                stmt.setString(2, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToAppointment(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user appointments: " + e.getMessage());
        }
        return list;
    }

    public List<String> getTakenSlots(Date date) {
        List<String> slots = new ArrayList<>();
        String sql = "SELECT time_slot FROM appointments WHERE appt_date = ? AND status != 'Cancelled'";
        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDate(1, date);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        slots.add(rs.getString("time_slot"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching taken slots: " + e.getMessage());
        }
        return slots;
    }

    public Appointment getAppointmentByBookingId(String bookingId) {
        String sql = "SELECT * FROM appointments WHERE booking_id = ?";
        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, bookingId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAppointment(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointment: " + e.getMessage());
        }
        return null;
    }

    public boolean isSlotTaken(Date date, String slot) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appt_date = ? AND time_slot = ? AND status != 'Cancelled'";
        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDate(1, date);
                stmt.setString(2, slot);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking slot: " + e.getMessage());
        }
        return false;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appt = new Appointment();
        appt.setId(rs.getInt("id"));
        appt.setBookingId(rs.getString("booking_id"));
        appt.setName(rs.getString("name"));
        appt.setEmail(rs.getString("email"));
        appt.setPhone(rs.getString("phone"));
        appt.setApptDate(rs.getDate("appt_date"));
        appt.setTimeSlot(rs.getString("time_slot"));
        appt.setStatus(rs.getString("status"));
        try {
            appt.setSource(rs.getString("source"));
            appt.setDestination(rs.getString("destination"));
            appt.setFare(rs.getDouble("fare"));
        } catch (SQLException ignored) { }
        appt.setCreatedAt(rs.getTimestamp("created_at"));
        return appt;
    }
}
