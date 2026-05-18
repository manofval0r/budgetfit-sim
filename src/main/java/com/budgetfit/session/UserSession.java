package com.budgetfit.session;

public final class UserSession {
    private static int userId = -1;
    private static String username = "";
    private static String role = "user";

    private UserSession() {
    }

    public static void setCurrentUser(int id, String name, String userRole) {
        userId = id;
        username = name == null ? "" : name;
        role = userRole == null ? "user" : userRole;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isLoggedIn() {
        return userId > 0;
    }

    public static boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public static void clear() {
        userId = -1;
        username = "";
        role = "user";
    }
}
