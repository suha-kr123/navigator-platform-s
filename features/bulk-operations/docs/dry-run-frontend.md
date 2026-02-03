# Dry-Run Frontend Implementation Guide

This guide explains how the bulk operation dry-run lifecycle works **from the client’s perspective** and describes the API calls, status transitions, and recommended UI states.

## Overview

Dry-run uploads let users validate and simulate a bulk operation before committing real changes. The backend performs validation and automatically runs the simulation. The user then inspects the preview report and chooses to execute the job once to apply the updates.

Key properties:

- No `isDryRun` flag or query parameter is exposed.
- Dry-run state is derived entirely from the `status` field returned by `GET /operations/{id}`.
- Simulation runs automatically after validation; the user only performs a single manual action to execute for real.

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/bulk-operations/dry-run/upload` | Upload CSV for dry-run simulation. Returns `operationId`. |
| `GET`  | `/api/v1/bulk-operations/operations/{id}` | Poll operation status and metrics. |
| `POST` | `/api/v1/bulk-operations/operations/{id}/execute-dry-run` | Execute for real **only after** the simulation is complete. |
| `GET`  | `/api/v1/bulk-operations/operations/{id}/summary` | Download the combined validation/simulation report (`text/csv`). |

## Status Lifecycle

Dry-run operations transition through these statuses:

| Status | Meaning | UI expectation |
|--------|---------|----------------|
| `UPLOADED_DRY_RUN` | CSV stored; validation pending. | Show “Validating…” loader. |
| `VALIDATION_IN_PROGRESS_DRY_RUN` | Validator running. | Same loader / progress. |
| `VALIDATED_DRY_RUN` | Validation succeeded; backend will immediately run simulation. | Transition to “Simulating…” state (typically very short). |
| `DRY_RUN_COMPLETED` | Simulation finished with success/failed counts populated and preview report stored. | Show details, enable one “Execute for real” button, provide report download (if `reportAvailable`). |
| `VALIDATED` | Returned right after the user confirms execution; backend begins real processing. | Switch to standard processing state (same as non-dry-run). |
| `PROCESSING_IN_PROGRESS` | Real processing running. | Show progress indicator. |
| Terminal statuses (`COMPLETED`, `PARTIALLY_COMPLETED`, `FAILED`) | Real execution finished. | Present final outcome and allow report download if available. |

> **Note:** Validation failures (`VALIDATION_FAILED`) can occur before or after the simulation. Treat them like normal uploads—surface `errorMessage` and report (if present).

## Suggested UI Flow

1. **Upload form**
   - POST multipart form to `/dry-run/upload` with `file` and `operationType`.
   - Store returned `operationId`.

2. **Polling loop**
   - Poll `GET /operations/{operationId}` every 2–3 seconds (or use exponential back-off).
   - Update UI based on `status`:
     ```tsx
     const isTerminal = ['COMPLETED', 'PARTIALLY_COMPLETED', 'FAILED', 'VALIDATION_FAILED'].includes(status);
     switch (status) {
       case 'UPLOADED_DRY_RUN':
       case 'VALIDATION_IN_PROGRESS_DRY_RUN':
         showLoader('Validating upload…');
         break;
       case 'VALIDATED_DRY_RUN':
         showLoader('Simulation starting…');
         break;
       case 'DRY_RUN_COMPLETED':
         showPreview(operation);
         enableExecuteButton();
         break;
       case 'VALIDATED':
       case 'PROCESSING_IN_PROGRESS':
         showLoader('Executing for real…');
         break;
       default:
         if (isTerminal) showFinalOutcome(operation);
     }
     ```

3. **Preview state (`DRY_RUN_COMPLETED`)**
   - Display counts (`statistics`), error messages, and enable report download if `reportAvailable`.
   - Offer a single “Execute for real” call-to-action that triggers `POST /operations/{id}/execute-dry-run`.

4. **Execution confirmation**
   - After the confirm endpoint returns success, resume polling.
   - Once the status reaches a terminal state, surface the final result and allow report download.

## Error Handling Tips

- **Validation failures**: Show `errorMessage` and provide the report (if available) so users can inspect row-level issues.
- **Network timeouts**: Retry polling; backend processing is idempotent.
- **Double submission**: Disable the “Execute for real” button while the confirm API call is in progress to avoid duplicates.
- **Report download**: Guard on `reportAvailable === true`; otherwise show a message like “Report is still being generated…”.

## Pseudocode (React-style)

```tsx
async function runDryRunUpload(file: File, operationType: string) {
  const operationId = await api.uploadDryRun(file, operationType); // POST /dry-run/upload

  let status: string | undefined;
  do {
    await delay(2500);
    const operation = await api.getOperation(operationId);
    status = operation.status;
    updateUi(operation);
  } while (!['DRY_RUN_COMPLETED','COMPLETED','PARTIALLY_COMPLETED','FAILED','VALIDATION_FAILED'].includes(status));

  return { operationId, status };
}

async function executeForReal(operationId: string) {
  await api.executeDryRun(operationId); // POST /operations/{id}/execute-dry-run
  // Resume polling until terminal state
}
```

## Checklist for Frontend

- [ ] Add upload action that targets `/dry-run/upload`.
- [ ] Implement status-aware polling with state-specific UI.
- [ ] Display simulation results (`statistics`, `reportAvailable`, `errorMessage`) when `DRY_RUN_COMPLETED`.
- [ ] Provide a single confirm button that triggers the execute endpoint.
- [ ] Resume polling until a terminal status is reached after execution.
- [ ] Handle errors gracefully (validation failures, processing errors, network issues).

Following this workflow keeps the user experience intuitive: upload → automatic simulation → inspect preview → single confirmation to execute for real.
