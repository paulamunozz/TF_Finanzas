# Auditoría financiera

Tasa.porcentaje almacena fracciones; 0.12 representa 12%. Períodos en días, año comercial360.
Nominal de período P y capitalización C: TEA=(1+j/(P/C))^(360/C)-1.
Efectiva de período P: TEA=(1+i)^(360/P)-1.
La implementación original truncaba días/360, invertía fechas y devolvía factores sin aplicar
capital. FinancialMath ahora valida períodos, multiplica capital y redondea HALF_UP a céntimos.
Las funciones de compatibilidad totalGracia/intereses mantienen cálculo compuesto documentado;
la confirmación real y la ruta legada de crédito usan el cronograma francés de
[cronograma-persistente.md](cronograma-persistente.md).
Los importes de cuotas anteriores se conservan. La mora histórica de una cuota pagada y su
fecha no se recalculan. La cotización pendiente usa la misma regla nominal simple30/360 que
el frontend, porcentaje anual a siete decimales y redondeo monetario con BigDecimal.
Math.pow se usa para potencias fraccionarias y se convierte a BigDecimal para multiplicación
y redondeo. No se atribuye precisión decimal exacta a una potencia binaria.
El esquema Double no se migró destructivamente; véase recomendaciones.md.
