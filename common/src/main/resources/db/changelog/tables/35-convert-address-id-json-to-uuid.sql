DO
$$
DECLARE
col_type text;
BEGIN
SELECT data_type
INTO col_type
FROM information_schema.columns
WHERE table_name = 'leads'
  AND column_name = 'address_id';

IF
col_type IS NULL THEN
        RETURN;
    ELSIF
col_type <> 'uuid' THEN
        EXECUTE 'ALTER TABLE leads ADD COLUMN IF NOT EXISTS address_id_temp uuid';
EXECUTE $$
UPDATE leads
SET address_id_temp = CASE
                          WHEN address_id IS NULL THEN NULL
                          WHEN jsonb_typeof(address_id) = 'string' THEN
                              CASE
                                  WHEN address_id::text ~ '^"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"$'
                        THEN (address_id::text)::uuid
                                  ELSE NULL
                                  END
                          WHEN jsonb_typeof(address_id) = 'array' AND jsonb_array_length(address_id) > 0 THEN
                              CASE
                                  WHEN jsonb_array_length(address_id) = 1 AND jsonb_typeof(address_id - > 0) = 'string'
                                      THEN
                                      CASE
                                          WHEN (address_id - > 0)::text ~ '^"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"$'
                                THEN ((address_id - > 0)::text)::uuid
                                ELSE NULL
END
ELSE NULL
END
ELSE NULL
END
WHERE address_id IS NOT NULL;
        $$;
EXECUTE 'ALTER TABLE leads DROP COLUMN address_id';
EXECUTE 'ALTER TABLE leads RENAME COLUMN address_id_temp TO address_id';
END IF;
END
$$;

DO
$$
DECLARE
col_type text;
BEGIN
SELECT data_type
INTO col_type
FROM information_schema.columns
WHERE table_name = 'leads'
  AND column_name = 'address_id';

IF
col_type IS NOT NULL AND col_type <> 'uuid' THEN
        EXECUTE 'ALTER TABLE leads DROP COLUMN address_id';
EXECUTE 'ALTER TABLE leads ADD COLUMN address_id uuid';
END IF;
END
$$;
