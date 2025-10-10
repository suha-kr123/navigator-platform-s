-- Add GIN index on person.mobile_numbers JSONB column for faster JSON searches
-- This index will significantly improve performance for queries that search within
-- the mobile_numbers JSONB array, such as LIKE searches on phone numbers

CREATE INDEX IF NOT EXISTS idx_person_mobile_numbers_gin ON person USING GIN (mobile_numbers);

-- Add comment to document the purpose of this index
COMMENT ON INDEX idx_person_mobile_numbers_gin IS 'GIN index on person.mobile_numbers JSONB column for improved performance on JSON-based phone number searches';
