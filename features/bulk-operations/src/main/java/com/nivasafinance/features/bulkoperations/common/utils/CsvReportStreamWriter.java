package com.nivasafinance.features.bulkoperations.common.utils;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationCsvValidationException;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationReportLayout;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Streams CSV report rows one-by-one to avoid building a full list in memory.
 * Write header first, then validation errors, success rows, and failed rows in order.
 */
@Slf4j
public final class CsvReportStreamWriter {

    private static final String REPORT_GENERATION_FAILED_MESSAGE = "Failed to generate CSV report";
    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";

    private final BulkOperationReportLayout layout;
    private final StringWriter writer;
    private final CSVPrinter csvPrinter;
    private boolean headerWritten;
    private int rowCount;

    public CsvReportStreamWriter(BulkOperationReportLayout layout) {
        this.layout = layout;
        this.writer = new StringWriter();
        try {
            this.csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
        } catch (IOException e) {
            log.error("Failed to create CSV printer", e);
            throw new BulkOperationCsvValidationException(REPORT_GENERATION_FAILED_MESSAGE, e);
        }
        this.headerWritten = false;
        this.rowCount = 0;
    }

    /**
     * Writes the CSV header. Must be called once before writing any rows.
     */
    public void writeHeader() {
        if (headerWritten) {
            return;
        }
        List<String> headers = layout.getReportHeaders();
        if (headers == null || headers.isEmpty()) {
            throw new BulkOperationCsvValidationException(REPORT_GENERATION_FAILED_MESSAGE);
        }
        try {
            csvPrinter.printRecord(headers);
            csvPrinter.flush();
            headerWritten = true;
        } catch (IOException e) {
            log.error("Failed to write CSV header", e);
            throw new BulkOperationCsvValidationException(REPORT_GENERATION_FAILED_MESSAGE, e);
        }
    }

    /**
     * Writes one validation error as a report row (status VALIDATION_FAILED).
     */
    public void writeValidationError(CsvValidationError error) {
        ensureHeaderWritten();
        CsvReportRow row = layout.toCsvReportRowForValidationError(error);
        writeRow(row, VALIDATION_FAILED);
    }

    /**
     * Writes one report row with the given status (SUCCESS or FAILED).
     */
    public void writeReportRow(CsvReportRow row, String status) {
        ensureHeaderWritten();
        writeRow(row, status);
    }

    private void writeRow(CsvReportRow row, String status) {
        try {
            List<String> values = layout.buildReportRow(row, status);
            csvPrinter.printRecord(values);
            rowCount++;
        } catch (IOException e) {
            log.error("Failed to write report row", e);
            throw new BulkOperationCsvValidationException(REPORT_GENERATION_FAILED_MESSAGE, e);
        }
    }

    private void ensureHeaderWritten() {
        if (!headerWritten) {
            writeHeader();
        }
    }

    /**
     * Flushes the underlying writer.
     */
    public void flush() {
        try {
            csvPrinter.flush();
        } catch (IOException e) {
            log.warn("Failed to flush CSV printer", e);
        }
    }

    /**
     * Returns the CSV content as a string. Call after writing all rows.
     */
    public String getContent() {
        flush();
        return writer.toString();
    }

    /**
     * Returns the number of data rows written (excluding header).
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Closes the printer. Call when done writing.
     */
    public void close() {
        try {
            csvPrinter.close();
        } catch (IOException e) {
            log.warn("Failed to close CSV printer", e);
        }
    }
}
