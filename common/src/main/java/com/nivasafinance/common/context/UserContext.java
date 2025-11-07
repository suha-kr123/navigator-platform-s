package com.nivasafinance.common.context;

public class UserContext {
    private static final ThreadLocal<String> usernameThreadLocal = new ThreadLocal<>();

    private UserContext() {
        // Private constructor to prevent instantiation
    }

    public static void setUsername(String username) {
        usernameThreadLocal.set(username);
    }

    public static String getUsername() {
        return usernameThreadLocal.get();
    }

    public static void clear() {
        usernameThreadLocal.remove();
    }
}