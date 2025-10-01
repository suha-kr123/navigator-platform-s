ALTER TABLE leads
ALTER
COLUMN requested_amount TYPE jsonb
USING jsonb_build_object('min', requested_amount, 'max', requested_amount);
