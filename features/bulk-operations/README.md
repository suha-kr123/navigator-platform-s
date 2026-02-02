# Bulk Operations

Asynchronous CSV-based bulk operations for leads and other entities. Users upload a CSV file; the system validates it, processes rows, and produces a unified report.

## Architecture

- **Upload** → File saved, operation created, message published to validation queue
- **Validation** → CSV parsed and validated; validation outcomes persisted on BulkOperation in separate `REQUIRES_NEW` transactions (so commit-phase failures do not mark the validation transaction rollback-only); working file and report are externalized after commit
- **Processing** → Each valid row processed by a type-specific processor; report generated after commit via `TransactionSynchronization.afterCommit()`
- **Dry-run** → Same flow, but processors skip domain calls and return simulated success

**Safe streaming model:** (1) Stream & process rows inside a transaction; (2) accumulate results transaction-locally; (3) externalize results (file storage, report) only after commit. Report generation uses `CsvReportStreamWriter` to write rows one-by-one (validation errors, then success, then failed) without building a full list in memory.

## Implementing a New Operation Type

To add a new bulk operation (e.g. `ONHOLD`, `REJECT`), create these five pieces:

### 1. Add to BulkOperationType enum

**File:** `engine/BulkOperationType.java`

Header validation for required/optional columns is handled by the base CSV validator. Operation-specific validators must not re-check header presence unless needed.

```java
ONHOLD(
    "ONHOLD",
    "On Hold Lead",
    "Bulk put leads on hold with reason codes",
    Arrays.asList("lead_identifier", "reason_code"),
    Arrays.asList("follow_up_date"));
```

- `identifier` – Used in API and DB
- `displayName` / `description` – Shown in operation types API
- `requiredColumns` – Must exist in CSV header
- `optionalColumns` – Optional columns (for validation/processing, not enforced at upload)

### 2. Row keys (optional but recommended)

**File:** `operations/<type>/<Type>RowKeys.java`

```java
public final class OnholdRowKeys {
    public static final String LEAD_IDENTIFIER = "lead_identifier";
    public static final String REASON_CODE = "reason_code";
    public static final String FOLLOW_UP_DATE = "follow_up_date";
    private OnholdRowKeys() {}
}
```

Use these constants in validator, processor, and report layout to avoid duplication and typos. Consider placing shared keys (e.g. `RowDataKeys.ROW_NUMBER`) in a core/common package rather than under `operations/`.

### 3. CSV Validator

**File:** `operations/<type>/<Type>CsvValidator.java`

Validators are pure: they parse and validate only. Persistence is handled by the validation service (`BulkValidationService`).

- Extend `AbstractBulkOperationCsvValidator`
- Annotate with `@Component`
- Implement:
  - `getType()` → return your `BulkOperationType`
  - `parseRows(CSVParser parser)` → read each `CSVRecord`, put values in a `Map<String, Object>`, set `RowDataKeys.ROW_NUMBER` (header = 1, first data row = 2)
  - `validateBusinessRules(List<Map<String, Object>> parsedRows)` → return `ValidationOutcome.of(validRows, errors)` (in-memory only; no persistence)

For invalid rows, add `CsvValidationError` with `rowNumber`, `columnName`, `errorCode`, `errorMessage`, `rowReference`, `rowData`, and `rowDataMap`. Set `rowDataMap` (e.g. via `rowToDataMap(row)`) so each invalid row shows full CSV column data in the summary report. Validators are auto-registered by `BulkOperationCsvValidatorRegistry` via `getType()`.

**Example (parsing):**
```java
row.put(RowDataKeys.ROW_NUMBER, rowNum);
row.put(OnholdRowKeys.LEAD_IDENTIFIER, getString(record, OnholdRowKeys.LEAD_IDENTIFIER));
row.put(OnholdRowKeys.REASON_CODE, getString(record, OnholdRowKeys.REASON_CODE));
```

**Example (validation error):**
```java
Map<String, String> rowDataMap = rowToDataMap(row);  // e.g. lead_identifier, reason_code
return CsvValidationError.builder()
    .rowNumber(rowNum)
    .columnName(OnholdRowKeys.REASON_CODE)
    .errorCode("INVALID_REASON")
    .errorMessage("reason_code '" + reasonCode + "' is not valid")
    .rowReference(leadId)
    .rowData(formatRowData(row))
    .rowDataMap(rowDataMap)
    .build();
```

### 4. Processor

**File:** `operations/<type>/<Type>Processor.java`

Processors and the domain services they call (e.g. `leadWriteService.dropoffLead`) must be **idempotent**. A row may be retried if processing fails or the operation is retried (async + retry flows). The engine does not add a row-level idempotency key; duplicate processing of the same row must be safe in the domain layer.

