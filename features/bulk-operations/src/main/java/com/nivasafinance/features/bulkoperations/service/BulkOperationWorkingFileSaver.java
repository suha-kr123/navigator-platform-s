package com.nivasafinance.features.bulkoperations.service;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nivasafinance.features.bulkoperations.common.utils.WorkingFileCsvUtils;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;

import lombok.RequiredArgsConstructor;

/**
 * Saves the working CSV file in a separate transaction (REQUIRES_NEW) so that
 * storage failures do not mark the validation transaction rollback-only.
 */
@Component
@RequiredArgsConstructor
public class BulkOperationWorkingFileSaver {

	private final BulkOperationFileStorageService fileStorageService;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String saveWorkingFileInNewTransaction(String csvContent, UUID operationId) {
		return fileStorageService.saveCsvContent(
				csvContent,
				operationId,
				WorkingFileCsvUtils.getWorkingFileName());
	}
}
