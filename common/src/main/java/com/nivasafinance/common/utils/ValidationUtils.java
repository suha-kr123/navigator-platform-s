package com.nivasafinance.common.utils;

public class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Checks if the object is not null
     * 
     * @param obj the object to check
     * @return true if object is not null, false otherwise
     */
    public static boolean isNonNull(Object obj) {
        return obj != null;
    }

    /**
     * Checks if the string is not null or empty
     * 
     * @param str the string to check
     * @return true if string is not null and not empty, false otherwise
     */
    public static boolean isNonNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Checks if at least one of the provided objects is not null
     * 
     * @param objects the objects to check
     * @return true if at least one object is not null (and not empty if string), false otherwise
     */
    public static boolean hasAtLeastOne(Object... objects) {
        for (Object obj : objects) {
            if (obj != null) {
                if (obj instanceof String) {
                    if (!((String) obj).trim().isEmpty()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }
}


