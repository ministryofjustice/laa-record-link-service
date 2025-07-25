CREATE TABLE link_request
(
    assigned_date       TIMESTAMP WITHOUT TIME ZONE,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    decision_date       TIMESTAMP WITHOUT TIME ZONE,
    ccms_user_id        uuid                    NOT NULL,
    id                  uuid                    NOT NULL,
    idam_legacy_user_id uuid                    NOT NULL,
    additional_info     VARCHAR(255),
    decision_reason     VARCHAR(255),
    idam_email          VARCHAR(255)                NOT NULL,
    idam_firm_code      VARCHAR(255),
    idam_firm_name      VARCHAR(255),
    idam_first_name     VARCHAR(255)                NOT NULL,
    idam_last_name      VARCHAR(255)                NOT NULL,
    laa_assignee        VARCHAR(255),
    status              VARCHAR(255)                NOT NULL,
    CONSTRAINT link_request_pkey PRIMARY KEY (id)
);
ALTER TABLE link_request
    ADD CONSTRAINT link_request_idam_email_key UNIQUE (idam_email);

ALTER TABLE link_request
    ADD CONSTRAINT link_request_idam_legacy_user_id_key UNIQUE (idam_legacy_user_id);

CREATE INDEX linked_request_created_date ON link_request (created_date);

CREATE INDEX linked_request_decision_date ON link_request (decision_date);

CREATE INDEX linked_request_idam_first_name ON link_request (idam_first_name);

CREATE INDEX linked_request_idam_last_name ON link_request (idam_last_name);

CREATE INDEX linked_request_laa_assignee ON link_request (laa_assignee);

CREATE INDEX linked_request_status ON link_request (status);

ALTER TABLE link_request
    ADD CONSTRAINT fk_link_request_ccms_user_id FOREIGN KEY (ccms_user_id) REFERENCES ccms_user (id) ON DELETE NO ACTION;