package com.nivasafinance.features.admin.service;

public interface AdminUserService {
    void deleteUser(String username);
    void undoDeleteUser(String username);
}
