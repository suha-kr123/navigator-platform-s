-- Add data_ext JSONB column to leads table for storing external data like freshsalesId
ALTER TABLE leads ADD COLUMN data_ext JSONB;

-- Add index on data_ext for better query performance
CREATE INDEX idx_leads_data_ext ON leads USING GIN (data_ext);
