package com.nivasafinance.common.interfaces;

/**
 * Interface for entities that have an office key.
 * Entities implementing this interface can be used to determine
 * which users are assignable based on office hierarchy.
 */
public interface HasOfficeKey {
    /**
     * Returns the office key associated with this entity.
     * @return the office key, or null if not set
     */
    String getOfficeKey();
}

