# Reporte de pruebas

Entorno local Windows, PowerShell, JDK17, Node26; fecha 13/11/2026.

## Comandos y resultado

| Comando                                   | Resultado                                            |
| ----------------------------------------- | ---------------------------------------------------- |
| git fetch origin                          | Correcto, referencias actualizadas                   |
| npm.cmd ci --no-audit --no-fund           | Correcto, lockfile conservado                        |
| npm.cmd run build                         | Correcto, compilación Angular producción             |
| npm.cmd test -- --watch=false             | 21 pruebas, 4 archivos, sin fallos                   |
| JAVA_HOME=JDK17 y mvnw.cmd -B test        | 12 pruebas; incluye inicio Spring y HTTP real con H2 |
| node scripts/browser-smoke.cjs con Chrome | Correcto a 390 y 1440 px; fixtures HTTP              |

## Cobertura comprobada

Backend: conversiones TNA/efectiva, capitalización fraccionaria y monetaria, fechas de febrero,
redondeo HALF_UP, amortización francesa que suma el capital y mora simple.
HTTP real: login, sesión, CSRF, lectura ajena rechazada, perfil persistido, logout, rol de negocio,
alta/edición de cuenta, catálogo, compra transaccional, FK de ítems, cuotas persistidas, deuda,
pago y rechazo de doble pago, compra rechazada por límite sin persistencia parcial, reporte y
acuerdos propios, desactivación, registro BCrypt, documento duplicado y aislamiento entre negocios.
La prueba de regresión también recorre preRegistrarCompra → registrarItem → registrarCompra,
conservando el ID y comprobando que no se generan cuotas dos veces.
Frontend: rutas de cronogramas conservadas, cálculos visuales existentes, interceptor y sesión.
Chrome: formulario login vacío, acceso cliente, reporte vacío, navegación a detalle de acuerdo,
ausencia de errores JavaScript y de desbordamiento horizontal en las vistas comprobadas.
Las capturas están en evidencias/. No son capturas de PostgreSQL ni de datos de producción.

## Incidencias resueltas durante la ejecución

npm.ps1 estaba bloqueado por PowerShell: se utilizó npm.cmd sin alterar la política.
JAVA_HOME apuntaba a un JRE: se utilizó el JDK17 instalado, sin modificar la configuración global.
La prueba inicial de contexto esperaba PostgreSQL: se aisló en perfil test con H2.
Se corrigió el tipo del guard en su prueba y el campo opcional usedCredit del contrato JSON.
La prueba de edición comprobó que la tasa moratoria sigue anual después de guardar.

## Límites de la verificación inicial

No se ejecutaron migraciones ni pruebas contra PostgreSQL: no hay conexión de pruebas
configurada ni psql/Docker disponibles en PATH. H2 no sustituye la validación PostgreSQL.
No se ejecutó un navegador conectado simultáneamente a PostgreSQL; la UI usa fixtures y
el backend se prueba por HTTP real con H2. Tampoco se certifica carga ni concurrencia.
CI queda preparada pero no se ejecutó en GitHub porque no se hizo push.

## Casos para el informe SI642

| Caso | Datos de entrada | Resultado esperado |
| --- | --- | --- |
| Crédito a tres cuotas | S/ 1.000, TEA 24 %, compra el 15/09/2026, corte el día 20 y pago el día 26 | 11 días comerciales de gracia y capital de S/ 1.006,59. Vencimientos el 26/10, 26/11 y 26/12, con cuotas de S/ 347,74, S/ 347,74 y S/ 347,75. |
| Cobro conjunto con mora | Dos compras previas al corte por S/ 200 y S/ 300, vencimiento común el día 25, mora nominal anual de 36 %, pago el día 5 del mes siguiente | 10 días comerciales de mora. Un solo recibo por S/ 505,00, con S/ 5,00 de mora y S/ 500,00 de capital. El cierre conserva S/ 500,00 emitidos y muestra S/ 5,00 como interés por mora cobrado. |

El primer caso se verifica en `FinancialMathTest`. El segundo se verifica por HTTP en
`SessionHttpTest`, junto con el rechazo de abonos y rutas individuales, reintentos concurrentes,
el límite por hora de corte y la conservación de vencimientos al editar la cuenta.

## Validación del cobro conjunto

- Angular. La compilación de producción y las 23 pruebas pasaron.
- Spring con H2. Pasaron las 15 pruebas.
- Spring con PostgreSQL 17.12 y esquema en modo `validate`. Pasaron las 15 pruebas, incluida la
  confirmación concurrente del mismo pago y la conciliación del listado.
- Migraciones V0–V5 en una base vacía. En una base V4 con pagos históricos de prueba, V5 conservó
  los importes, estados y fechas de las cuotas.
- Edge con PostgreSQL. Se recorrieron los tres perfiles, se comprobó la mora de un pago histórico
  y se registró un cobro conjunto de S/ 505,00. La cancelación se mantuvo tras recargar.
- Prueba de ayudas en Edge con datos simulados. Se abrieron y cerraron con teclado a 1440 px y
  con toque a 390 px. El script admite `BROWSER_CHANNEL=msedge` y un `EVIDENCE_DIR` alternativo.
