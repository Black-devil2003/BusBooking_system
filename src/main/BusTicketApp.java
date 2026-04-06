package main;

import com.google.gson.Gson;

import model.Bus;
import model.Route;
import service.BookingService;
import service.BusService;
import service.RouteService;
import service.SeatService;
import util.ConsoleUI;
import util.DatabaseConnection;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * Bus Ticket Booking System - Main Application
 * A console-based bus ticket booking system with JDBC
 * 
 * @author Student
 * @version 1.0
 */
public class BusTicketApp {

    private Scanner scanner;
    private RouteService routeService;
    private BusService busService;
    private SeatService seatService;
    private BookingService bookingService;

    private static final String APP_TITLE = "=== BUS TICKET BOOKING SYSTEM ";

    public BusTicketApp() {
        this.scanner = new Scanner(System.in);
        this.routeService = new RouteService(scanner);
        this.busService = new BusService(scanner);
        this.seatService = new SeatService(scanner);
        this.bookingService = new BookingService(scanner);
    }

    /**
     * Start the application
     */
    public void start() {
        // Display welcome screen
        displayWelcome();

        // Test database connection
        if (!testDatabaseConnection()) {
            ConsoleUI.printError("Cannot connect to database. Please check your configuration.");
            ConsoleUI.printInfo("Make sure MySQL is running and database 'bus_booking_db' exists.");
            return;
        }

        // Main menu loop
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getMenuChoice(0, 8);

            switch (choice) {
                case 1:
                    viewAllRoutes();
                    break;
                case 2:
                    searchBuses();
                    break;
                case 3:
                    viewSeatAvailability();
                    break;
                case 4:
                    bookTicket();
                    break;
                case 5:
                    cancelBooking();
                    break;
                case 6:
                    viewBookingDetails();
                    break;
                case 7:
                    searchBookings();
                    break;
                case 8:
                    viewAllBookings();
                    break;
                case 0:
                    running = false;
                    displayGoodbye();
                    break;
                default:
                    ConsoleUI.printError("Invalid option. Please try again.");
            }
        }

