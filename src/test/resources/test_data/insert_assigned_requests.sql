-- Test data for findByLaaAssignee repository tests
-- Creates test requests with different assignees for testing pagination and filtering

-- Insert CCMS users first
INSERT INTO ccms_user (id, login_id, first_name, last_name, firm_name, firm_code, email)
VALUES 
    (gen_random_uuid(), 'assignedUser1', 'Test', 'User1', 'Test Firm 1', 'TF001', 'Test.user1@test.com'),
    (gen_random_uuid(), 'assignedUser2', 'Test', 'User2', 'Test Firm 2', 'TF002', 'Test.user2@test.com'),
    (gen_random_uuid(), 'assignedUser3', 'Test', 'User3', 'Test Firm 3', 'TF003', 'Test.user3@test.com');

-- Insert link requests assigned to testUser1 (3 requests)
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
    laa_assignee,
    provided_old_login_id
)
SELECT
    gen_random_uuid(),
    cu.id,
    gen_random_uuid(),
    'Test',
    'Person1',
    'test.person1@example.com',
    'Test Firm A',
    'TFA001',
    'OPEN',
    NOW() - INTERVAL '20 days',
    NOW() - INTERVAL '18 days',
    'testUser1',
    'oldUser1'
FROM ccms_user cu
WHERE cu.login_id = 'assignedUser1';

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
    laa_assignee,
    provided_old_login_id
)
SELECT
    gen_random_uuid(),
    cu.id,
    gen_random_uuid(),
    'Test',
    'Person2',
    'test.person2@example.com',
    'Test Firm B',
    'TFB002',
    'APPROVED',
    NOW() - INTERVAL '19 days',
    NOW() - INTERVAL '17 days',
    'testUser1',
    'oldUser2'
FROM ccms_user cu
WHERE cu.login_id = 'assignedUser2';

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
    laa_assignee,
    provided_old_login_id
)
SELECT
    gen_random_uuid(),
    cu.id,
    gen_random_uuid(),
    'Test',
    'Person3',
    'test.person3@example.com',
    'Test Firm C',
    'TFC003',
    'REJECTED',
    NOW() - INTERVAL '18 days',
    NOW() - INTERVAL '16 days',
    'testUser1',
    'oldUser3'
FROM ccms_user cu
WHERE cu.login_id = 'assignedUser3';

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
    laa_assignee,
    provided_old_login_id
)
SELECT
    gen_random_uuid(),
    cu.id,
    gen_random_uuid(),
    'Test',
    'Person6',
    'test.person6@example.com',
    'Test Firm F',
    'TFF006',
    'OPEN',
    NOW() - INTERVAL '16 days',
    NULL,
    NULL,
    'oldUser6'
FROM ccms_user cu
WHERE cu.login_id = 'assignedUser1';

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
    laa_assignee,
    provided_old_login_id
)
SELECT
    gen_random_uuid(),
    cu.id,
    gen_random_uuid(),
    'Test',
    'Person7',
    'test.person7@example.com',
    'Test Firm G',
    'TFG007',
    'OPEN',
    NOW() - INTERVAL '15 days',
    NULL,
    NULL,
    'oldUser7'
FROM ccms_user cu
WHERE cu.login_id = 'assignedUser2';
