package model;

import java.sql.Time;

/**
 * Bus Model Class
 * Represents a bus that operates on a specific route
 */
public class Bus {
    private int busId;
    private int routeId;
    private String busNumber;
    private String busType;
    private int totalSeats;
    private Time departureTime;
    
    // Additional fields for display purposes
    private String source;
    private String destination;
    private double fare;

    // Default Constructor
    public Bus() {}

    // Parameterized Constructor
    public Bus(int busId, int routeId, String busNumber, String busType, int totalSeats, Time departureTime) {
        this.busId = busId;
        this.routeId = routeId;
        this.busNumber = busNumber;
        this.busType = busType;
        this.totalSeats = totalSeats;
        this.departureTime = departureTime;
    }

    // Getters and Setters
    public int getBusId() {
        return busId;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Time getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Time departureTime) {
        this.departureTime = departureTime;
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

    // Get formatted departure time
    public String getFormattedDepartureTime() {
        if (departureTime == null) return "N/A";
        return departureTime.toString().substring(0, 5);
    }

    // Get bus type display name
    public String getBusTypeDisplay() {
        switch (busType) {
            case "AC": return "AC";
            case "NON_AC": return "Non-AC";
            case "SLEEPER": return "Sleeper";
            case "SEMI_SLEEPER": return "Semi-Sleeper";
            default: return busType;
        }
    }

    @Override
    public String toString() {
        return String.format("Bus[%s] %s | Type: %s | Seats: %d | Departure: %s",
                busNumber, source != null ? source + " -> " + destination : "",
                getBusTypeDisplay(), totalSeats, getFormattedDepartureTime());
    }
}
