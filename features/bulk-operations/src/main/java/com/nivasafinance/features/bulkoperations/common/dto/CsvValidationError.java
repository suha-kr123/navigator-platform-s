package com.nivasafinance.features.bulkoperations.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvValidationError {
    private static final String KEY_ROW_NUMBER = "rowNumber";
    private static final String KEY_COLUMN_NAME = "columnName";
    private static final String KEY_ERROR_CODE = "errorCode";
    private static final String KEY_ERROR_MESSAGE = "errorMessage";
    private static final String KEY_ROW_DATA = "rowData";
    private static final String KEY_ROW_REFERENCE = "rowReference";
    private static final String KEY_ROW_DATA_MAP = "rowDataMap";

    private Integer rowNumber;
    private String columnName;
    private String errorCode;
    private String errorMessage;
    private String rowData;
    private String rowReference;
    /** Per-column values for report (e.g. lead_identifier, reason_code) so each invalid row shows full CSV data. */
    private Map<String, String> rowDataMap;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_ROW_NUMBER, rowNumber != null ? rowNumber : 0);
        map.put(KEY_COLUMN_NAME, columnName != null ? columnName : "");
        map.put(KEY_ERROR_CODE, errorCode != null ? errorCode : "");
        map.put(KEY_ERROR_MESSAGE, errorMessage != null ? errorMessage : "");
        map.put(KEY_ROW_DATA, rowData != null ? rowData : "");
        map.put(KEY_ROW_REFERENCE, rowReference != null ? rowReference : "");
        map.put(KEY_ROW_DATA_MAP, rowDataMap != null ? new HashMap<>(rowDataMap) : Collections.emptyMap());
        return map;
    }

    public static CsvValidationError fromMap(Map<String, Object> map) {
        if (map == null) return null;
        Object rn = map.get(KEY_ROW_NUMBER);
        return CsvValidationError.builder()
                .rowNumber(rn instanceof Number n ? n.intValue() : null)
                .columnName(getString(map, KEY_COLUMN_NAME))
                .errorCode(getString(map, KEY_ERROR_CODE))
                .errorMessage(getString(map, KEY_ERROR_MESSAGE))
                .rowData(getString(map, KEY_ROW_DATA))
                .rowReference(getString(map, KEY_ROW_REFERENCE))
                .rowDataMap(getRowDataMapFromMap(map))
                .build();
    }

    private static Map<String, String> getRowDataMapFromMap(Map<String, Object> map) {
        Object val = map.get(KEY_ROW_DATA_MAP);
        if (val == null || !(val instanceof Map)) return null;
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<?, ?> e : ((Map<?, ?>) val).entrySet()) {
            if (e.getKey() != null) result.put(e.getKey().toString(), e.getValue() != null ? e.getValue().toString() : "");
        }
        return result;
    }

    public static List<CsvValidationError> fromMapList(List<?> list) {
        if (list == null) return List.of();
        List<CsvValidationError> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                CsvValidationError err = fromMap((Map<String, Object>) item);
                if (err != null) result.add(err);
            }
        }
        return result;
    }

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : null;
    }
}
