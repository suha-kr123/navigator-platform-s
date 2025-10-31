package com.nivasafinance.common.base.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Objects;

public class PaginationRequest {
    public static final int DEFAULT_LIMIT = 20;
    public static final long MAX_LIMIT = 100L;

    @Min(value = 0, message = "Offset must be 0 or greater")
    private int offset = 0;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = MAX_LIMIT, message = "Limit cannot exceed " + MAX_LIMIT)
    private int limit = DEFAULT_LIMIT;

    private String sortBy;
    private String sortDirection = "ASC";

    public PaginationRequest() {
    }

    public PaginationRequest(int offset, int limit, String sortBy, String sortDirection) {
        this.offset = offset;
        this.limit = limit;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection != null ? sortDirection : "ASC";
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationRequest that = (PaginationRequest) o;
        return offset == that.offset &&
                limit == that.limit &&
                Objects.equals(sortBy, that.sortBy) &&
                Objects.equals(sortDirection, that.sortDirection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, sortBy, sortDirection);
    }

    @Override
    public String toString() {
        return "PaginationRequest{" +
                "offset=" + offset +
                ", limit=" + limit +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}

