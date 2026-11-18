-- Aplicar manualmente sobre una copia respaldada antes del arranque con ddl-auto=validate.
-- No modifica importes, estados ni fechas de cuotas anteriores.
ALTER TABLE cuota ADD COLUMN IF NOT EXISTS fecha_pago DATE;
