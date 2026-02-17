package org.example.util;

public class Session {

    private static int userId;
    private static String role;

    public static void set(int id, String r) {
        userId = id;
        role = r;
        System.out.println("SESSION → id=" + id + ", role=" + r);
    }

    public static int getUserId() {
        return userId;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public static boolean isCoach() {
        return "COACH".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    public static void clear() {
        userId = 0;
        role = null;
    }
}
