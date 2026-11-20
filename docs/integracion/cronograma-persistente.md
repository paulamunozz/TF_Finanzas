# Contrato del cronograma persistente

Se adopta el sistema francés mencionado en Cuentix-front/docs/payment-schedules.md.
Los ejemplos mock se conservan como regresión; no se insertan como operaciones reales.
Crédito a cuotas: gracia capitalizada hasta el día de pago del ciclo de corte. La primera cuota
vence un mes después de esa fecha; siguientes vencimientos mensuales. Tasa mensual efectiva
i=(1+TEA)^(30/360)-1. Cuota A=G*i/(1-(1+i)^(-n)); si i=0, A=G/n.
Cada período: interés=saldo*i, amortización=A-interés. HALF_UP a dos decimales;
la última cuota ajusta el saldo restante para que la suma de amortización sea exactamente G.
Pago único: una obligación al día de pago del ciclo, por el capital capitalizado hasta esa fecha.
Mora nominal anual simple 30/360 sobre la cuota vencida. Tasas moratorias del formulario se
interpretan anuales, acorde a lateAnnualRate de Manuel. La mora efectiva se convierte a
equivalente anual antes de aplicar la regla simple documentada.
La deuda reservada es la suma de cuotas (sin mora futura). Se rechaza si excede el disponible.
Confirmación transaccional: bloqueo de cuenta, validación de productos/negocio, compra,
ítems, cuotas y actualización de deuda. Cancelación bloquea cuenta y cuota; un segundo pago
se rechaza. Se guarda fecha de pago en cuota.fecha_pago mediante migración manual versionada.
No se recalculan cuotas históricas ni se generan cuotas sobre compras ya finalizadas.
