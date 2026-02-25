package com.nivasafinance.features.master.codemaster.repository;

import com.nivasafinance.features.master.codemaster.dto.MasterCodeSearchResponse;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MasterCodeSearchRowMapper implements RowMapper<MasterCodeSearchResponse> {

    @Override
    public MasterCodeSearchResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return MasterCodeSearchResponse.builder()
                .key(rs.getString("key"))
                .codeKey(rs.getString("code_key"))
                .displayText(rs.getString("display_text"))
                .description(rs.getString("description"))
                .parentId(rs.getObject("parent_id", Long.class))
                .isActive(null)
                .build();
    }
}
