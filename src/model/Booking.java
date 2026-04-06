package model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Booking Model Class
 * Represents a ticket booking/reservation
 */
public class Booking {
    private int bookingId;
    private int seatId;
    private String passengerName;
    private String passengerPhone;
    private String passengerEmail;
    private Date travelDate;
    private Timestamp bookingTime;
    private String status;

    // Additional fields for display purposes
    private String seatNumber;
    private String busNumber;
    private String busType;
    private String source;
    private String destination;
    private double fare;
    private String departureTime;

    // Status constants
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_PENDING = "PENDING";

    // Default Constructor
    public Booking() {
        this.status = STATUS_CONFIRMED;
    }

    // Parameterized Constructor
    public Booking(int bookingId, int seatId, String passengerName, String passengerPhone,
                   Date travelDate, Timestamp bookingTime, String status) {
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
        this.travelDate = travelDate;
        this.bookingTime = bookingTime;
        this.status = status;
    }

    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public Date getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(Date travelDate) {
        this.travelDate = travelDate;
    }

    public Timestamp getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(Timestamp bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
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

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    // Check if booking is confirmed
    public boolean isConfirmed() {
        return STATUS_CONFIRMED.equals(status);
    }

    // Check if booking is cancelled
    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    @Override
    public String toString() {
        return String.format("Booking #%d | %s | Seat: %s | %s -> %s | Date: %s | Status: %s",
                bookingId, passengerName, seatNumber,
                source != null ? source : "N/A",
                destination != null ? destination : "N/A",
                travelDate, status);
    }
}
