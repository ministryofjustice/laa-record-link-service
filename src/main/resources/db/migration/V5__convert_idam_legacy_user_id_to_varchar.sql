ALTER TABLE link_request 
    ALTER COLUMN idam_legacy_user_id 
    TYPE VARCHAR(255)
    USING idam_legacy_user_id::text;