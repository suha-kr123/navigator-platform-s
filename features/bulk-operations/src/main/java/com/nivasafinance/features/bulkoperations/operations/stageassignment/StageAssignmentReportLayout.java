package com.nivasafinance.features.bulkoperations.operations.stageassignment;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.engine.AbstractBulkOperationReportLayout;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class StageAssignmentReportLayout extends AbstractBulkOperationReportLayout {

    private static final List<String> HEADERS = List.of(
            "Row_Number",
            "Lead_Identifier",
            "Stage_Key",
            "Assigned_To",
            "Status",
            "Error_Message"
    );

    @Override
    public BulkOperationType getType() {
        return BulkOperationType.STAGE_ASSIGNMENT_CHANGE;
    }

    @Override
    public String getRowReferenceColumnName() {
        return StageAssignmentRowKeys.LEAD_IDENTIFIER;
    }

    @Override
    public List<String> getReportHeaders() {
        return HEADERS;
    }

    @Override
    public List<String> buildReportRow(CsvReportRow row, String rowStatus) {
        Map<String, String> data = rowData(row);
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(row.getRowNumber()));
        values.add(getValue(data, StageAssignmentRowKeys.LEAD_IDENTIFIER));
        values.add(getValue(data, StageAssignmentRowKeys.STAGE_KEY));
        values.add(getValue(data, StageAssignmentRowKeys.ASSIGNED_TO));
        values.add(rowStatus);
        values.add(getErrorMessage(row));
        return values;
    }
}

