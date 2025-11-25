package com.nivasafinance.common.utils;

import java.util.Collection;
import java.util.function.Supplier;

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

    /**
     * Throws a RuntimeException if the object is null.
     *
     * @param obj The object to check.
     * @param exceptionSupplier A supplier for the exception to throw if the object is null.
     * @param <T> The type of the object.
     * @param <X> The type of the exception.
     * @return The non-null object.
     * @throws X if the object is null.
     */
    public static <T, X extends RuntimeException> T requireNonNull(T obj, Supplier<X> exceptionSupplier) throws X {
        if (obj == null) {
            throw exceptionSupplier.get();
        }
        return obj;
    }

    /**
     * Throws a RuntimeException if the string is null or empty.
     *
     * @param str The string to check.
     * @param exceptionSupplier A supplier for the exception to throw if the string is null or empty.
     * @param <X> The type of the exception.
     * @return The non-null and non-empty string.
     * @throws X if the string is null or empty.
     */
    public static <X extends RuntimeException> String requireNonNullOrEmpty(String str, Supplier<X> exceptionSupplier) throws X {
        if (str == null || str.trim().isEmpty()) {
            throw exceptionSupplier.get();
        }
        return str;
    }

    /**
     * Throws a RuntimeException if the collection is null or empty.
     *
     * @param collection The collection to check.
     * @param exceptionSupplier A supplier for the exception to throw if the collection is null or empty.
     * @param <T> The type of elements in the collection.
     * @param <X> The type of the exception.
     * @return The non-null and non-empty collection.
     * @throws X if the collection is null or empty.
     */
    public static <T extends Collection<?>, X extends RuntimeException> T requireNonNullOrEmpty(T collection, Supplier<X> exceptionSupplier) throws X {
        if (collection == null || collection.isEmpty()) {
            throw exceptionSupplier.get();
        }
        return collection;
    }
}


