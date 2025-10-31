package com.nivasafinance.common.base.model;

import java.util.List;
import java.util.Objects;

public class PaginatedResponse<T> {
    private final List<T> content;
    private final PaginationInfo pagination;

    public PaginatedResponse(List<T> content, PaginationInfo pagination) {
        this.content = content;
        this.pagination = pagination;
    }

    public List<T> getContent() {
        return content;
    }

    public PaginationInfo getPagination() {
        return pagination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginatedResponse<?> that = (PaginatedResponse<?>) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(pagination, that.pagination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, pagination);
    }

    @Override
    public String toString() {
        return "PaginatedResponse{" +
                "content=" + content +
                ", pagination=" + pagination +
                '}';
    }
}

