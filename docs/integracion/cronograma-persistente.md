# Contrato del cronograma persistente

Se adopta el sistema francés mencionado en Cuentix-front/docs/payment-schedules.md.
Los ejemplos mock se conservan como regresión; no se insertan como operaciones reales.
Crédito a cuotas: gracia capitalizada hasta el día de pago del ciclo de corte. La primera cuota
vence un mes después de esa fecha; siguientes vencimientos mensuales. Tasa mensual efectiva
i=(1+TEA)^(30/360)-1. Cuota A=G*i/(1-(1+i)^(-n)); si i=0, A=G/n.
Cada período: interés=saldo*i, amortización=A-interés. HALF_UP a dos decimales;
la última cuota ajusta el saldo restante para que la suma de amortización sea exactamente G.
Pago único: una obligación al día de pago del ciclo, por el capital capitalizado hasta esa fecha.
El formulario toma las tasas moratorias como anuales. Para cada obligación se aplica la
mora nominal o efectiva según las [fórmulas de cálculo](finanzas.md).
La deuda reservada es la suma de cuotas (sin mora futura). Se rechaza si excede el disponible.
La compra valida los productos y el crédito, persiste sus ítems y cuotas y reserva la deuda
en una transacción que bloquea la cuenta. El cobro toma el mismo bloqueo, valida el importe
y liquida todas las obligaciones pendientes del vencimiento. Un reintento con la misma clave
y datos devuelve el recibo original. Las cuotas guardan la fecha efectiva y la mora cobrada.

El cierre mantiene el importe emitido. Cuando se registra un pago, incorpora el importe de mora
cobrado junto a la fecha efectiva. Un registro incorporado después de emitir el cierre se
identifica como ajuste posterior. Cada cuota aparece a lo sumo una vez por cierre.