        // Cleanup
        scanner.close();
        DatabaseConnection.getInstance().closeConnection();
    }

    /**
     * Display welcome screen
     */
    private void displayWelcome() {
        ConsoleUI.clearScreen();
        System.out.println();
        System.out.println(ConsoleUI.CYAN + ConsoleUI.BOLD);
        System.out.println("    +======================================================+");
        System.out.println("    |                                                      |");
        System.out.println("    |       *** BUS TICKET BOOKING SYSTEM ***              |");
        System.out.println("    |                                                      |");
        System.out.println("    |            Book Your Journey Today!                  |");
        System.out.println("    |                                                      |");
        System.out.println("    +======================================================+");
        System.out.println(ConsoleUI.RESET);
        System.out.println();
        ConsoleUI.printLoading("Initializing system");
    }

    /**
     * Test database connection
     */
    private boolean testDatabaseConnection() {
        try {
            DatabaseConnection.getInstance().getConnection();
            ConsoleUI.printSuccess("✓ Database connection successful!");
            return true;
        } catch (Exception e) {
            ConsoleUI.printError("✗ Database connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Display main menu
     */
    private void displayMainMenu() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader(APP_TITLE);

        System.out.println();
        System.out.println(ConsoleUI.BOLD + "  MAIN MENU" + ConsoleUI.RESET);
        System.out.println("  " + ConsoleUI.repeat(ConsoleUI.SH, 40));
        System.out.println();

        ConsoleUI.printMenuOption(1, "View All Routes");
        ConsoleUI.printMenuOption(2, "Search Buses by Route");
        ConsoleUI.printMenuOption(3, "View Seat Availability");
        ConsoleUI.printMenuOption(4, "Book a Ticket");
        ConsoleUI.printMenuOption(5, "Cancel Booking");
        ConsoleUI.printMenuOption(6, "View Booking Details");
        ConsoleUI.printMenuOption(7, "Search Bookings by Phone");
        ConsoleUI.printMenuOption(8, "View All Bookings");

        System.out.println();
        System.out.println("  " + ConsoleUI.repeat(ConsoleUI.SH, 40));
        ConsoleUI.printMenuOption(0, "Exit");
        System.out.println();
    }

    /**
     * Get menu choice from user
     */
    private int getMenuChoice(int min, int max) {
        ConsoleUI.printPrompt("Enter your choice");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice < min || choice > max) {
                ConsoleUI.printError("Please enter a number between " + min + " and " + max);
                return -1;
            }
            return choice;
        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid input. Please enter a number.");
            return -1;
        }
    }

    /**
     * Option 1: View all routes
     */
    private void viewAllRoutes() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("VIEW ALL ROUTES");

        routeService.displayAllRoutes();

        waitForUser();
    }

    /**
     * Option 2: Search buses by route
     */
    private void searchBuses() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("SEARCH BUSES");

        // Select route
        Route route = routeService.selectRoute();

        if (route == null) {
            return;
        }

        // Get travel date
        Date travelDate = getTravelDate();
        if (travelDate == null) {
            return;
        }

        // Display buses
        busService.displayBusesByRoute(route.getRouteId(), travelDate);

        waitForUser();
    }

    /**
     * Option 3: View seat availability
     */
    private void viewSeatAvailability() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("VIEW SEAT AVAILABILITY");

        // Select route first
        Route route = routeService.selectRoute();
        if (route == null) {
            return;
        }

        // Get travel date
        Date travelDate = getTravelDate();
        if (travelDate == null) {
            return;
        }

        // Select bus
        Bus bus = busService.selectBus(route, travelDate);
        if (bus == null) {
            return;
        }

        // Display seat map
        seatService.displaySeatMap(bus.getBusId(), travelDate);

        waitForUser();
    }

    /**
     * Option 4: Book a ticket
     */
    private void bookTicket() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("BOOK A TICKET");

        // Step 1: Select route
        ConsoleUI.printSubHeader("Step 1: Select Route");
        Route route = routeService.selectRoute();
        if (route == null) {
            return;
        }

        // Step 2: Get travel date
        ConsoleUI.printSubHeader("Step 2: Select Travel Date");
        Date travelDate = getTravelDate();
        if (travelDate == null) {
            return;
        }

        // Step 3: Select bus
        ConsoleUI.printSubHeader("Step 3: Select Bus");
        Bus bus = busService.selectBus(route, travelDate);
        if (bus == null) {
            return;
        }

        // Step 4: View seats and select
        ConsoleUI.printSubHeader("Step 4: Select Seat");
        seatService.displaySeatMap(bus.getBusId(), travelDate);

        // Show available seats list
        seatService.displayAvailableSeats(bus.getBusId(), travelDate);

        System.out.println();
        ConsoleUI.printPrompt("Enter Seat Number (e.g., 01, 15)");
        String seatNumber = scanner.nextLine().trim().toUpperCase();

        // Validate seat
        if (!seatService.isSeatAvailable(bus.getBusId(), seatNumber, travelDate)) {
            ConsoleUI.printError("Seat " + seatNumber + " is not available. Please try a different seat.");
            waitForUser();
            return;
        }

        ConsoleUI.printSuccess("Seat " + seatNumber + " is available!");

        // Step 5: Get passenger details
        ConsoleUI.printSubHeader("Step 5: Enter Passenger Details");

        ConsoleUI.printPrompt("Passenger Name");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            ConsoleUI.printError("Name cannot be empty.");
            waitForUser();
            return;
        }

        ConsoleUI.printPrompt("Phone Number");
        String phone = scanner.nextLine().trim();
        if (phone.isEmpty() || phone.length() < 10) {
            ConsoleUI.printError("Please enter a valid phone number.");
            waitForUser();
            return;
        }

        ConsoleUI.printPrompt("Email (optional, press Enter to skip)");
        String email = scanner.nextLine().trim();

        // Confirm booking
        System.out.println();
        ConsoleUI.printSubHeader("Booking Summary");
        System.out.println("  Route: " + route.getSource() + " -> " + route.getDestination());
        System.out.println("  Bus: " + bus.getBusNumber() + " (" + bus.getBusTypeDisplay() + ")");
        System.out.println("  Date: " + travelDate + " | Departure: " + bus.getFormattedDepartureTime());
        System.out.println("  Seat: " + seatNumber);
        System.out.println("  Fare: Rs." + String.format("%.2f", route.getFare()));
        System.out.println("  Passenger: " + name + " | Phone: " + phone);
        System.out.println();

        ConsoleUI.printPrompt("Confirm booking? (yes/no)");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes") && !confirm.equals("y")) {
            ConsoleUI.printWarning("Booking cancelled.");
            waitForUser();
            return;
        }

        // Perform booking
        ConsoleUI.printLoading("Processing your booking");
        try {
            boolean success = bookingService.bookSeat(bus.getBusId(), seatNumber, name, phone, email, travelDate);
            if (!success) {
                ConsoleUI.printError("Booking failed. Please try again.");
            }
        } catch (java.sql.SQLException e) {
            ConsoleUI.printError("Database error during booking: " + e.getMessage());
        }

        waitForUser();
    }

    /**
     * Option 5: Cancel booking
     */
    private void cancelBooking() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("CANCEL BOOKING");

        ConsoleUI.printPrompt("Enter Booking ID");

        try {
            int bookingId = Integer.parseInt(scanner.nextLine().trim());

            ConsoleUI.printLoading("Processing cancellation");
            System.out.println();

            bookingService.cancelBooking(bookingId);

        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid booking ID. Please enter a number.");
        }

        waitForUser();
    }

    /**
     * Option 6: View booking details
     */
    private void viewBookingDetails() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("VIEW BOOKING DETAILS");

        ConsoleUI.printPrompt("Enter Booking ID");

        try {
            int bookingId = Integer.parseInt(scanner.nextLine().trim());

            bookingService.viewBookingById(bookingId);

        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid booking ID. Please enter a number.");
        }

        waitForUser();
    }

    /**
     * Option 7: Search bookings by phone
     */
    private void searchBookings() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("SEARCH BOOKINGS");

        ConsoleUI.printPrompt("Enter Phone Number");
        String phone = scanner.nextLine().trim();

        if (phone.isEmpty()) {
            ConsoleUI.printError("Phone number cannot be empty.");
            waitForUser();
            return;
        }

        bookingService.searchBookingsByPhone(phone);

        waitForUser();
    }

    /**
     * Option 8: View all bookings
     */
    private void viewAllBookings() {
        ConsoleUI.clearScreen();
        ConsoleUI.printHeader("ALL BOOKINGS");

        bookingService.viewAllBookings();

        waitForUser();
    }

    /**
     * Get travel date from user
     */
    private Date getTravelDate() {
        ConsoleUI.printPrompt("Enter Travel Date (YYYY-MM-DD)");
        String dateStr = scanner.nextLine().trim();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            java.util.Date parsed = sdf.parse(dateStr);

            Date sqlDate = new Date(parsed.getTime());

            // Validate date is not in the past
            Date today = new Date(System.currentTimeMillis());
            if (sqlDate.before(today)) {
                ConsoleUI.printError("Travel date cannot be in the past.");
                return null;
            }

            return sqlDate;

        } catch (ParseException e) {
            ConsoleUI.printError("Invalid date format. Please use YYYY-MM-DD (e.g., 2026-01-15)");
            return null;
        }
    }

    /**
     * Display goodbye message
     */
    private void displayGoodbye() {
        ConsoleUI.clearScreen();
        System.out.println();
        System.out.println(ConsoleUI.CYAN + ConsoleUI.BOLD);
        System.out.println("    +======================================================+");
        System.out.println("    |                                                      |");
        System.out.println("    |          Thank you for using our service!            |");
        System.out.println("    |                                                      |");
        System.out.println("    |               Have a safe journey!                   |");
        System.out.println("    |                                                      |");
        System.out.println("    +======================================================+");
        System.out.println(ConsoleUI.RESET);
        System.out.println();
    }

    /**
     * Wait for user to press Enter
     */
    private void waitForUser() {
        System.out.println();
        System.out.print(ConsoleUI.YELLOW + "  Press Enter to continue..." + ConsoleUI.RESET);
        scanner.nextLine();
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        // Handle API calls
        if (args.length > 0 && args[0].equals("--api")) {
            handleApiCall(args);
            return;
        }

        // Normal console application
        BusTicketApp app = new BusTicketApp();
        app.start();
    }

    /**
     * Handle API calls from the web server
     */
    private static void handleApiCall(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("{\"error\": \"Invalid API call\"}");
                return;
            }

            String operation = args[1];

            switch (operation) {
                case "--get-routes":
                    handleGetRoutes();
                    break;
                case "--get-buses":
                    if (args.length > 2) {
                        int routeId = Integer.parseInt(args[2]);
                        handleGetBuses(routeId);
                    }
                    break;
                case "--get-seats":
                    if (args.length > 2) {
                        int busId = Integer.parseInt(args[2]);
                        handleGetSeats(busId);
                    }
                    break;
                case "--book-ticket":
                    if (args.length >= 8) {
                        int busId = Integer.parseInt(args[2]);
                        String seatNumber = args[3];
                        String passengerName = args[4];
                        String phone = args[5];
                        String email = args[6];
                        String travelDateStr = args[7];
                        handleBookTicket(busId, seatNumber, passengerName, phone, email, travelDateStr);
                    }
                    break;
                default:
                    System.out.println("{\"error\": \"Unknown operation\"}");
            }
        } catch (Exception e) {
            System.out.println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private static void handleGetRoutes() {
        try {
            RouteService routeService = new RouteService(null);
            var routes = routeService.getAllRoutes();
            Gson gson = new Gson();
            System.out.println(gson.toJson(routes));
        } catch (Exception e) {
            System.out.println("[]");
        }
    }

    private static void handleGetBuses(int routeId) {
        try {
            BusService busService = new BusService(null);
            var buses = busService.getBusesByRoute(routeId);
            Gson gson = new Gson();
            System.out.println(gson.toJson(buses));
        } catch (Exception e) {
            System.out.println("[]");
        }
    }

    private static void handleGetSeats(int busId) {
        try {
            SeatService seatService = new SeatService(null);
            var seats = seatService.getSeatsByBus(busId);
            Gson gson = new Gson();
            System.out.println(gson.toJson(seats));
        } catch (Exception e) {
            System.out.println("[]");
        }
    }

    private static void handleBookTicket(int busId, String seatNumber, String passengerName, String phone, String email, String travelDateStr) {
        try {
            BookingService bookingService = new BookingService(null);
            java.sql.Date travelDate = java.sql.Date.valueOf(travelDateStr);
            boolean success = bookingService.bookSeat(busId, seatNumber, passengerName, phone, email, travelDate);
            if (success) {
                System.out.println("{\"success\": true, \"message\": \"Booking successful\"}");
            } else {
                System.out.println("{\"success\": false, \"message\": \"Booking failed\"}");
            }
        } catch (Exception e) {
            System.out.println("{\"success\": false, \"message\": \"Booking failed: " + e.getMessage() + "\"}");
        }
    }
}
