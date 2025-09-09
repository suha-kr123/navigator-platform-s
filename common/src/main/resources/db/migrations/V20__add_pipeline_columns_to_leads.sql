ALTER TABLE leads 
ADD COLUMN current_stage VARCHAR(100),
ADD COLUMN pipeline VARCHAR(100);

CREATE INDEX idx_leads_current_stage ON leads(current_stage);
CREATE INDEX idx_leads_pipeline ON leads(pipeline);
