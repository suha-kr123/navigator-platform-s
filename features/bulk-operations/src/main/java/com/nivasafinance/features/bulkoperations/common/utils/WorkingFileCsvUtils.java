package com.nivasafinance.features.bulkoperations.common.utils;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities to serialize validated rows to a working CSV file and parse them back.
 * Working file format: header row (rowNumber + required + optional columns), then one row per valid row.
 */
@Slf4j
public final class WorkingFileCsvUtils {

    private static final String WORKING_FILE_NAME = "working_rows.csv";
    private static final String ERROR_GENERATING_WORKING_CSV = "Error generating working file CSV";
    private static final String ERROR_PARSING_WORKING_CSV = "Error parsing working file CSV";

    private WorkingFileCsvUtils() {
    }

    /**
     * Builds the ordered header list for the working file: rowNumber, then required columns, then optional columns.
     */
    public static List<String> getWorkingFileHeaders(BulkOperationType type) {
        List<String> headers = new ArrayList<>();
        headers.add(RowDataKeys.ROW_NUMBER);
        if (type.getRequiredColumns() != null) {
            headers.addAll(type.getRequiredColumns());
        }
        if (type.getOptionalColumns() != null) {
            headers.addAll(type.getOptionalColumns());
        }
        return headers;
    }

    /**
     * Generates CSV content for the working file from validated rows.
     */
    public static String generateWorkingFileCsv(BulkOperationType type, List<Map<String, Object>> validRows) {
        if (ValidationUtils.isNullOrEmpty(validRows)) {
            return "";
        }
        List<String> headers = getWorkingFileHeaders(type);
        try (StringWriter writer = new StringWriter();
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord(headers);
            for (Map<String, Object> row : validRows) {
                List<Object> values = new ArrayList<>();
                for (String header : headers) {
                    Object v = row.get(header);
                    values.add(v != null ? v.toString() : "");
                }
                printer.printRecord(values);
            }
            printer.flush();
            return writer.toString();
        } catch (IOException e) {
            log.error(ERROR_GENERATING_WORKING_CSV, e);
            throw new IllegalStateException(ERROR_GENERATING_WORKING_CSV, e);
        }
    }

    /**
     * Parses the working file CSV from the given input stream into a list of row maps.
     * The first record is treated as the header; row numbers are preserved from the file or set by index.
     */
    public static List<Map<String, Object>> parseWorkingFileCsv(InputStream inputStream) {
        try (CSVParser parser = CSVParser.parse(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setTrim(true)
                        .setIgnoreEmptyLines(true)
                        .get())) {
            List<String> headerNames = parser.getHeaderNames();
            if (ValidationUtils.isNullOrEmpty(headerNames)) {
                return List.of();
            }
            List<Map<String, Object>> rows = new ArrayList<>();
            int rowIndex = 0;
            for (var record : parser) {
                rowIndex++;
                Map<String, Object> row = new LinkedHashMap<>();
                for (String header : headerNames) {
                    String value = record.get(header);
                    if (RowDataKeys.ROW_NUMBER.equals(header)) {
                        if (value != null && !value.isBlank()) {
                            try {
                                row.put(header, Integer.parseInt(value.trim()));
                            } catch (NumberFormatException e) {
                                row.put(header, rowIndex + 1);
                            }
                        } else {
                            row.put(header, rowIndex + 1);
                        }
                    } else {
                        row.put(header, (value != null && !value.isBlank()) ? value.trim() : null);
                    }
                }
                if (!row.containsKey(RowDataKeys.ROW_NUMBER)) {
                    row.put(RowDataKeys.ROW_NUMBER, rowIndex + 1);
                }
                rows.add(row);
            }
            return rows;
        } catch (IOException e) {
            log.error(ERROR_PARSING_WORKING_CSV, e);
            throw new IllegalStateException(ERROR_PARSING_WORKING_CSV, e);
        }
    }

    public static String getWorkingFileName() {
        return WORKING_FILE_NAME;
    }
}
