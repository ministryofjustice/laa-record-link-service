CREATE TABLE ccms_user
(
    id         uuid     NOT NULL,
    email      VARCHAR(255),
    firm_code  VARCHAR(255),
    firm_name  VARCHAR(255),
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    login_id   VARCHAR(255) NOT NULL,
    CONSTRAINT ccms_user_pkey PRIMARY KEY (id)
);