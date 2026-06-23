package com.pharmacy.util;

/**
 * Lightweight global session manager that stores the currently
 * logged-in user's credentials and role for the duration of the session.
 * Used by controllers to enforce Role-Based Access Control (RBAC).
 */
public class UserSession {
    // 1. Static variables to hold the active user's credentials globally in memory
    private static String currentUsername;
    private static String currentRole;

    /**
     * Sets the active session after a successful login.
     *
     * @param username The authenticated username.
     * @param role     The user's role ("Admin" or "Cashier").
     */
    public static void login(String username, String role) {
        // 2. Assign the passed credentials to the static variables, effectively "logging in" the user
        currentUsername = username;
        currentRole = role;
    }

    /**
     * Clears the session on logout.
     */
    public static void logout() {
        // 3. Nullify the static variables, effectively "logging out" the user and preventing further access
        currentUsername = null;
        currentRole = null;
    }

    /** 
     * @return The currently logged-in username, or null if no session exists. 
     */
    public static String getUsername() {
        // 4. Return the username string for UI display or logging purposes
        return currentUsername;
    }

    /** 
     * @return The current user's role ("Admin" or "Cashier"), or null if no session exists. 
     */
    public static String getRole() {
        // 5. Return the exact role string, primarily used for internal role-checking logic
        return currentRole;
    }

    /** 
     * Helper method to determine if the active session possesses administrator privileges.
     * @return true if the current user has the Admin role. 
     */
    public static boolean isAdmin() {
        // 6. Compare the active role against the literal "Admin" string safely
        return "Admin".equals(currentRole);
    }

    /** 
     * Helper method to determine if the active session is restricted to cashier privileges.
     * @return true if the current user has the Cashier role. 
     */
    public static boolean isCashier() {
        // 7. Compare the active role against the literal "Cashier" string safely
        return "Cashier".equals(currentRole);
    }
}
