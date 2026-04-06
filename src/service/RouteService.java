package service;

import dao.RouteDAO;
import model.Route;
import util.ConsoleUI;

import java.util.List;
import java.util.Scanner;

/**
 * Route Service
 * Handles business logic for route operations
 */
public class RouteService {
    
    private RouteDAO routeDAO;
    private Scanner scanner;
    
    public RouteService(Scanner scanner) {
        this.routeDAO = new RouteDAO();
        this.scanner = scanner;
    }
    
    /**
     * Display all available routes
     */
    public void displayAllRoutes() {
        List<Route> routes = routeDAO.getAllRoutes();
        
        if (routes.isEmpty()) {
            ConsoleUI.printWarning("No routes available.");
            return;
        }
        
        ConsoleUI.printSubHeader("Available Routes (" + routes.size() + ")");
        
        int[] widths = {5, 15, 15, 10, 10};
        ConsoleUI.printTableHeader(widths, "ID", "From", "To", "Fare", "Duration");
        
        for (Route route : routes) {
            ConsoleUI.printTableDataRow(widths,
                String.valueOf(route.getRouteId()),
                route.getSource(),
                route.getDestination(),
                "Rs." + String.format("%.0f", route.getFare()),
                route.getFormattedDuration()
            );
        }
        
        ConsoleUI.printTableFooter(widths);
    }
    
    /**
     * Search routes by source and/or destination
     */
    public List<Route> searchRoutes(String source, String destination) {
        List<Route> routes = routeDAO.searchRoutes(source, destination);
        
        if (routes.isEmpty()) {
            ConsoleUI.printWarning("No routes found matching your search.");
            return routes;
        }
        
        ConsoleUI.printSubHeader("Search Results (" + routes.size() + " route(s) found)");
        
        int[] widths = {5, 15, 15, 10, 10};
        ConsoleUI.printTableHeader(widths, "ID", "From", "To", "Fare", "Duration");
        
        for (Route route : routes) {
            ConsoleUI.printTableDataRow(widths,
                String.valueOf(route.getRouteId()),
                route.getSource(),
                route.getDestination(),
                "Rs." + String.format("%.0f", route.getFare()),
                route.getFormattedDuration()
            );
        }
        
        ConsoleUI.printTableFooter(widths);
        
        return routes;
    }
    
    /**
     * Get all routes (for selection)
     */
    public List<Route> getAllRoutes() {
        return routeDAO.getAllRoutes();
    }
    
    /**
     * Get route by ID
     */
    public Route getRouteById(int routeId) {
        return routeDAO.getRouteById(routeId);
    }
    
    /**
     * Interactive route selection
     */
    public Route selectRoute() {
        displayAllRoutes();
        
        System.out.println();
        ConsoleUI.printPrompt("Enter Route ID (or 0 to go back)");
        
        try {
            int routeId = Integer.parseInt(scanner.nextLine().trim());
            
            if (routeId == 0) {
                return null;
            }
            
            Route route = routeDAO.getRouteById(routeId);
            
            if (route == null) {
                ConsoleUI.printError("Invalid Route ID. Please try again.");
                return null;
            }
            
            ConsoleUI.printSuccess("Selected: " + route.getSource() + " -> " + route.getDestination());
            return route;
            
        } catch (NumberFormatException e) {
            ConsoleUI.printError("Invalid input. Please enter a number.");
            return null;
        }
    }
    
    /**
     * Display unique sources
     */
    public void displaySources() {
        List<String> sources = routeDAO.getUniqueSources();
        
        ConsoleUI.printSubHeader("Available Source Cities");
        for (int i = 0; i < sources.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, sources.get(i));
        }
    }
    
    /**
     * Display unique destinations
     */
    public void displayDestinations() {
        List<String> destinations = routeDAO.getUniqueDestinations();
        
        ConsoleUI.printSubHeader("Available Destination Cities");
        for (int i = 0; i < destinations.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, destinations.get(i));
        }
    }
}
