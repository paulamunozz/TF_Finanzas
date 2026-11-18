\getenv clave CUENTIX_DEMO_PASSWORD
BEGIN;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Only the first run provisions accounts. Later runs preserve operator changes.
CREATE TABLE IF NOT EXISTS docker_demo_setup (id integer PRIMARY KEY);
SELECT NOT EXISTS (SELECT 1 FROM docker_demo_setup WHERE id = 1) AS inicializar \gset
\if :inicializar
INSERT INTO usuario(codigo, contrasena, estado, rol_id)
SELECT codigo, crypt(:'clave', gen_salt('bf', 10)), true, rol
FROM (VALUES ('admin-demo', 1), ('20123456789', 3), ('12345678', 2)) AS demo(codigo, rol);
INSERT INTO admin(usuario_id) SELECT id_usuario FROM usuario WHERE codigo = 'admin-demo';
INSERT INTO negocio(razon_social, direccion, telefono, estado, giro_id, usuario_id)
SELECT 'Comercio de prueba', 'Lima', '999111222', true,
       (SELECT min(id_giro) FROM giro), id_usuario FROM usuario WHERE codigo = '20123456789';
INSERT INTO cliente(nombre, apellido, telefono, estado, usuario_id)
SELECT 'Cliente', 'Demo', '999333444', true, id_usuario FROM usuario WHERE codigo = '12345678';
INSERT INTO docker_demo_setup(id) VALUES (1);
\endif
COMMIT;
