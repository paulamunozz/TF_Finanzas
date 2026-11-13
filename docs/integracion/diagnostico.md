# Diagnóstico y matriz de integración

Base Manuel: 66ea394a569e6c1a870f20caab8d85fbfc9095a4.
Álvaro: 517e8deb30900a77537b22db19168822150863ed.
Ancestro: 39bc39f6354377599a40cfbcd6e38b561eee7d5e. Divergencia 4/1.
Árbol limpio antes de fetch y creación de integracion/alvaro-manuel.

| Archivo o módulo                                                                                                     | Manuel                        | Álvaro                       | Conflicto        | Decisión                                            | Justificación                             |
| -------------------------------------------------------------------------------------------------------------------- | ----------------------------- | ---------------------------- | ---------------- | --------------------------------------------------- | ----------------------------------------- |
| app.config.ts                                                                                                        | Repositorios mock, HTTP       | HTTP                         | Sí               | Conservar interfaces y activar HTTP con interceptor | Preservar flujos                          |
| app.html                                                                                                             | router-outlet                 | router-outlet                | Sí               | Una salida                                          | Equivalentes                              |
| app.routes.ts                                                                                                        | Layouts y features            | Páginas cliente              | Sí               | Rutas por rol                                       | Conservar módulos                         |
| styles.css/styles.scss                                                                                               | Tokens SCSS                   | CSS básico                   | Sí modify/delete | SCSS y tokens                                       | Identidad Manuel                          |
| pages y services                                                                                                     | No existen                    | Módulo cliente               | No               | features/client y core/services                     | Arquitectura Manuel                       |
| ClienteService                                                                                                       | CRUD                          | BCrypt y perfil propio       | No               | Conservar                                           | Persistencia y desactivación              |
| UsuarioController                                                                                                    | Login                         | Sesión y CSRF parcial        | No               | Recuperar sesión y proteger API                     | sessionStorage no autoriza                |
| Reportes cliente                                                                                                     | No existen                    | Listas vacías sin HTTP       | No               | Lecturas propias por sesión                         | No simular datos                          |
| CompraService                                                                                                        | Mapeo implícito               | Asocia cuenta explícitamente | No               | Conservar y transacciones                           | Integridad FK                             |
| ItemService                                                                                                          | Mapeo implícito               | Igual                        | No               | Asociar compra/producto                             | Integridad referencial                    |
| Finanzas                                                                                                             | Double y factores sin capital | Igual                        | No               | FinancialMath y cronograma francés probado          | Capital, períodos y céntimos verificables |
| Dependencias                                                                                                         | Angular22.2 TS6 fuentes       | Mismas sin fuentes           | No               | Conservar Manuel                                    | Sin downgrade                             |
| Backend                                                                                                              | Boot4.1.1 Java17              | Añade crypto                 | No               | Conservar BCrypt                                    | Hash                                      |
| Entidades                                                                                                            | 14 entidades                  | Sin cambios de esquema       | No               | Sin migración destructiva                           | Preservar datos                           |
| application.properties                                                                                               | create-drop y credenciales    | Igual                        | No               | validate y variables entorno                        | Evitar pérdida de datos                   |
| Se conservan controllers Cliente, Compra, CuentaCredito, Item, Negocio, Producto y Usuario;                          |
| sus servicios, interfaces, DTO y repositorios. Entidades: Admin, Cliente, Compra, CuentaCredito,                     |
| Cuota, Giro, Item, Moneda, Negocio, Producto, Rol, Tasa, UnidadMedida y Usuario.                                     |
| Manuel conserva dashboard, customers, products, purchases y payment-schedules.                                       |
| Álvaro aporta login, registro, panel, perfil/edición/desactivación, acuerdos y reportes incompletos.                 |
| Los adaptadores HTTP de Manuel declaran contratos provisionales inexistentes en Spring.                              |
| Inicialmente los proveedores activos eran mock y el catálogo era local; no existía generación persistente de cuotas. |
| La versión final activa HTTP e implementa contratos de cuentas, catálogo, compras, cuotas y pagos.                   |
| Se añade cuota.fecha_pago mediante V1 y un índice de usuario.codigo mediante V2; no se aplicaron a una base real.    |
