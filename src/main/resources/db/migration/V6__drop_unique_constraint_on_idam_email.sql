-- Drop the unique constraints
ALTER TABLE link_request
DROP CONSTRAINT IF EXISTS link_request_idam_email_key;
ALTER TABLE link_request
DROP CONSTRAINT IF EXISTS link_request_idam_legacy_user_id_key;
