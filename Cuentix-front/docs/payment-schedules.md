# Cronogramas y cobranzas
Se conservan las rutas /clients/:id/payments, /purchases/history, /purchases/:purchaseId/schedule,
 /installments/:paymentId y los alias /clientes/:id/cronograma, /compras/:purchaseId/cronograma,
 /cuotas/:paymentId. ScheduleQuery y PaymentRepository siguen coordinando consultas y pagos.
Los proveedores activos son HTTP; los mocks se utilizan solo en pruebas de regresión.
La proyección Client identifica una cuenta de crédito para conservar moneda y condiciones propias.
Purchase.accountId y los campos clientId se relacionan con CuentaCredito.id.
La referencia documental externa mencionada por Manuel no está en este repositorio; sus ejemplos
COMP-001/002/003 se mantienen como fixtures, sin presentarlos como datos de PostgreSQL.
El nuevo registro genera compra, ítems y cronograma en Spring y actualiza deuda en una transacción.
La mora se cotiza nominal simple30/360; las cuotas pagadas preservan importe y fecha.
Ver ../../docs/integracion/cronograma-persistente.md, contratos.md y pruebas.md.
