-- Make advisor_code column nullable
ALTER TABLE advisor ALTER COLUMN advisor_code DROP NOT NULL;

-- Drop the unique constraint since we're allowing nulls
ALTER TABLE advisor DROP CONSTRAINT IF EXISTS advisor_advisor_code_key;

-- Recreate unique constraint excluding nulls
CREATE UNIQUE INDEX idx_advisor_code_unique ON advisor(advisor_code) WHERE advisor_code IS NOT NULL;
