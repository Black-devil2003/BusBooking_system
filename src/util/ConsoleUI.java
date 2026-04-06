package util;

/**
 * Console UI Utility
 * Provides methods for creating beautiful console output
 */
public class ConsoleUI {
    
    public static boolean quietMode = false;
    
    // ANSI Color Codes
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    
    // Background colors
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    
    // Text styles
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    
    // Box drawing characters - using ASCII alternatives for Windows compatibility
    public static final char TL = '+';  // Top-left
    public static final char TR = '+';  // Top-right
    public static final char BL = '+';  // Bottom-left
    public static final char BR = '+';  // Bottom-right
    public static final char H = '=';   // Horizontal
    public static final char V = '|';   // Vertical
    public static final char LT = '+';  // Left-T
    public static final char RT = '+';  // Right-T
    public static final char TT = '+';  // Top-T
    public static final char BT = '+';  // Bottom-T
    public static final char CR = '+';  // Cross
    
    // Single line characters
    public static final char STL = '+';
    public static final char STR = '+';
    public static final char SBL = '+';
    public static final char SBR = '+';
    public static final char SH = '-';
    public static final char SV = '|';
    
    /**
     * Clear console screen
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    
    /**
     * Print a horizontal line
     */
    public static void printLine(int width) {
        System.out.println(repeat(H, width));
    }
    
    /**
     * Print a single horizontal line
     */
    public static void printSingleLine(int width) {
        System.out.println(repeat(SH, width));
    }
    
    /**
     * Print a box with title
     */
    public static void printBox(String title, int width) {
        int padding = (width - title.length() - 2) / 2;
        
        // Top border
        System.out.print(TL);
        System.out.print(repeat(H, width - 2));
        System.out.println(TR);
        
        // Title
        System.out.print(V);
        System.out.print(repeat(' ', padding));
        System.out.print(BOLD + CYAN + title + RESET);
        System.out.print(repeat(' ', width - padding - title.length() - 2));
        System.out.println(V);
        
        // Bottom border
        System.out.print(BL);
        System.out.print(repeat(H, width - 2));
        System.out.println(BR);
    }
    
    /**
     * Print a header banner
     */
    public static void printHeader(String title) {
        int width = 60;
        System.out.println();
        System.out.print(CYAN + BOLD);
        System.out.print(TL);
        System.out.print(repeat(H, width - 2));
        System.out.println(TR);
        
        String paddedTitle = centerText(title, width - 2);
        System.out.print(V + " ");
        System.out.print(paddedTitle);
        System.out.println(" " + V);
        
        System.out.print(BL);
        System.out.print(repeat(H, width - 2));
        System.out.println(BR);
        System.out.print(RESET);
    }
    
    /**
     * Print a sub-header
     */
    public static void printSubHeader(String title) {
        if (quietMode) return;
        System.out.println();
        System.out.println(YELLOW + BOLD + "  " + title + RESET);
        System.out.println("  " + repeat(SH, title.length()));
    }
    
    /**
     * Print success message
     */
    public static void printSuccess(String message) {
        if (!quietMode) System.out.println(GREEN + BOLD + "  [OK] " + message + RESET);
    }
    
    public static void printError(String message) {
        if (!quietMode) System.out.println(RED + BOLD + "  [ERROR] " + message + RESET);
    }
    
    /**
     * Print warning message
     */
    public static void printWarning(String message) {
        System.out.println(YELLOW + "  [!] " + message + RESET);
    }
    
    /**
     * Print info message
     */
    public static void printInfo(String message) {
        System.out.println(CYAN + "  [i] " + message + RESET);
    }
    
    /**
     * Print a menu option
     */
    public static void printMenuOption(int number, String text) {
        System.out.printf("  %s[%d]%s %s%n", CYAN + BOLD, number, RESET, text);
    }
    
    /**
     * Print a menu option with icon
     */
    public static void printMenuOption(int number, String icon, String text) {
        System.out.printf("  %s[%d]%s %s %s%n", CYAN + BOLD, number, RESET, icon, text);
    }
    
    /**
     * Print a table row
     */
    public static void printTableRow(String... cells) {
        System.out.print("  " + SV);
        for (String cell : cells) {
            System.out.print(" " + cell + " " + SV);
        }
        System.out.println();
    }
    
    /**
     * Print a table header row
     */
    public static void printTableHeader(int[] widths, String... headers) {
        // Top border
        System.out.print("  " + STL);
        for (int i = 0; i < widths.length; i++) {
            System.out.print(repeat(SH, widths[i] + 2));
            if (i < widths.length - 1) System.out.print("+");
        }
        System.out.println(STR);
        
        // Header row
        System.out.print("  " + SV);
        for (int i = 0; i < headers.length; i++) {
            String header = centerText(headers[i], widths[i]);
            System.out.print(" " + BOLD + CYAN + header + RESET + " " + SV);
        }
        System.out.println();
        
        // Separator
        System.out.print("  ├");
        for (int i = 0; i < widths.length; i++) {
            System.out.print(repeat(SH, widths[i] + 2));
            if (i < widths.length - 1) System.out.print("+");
        }
        System.out.println("+");
    }
    
