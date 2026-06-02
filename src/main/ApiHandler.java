package main;

import com.google.gson.Gson;
import dao.*;
import model.*;
import service.*;
import java.sql.Date;
import java.util.Map;

public class ApiHandler {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("{\"error\": \"No operation specified\"}");
            return;
        }

        String operation = args[0];
        util.ConsoleUI.quietMode = true;

        try {
            switch (operation) {
                case "get-routes":
                    handleGetRoutes();
                    break;
                case "get-buses":
                    if (args.length > 1) handleGetBuses(Integer.parseInt(args[1]));
                    break;
                case "get-seats":
                    if (args.length > 1) handleGetSeats(Integer.parseInt(args[1]));
                    break;
                case "book-ticket":
                    if (args.length >= 7) handleBookTicket(Integer.parseInt(args[1]), args[2], args[3], args[4], args[5], args[6]);
                    break;
                case "get-bookings":
                    if (args.length > 1) handleGetBookings(args[1]);
                    break;
                case "register":
                    if (args.length >= 4) handleRegister(args[1], args[2], args[3]);
                    break;
                case "login":
                    if (args.length >= 3) handleLogin(args[1], args[2]);
                    break;
                case "manage-add-route":
                    if (args.length >= 5) handleAddRoute(args[1], args[2], Double.parseDouble(args[3]), Integer.parseInt(args[4]));
                    break;
                case "manage-update-route":
                    if (args.length >= 6) handleUpdateRoute(Integer.parseInt(args[1]), args[2], args[3], Double.parseDouble(args[4]), Integer.parseInt(args[5]));
                    break;
                case "manage-delete-route":
                    if (args.length > 1) handleDeleteRoute(Integer.parseInt(args[1]));
                    break;
                case "manage-stats":
                    handleGetStats();
                    break;
                case "appt-book":
                    if (args.length >= 10) handleApptBook(args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], Double.parseDouble(args[9]));
                    else if (args.length >= 7) handleApptBook(args[1], args[2], args[3], args[4], args[5], args[6], "", "", 0.0);
                    break;
                case "appt-get-all":
                    handleApptGetAll();
                    break;
                case "appt-get-taken-slots":
                    if (args.length > 1) handleApptGetTakenSlots(args[1]);
                    break;
                case "appt-get-my":
                    if (args.length >= 3) handleApptGetMy(args[1], args[2]);
                    break;
                default:
                    System.out.println("{\"error\": \"Unknown operation: " + operation + "\"}");
            }
        } catch (Exception e) {
            System.out.println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private static void handleGetRoutes() {
        RouteDAO dao = new RouteDAO();
        System.out.println(gson.toJson(dao.getAllRoutes()));
    }

    private static void handleGetBuses(int routeId) {
        BusDAO dao = new BusDAO();
        System.out.println(gson.toJson(dao.getBusesByRoute(routeId)));
    }

    private static void handleGetSeats(int busId) {
        SeatDAO dao = new SeatDAO();
        try {
            System.out.println(gson.toJson(dao.getAvailableSeats(busId, new java.sql.Date(System.currentTimeMillis()))));
        } catch (java.sql.SQLException e) {
            System.out.println("[]");
        }
    }

    private static void handleBookTicket(int busId, String seatNumber, String passengerName, String phone, String email, String travelDateStr) {
        try {
            BookingService service = new BookingService(null);
            boolean success = service.bookSeat(busId, seatNumber, passengerName, phone, email, Date.valueOf(travelDateStr));
            System.out.println("{\"success\": " + success + "}");
        } catch (Exception e) {
            System.out.println("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private static void handleGetBookings(String phone) {
        BookingDAO dao = new BookingDAO();
        System.out.println(gson.toJson(dao.getBookingsByPhone(phone)));
    }

    private static void handleRegister(String name, String email, String password) {
        UserDAO dao = new UserDAO();
        dao.ensureUserTableExists();
        boolean success = dao.registerUser(name, email, password);
        if (success) {
            System.out.println("{\"success\": true}");
        } else {
            String message = dao.getLastErrorMessage();
            if (message == null || message.trim().isEmpty()) {
                message = "Registration failed";
            }
            System.out.println(gson.toJson(java.util.Map.of(
                    "success", false,
                    "message", message
            )));
        }
    }

    private static void handleLogin(String email, String password) {
        UserDAO dao = new UserDAO();
        User user = dao.loginUser(email, password);
        if (user != null) {
            System.out.println("{\"success\": true, \"user\": " + gson.toJson(user) + "}");
        } else {
            System.out.println("{\"success\": false}");
        }
    }

    private static void handleAddRoute(String source, String destination, double fare, int duration) {
        Route route = new Route();
        route.setSource(source);
        route.setDestination(destination);
        route.setFare(fare);
        route.setDurationMinutes(duration);
        boolean success = new RouteDAO().addRoute(route);
        System.out.println("{\"success\": " + success + ", \"route\": " + gson.toJson(route) + "}");
    }

    private static void handleUpdateRoute(int id, String source, String destination, double fare, int duration) {
        Route route = new Route();
        route.setRouteId(id);
        route.setSource(source);
        route.setDestination(destination);
        route.setFare(fare);
        route.setDurationMinutes(duration);
        boolean success = new RouteDAO().updateRoute(route);
        System.out.println("{\"success\": " + success + "}");
    }

    private static void handleDeleteRoute(int id) {
        boolean success = new RouteDAO().deleteRoute(id);
        System.out.println("{\"success\": " + success + "}");
    }

    private static void handleGetStats() {
        Map<String, Object> stats = new RouteDAO().getRouteStats();
        System.out.println(gson.toJson(stats));
    }

    private static void handleApptBook(String bookingId, String name, String email, String phone, String date, String slot, String source, String destination, double fare) {
        Appointment appt = new Appointment();
        appt.setBookingId(bookingId);
        appt.setName(name);
        appt.setEmail(email);
        appt.setPhone(phone);
        appt.setApptDate(Date.valueOf(date));
        appt.setTimeSlot(slot);
        appt.setStatus("Confirmed");
        appt.setSource(source);
        appt.setDestination(destination);
        appt.setFare(fare);
        boolean success = new AppointmentDAO().bookAppointment(appt);
        System.out.println("{\"success\": " + success + ", \"booking\": " + gson.toJson(appt) + "}");
    }

    private static void handleApptGetAll() {
        System.out.println(gson.toJson(new AppointmentDAO().getAllAppointments()));
    }

    private static void handleApptGetTakenSlots(String date) {
        System.out.println(gson.toJson(new AppointmentDAO().getTakenSlots(Date.valueOf(date))));
    }

    private static void handleApptGetMy(String phone, String email) {
        java.util.List<Object> all = new java.util.ArrayList<>();
        all.addAll(new AppointmentDAO().getAppointmentsByContact(phone, email));
        all.addAll(new BookingDAO().getBookingsByContact(phone, email));
        System.out.println(gson.toJson(all));
    }
}
