package com.nivasafinance.common.base.model;

import java.util.Objects;

public class PaginationInfo {
    private final int offset;
    private final int limit;
    private final long totalElements; // -1 when total count is not calculated for performance
    private final int totalPages; // -1 when total count is not calculated
    private final int currentPage;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PaginationInfo(int offset, int limit, long totalElements, int totalPages,
                          int currentPage, boolean hasNext, boolean hasPrevious) {
        this.offset = offset;
        this.limit = limit;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationInfo that = (PaginationInfo) o;
        return offset == that.offset &&
                limit == that.limit &&
                totalElements == that.totalElements &&
                totalPages == that.totalPages &&
                currentPage == that.currentPage &&
                hasNext == that.hasNext &&
                hasPrevious == that.hasPrevious;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, limit, totalElements, totalPages, currentPage, hasNext, hasPrevious);
    }

    @Override
    public String toString() {
        return "PaginationInfo{" +
                "offset=" + offset +
                ", limit=" + limit +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", currentPage=" + currentPage +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}

