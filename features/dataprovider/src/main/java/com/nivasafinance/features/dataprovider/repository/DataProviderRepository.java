package com.nivasafinance.features.dataprovider.repository;

import com.nivasafinance.features.dataprovider.entity.DataProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataProviderRepository extends JpaRepository<DataProvider, Long> {

    Optional<DataProvider> findByName(String name);
}
