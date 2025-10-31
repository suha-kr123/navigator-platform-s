package com.nivasafinance.security.model;

import java.util.Objects;

public class UserInfo {
    private final String userId;
    private final String username;
    private final String email;
    private final String phoneNumber;

    public UserInfo(String userId, String username, String email, String phoneNumber) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(userId, userInfo.userId) &&
                Objects.equals(username, userInfo.username) &&
                Objects.equals(email, userInfo.email) &&
                Objects.equals(phoneNumber, userInfo.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, email, phoneNumber);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}

