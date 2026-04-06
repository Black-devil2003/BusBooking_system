package model;

/**
 * Seat Model Class
 * Represents an individual seat in a bus
 */
public class Seat {
    private int seatId;
    private int busId;
    private String seatNumber;
    private String seatType;
    private boolean isBooked;

    // Default Constructor
    public Seat() {}

    // Parameterized Constructor
    public Seat(int seatId, int busId, String seatNumber, String seatType, boolean isBooked) {
        this.seatId = seatId;
        this.busId = busId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.isBooked = isBooked;
    }

    // Getters and Setters
    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public int getBusId() {
        return busId;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    // Get seat type icon for display
    public String getSeatTypeIcon() {
        switch (seatType) {
            case "WINDOW": return "W";
            case "AISLE": return "A";
            case "MIDDLE": return "M";
            default: return "-";
        }
    }

    // Get display status
    public String getStatusDisplay() {
        return isBooked ? "BOOKED" : "AVAILABLE";
    }

    @Override
    public String toString() {
        return String.format("Seat[%s] Type: %s | Status: %s",
                seatNumber, seatType, getStatusDisplay());
    }
}
