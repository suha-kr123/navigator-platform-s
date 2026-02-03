package com.nivasafinance.features.bulkoperations.operations.substagechange;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.engine.AbstractBulkOperationReportLayout;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Report layout for SUBSTAGE_CHANGE bulk operations.
 */
@Component
public class SubstageChangeReportLayout extends AbstractBulkOperationReportLayout {

    private static final List<String> HEADERS = List.of(
            "Row_Number",
            "Lead_Identifier",
            "Stage_Key",
            "Substage_Key",
            "Status",
            "Error_Message"
    );

    @Override
    public BulkOperationType getType() {
        return BulkOperationType.SUBSTAGE_CHANGE;
    }

    @Override
    public String getRowReferenceColumnName() {
        return SubstageChangeRowKeys.LEAD_IDENTIFIER;
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
        values.add(getValue(data, SubstageChangeRowKeys.LEAD_IDENTIFIER));
        values.add(getValue(data, SubstageChangeRowKeys.STAGE_KEY));
        values.add(getValue(data, SubstageChangeRowKeys.SUBSTAGE_KEY));
        values.add(rowStatus);
        values.add(getErrorMessage(row));
        return values;
    }
}
