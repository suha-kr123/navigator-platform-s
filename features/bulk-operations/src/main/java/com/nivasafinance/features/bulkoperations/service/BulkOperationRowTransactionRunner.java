package com.nivasafinance.features.bulkoperations.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Runs row-level work in a separate transaction (REQUIRES_NEW) so a single row failure
 * or commit-phase issue does not roll back the bulk orchestration transaction.
 */
@Component
public class BulkOperationRowTransactionRunner {

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <T> T runInNewTransaction(Supplier<T> supplier) {
		return supplier.get();
	}
}
