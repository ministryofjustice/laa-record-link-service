-- Test data for findFirstByLaaAssigneeIsNullAndStatusOrderByCreatedDateAsc tests
INSERT INTO link_request (
    id,
    provided_old_login_id,
    idam_legacy_user_id,
    idam_first_name,
    idam_last_name,
    idam_firm_name,
    idam_firm_code,
    idam_email,
    created_date,
    assigned_date,
    laa_assignee,
    status
) VALUES 
-- Unassigned OPEN requests
('11111111-1111-1111-1111-111111111111', 'newest_unassigned', 'IDAM_NEW', 'New', 'User', 'New Firm', 'NF001', 'new.user@example.com', '2024-01-20 10:00:00', NULL, NULL, 'OPEN'),
('22222222-2222-2222-2222-222222222222', 'oldest_unassigned', 'IDAM_OLD', 'Old', 'User', 'Old Firm', 'OF001', 'old.user@example.com', '2024-01-15 09:00:00', NULL, NULL, 'OPEN'),
('33333333-3333-3333-3333-333333333333', 'middle_unassigned', 'IDAM_MID', 'Middle', 'User', 'Middle Firm', 'MF001', 'middle.user@example.com', '2024-01-18 11:00:00', NULL, NULL, 'OPEN'),

-- Assigned OPEN requests
('44444444-4444-4444-4444-444444444444', 'assigned_open', 'IDAM_ASSIGNED1', 'Assigned', 'User1', 'Assigned Firm', 'AF001', 'assigned1@example.com', '2024-01-10 08:00:00', '2024-01-11 09:00:00', 'testUser1', 'OPEN'),
('55555555-5555-5555-5555-555555555555', 'assigned_open2', 'IDAM_ASSIGNED2', 'Assigned', 'User2', 'Assigned Firm', 'AF002', 'assigned2@example.com', '2024-01-12 08:00:00', '2024-01-13 09:00:00', 'testUser2', 'OPEN'),

-- Assigned non-OPEN requests (realistic scenario - approved/rejected requests should have assignees)
('66666666-6666-6666-6666-666666666666', 'assigned_approved', 'IDAM_APPROVED', 'Approved', 'User', 'Approved Firm', 'APF001', 'approved@example.com', '2024-01-05 07:00:00', '2024-01-06 08:00:00', 'testUser3', 'APPROVED'),
('77777777-7777-7777-7777-777777777777', 'unassigned_rejected', 'IDAM_REJECTED', 'Rejected', 'User', 'Rejected Firm', 'RF001', 'rejected@example.com', '2024-01-08 07:00:00', NULL, NULL, 'REJECTED');
