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

## Límites

No se ejecutaron migraciones ni pruebas contra PostgreSQL: no hay conexión de pruebas
configurada ni psql/Docker disponibles en PATH. H2 no sustituye la validación PostgreSQL.
No se ejecutó un navegador conectado simultáneamente a PostgreSQL; la UI usa fixtures y
el backend se prueba por HTTP real con H2. Tampoco se certifica carga ni concurrencia.
CI queda preparada pero no se ejecutó en GitHub porque no se hizo push.
