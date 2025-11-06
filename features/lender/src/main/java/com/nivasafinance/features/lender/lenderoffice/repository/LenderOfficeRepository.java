package com.nivasafinance.features.lender.lenderoffice.repository;

import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LenderOfficeRepository extends JpaRepository<LenderOffice, UUID> {
    Optional<LenderOffice> findByKey(String key);

    @Query("SELECT lo FROM LenderOffice lo WHERE lo.lenderKey = :lenderKey AND lo.status = :status")
    List<LenderOffice> findByLenderKeyAndStatus(
            @Param("lenderKey") String lenderKey,
            @Param("status") LenderOfficeStatus status
    );
}

