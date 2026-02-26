package com.nivasafinance.features.lender.lender.repository;

import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LenderSearchRowMapper implements RowMapper<LenderResponseData> {

    @Override
    public LenderResponseData mapRow(ResultSet rs, int rowNum) throws SQLException {
        String idStr = rs.getString("id");
        UUID id = idStr != null ? UUID.fromString(idStr) : null;
        String name = rs.getString("name");
        String key = rs.getString("key");
        String statusStr = rs.getString("status");
        LenderStatus status = statusStr != null ? LenderStatus.valueOf(statusStr) : null;
        return new LenderResponseData(id, name, key, status);
    }
}
