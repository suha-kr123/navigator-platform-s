package com.nivasafinance.features.lead.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.nivasafinance.features.lead.dto.LeadWorkflowDetailsDto;

@Component
public class LeadWorkflowDetailsRowMapper implements RowMapper<LeadWorkflowDetailsDto> {

    @Override
    public LeadWorkflowDetailsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return LeadWorkflowDetailsDto.builder()
                .leadId(rs.getLong("lead_id"))
                .leadIdentifier(UUID.fromString(rs.getString("lead_identifier")))
                .workflowConfigKey(rs.getString("workflow_config_key"))
                .currentStageKey(rs.getString("current_stage_key"))
                .currentSubStageKey(rs.getString("current_sub_stage_key"))
                .currentStageAssignedTo(rs.getString("assigned_to"))
                .build();
    }
}
