package com.pharmacy.util;

/**
 * Lightweight global session manager that stores the currently
 * logged-in user's credentials and role for the duration of the session.
 * Used by controllers to enforce Role-Based Access Control (RBAC).
 */
public class UserSession {
    private static String currentUsername;
    private static String currentRole;

    /**
     * Sets the active session after a successful login.
     *
     * @param username The authenticated username.
     * @param role     The user's role ("Admin" or "Cashier").
     */
    public static void login(String username, String role) {
        currentUsername = username;
        currentRole = role;
    }

    /**
     * Clears the session on logout.
     */
    public static void logout() {
        currentUsername = null;
        currentRole = null;
    }

    /** @return The currently logged-in username, or null if no session. */
    public static String getUsername() {
        return currentUsername;
    }

    /** @return The current user's role ("Admin" or "Cashier"), or null if no session. */
    public static String getRole() {
        return currentRole;
    }

    /** @return true if the current user has the Admin role. */
    public static boolean isAdmin() {
        return "Admin".equals(currentRole);
    }

    /** @return true if the current user has the Cashier role. */
    public static boolean isCashier() {
        return "Cashier".equals(currentRole);
    }
}
