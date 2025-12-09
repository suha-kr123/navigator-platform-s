# Frontend Implementation Guide: Bulk Sales Owner Assignment

This guide covers the implementation of bulk sales owner assignment APIs for both **Leads** and **Advisors**.

## API Endpoints

### 1. Bulk Assign Sales Owner to Leads
**Endpoint:** `POST /v1/leads/bulk-assign-sales-owner`

### 2. Bulk Assign Sales Owner to Advisors
**Endpoint:** `POST /v1/advisors/bulk-assign-sales-owner`

---

## Request/Response Structure

### Request Body
Both APIs use the same request structure:

```typescript
interface BulkSalesOwnerAssignmentRequest {
  leadIdentifiers: string[];      // For leads API
  advisorIdentifiers: string[];  // For advisors API
  salesOwner: string;              // Username of the sales owner
}
```

### Response Body
Both APIs return the same response structure:

```typescript
interface BulkSalesOwnerAssignmentResponse {
  totalRequested: number;
  successful: number;
  failed: number;
  successfulLeadIdentifiers?: string[];    // For leads API
  successfulAdvisorIdentifiers?: string[]; // For advisors API
  errors: AssignmentError[];
}

interface AssignmentError {
  leadIdentifier?: string;      // For leads API
  advisorIdentifier?: string;    // For advisors API
  errorMessage: string;
}
```

---

## Frontend Implementation

### Step 1: Create TypeScript Interfaces

Create a file `types/bulkAssignment.ts`:

```typescript
// For Leads
export interface BulkLeadSalesOwnerRequest {
  leadIdentifiers: string[];
  salesOwner: string;
}

export interface BulkLeadSalesOwnerResponse {
  totalRequested: number;
  successful: number;
  failed: number;
  successfulLeadIdentifiers: string[];
  errors: LeadAssignmentError[];
}

export interface LeadAssignmentError {
  leadIdentifier: string;
  errorMessage: string;
}

// For Advisors
export interface BulkAdvisorSalesOwnerRequest {
  advisorIdentifiers: string[];
  salesOwner: string;
}

export interface BulkAdvisorSalesOwnerResponse {
  totalRequested: number;
  successful: number;
  failed: number;
  successfulAdvisorIdentifiers: string[];
  errors: AdvisorAssignmentError[];
}

export interface AdvisorAssignmentError {
  advisorIdentifier: string;
  errorMessage: string;
}
```

### Step 2: Create API Service Functions

Create a file `services/bulkAssignmentService.ts`:

```typescript
import axios from 'axios';
import { 
  BulkLeadSalesOwnerRequest, 
  BulkLeadSalesOwnerResponse,
  BulkAdvisorSalesOwnerRequest,
  BulkAdvisorSalesOwnerResponse
} from '../types/bulkAssignment';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || '/api';

/**
 * Bulk assign sales owner to multiple leads
 */
export const bulkAssignSalesOwnerToLeads = async (
  request: BulkLeadSalesOwnerRequest
): Promise<BulkLeadSalesOwnerResponse> => {
  const response = await axios.post<BulkLeadSalesOwnerResponse>(
    `${API_BASE_URL}/v1/leads/bulk-assign-sales-owner`,
    request
  );
  return response.data;
};

/**
 * Bulk assign sales owner to multiple advisors
 */
export const bulkAssignSalesOwnerToAdvisors = async (
  request: BulkAdvisorSalesOwnerRequest
): Promise<BulkAdvisorSalesOwnerResponse> => {
  const response = await axios.post<BulkAdvisorSalesOwnerResponse>(
    `${API_BASE_URL}/v1/advisors/bulk-assign-sales-owner`,
    request
  );
  return response.data;
};
```

### Step 3: Create React Hook (Optional but Recommended)

Create a file `hooks/useBulkSalesOwnerAssignment.ts`:

