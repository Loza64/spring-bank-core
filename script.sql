INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN' AND p.method = 'GET'
ON CONFLICT DO NOTHING;

select * from roles r ;
select * from role_permissions;
select * from permissions;

delete from role_permissions ;