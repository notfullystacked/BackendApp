INSERT INTO role (role_name) VALUES ('MovieEditor'), ('Admin'), ('Clerk'), ('Guest');

INSERT INTO employee (name, username, email, password)
VALUES ('Mads', 'mads', 'mads@kino.dk', '$2a$10$f09ojmkgzfNINU39v5lOoOPQHgmGR44kB7uZrUsRT6zaN3N39Fef6');

-- Mads er Clerk (3) og Admin (2), så alle endpoints kan testes
INSERT INTO employee_role (employee_id, role_id) VALUES (1, 3), (1, 2);