```typescript
import { useState } from 'react';
import { 
  bulkAssignSalesOwnerToLeads,
  bulkAssignSalesOwnerToAdvisors 
} from '../services/bulkAssignmentService';
import {
  BulkLeadSalesOwnerRequest,
  BulkAdvisorSalesOwnerRequest
} from '../types/bulkAssignment';

interface UseBulkAssignmentResult {
  assignToLeads: (request: BulkLeadSalesOwnerRequest) => Promise<void>;
  assignToAdvisors: (request: BulkAdvisorSalesOwnerRequest) => Promise<void>;
  loading: boolean;
  error: string | null;
  success: boolean;
  result: {
    totalRequested: number;
    successful: number;
    failed: number;
  } | null;
}

export const useBulkSalesOwnerAssignment = (): UseBulkAssignmentResult => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [result, setResult] = useState<{
    totalRequested: number;
    successful: number;
    failed: number;
  } | null>(null);

  const assignToLeads = async (request: BulkLeadSalesOwnerRequest) => {
    setLoading(true);
    setError(null);
    setSuccess(false);
    
    try {
      const response = await bulkAssignSalesOwnerToLeads(request);
      setResult({
        totalRequested: response.totalRequested,
        successful: response.successful,
        failed: response.failed
      });
      setSuccess(true);
      
      if (response.failed > 0) {
        setError(`${response.failed} out of ${response.totalRequested} assignments failed`);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to assign sales owner');
      setSuccess(false);
    } finally {
      setLoading(false);
    }
  };

  const assignToAdvisors = async (request: BulkAdvisorSalesOwnerRequest) => {
    setLoading(true);
    setError(null);
    setSuccess(false);
    
    try {
      const response = await bulkAssignSalesOwnerToAdvisors(request);
      setResult({
        totalRequested: response.totalRequested,
        successful: response.successful,
        failed: response.failed
      });
      setSuccess(true);
      
      if (response.failed > 0) {
        setError(`${response.failed} out of ${response.totalRequested} assignments failed`);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to assign sales owner');
      setSuccess(false);
    } finally {
      setLoading(false);
    }
  };

  return {
    assignToLeads,
    assignToAdvisors,
    loading,
    error,
    success,
    result
  };
};
```

### Step 4: Create UI Component Example

Create a file `components/BulkSalesOwnerAssignmentModal.tsx`:

```typescript
import React, { useState } from 'react';
import { useBulkSalesOwnerAssignment } from '../hooks/useBulkSalesOwnerAssignment';
import { Modal, Button, Select, Alert, Progress } from 'antd';

interface BulkSalesOwnerAssignmentModalProps {
  visible: boolean;
  onClose: () => void;
  selectedIds: string[];
  type: 'lead' | 'advisor';
  onSuccess?: () => void;
}

const BulkSalesOwnerAssignmentModal: React.FC<BulkSalesOwnerAssignmentModalProps> = ({
  visible,
  onClose,
  selectedIds,
  type,
  onSuccess
}) => {
  const [selectedSalesOwner, setSelectedSalesOwner] = useState<string>('');
  const { assignToLeads, assignToAdvisors, loading, error, success, result } = 
    useBulkSalesOwnerAssignment();

  const handleSubmit = async () => {
    if (!selectedSalesOwner) {
      return;
    }

    if (type === 'lead') {
      await assignToLeads({
        leadIdentifiers: selectedIds,
        salesOwner: selectedSalesOwner
      });
    } else {
      await assignToAdvisors({
        advisorIdentifiers: selectedIds,
        salesOwner: selectedSalesOwner
      });
    }

    if (success && result?.failed === 0) {
      onSuccess?.();
      onClose();
    }
  };

  return (
    <Modal
      title={`Bulk Assign Sales Owner to ${type === 'lead' ? 'Leads' : 'Advisors'}`}
      open={visible}
      onCancel={onClose}
      footer={[
        <Button key="cancel" onClick={onClose} disabled={loading}>
          Cancel
        </Button>,
        <Button
          key="submit"
          type="primary"
          onClick={handleSubmit}
          loading={loading}
          disabled={!selectedSalesOwner}
        >
          Assign
        </Button>
      ]}
    >
      <div style={{ marginBottom: 16 }}>
        <p>
          Assigning sales owner to <strong>{selectedIds.length}</strong>{' '}
          {type === 'lead' ? 'lead(s)' : 'advisor(s)'}
        </p>
      </div>

      <div style={{ marginBottom: 16 }}>
        <label>Sales Owner:</label>
        <Select
          style={{ width: '100%', marginTop: 8 }}
          placeholder="Select sales owner"
          value={selectedSalesOwner}
          onChange={setSelectedSalesOwner}
          disabled={loading}
          // TODO: Populate with actual sales owners list
          options={[
            { label: 'John Doe', value: 'john.doe' },
            { label: 'Jane Smith', value: 'jane.smith' },
          ]}
        />
      </div>

      {loading && (
        <div style={{ marginBottom: 16 }}>
          <Progress percent={50} status="active" />
          <p>Processing assignments...</p>
        </div>
      )}

      {error && (
        <Alert
          message="Error"
          description={error}
          type="error"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      {success && result && (
        <Alert
          message="Assignment Complete"
          description={
            <div>
              <p>Total: {result.totalRequested}</p>
              <p>Successful: {result.successful}</p>
              {result.failed > 0 && (
                <p style={{ color: 'red' }}>Failed: {result.failed}</p>
              )}
            </div>
          }
          type={result.failed === 0 ? 'success' : 'warning'}
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}
    </Modal>
  );
};

export default BulkSalesOwnerAssignmentModal;
```

