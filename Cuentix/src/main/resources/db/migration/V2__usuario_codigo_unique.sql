-- No aplicar si existen códigos duplicados. Revisar antes:
-- SELECT codigo, count(*) FROM usuario GROUP BY codigo HAVING count(*) > 1;
-- La aplicación no reutiliza documentos de cuentas desactivadas.
CREATE UNIQUE INDEX IF NOT EXISTS ux_usuario_codigo ON usuario(codigo);
