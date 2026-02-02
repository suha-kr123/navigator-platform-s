package com.nivasafinance.features.bulkoperations.operations.onhold;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.engine.AbstractBulkOperationReportLayout;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Report layout for ONHOLD bulk operations.
 */
@Component
public class OnholdReportLayout extends AbstractBulkOperationReportLayout {

	private static final List<String> HEADERS = List.of(
			"Row_Number",
			"Lead_Identifier",
			"Status",
			"Reason_Code",
			"Follow_Up_Date",
			"Error_Message"
	);

	@Override
	public BulkOperationType getType() {
		return BulkOperationType.ONHOLD;
	}

	@Override
	public String getRowReferenceColumnName() {
		return OnholdRowKeys.LEAD_IDENTIFIER;
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
		values.add(getValue(data, OnholdRowKeys.LEAD_IDENTIFIER));
		values.add(rowStatus);
		values.add(getValue(data, OnholdRowKeys.REASON_CODE));
		values.add(getValue(data, OnholdRowKeys.FOLLOW_UP_DATE));
		values.add(getErrorMessage(row));
		return values;
	}
}