### Step 5: Usage Example in Dashboard/List Component

```typescript
import React, { useState } from 'react';
import { Button, Table } from 'antd';
import BulkSalesOwnerAssignmentModal from '../components/BulkSalesOwnerAssignmentModal';

const LeadsDashboard: React.FC = () => {
  const [selectedLeadIds, setSelectedLeadIds] = useState<string[]>([]);
  const [modalVisible, setModalVisible] = useState(false);

  const rowSelection = {
    selectedRowKeys: selectedLeadIds,
    onChange: (selectedKeys: React.Key[]) => {
      setSelectedLeadIds(selectedKeys as string[]);
    },
  };

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button
          type="primary"
          disabled={selectedLeadIds.length === 0}
          onClick={() => setModalVisible(true)}
        >
          Bulk Assign Sales Owner ({selectedLeadIds.length} selected)
        </Button>
      </div>

      <Table
        rowSelection={rowSelection}
        // ... other table props
      />

      <BulkSalesOwnerAssignmentModal
        visible={modalVisible}
        onClose={() => setModalVisible(false)}
        selectedIds={selectedLeadIds}
        type="lead"
        onSuccess={() => {
          // Refresh the table or show success message
          setSelectedLeadIds([]);
        }}
      />
    </div>
  );
};
```

---

## Error Handling

### Common Error Scenarios

1. **Invalid Sales Owner**: If the sales owner username doesn't exist
2. **Lead/Advisor Not Found**: If any of the provided identifiers don't exist
3. **Network Errors**: Handle network failures gracefully
4. **Partial Failures**: Some assignments may succeed while others fail

### Error Handling Example

```typescript
try {
  const response = await bulkAssignSalesOwnerToLeads({
    leadIdentifiers: ['id1', 'id2', 'id3'],
    salesOwner: 'username'
  });

  if (response.failed > 0) {
    // Show warning with details
    console.warn('Some assignments failed:', response.errors);
    // Display errors to user
    response.errors.forEach(error => {
      console.error(`Lead ${error.leadIdentifier}: ${error.errorMessage}`);
    });
  }
} catch (error: any) {
  if (error.response?.status === 400) {
    // Validation error
    console.error('Validation error:', error.response.data);
  } else if (error.response?.status === 500) {
    // Server error
    console.error('Server error:', error.response.data);
  } else {
    // Network or other error
    console.error('Request failed:', error.message);
  }
}
```

---

## Best Practices

1. **Loading States**: Always show loading indicators during API calls
2. **User Feedback**: Display success/error messages clearly
3. **Validation**: Validate input before making API calls
4. **Error Details**: Show specific error messages for failed assignments
5. **Optimistic Updates**: Consider updating UI optimistically, then sync with server response
6. **Retry Logic**: Implement retry for failed network requests
7. **Batch Size**: Consider limiting batch size for very large selections

---

## Testing

### Unit Test Example

```typescript
import { bulkAssignSalesOwnerToLeads } from '../services/bulkAssignmentService';
import axios from 'axios';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('bulkAssignSalesOwnerToLeads', () => {
  it('should successfully assign sales owner to leads', async () => {
    const mockResponse = {
      data: {
        totalRequested: 2,
        successful: 2,
        failed: 0,
        successfulLeadIdentifiers: ['id1', 'id2'],
        errors: []
      }
    };

    mockedAxios.post.mockResolvedValue(mockResponse);

    const result = await bulkAssignSalesOwnerToLeads({
      leadIdentifiers: ['id1', 'id2'],
      salesOwner: 'username'
    });

    expect(result.successful).toBe(2);
    expect(result.failed).toBe(0);
  });
});
```

---

## API Response Examples

### Success Response
```json
{
  "totalRequested": 3,
  "successful": 3,
  "failed": 0,
  "successfulLeadIdentifiers": ["uuid1", "uuid2", "uuid3"],
  "errors": []
}
```

### Partial Success Response
```json
{
  "totalRequested": 3,
  "successful": 2,
  "failed": 1,
  "successfulLeadIdentifiers": ["uuid1", "uuid2"],
  "errors": [
    {
      "leadIdentifier": "uuid3",
      "errorMessage": "Lead not found"
    }
  ]
}
```

---

## Notes

- Both APIs follow the same pattern, making it easy to reuse components
- The response includes detailed error information for each failed assignment
- All assignments are processed in a single transaction for consistency
- Failed assignments don't prevent successful ones from being saved

