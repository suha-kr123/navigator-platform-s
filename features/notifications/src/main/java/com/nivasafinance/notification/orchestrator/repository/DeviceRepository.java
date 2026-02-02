package com.nivasafinance.notification.orchestrator.repository;

import com.nivasafinance.notification.orchestrator.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByAppUserAndNotificationToken(String appUser, String notificationToken);

    Optional<Device> findByAppUserAndDeviceId(String appUser, String deviceId);

    @Query("SELECT d FROM Device d WHERE d.appUser = :appUser AND d.notificationToken IN :notificationTokens")
    List<Device> findByAppUserAndNotificationTokenIn(@Param("appUser") String appUser, @Param("notificationTokens") List<String> notificationTokens);

    @Query("SELECT d.notificationToken FROM Device d WHERE d.appUser = :appUser AND d.isActive = true")
    List<String> findActiveNotificationTokensByAppUser(@Param("appUser") String appUser);
}

