package service;

import dao.BusDAO;
import model.Bus;
import model.Route;
import util.ConsoleUI;

import java.sql.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Bus Service
 * Handles business logic for bus operations
 */
public class BusService {
    
    private BusDAO busDAO;
    private Scanner scanner;
    
    public BusService(Scanner scanner) {
        this.busDAO = new BusDAO();
        this.scanner = scanner;
    }
    
    /**
     * Display all buses for a route
     */
    public void displayBusesByRoute(int routeId, Date travelDate) {
        List<Bus> buses = busDAO.getBusesByRoute(routeId);
        
        if (buses.isEmpty()) {
            ConsoleUI.printWarning("No buses found for this route.");
            return;
        }
        
        ConsoleUI.printSubHeader("Available Buses (" + buses.size() + ")");
        
        if (travelDate != null) {
            System.out.println("  " + ConsoleUI.CYAN + "Travel Date: " + ConsoleUI.RESET + travelDate);
            System.out.println();
        }
        
        int[] widths = {5, 12, 14, 10, 8, 10};
        ConsoleUI.printTableHeader(widths, "ID", "Bus No.", "Type", "Departure", "Seats", "Available");
        
        for (Bus bus : buses) {
            int availableSeats = travelDate != null ? 
                busDAO.getAvailableSeatCount(bus.getBusId(), travelDate) : bus.getTotalSeats();
            
            ConsoleUI.printTableDataRow(widths,
                String.valueOf(bus.getBusId()),
                bus.getBusNumber(),
                bus.getBusTypeDisplay(),
                bus.getFormattedDepartureTime(),
                String.valueOf(bus.getTotalSeats()),
                String.valueOf(availableSeats)
            );
        }
        
        ConsoleUI.printTableFooter(widths);
    }
    
    /**
     * Display all buses
     */
    public void displayAllBuses() {
        List<Bus> buses = busDAO.getAllBuses();
        
        if (buses.isEmpty()) {
            ConsoleUI.printWarning("No buses available.");
            return;
        }
        
        ConsoleUI.printSubHeader("All Buses (" + buses.size() + ")");
        
        int[] widths = {5, 12, 20, 14, 10, 10};
        ConsoleUI.printTableHeader(widths, "ID", "Bus No.", "Route", "Type", "Departure", "Fare");
        
        for (Bus bus : buses) {
            String route = bus.getSource() + " -> " + bus.getDestination();
            if (route.length() > 20) route = route.substring(0, 17) + "...";
            
            ConsoleUI.printTableDataRow(widths,
                String.valueOf(bus.getBusId()),
                bus.getBusNumber(),
                route,
                bus.getBusTypeDisplay(),
                bus.getFormattedDepartureTime(),
                "Rs." + String.format("%.0f", bus.getFare())
            );
        }
        
        ConsoleUI.printTableFooter(widths);
    }
    
    /**
     * Get buses by route
     */
    public List<Bus> getBusesByRoute(int routeId) {
        return busDAO.getBusesByRoute(routeId);
    }
    
    /**
     * Get bus by ID
     */
    public Bus getBusById(int busId) {
        return busDAO.getBusById(busId);
    }
    
    /**
     * Interactive bus selection
     */
    public Bus selectBus(Route route, Date travelDate) {
        displayBusesByRoute(route.getRouteId(), travelDate);
        
        List<Bus> buses = busDAO.getBusesByRoute(route.getRouteId());
        
        if (buses.isEmpty()) {
            return null;
        }
        
        System.out.println();
        ConsoleUI.printPrompt("Enter Bus ID (or 0 to go back)");
        
        try {
            int busId = Integer.parseInt(scanner.nextLine().trim());
            
            if (busId == 0) {
                return null;
            }
            
            Bus bus = busDAO.getBusById(busId);
            
            if (bus == null || bus.getRouteId() != route.getRouteId()) {
                ConsoleUI.printError("Invalid Bus ID for this route. Please try again.");
                return null;
            }
            
            ConsoleUI.printSuccess("Selected: " + bus.getBusNumber() + " (" + bus.getBusTypeDisplay() + ")");
            return bus;
            
        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid input. Please enter a number.");
            return null;
        }
    }
}
