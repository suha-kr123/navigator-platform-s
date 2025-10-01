INSERT INTO master_code (id, key, name, description, is_system_defined, created_by, created_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440009', 'TASK_STATUS', '{"default": "Task Status"}',
        '{"default": "Status of a task in the workflow"}', true, 'system', '2025-01-27 10:00:00', 0);

INSERT INTO master_code_value (id, key, code_key, value, description, is_active, created_by, created_at, version)
VALUES ('550e8400-e29b-41d4-a716-446655440901', 'TODO', 'TASK_STATUS', '{"default": "To Do"}',
        '{"default": "Task is pending and not yet started"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440902', 'INPROGRESS', 'TASK_STATUS', '{"default": "In Progress"}',
        '{"default": "Task is currently being worked on"}', true, 'system', '2025-01-27 10:00:00', 0),
       ('550e8400-e29b-41d4-a716-446655440903', 'COMPLETED', 'TASK_STATUS', '{"default": "Completed"}',
        '{"default": "Task has been finished successfully"}', true, 'system', '2025-01-27 10:00:00', 0);