- Extend `BaseBulkOperationProcessor`
- Annotate with `@Component`
- Implement:
  - `getType()` → return your `BulkOperationType`
  - `processRow(BulkOperation bulkOperation, Map<String, Object> row)` → call domain service, return `ProcessingResult.success(...)` or `ProcessingResult.failed(...)`
  - `toRowDataMap(Map<String, Object> row)` → map row to `Map<String, String>` for report
  - `getRowReference(Map<String, Object> row)` → entity id string for the row

**Dry-run:** Check `bulkOperation.getIsDryRun()` and skip domain calls; return simulated success instead.

**Example:**
```java
import com.nivasafinance.common.exception.ExceptionUtils;

if (Boolean.TRUE.equals(bulkOperation.getIsDryRun())) {
    return ProcessingResult.success(original, Map.of("status", "ONHOLD (dry-run)"));
}
try {
    leadWriteService.onholdLead(leadId, request);
    return ProcessingResult.success(original, Map.of("status", "ONHOLD"));
} catch (Exception e) {
    return ProcessingResult.failed(ExceptionUtils.getRootCauseMessage(e), original);
}
```
Use `ExceptionUtils.getRootCauseMessage(e)` for failed results so users see the real error (e.g. "Lead is already on dropoff") instead of generic transaction messages.

### 5. Report Layout

**File:** `operations/<type>/<Type>ReportLayout.java`

- Extend `AbstractBulkOperationReportLayout`
- Annotate with `@Component`
- Implement:
  - `getType()` → return your `BulkOperationType`
  - `getRowReferenceColumnName()` → column that receives the row reference for validation error rows (e.g. `"lead_identifier"`); keeps the engine generic (no entity-specific identity)
  - `getReportHeaders()` → list of report column names
  - `buildReportRow(CsvReportRow row, String rowStatus)` → build row values in same order as headers

**Example:**
```java
private static final List<String> HEADERS = List.of(
    "Row_Number", "Lead_Identifier", "Status", "Reason_Code", "Follow_Up_Date", "Error_Message");

@Override
public List<String> buildReportRow(CsvReportRow row, String rowStatus) {
    Map<String, String> data = rowData(row);
    return List.of(
        String.valueOf(row.getRowNumber()),
        getValue(data, OnholdRowKeys.LEAD_IDENTIFIER),
        rowStatus,
        getValue(data, OnholdRowKeys.REASON_CODE),
        getValue(data, OnholdRowKeys.FOLLOW_UP_DATE),
        getErrorMessage(row));
}
```

## Checklist for a New Operation

| Step | Action |
|------|--------|
| 1 | Add entry to `BulkOperationType` enum |
| 2 | Create `operations/<type>/` package and `*RowKeys` class |
| 3 | Create `*CsvValidator` extending `AbstractBulkOperationCsvValidator` |
| 4 | Create `*Processor` extending `BaseBulkOperationProcessor` |
| 5 | Create `*ReportLayout` extending `AbstractBulkOperationReportLayout` |
| 6 | Add tests for validator + processor |

No extra wiring needed: validators, processors, and report layouts are discovered via `@Component` and registered by type.

## Reference Implementations

- **DROPOFF** (`operations/dropoff/`): lead_identifier, reason_code.
- **ONHOLD** (`operations/onhold/`): lead_identifier, reason_code, follow_up_date (on-hold reason and follow-up date; CSV date format yyyy-MM-dd).
- **REJECTED** (`operations/rejected/`): lead_identifier, reason_code (reject reason from LEAD_REJECT_REASON_MASTER).

## Module Structure

```
bulk-operations/
├── common/           # DTOs, exceptions, config, utilities
├── controller/       # REST API
├── engine/           # Interfaces, base classes, registries
├── listener/         # Validation & processing queue listeners
├── operations/       # Type-specific implementations (dropoff, onhold, etc.)
├── repository/
├── scheduler/        # Cleanup, timeout
├── service/
└── storage/          # File storage
```

If non-lead operations grow, consider splitting by domain (e.g. `operations-lead/`, `operations-campaign/`). Optional and not required for current scope.

## API Endpoints

| Method | Path | Permission |
|--------|------|------------|
| GET | `/api/v1/bulk-operations/operation-types` | VIEW_BULK_OPERATION_TYPES |
| POST | `/api/v1/bulk-operations/upload` | BULK_UPDATE_LEAD |
| GET | `/api/v1/bulk-operations/operations/{id}` | VIEW_BULK_OPERATION_STATUS |
| POST | `/api/v1/bulk-operations/operations/{id}/cancel` | CANCEL_BULK_OPERATION |
| POST | `/api/v1/bulk-operations/operations/{id}/execute-dry-run` | PREVIEW_BULK_OPERATION |
| GET | `/api/v1/bulk-operations/operations` | VIEW_BULK_OPERATION_STATUS |
| GET | `/api/v1/bulk-operations/operations/{id}/summary` | DOWNLOAD_BULK_OPERATION_REPORTS |

