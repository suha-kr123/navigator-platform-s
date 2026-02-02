# Bulk Operations

Asynchronous CSV-based bulk operations for leads and other entities. Users upload a CSV file; the system validates it, processes rows, and produces a unified report.

## Architecture

- **Upload** → File saved, operation created, message published to validation queue
- **Validation** → CSV parsed and validated; validation outcomes persisted on BulkOperation; row-level results written to the working CSV (valid rows are not stored in DB—they are either rewritten into the working CSV or kept transiently during validation)
- **Processing** → Each valid row processed by a type-specific processor; report generated
- **Dry-run** → Same flow, but processors skip domain calls and return simulated success

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

For invalid rows, add `CsvValidationError` with `rowNumber`, `columnName`, `errorCode`, `errorMessage`, `rowReference`, `rowData`. Validators are auto-registered by `BulkOperationCsvValidatorRegistry` via `getType()`.

**Example (parsing):**
```java
row.put(RowDataKeys.ROW_NUMBER, rowNum);
row.put(OnholdRowKeys.LEAD_IDENTIFIER, getString(record, OnholdRowKeys.LEAD_IDENTIFIER));
row.put(OnholdRowKeys.REASON_CODE, getString(record, OnholdRowKeys.REASON_CODE));
```

**Example (validation error):**
```java
return CsvValidationError.builder()
    .rowNumber(rowNum)
    .columnName(OnholdRowKeys.REASON_CODE)
    .errorCode("INVALID_REASON")
    .errorMessage("reason_code '" + reasonCode + "' is not valid")
    .rowReference(leadId)
    .rowData(formatRowData(row))
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
if (Boolean.TRUE.equals(bulkOperation.getIsDryRun())) {
    return ProcessingResult.success(original, Map.of("status", "ONHOLD (dry-run)"));
}
try {
    leadWriteService.onholdLead(leadId, request);
    return ProcessingResult.success(original, Map.of("status", "ONHOLD"));
} catch (Exception e) {
    return ProcessingResult.failed(e.getMessage(), original);
}
```

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

## Reference Implementation

See the **DROPOFF** operation in `operations/dropoff/`:

- `DropoffRowKeys.java`
- `DropoffCsvValidator.java`
- `DropoffProcessor.java`
- `DropoffReportLayout.java`

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

## Design notes

- **Upload limits**: Each operation type returned by `GET /operation-types` includes `uploadLimits` (max rows, max file size in bytes). Clients should validate file size and row count before upload to avoid validation errors.
- **Report availability**: Operation status responses include `reportAvailable` (true when the summary CSV can be downloaded). It is false if report generation failed after commit (see `errorMessage`) or the report is not yet generated.