    /**
     * Print a table data row
     */
    public static void printTableDataRow(int[] widths, String... data) {
        System.out.print("  " + SV);
        for (int i = 0; i < data.length; i++) {
            String cell = padRight(data[i], widths[i]);
            System.out.print(" " + cell + " " + SV);
        }
        System.out.println();
    }
    
    /**
     * Print table footer
     */
    public static void printTableFooter(int[] widths) {
        System.out.print("  " + SBL);
        for (int i = 0; i < widths.length; i++) {
            System.out.print(repeat(SH, widths[i] + 2));
            if (i < widths.length - 1) System.out.print("+");
        }
        System.out.println(SBR);
    }
    
    /**
     * Print seat map legend
     */
    public static void printSeatLegend() {
        System.out.println();
        System.out.println("  " + BOLD + "Seat Legend:" + RESET);
        System.out.println("    " + GREEN + "[  ]" + RESET + " Available    " + 
                          RED + "[XX]" + RESET + " Booked    " +
                          CYAN + "[W]" + RESET + " Window    " +
                          YELLOW + "[A]" + RESET + " Aisle");
    }
    
    /**
     * Print available seat
     */
    public static String formatAvailableSeat(String seatNum) {
        return GREEN + "[" + padCenter(seatNum, 2) + "]" + RESET;
    }
    
    /**
     * Print booked seat
     */
    public static String formatBookedSeat(String seatNum) {
        return RED + "[XX]" + RESET;
    }
    
    /**
     * Print a prompt
     */
    public static void printPrompt(String message) {
        System.out.print(YELLOW + "  > " + message + ": " + RESET);
    }
    
    /**
     * Print a booking ticket
     */
    public static void printTicket(String bookingId, String passenger, String phone,
                                    String bus, String route, String seat,
                                    String date, String time, String fare, String status) {
        if (quietMode) return;
        int width = 50;
        System.out.println();
        System.out.println(CYAN + "  " + TL + repeat(H, width) + TR + RESET);
        System.out.println(CYAN + "  " + V + RESET + centerText(BOLD + "*** BUS TICKET ***" + RESET, width) + CYAN + V + RESET);
        System.out.println(CYAN + "  " + LT + repeat(H, width) + RT + RESET);
        
        printTicketRow("Booking ID", bookingId, width);
        printTicketRow("Passenger", passenger, width);
        printTicketRow("Phone", phone, width);
        System.out.println(CYAN + "  " + LT + repeat(H, width) + RT + RESET);
        printTicketRow("Bus", bus, width);
        printTicketRow("Route", route, width);
        printTicketRow("Seat No.", seat, width);
        System.out.println(CYAN + "  " + LT + repeat(H, width) + RT + RESET);
        printTicketRow("Travel Date", date, width);
        printTicketRow("Departure", time, width);
        printTicketRow("Fare", "Rs." + fare, width);
        System.out.println(CYAN + "  " + LT + repeat(H, width) + RT + RESET);
        
        String statusColor = status.equals("CONFIRMED") ? GREEN : RED;
        System.out.println(CYAN + "  " + V + RESET + 
                          centerText("Status: " + statusColor + BOLD + status + RESET, width) + 
                          CYAN + V + RESET);
        
        System.out.println(CYAN + "  " + BL + repeat(H, width) + BR + RESET);
    }
    
    private static void printTicketRow(String label, String value, int width) {
        String content = String.format("  %-12s: %s", label, value);
        System.out.println(CYAN + "  " + V + RESET + padRight(content, width) + CYAN + V + RESET);
    }
    
    /**
     * Helper: Repeat a character
     */
    public static String repeat(char c, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
    
    /**
     * Helper: Center text
     */
    public static String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return repeat(' ', padding) + text + repeat(' ', width - text.length() - padding);
    }
    
    /**
     * Helper: Pad right
     */
    public static String padRight(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        return text + repeat(' ', width - text.length());
    }
    
    /**
     * Helper: Pad center
     */
    public static String padCenter(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return repeat(' ', padding) + text + repeat(' ', width - text.length() - padding);
    }
    
    /**
     * Print loading animation
     */
    public static void printLoading(String message) {
        System.out.print(CYAN + "  " + message + "..." + RESET);
    }
    
    /**
     * Print done after loading
     */
    public static void printDone() {
        System.out.println(GREEN + " Done!" + RESET);
    }
    
    /**
     * Wait for user to press Enter
     */
    public static void pressEnterToContinue() {
        System.out.println();
        System.out.print(YELLOW + "  Press Enter to continue..." + RESET);
        try {
            System.in.read();
        } catch (Exception e) {}
    }
}
