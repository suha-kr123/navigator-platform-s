package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    List<Country> findAllByIsActiveTrue();
    java.util.Optional<Country> findByCodeAndIsActiveTrue(String code);
}

