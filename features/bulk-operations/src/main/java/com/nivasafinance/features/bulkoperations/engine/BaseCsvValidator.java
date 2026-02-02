package com.nivasafinance.features.bulkoperations.engine;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationResult;
import com.nivasafinance.features.bulkoperations.common.dto.ValidationOutcome;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseCsvValidator {

	private static final String FILE_NAME = "fileName";
	private static final String FILE_SIZE = "fileSize";
	private static final String ENCODING = "encoding";
	private static final String HEADERS = "headers";
	private static final String FILE_EXTENSION = ".csv";

	protected final BulkOperationCsvProperties bulkOperationCsvProperties;
	protected final BulkOperationExceptionFactory bulkOperationExceptionFactory;

	public CsvValidationResult validate(MultipartFile file, List<String> requiredColumns) {
		try {
			validateFile(file);

			CSVParser parser = parseCsv(file);
			Map<String, Integer> headerMap = parser.getHeaderMap();

			validateHeader(headerMap, requiredColumns);

			List<Map<String, Object>> parsedRows = parseRows(parser);

			if (parsedRows.size() > bulkOperationCsvProperties.getMaxRows()) {
				throw bulkOperationExceptionFactory.bulkOperationCsvValidationRowCountExceededException(bulkOperationCsvProperties.getMaxRows());
			}

			ValidationOutcome outcome = validateBusinessRules(parsedRows);

			List<String> headerNames = headerMap.keySet().stream().sorted().toList();
			return CsvValidationResult.success(
					outcome.validRows(),
					outcome.errors(),
					parsedRows.size(),
					outcome.validRows().size(),
					parsedRows.size() - outcome.validRows().size(),
					Map.of(
							FILE_NAME, Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"),
							FILE_SIZE, file.getSize(),
							ENCODING, StandardCharsets.UTF_8.name(),
							HEADERS, headerNames));

		} catch (Exception e) {
			log.error("CSV validation failed for file {}",
					Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"), e);
			throw bulkOperationExceptionFactory.bulkOperationCsvValidationException(e.getMessage());
		}
	}

	private CSVParser parseCsv(MultipartFile file) {
		try {
			return CSVParser.parse(
					new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8),
					CSVFormat.DEFAULT.builder()
							.setHeader()
							.setTrim(true)
							.setIgnoreEmptyLines(true)
							.get());
		} catch (Exception e) {
			log.error("Failed to parse CSV file {}", Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"),
					e);
			throw bulkOperationExceptionFactory.bulkOperationCsvValidationFileParseFailedException();
		}
	}

	private void validateFile(MultipartFile file) {
		if (!ValidationUtils.isNonNull(file) || file.isEmpty()) {
			throw bulkOperationExceptionFactory.bulkOperationCsvValidationFileEmptyException();
		}

		if (file.getSize() > bulkOperationCsvProperties.getMaxFileSize()) {
			throw bulkOperationExceptionFactory.bulkOperationCsvValidationFileSizeExceededException();
		}

		String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
		if (!filename.toLowerCase().endsWith(FILE_EXTENSION)) {
			throw bulkOperationExceptionFactory.bulkOperationCsvValidationFileExtensionException();
		}
	}

	private void validateHeader(Map<String, Integer> headerMap, List<String> requiredColumns) {
		if (!ValidationUtils.isNonNull(headerMap) || headerMap.isEmpty()) {
			throw bulkOperationExceptionFactory.bulkOperationCsvValidationHeaderRequiredException();
		}

		List<String> missingColumns = requiredColumns.stream()
				.filter(col -> !headerMap.containsKey(col))
				.collect(Collectors.toList());

		if (!missingColumns.isEmpty()) {
			throw bulkOperationExceptionFactory.bulkOperationCsvValidationMissingColumnsException(missingColumns);
		}
	}

	protected abstract List<Map<String, Object>> parseRows(CSVParser parser);

	/**
	 * Validates rows and returns valid ones plus per-row errors.
	 * Invalid rows should be added to errors with row number and message.
	 */
	protected abstract ValidationOutcome validateBusinessRules(List<Map<String, Object>> parsedRows);

}
