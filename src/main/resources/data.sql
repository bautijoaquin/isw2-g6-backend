-- =====================================================================
-- Liga Amateur Santafesina: datos iniciales (data.sql)
-- Solo las tablas que NO tienen CRUD en el sistema, más el administrador inicial.
-- Equipos, jugadores, partidos y eventos arrancan vacíos: se cargan desde la app.
-- =====================================================================

-- Administrador inicial: usuario admin, contraseña Admin2026! (guardada con BCrypt)
INSERT INTO usuario (id, username, password_hash, nombre_completo, rol, activo)
VALUES (1, 'admin', '$2a$10$nlQuA4lskuCHKf20.SP0Y.qxRBSzebNuT5bVGy43ySrDtDMz9ryQ2', 'Administrador de la liga', 'ADMIN', TRUE);

-- Torneo en curso
INSERT INTO torneo (id, nombre, anio, activo) VALUES (1, 'Apertura 2026', 2026, TRUE);

-- Canchas
INSERT INTO cancha (id, nombre, direccion) VALUES (1, 'Cancha Municipal Norte', 'Av. Blas Parera 7400');
INSERT INTO cancha (id, nombre, direccion) VALUES (2, 'Predio La Costanera', 'Av. Almirante Brown 5200');
INSERT INTO cancha (id, nombre, direccion) VALUES (3, 'Complejo El Alero', 'Pje. Los Ceibos 1350');

-- Árbitros
INSERT INTO arbitro (id, nombre, apellido, dni) VALUES (1, 'Marcelo', 'Quiroga', '28111222');
INSERT INTO arbitro (id, nombre, apellido, dni) VALUES (2, 'Daniela', 'Ferreyra', '30222333');
INSERT INTO arbitro (id, nombre, apellido, dni) VALUES (3, 'Hernán', 'Bustos', '27333444');
INSERT INTO arbitro (id, nombre, apellido, dni) VALUES (4, 'Paula', 'Godoy', '32444555');

-- Los ids anteriores se cargaron a mano: se adelantan los contadores
ALTER TABLE usuario ALTER COLUMN id RESTART WITH 2;
ALTER TABLE torneo  ALTER COLUMN id RESTART WITH 2;
ALTER TABLE cancha  ALTER COLUMN id RESTART WITH 4;
ALTER TABLE arbitro ALTER COLUMN id RESTART WITH 5;
