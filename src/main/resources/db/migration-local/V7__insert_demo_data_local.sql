-- replace line 10 with your email if needed
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
    'Idam',
    'Name',
    'idam1@gmail.com',
    'Idam Firm 1',
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
    'Idam',
    'Name',
    'idam2@gmail.com',
    'Idam Firm 2',
    'IF2',
    'REJECTED',
    NOW() - INTERVAL '15 days',
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '5 days',
    'Unable to verify identity'
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
    created_date
)
SELECT 
    gen_random_uuid(),
    cu.id,
    gen_random_uuid(),
    'Idam',
    'Name',
    'idam3@gmail.com',
    'Idam Firm 3',
    'IF3',
    'OPEN',
    NOW() - INTERVAL '15 days'
FROM ccms_user cu
WHERE cu.login_id = '123';
