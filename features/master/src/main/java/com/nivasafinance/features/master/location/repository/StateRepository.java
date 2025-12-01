package com.nivasafinance.features.master.location.repository;

import com.nivasafinance.features.master.location.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findAllByIsActiveTrue();
    List<State> findByCountryIdAndIsActiveTrue(Long countryId);
}

