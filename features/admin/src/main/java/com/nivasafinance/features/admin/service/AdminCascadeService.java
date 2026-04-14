package com.nivasafinance.features.admin.service;

public interface AdminCascadeService {

    void cascadeDeletePerson(String mobileNumber);

    void cascadeRestorePerson(String mobileNumber);

    void cascadeDeleteUser(String username);

    void cascadeRestoreUser(String username);
}