## Design trade-offs

| Decision | Benefit | Trade-off |
|----------|---------|-----------|
| **Async validation + processing via queues** | Upload returns immediately; validation and processing run in background. Scales with queue workers. | UI must poll by `operationId`; no long-lived connection. Eventual consistency: status/report appear after workers run. |
| **Dual transport (SQS vs DB polling)** | Works with or without AWS; local/dev uses DB polling for `UPLOADED` and `VALIDATION_IN_PROGRESS`. | Two code paths to maintain; local polling delay and single-consumer behaviour when not using SQS. |
| **Validation errors in object storage (not DB metadata)** | Avoids large JSON in DB; no metadata bloat; file deleted after report generation. | Extra storage read when building report; transient file lifecycle (create → use → delete). |
| **Report generated after commit (`TransactionSynchronization.afterCommit`)** | DB state (status, counts) is committed first; report build failures do not roll back processing. | Report appears slightly after status; if report build fails, status is final but `reportAvailable` stays false and `errorMessage` is set. |
| **Persistence in separate `REQUIRES_NEW` transactions** | Commit-phase failures (e.g. Javers, auditing) in a small transaction do not mark the main validation/processing transaction rollback-only. | More DB round-trips; possible brief inconsistency if persistence succeeds but main transaction fails (mitigated by reloading entity in each persistence call). |
| **Working file (valid rows only)** | Processing reads only valid rows; no re-parse of original CSV. | Two files per operation until cleanup: original upload + working CSV; storage and cleanup complexity. |
| **Unified report (validation + success + failed)** | Single download for the user; one CSV with all outcomes. | Report build needs validation errors (from storage) + in-memory success/failed lists; processing keeps success/failed rows in memory for the run (bounded by batch size and row count limit). |
| **Streaming report write (`CsvReportStreamWriter`)** | Report rows written one-by-one; no full report list in memory. | Validation errors and success/failed rows are still held in memory during processing; only the final CSV write is streamed. |
| **Row processing in per-row `REQUIRES_NEW`** | One row failure does not roll back others; clear per-row success/failure. | Higher transaction overhead; domain must tolerate retries and be idempotent. |
| **No storage key in API response** | Internal storage keys not exposed; download by `operationId` only. | UI cannot construct a direct storage URL; must use `GET .../operations/{id}/summary` and rely on `reportAvailable`. |
| **Timeout + cleanup schedulers** | Stuck operations are auto-cancelled; old files are removed after retention. | Fixed schedule (e.g. 5 min timeout check, daily cleanup); not in a dedicated job framework yet (noted in TODOs). |
| **Idempotent processors** | Safe retries and replay when messages are redelivered or DB polling reprocesses. | Domain layer must implement idempotency; no engine-level dedup key. |

## Design notes

- **Upload limits**: Each operation type returned by `GET /operation-types` includes `uploadLimits` (max rows, max file size in bytes). Clients should validate file size and row count before upload to avoid validation errors.
- **Report availability**: Operation status responses include `reportAvailable` (true when the summary CSV can be downloaded). It is false if report generation failed after commit (see `errorMessage`) or the report is not yet generated.
- **Validation errors in report**: Validation errors are saved to object storage (JSON file) at validation time; the storage key is stored on `BulkOperation.validationErrorsStorageKey`. When the unified report is built after processing, errors are read from storage and included in the report. The file is deleted and the key cleared after report generation (nothing is stored in metadata).
- **Storage keys and cleanup**: There are four storage keys per operation. All are deleted by the cleanup job for terminal operations older than `bulk.operations.temp-file-retention-days` (default 30). `validation_errors_storage_key` is also deleted immediately after the unified report is built (so it is short-lived); the cleanup job still clears it if present (e.g. if report build failed before delete).
- **Local polling**: When not using SQS, the validation listener polls operations with status `UPLOADED` or `VALIDATION_IN_PROGRESS` so stuck operations are retried.
- **Publish to processing**: An operation is published to the processing queue only when status is `VALIDATED`, not dry run, has valid rows, and has a working file key.

### Storage keys (per operation)

| Key | Purpose | When deleted |
|-----|---------|--------------|
| **file_storage_key** | Original uploaded CSV | Cleanup job (terminal + older than retention) |
| **working_file_storage_key** | Valid rows only (used for processing) | Cleanup job |
| **validation_errors_storage_key** | Validation errors JSON (used to build report) | Deleted right after report is built; cleanup job clears key/file if still present |
| **summary_storage_key** | Unified report CSV (user download) | Cleanup job |

So: **yes**, all four keys correspond to files that are deleted. The validation-errors file is removed as soon as the report is generated; the other three (upload, working file, report) are removed by the **file cleanup job** for operations in a terminal status that are older than the configured retention (e.g. 30 days).
