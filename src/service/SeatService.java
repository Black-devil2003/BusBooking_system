package service;

import dao.BusDAO;
import dao.SeatDAO;
import model.Bus;
import model.Seat;
import util.ConsoleUI;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Seat Service
 * Handles business logic for seat operations
 */
public class SeatService {

    private SeatDAO seatDAO;
    private BusDAO busDAO;

    public SeatService(Scanner scanner) {
        this.seatDAO = new SeatDAO();
        this.busDAO = new BusDAO();
    }

    /**
     * Display visual seat map for a bus on a specific date
     */
    public void displaySeatMap(int busId, Date travelDate) {
        Bus bus = busDAO.getBusById(busId);

        if (bus == null) {
            ConsoleUI.printError("Bus not found!");
            return;
        }

        List<Seat> seats;
        try {
            seats = seatDAO.getAvailableSeats(busId, travelDate);
        } catch (SQLException e) {
            ConsoleUI.printError("Database error fetching seats: " + e.getMessage());
            return;
        }

        if (seats.isEmpty()) {
            ConsoleUI.printWarning("No seat information available for this bus.");
            return;
        }

        // Display bus info
        ConsoleUI.printSubHeader("Seat Layout - " + bus.getBusNumber());
        System.out.println("  " + ConsoleUI.CYAN + "Route: " + ConsoleUI.RESET +
                bus.getSource() + " -> " + bus.getDestination());
        System.out.println("  " + ConsoleUI.CYAN + "Type: " + ConsoleUI.RESET +
                bus.getBusTypeDisplay() + " | " +
                ConsoleUI.CYAN + "Departure: " + ConsoleUI.RESET +
                bus.getFormattedDepartureTime());
        System.out.println("  " + ConsoleUI.CYAN + "Date: " + ConsoleUI.RESET + travelDate);

        // Print legend
        ConsoleUI.printSeatLegend();

        System.out.println();

        // Calculate layout
        int seatsPerRow = 4;
        int aisleAfter = 2;

        // Print seats in a visual layout
        System.out.println("  " + ConsoleUI.BOLD + "+-----------------------------+" + ConsoleUI.RESET);
        System.out.println("  " + ConsoleUI.BOLD + "|         FRONT               |" + ConsoleUI.RESET);
        System.out.println("  " + ConsoleUI.BOLD + "+-----------------------------+" + ConsoleUI.RESET);

        int availableCount = 0;
        int bookedCount = 0;

        for (int i = 0; i < seats.size(); i += seatsPerRow) {
            System.out.print("  │ ");

            for (int j = 0; j < seatsPerRow && (i + j) < seats.size(); j++) {
                Seat seat = seats.get(i + j);

                if (seat.isBooked()) {
                    System.out.print(ConsoleUI.formatBookedSeat(seat.getSeatNumber()));
                    bookedCount++;
                } else {
                    System.out.print(ConsoleUI.formatAvailableSeat(seat.getSeatNumber()));
                    availableCount++;
                }

                // Add aisle space
                if (j == aisleAfter - 1) {
                    System.out.print("  "); // Aisle
                } else {
                    System.out.print(" ");
                }
            }

            System.out.println("|");
        }

        System.out.println("  " + ConsoleUI.BOLD + "+-----------------------------+" + ConsoleUI.RESET);
        System.out.println("  " + ConsoleUI.BOLD + "|          REAR               |" + ConsoleUI.RESET);
        System.out.println("  " + ConsoleUI.BOLD + "+-----------------------------+" + ConsoleUI.RESET);

        // Summary
        System.out.println();
        System.out.println("  " + ConsoleUI.GREEN + "Available: " + availableCount + ConsoleUI.RESET +
                " | " + ConsoleUI.RED + "Booked: " + bookedCount + ConsoleUI.RESET +
                " | Total: " + seats.size());
    }

    /**
     * Display seat availability in table format
     */
    public void displaySeatAvailabilityTable(int busId, Date travelDate) {
        Bus bus = busDAO.getBusById(busId);

        if (bus == null) {
            ConsoleUI.printError("Bus not found!");
            return;
        }

        List<Seat> seats;
        try {
            seats = seatDAO.getAvailableSeats(busId, travelDate);
        } catch (SQLException e) {
            ConsoleUI.printError("Database error fetching seats: " + e.getMessage());
            return;
        }

        if (seats.isEmpty()) {
            ConsoleUI.printWarning("No seat information available.");
            return;
        }

        ConsoleUI.printSubHeader("Seat Availability - " + bus.getBusNumber());

        int[] widths = { 8, 10, 12 };
        ConsoleUI.printTableHeader(widths, "Seat #", "Type", "Status");

        for (Seat seat : seats) {
            String status = seat.isBooked() ? ConsoleUI.RED + "BOOKED" + ConsoleUI.RESET
                    : ConsoleUI.GREEN + "AVAILABLE" + ConsoleUI.RESET;

            ConsoleUI.printTableDataRow(widths,
                    seat.getSeatNumber(),
                    seat.getSeatType(),
                    status);
        }

        ConsoleUI.printTableFooter(widths);
    }

    /**
     * Get list of available seats
     */
    public List<Seat> getAvailableSeats(int busId, Date travelDate) {
        try {
            return seatDAO.getOnlyAvailableSeats(busId, travelDate);
        } catch (SQLException e) {
            ConsoleUI.printError("Database error fetching only available seats: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Check if a specific seat is available
     */
    public boolean isSeatAvailable(int busId, String seatNumber, Date travelDate) {
        try {
            Seat seat = seatDAO.getSeatByBusAndNumber(busId, seatNumber);

            if (seat == null) {
                return false;
            }

            List<Seat> availableSeats = seatDAO.getOnlyAvailableSeats(busId, travelDate);

            for (Seat s : availableSeats) {
                if (s.getSeatId() == seat.getSeatId()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            ConsoleUI.printError("Database error checking seat availability: " + e.getMessage());
        }

        return false;
    }

    /**
     * Display available seat list (simplified)
     */
    public void displayAvailableSeats(int busId, Date travelDate) {
        List<Seat> seats;
        try {
            seats = seatDAO.getOnlyAvailableSeats(busId, travelDate);
        } catch (SQLException e) {
            ConsoleUI.printError("Database error fetching available seats: " + e.getMessage());
            return;
        }

        if (seats.isEmpty()) {
            ConsoleUI.printWarning("No seats available!");
            return;
        }

        System.out.println();
        System.out.print("  " + ConsoleUI.GREEN + "Available Seats: " + ConsoleUI.RESET);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seats.size(); i++) {
            sb.append(seats.get(i).getSeatNumber());
            if (i < seats.size() - 1) {
                sb.append(", ");
            }
        }
        System.out.println(sb.toString());
        System.out.println("  " + ConsoleUI.CYAN + "Total: " + seats.size() + " seats available" + ConsoleUI.RESET);
    }

    public List<Seat> getSeatsByBus(int busId) {
        try {
            return seatDAO.getAvailableSeats(busId, new java.sql.Date(System.currentTimeMillis()));
        } catch (SQLException e) {
            ConsoleUI.printError("Database error fetching seats by bus: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
