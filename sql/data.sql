INSERT INTO users(id, name, lastname, email, password)
VALUES (1, 'name', 'lastname', 'user@mail.com', '$2a$10$wK7nKj1Mv3s.W3tZcK0Z6.jG7lFk4H/1bJ2Q9aXp.k5FqK2W0O9Ce'); -- Use generate-password in AuthController --admin123

INSERT INTO roles(id, name, description, user_created)
VALUES (1, 'ROOT', 'Superuser', 1),
        (2, 'ADMIN', 'Admin user', 1),
        (3, 'USER', 'Normal user', 1);

INSERT INTO users_roles(id, user_id, role_id, user_created)
VALUES (1, 1, 1, 1);

INSERT INTO sale_status(id, status, user_created)
VALUES (1, 'IN PROGRESS', 1),
        (2, 'PENDING DELIVERY', 1),
        (3, 'DONE', 1),
        (4, 'DELETED', 1);

INSERT INTO entry_status(id, status, user_created)
VALUES (1, 'IN PROGRESS', 1),
        (2, 'DONE', 1),
        (3, 'DELETED', 1);
