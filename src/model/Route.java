package model;

/**
 * Route Model Class
 * Represents a bus route between two cities
 */
public class Route {
    private int routeId;
    private String source;
    private String destination;
    private double fare;
    private int durationMinutes;

    // Default Constructor
    public Route() {}

    // Parameterized Constructor
    public Route(int routeId, String source, String destination, double fare, int durationMinutes) {
        this.routeId = routeId;
        this.source = source;
        this.destination = destination;
        this.fare = fare;
        this.durationMinutes = durationMinutes;
    }

    // Getters and Setters
    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    // Get formatted duration (e.g., "5h 30m")
    public String getFormattedDuration() {
        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;
        return String.format("%dh %02dm", hours, mins);
    }

    @Override
    public String toString() {
        return String.format("Route[%d]: %s -> %s | Fare: Rs.%.2f | Duration: %s",
                routeId, source, destination, fare, getFormattedDuration());
    }
}
