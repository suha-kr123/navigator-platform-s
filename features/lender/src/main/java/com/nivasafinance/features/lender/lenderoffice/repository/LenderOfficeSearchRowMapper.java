package com.nivasafinance.features.lender.lenderoffice.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LenderOfficeSearchRowMapper implements RowMapper<LenderOfficeReponseData> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public LenderOfficeReponseData mapRow(ResultSet rs, int rowNum) throws SQLException {
        String idStr = rs.getString("id");
        UUID id = idStr != null ? UUID.fromString(idStr) : null;
        String name = rs.getString("name");
        String key = rs.getString("key");
        String lenderKey = rs.getString("lender_key");
        String statusStr = rs.getString("status");
        LenderOfficeStatus status = statusStr != null ? LenderOfficeStatus.valueOf(statusStr) : null;
        AddressData address = mapAddress(rs.getString("address"));
        return new LenderOfficeReponseData(id, name, key, lenderKey, address, status);
    }

    private static AddressData mapAddress(String addressJson) {
        if (addressJson == null || addressJson.isBlank()) {
            return null;
        }
        try {
            LenderOffice.AddressDetails details = OBJECT_MAPPER.readValue(addressJson, LenderOffice.AddressDetails.class);
            return details != null ? details.getAddress() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
