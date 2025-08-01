INSERT INTO ccms_user (id, login_id, first_name, last_name, firm_name, firm_code, email)
VALUES (
           gen_random_uuid(),
           '123',
           'Internal',
           'Principal',
           'Internal Firm',
           'F123',
           'your.email@version1workforcesandbox.onmicrosoft.com'
       );

INSERT INTO ccms_user (id, login_id, first_name, last_name, firm_name, firm_code, email)
VALUES (
           gen_random_uuid(),
           '456',
           'Internal',
           'Principal',
           'Internal Firm',
           'F123',
           'your.email1@version1workforcesandbox.onmicrosoft.com'
       );

INSERT INTO ccms_user (id, login_id, first_name, last_name, firm_name, firm_code, email)
VALUES (
           gen_random_uuid(),
           '678',
           'Internal',
           'Principal',
           'Internal Firm',
           'F123',
           'your.email2@version1workforcesandbox.onmicrosoft.com'
       );

INSERT INTO ccms_user (id, login_id, first_name, last_name, firm_name, firm_code, email)
VALUES (
           gen_random_uuid(),
           '999',
           'Internal',
           'Principal',
           'Internal Firm',
           'F123',
           'your.email3@version1workforcesandbox.onmicrosoft.com'
       );

INSERT INTO link_request (
    id,
    ccms_user_id,
    idam_legacy_user_id,
    idam_first_name,
    idam_last_name,
    idam_email,
    idam_firm_name,
    idam_firm_code,
    status,
    created_date,
    assigned_date,
    decision_date,
    decision_reason
)
SELECT
    gen_random_uuid(),
    cu.id,
    gen_random_uuid(),
    'Internal',
    'Principal',
    'your.email@version1workforcesandbox.onmicrosoft.com',
    'Internal Firm',
    'IF1',
    'APPROVED',
    NOW() - INTERVAL '15 days',
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '5 days',
    'Identity verified successfully'
FROM ccms_user cu
WHERE cu.login_id = '123';

INSERT INTO link_request (
    id,
    ccms_user_id,
    idam_legacy_user_id,
    idam_first_name,
    idam_last_name,
    idam_email,
    idam_firm_name,
    idam_firm_code,
    status,
    created_date,
    assigned_date,
    decision_date,
    decision_reason
)
SELECT
    gen_random_uuid(),
    cu.id,
    gen_random_uuid(),
    'Internal',
    'Principal',
    'your.email1@version1workforcesandbox.onmicrosoft.com',
    'Internal Firm',
    'F123',
    'OPEN',
    NOW() - INTERVAL '15 days',
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '5 days',
    'Unable to verify identity'
FROM ccms_user cu
WHERE cu.login_id = '456';

INSERT INTO link_request (
    id,
    ccms_user_id,
    idam_legacy_user_id,
    idam_first_name,
    idam_last_name,
    idam_email,
    idam_firm_name,
    idam_firm_code,
    status,
    created_date,
    assigned_date,
    decision_date,
    decision_reason
)
SELECT
    gen_random_uuid(),
    cu.id,
    gen_random_uuid(),
    'Internal',
    'Principal',
    'your.email2@version1workforcesandbox.onmicrosoft.com',
    'Internal Firm',
    'F123',
    'REJECTED',
    NOW() - INTERVAL '15 days',
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '5 days',
    'Unable to verify identity'
FROM ccms_user cu
WHERE cu.login_id = '678';

