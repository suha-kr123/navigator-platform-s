package com.nivasafinance.features.bulkoperations.operations.dropoff;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.engine.AbstractBulkOperationReportLayout;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Report layout for DROPOFF bulk operations.
 */
@Component
public class DropoffReportLayout extends AbstractBulkOperationReportLayout {

    private static final List<String> HEADERS = List.of(
            "Row_Number",
            "Lead_Identifier",
            "Status",
            "Reason_Code",
            "Error_Message"
    );

    @Override
    public BulkOperationType getType() {
        return BulkOperationType.DROPOFF;
    }

    @Override
    public String getRowReferenceColumnName() {
        return DropoffRowKeys.LEAD_IDENTIFIER;
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
        values.add(getValue(data, DropoffRowKeys.LEAD_IDENTIFIER));
        values.add(rowStatus);
        values.add(getValue(data, DropoffRowKeys.REASON_CODE));
        values.add(getErrorMessage(row));
        return values;
    }
}
