# Auditoría financiera

Tasa.porcentaje almacena fracciones; 0.12 representa 12%. Períodos en días, año comercial360.
Nominal de período P y capitalización C: TEA=(1+j/(P/C))^(360/C)-1.
Efectiva de período P: TEA=(1+i)^(360/P)-1.
La implementación original truncaba días/360, invertía fechas y devolvía factores sin aplicar
capital. FinancialMath ahora valida períodos, multiplica capital y redondea HALF_UP a céntimos.
Las funciones de compatibilidad totalGracia/intereses mantienen cálculo compuesto documentado;
la confirmación real y la ruta legada de crédito usan el cronograma francés de
[cronograma-persistente.md](cronograma-persistente.md).
Para `d` días comerciales, el factor de gracia es `(1 + TEA)^(d/360)`. Se cuentan
360 días por año, 30 por mes y cada día del mes se limita a 30 al calcular la diferencia.

Para una obligación vencida de importe `A`, la mora nominal anual `j` es `A × j × d/360`.
Para una mora efectiva anual `i` es `A × ((1 + i)^(d/360) − 1)`.
El servidor utiliza al menos siete decimales para las tasas y redondea los importes a
dos decimales con `HALF_UP`. Las potencias fraccionarias usan aritmética de doble precisión.

El importe del cobro conjunto suma los cargos de cada obligación, calculados con la tasa
pactada en su compra. El recibo registra la imputación en orden de mora, interés compensatorio
y capital. En un cronograma francés, la gracia se distribuye proporcionalmente a la amortización,
ajustando el redondeo acumulado para asignar todos los céntimos. El listado incluye la gracia dentro
del interés compensatorio y conserva el total emitido. En pagos históricos, la mora cobrada y la fecha
se leen de la cuota.
