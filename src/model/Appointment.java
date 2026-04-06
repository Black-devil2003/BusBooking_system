package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Appointment {
    private int id;
    private String bookingId;
    private String name;
    private String email;
    private String phone;
    private Date apptDate;
    private String timeSlot;
    private String status;
    private String source;
    private String destination;
    private Double fare;
    private Timestamp createdAt;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Date getApptDate() { return apptDate; }
    public void setApptDate(Date apptDate) { this.apptDate = apptDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Double getFare() { return fare; }
    public void setFare(Double fare) { this.fare = fare; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
