ALTER TABLE link_request
    ADD COLUMN provided_old_login_id VARCHAR(255) NOT NULL DEFAULT '';

CREATE INDEX linked_request_provided_old_login_id ON link_request (provided_old_login_id);
