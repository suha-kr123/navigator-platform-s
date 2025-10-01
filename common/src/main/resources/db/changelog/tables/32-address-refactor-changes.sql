DO
$$
BEGIN
    IF
EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'leads' AND column_name = 'address_ids'
    ) THEN
        EXECUTE 'ALTER TABLE leads RENAME COLUMN address_ids TO address_id';
EXECUTE 'ALTER TABLE leads ALTER COLUMN address_id TYPE uuid USING address_id::uuid';
END IF;
END
$$;

DO
$$
BEGIN
    IF
EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'address' AND column_name = 'address_type'
    ) THEN
        EXECUTE 'ALTER TABLE address ALTER COLUMN address_type DROP NOT NULL';
END IF;
END
$$;

DO
$$
BEGIN
    IF
EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'address' AND column_name = 'address_source'
    ) THEN
        EXECUTE 'ALTER TABLE address ALTER COLUMN address_source DROP NOT NULL';
END IF;
END
$$;
