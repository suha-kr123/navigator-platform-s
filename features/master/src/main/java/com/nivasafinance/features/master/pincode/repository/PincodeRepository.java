package com.nivasafinance.features.master.pincode.repository;

import com.nivasafinance.features.master.pincode.entity.Pincode;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@JaversSpringDataAuditable
public interface PincodeRepository extends JpaRepository<Pincode, Long> {
    List<Pincode> findAllByPincode(String pincode);
}

