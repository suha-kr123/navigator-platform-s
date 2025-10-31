package com.nivasafinance.features.notes.dto;

import java.util.Objects;

public class NotesUpdateRequest {
    private String title;
    private String content;

    public NotesUpdateRequest() {
    }

    public NotesUpdateRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotesUpdateRequest that = (NotesUpdateRequest) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, content);
    }

    @Override
    public String toString() {
        return "NotesUpdateRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}

