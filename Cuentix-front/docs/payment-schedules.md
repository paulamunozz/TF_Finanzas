# Cronogramas y cobranzas

La documentación financiera se consultó en `C:/Users/henos/Downloads/TrabajoParcial_Finanzas.md`; no existe una copia dentro del repositorio. Los algoritmos documentan Compra → CuentaCredito e Item → Compra. Las referencias incluidas aquí son modelos de lectura, no cambios de esquema SQL.

## Consultas

- `/clients/:id/payments`: consolidado por cliente.
- `/purchases/history`: compras del negocio activo.
- `/purchases/:purchaseId/schedule`: cronograma individual.
- `/installments/:paymentId`: detalle y cancelación completa.
- Alias: `/clientes/:id/cronograma`, `/compras/:purchaseId/cronograma`, `/cuotas/:paymentId`.

`ScheduleQuery` coordina repositorio, cliente y resúmenes; `schedule-domain.ts` normaliza fechas y centraliza estados, sumas monetarias y mora nominal no capitalizable. `PaymentRepository` admite consultas por cliente, compra y cuota, y cancelación completa. Los componentes comparten el diálogo de pago.

Las compras mock COMP-001, COMP-002 y COMP-003 reproducen los importes documentados de los casos 1, 3 y 2. COMP-003 ya está cancelada al 15/04/2026. COMP-OTHER pertenece exclusivamente al negocio b2. La línea mock de c1 se elevó a 5,000 y su plazo a seis meses para admitir las compras de prueba. Los productos de prueba son descripciones de operaciones: el informe no identifica sus nombres comerciales.

El principal de COMP-002 es 1,200; la amortización incluye la gracia capitalizada de 10.08. La suma de intereses de cuotas es 55.21 y el total programado 1,265.29. La gracia se informa por separado y no se vuelve a sumar a la deuda. No se muestran saldos iniciales/finales porque el repositorio no los proporciona.

El importe pendiente incluye cuota y mora; el total programado excluye mora. El cancelado incluye mora efectivamente pagada. Las cuotas pagadas mantienen sus importes históricos y se excluyen del pendiente. La mora se calcula con la TNA histórica de cada obligación, base comercial 30/360, y se redondea a céntimos mediante aritmética entera. La fecha de referencia utiliza America/Lima.

## Límites actuales

- Persistencia únicamente en la sesión mock. No hay API ni autorización de backend implementada; el adaptador HTTP incluye el negocio en cada consulta/cancelación.
- El formulario anterior de registro de compra todavía no genera ni persiste cronogramas. El historial nuevo muestra operaciones del repositorio de cronogramas.
- El modelo existente de cliente combina identidad y configuración crediticia. `accountId` en la proyección identifica la cuenta de origen; la futura API debe entregar la relación real de CuentaCredito.
- Las consultas consolidadas que mezclen monedas se rechazan, evitando sumas incorrectas. La consulta individual usa la moneda de su compra.
- La inspección visual necesita un navegador conectado; los tests de rutas verifican el renderizado y la navegación con Angular.

Verificación: `npm.cmd run build`; `npm.cmd test -- --watch=false`.
