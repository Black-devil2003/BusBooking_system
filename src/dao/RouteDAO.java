package dao;

import model.Route;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Route Data Access Object
 * Handles all database operations for routes
 */
public class RouteDAO {

    private Connection connection;

    public RouteDAO() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            // Silently fallback to mock mode
        }
    }

    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DatabaseConnection.getInstance().getConnection();
        }
    }

    /**
     * Get all routes
     */
    public List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();
        if (connection == null) {
            routes.add(new Route(1, "Mumbai", "Pune", 450.0, 180));
            routes.add(new Route(2, "Delhi", "Agra", 600.0, 240));
            routes.add(new Route(3, "Bangalore", "Chennai", 550.0, 360));
            return routes;
        }
        String sql = "SELECT route_id, source, destination, fare, duration_minutes FROM routes ORDER BY source, destination";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Route route = new Route();
                route.setRouteId(rs.getInt("route_id"));
                route.setSource(rs.getString("source"));
                route.setDestination(rs.getString("destination"));
                route.setFare(rs.getDouble("fare"));
                route.setDurationMinutes(rs.getInt("duration_minutes"));
                routes.add(route);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching routes: " + e.getMessage());
        }

        return routes;
    }

    /**
     * Get route by ID
     */
    public Route getRouteById(int routeId) {
        if (connection == null) {
            if (routeId == 1)
                return new Route(1, "Mumbai", "Pune", 450.0, 180);
            if (routeId == 2)
                return new Route(2, "Delhi", "Agra", 600.0, 240);
            if (routeId == 3)
                return new Route(3, "Bangalore", "Chennai", 550.0, 360);
            return null;
        }
        String sql = "SELECT route_id, source, destination, fare, duration_minutes FROM routes WHERE route_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, routeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Route route = new Route();
                    route.setRouteId(rs.getInt("route_id"));
                    route.setSource(rs.getString("source"));
                    route.setDestination(rs.getString("destination"));
                    route.setFare(rs.getDouble("fare"));
                    route.setDurationMinutes(rs.getInt("duration_minutes"));
                    return route;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching route by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Search routes by source and/or destination
     */
    public List<Route> searchRoutes(String source, String destination) {
        List<Route> routes = new ArrayList<>();
        if (connection == null) {
            // Simple mock search
            List<Route> all = getAllRoutes();
            for (Route r : all) {
                boolean matchSource = source == null || r.getSource().toLowerCase().contains(source.toLowerCase());
                boolean matchDest = destination == null
                        || r.getDestination().toLowerCase().contains(destination.toLowerCase());
                if (matchSource && matchDest) {
                    routes.add(r);
                }
            }
            return routes;
        }
        StringBuilder sql = new StringBuilder(
                "SELECT route_id, source, destination, fare, duration_minutes FROM routes WHERE 1=1");

        if (source != null && !source.trim().isEmpty()) {
            sql.append(" AND LOWER(source) LIKE LOWER(?)");
        }
        if (destination != null && !destination.trim().isEmpty()) {
            sql.append(" AND LOWER(destination) LIKE LOWER(?)");
        }
        sql.append(" ORDER BY source, destination");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (source != null && !source.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + source.trim() + "%");
            }
            if (destination != null && !destination.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + destination.trim() + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Route route = new Route();
                    route.setRouteId(rs.getInt("route_id"));
                    route.setSource(rs.getString("source"));
                    route.setDestination(rs.getString("destination"));
                    route.setFare(rs.getDouble("fare"));
                    route.setDurationMinutes(rs.getInt("duration_minutes"));
                    routes.add(route);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching routes: " + e.getMessage());
        }

        return routes;
    }

    /**
     * Add a new route
     */
    public boolean addRoute(Route route) {
        String sql = "INSERT INTO routes (source, destination, fare, duration_minutes) VALUES (?, ?, ?, ?)";

        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, route.getSource());
                stmt.setString(2, route.getDestination());
                stmt.setDouble(3, route.getFare());
                stmt.setInt(4, route.getDurationMinutes());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            route.setRouteId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding route: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update an existing route
     */
    public boolean updateRoute(Route route) {
        String sql = "UPDATE routes SET source = ?, destination = ?, fare = ?, duration_minutes = ? WHERE route_id = ?";
        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, route.getSource());
                stmt.setString(2, route.getDestination());
                stmt.setDouble(3, route.getFare());
                stmt.setInt(4, route.getDurationMinutes());
                stmt.setInt(5, route.getRouteId());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating route: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a route
     */
    public boolean deleteRoute(int routeId) {
        String sql = "DELETE FROM routes WHERE route_id = ?";
        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, routeId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting route: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get basic statistics for routes
     */
    public java.util.Map<String, Object> getRouteStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        try {
            checkConnection();
            String sqlTotal = "SELECT COUNT(*) FROM routes";
            String sqlCities = "SELECT COUNT(DISTINCT city) FROM (SELECT source as city FROM routes UNION SELECT destination as city FROM routes) as cities";
            String sqlToday = "SELECT COUNT(*) FROM routes WHERE DATE(created_at) = CURDATE()";

            try (Statement stmt = connection.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sqlTotal)) {
                    if (rs.next()) stats.put("total", rs.getInt(1));
                }
                try (ResultSet rs = stmt.executeQuery(sqlCities)) {
                    if (rs.next()) stats.put("cities", rs.getInt(1));
                }
                try (ResultSet rs = stmt.executeQuery(sqlToday)) {
                    if (rs.next()) stats.put("today", rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching route stats: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Get unique sources
     */
    public List<String> getUniqueSources() {
        List<String> sources = new ArrayList<>();
        String sql = "SELECT DISTINCT source FROM routes ORDER BY source";

        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    sources.add(rs.getString("source"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sources: " + e.getMessage());
        }

        return sources;
    }

    /**
     * Get unique destinations
     */
    public List<String> getUniqueDestinations() {
        List<String> destinations = new ArrayList<>();
        String sql = "SELECT DISTINCT destination FROM routes ORDER BY destination";

        try {
            checkConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    destinations.add(rs.getString("destination"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching destinations: " + e.getMessage());
        }

        return destinations;
    }
}
