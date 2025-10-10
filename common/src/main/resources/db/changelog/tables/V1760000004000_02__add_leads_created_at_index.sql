-- Add index on leads.created_at for better query performance
-- This index will improve performance for ORDER BY created_at DESC queries
-- and range queries on creation dates

CREATE INDEX IF NOT EXISTS idx_leads_created_at ON leads (created_at DESC);

-- Add comment to document the purpose of this index
COMMENT ON INDEX idx_leads_created_at IS 'Index on leads.created_at for improved query performance on date-based sorting and filtering';
