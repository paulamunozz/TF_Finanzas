# Contratos de integración

Todas las rutas /api funcionan en el mismo origen. Cookie JSESSIONID HttpOnly; token de sesión
en X-Cuentix-CSRF para escrituras. La sesión del servidor decide identidad, rol y propiedad.

| Ruta                                                                                              | Método         | Uso                                     | Autorización                               |
| ------------------------------------------------------------------------------------------------- | -------------- | --------------------------------------- | ------------------------------------------ |
| /api/auth/login                                                                                   | POST           | Credenciales y sesión nueva             | Público                                    |
| /api/auth/register                                                                                | POST           | Registro cliente con BCrypt             | Público                                    |
| /api/auth/session                                                                                 | GET            | Recuperar identidad y token actuales    | Sesión activa                              |
| /api/logout                                                                                       | POST           | Invalidar sesión                        | Sesión y CSRF                              |
| /api/cliente/mi-perfil                                                                            | GET/PUT        | Perfil propio                           | Cliente, CSRF en PUT                       |
| /api/cliente/mi-cuenta                                                                            | DELETE         | Desactivar con contraseña actual        | Cliente y CSRF                             |
| /api/cliente/mis-acuerdos                                                                         | GET            | Acuerdos propios                        | Cliente y token                            |
| /api/cliente/mis-compras                                                                          | GET            | Compras propias                         | Cliente                                    |
| /api/cliente/mis-compras/{id}                                                                     | GET            | Productos/cuotas y mora                 | Propiedad verificada                       |
| /api/business/context                                                                             | GET            | Negocio de la sesión                    | Negocio                                    |
| /api/clients                                                                                      | GET/POST       | Listar/crear cuentas para DNI existente | Negocio propio; administrador puede listar |
| /api/clients/{id}                                                                                 | GET/PUT/DELETE | Consultar/modificar/desactivar cuenta   | Negocio propietario o administrador        |
| /api/catalog                                                                                      | GET/POST       | Catálogo propio                         | Negocio                                    |
| /api/catalog/{id}                                                                                 | PUT/DELETE     | Modificar/desactivar producto           | Propietario o administrador                |
| /api/purchases                                                                                    | GET            | Historial                               | Filtrado por negocio                       |
| /api/purchases/{id}                                                                               | GET            | Compra y productos                      | Propietario o administrador                |
| /api/business/purchases                                                                           | POST           | Compra, ítems, cronograma y deuda       | Propietario y CSRF                         |
| /api/payments                                                                                     | GET            | Cuotas; filtros clientId/purchaseId     | Propiedad de cuenta/compra                 |
| /api/payments/{id}                                                                                | GET            | Detalle cuota                           | Propietario o administrador                |
| /api/payments/{id}/cancel                                                                         | POST           | Cancelar y actualizar deuda             | Propietario y CSRF                         |
| ClientView de Manuel combina identidad y acuerdo. Para representar varias cuentas por persona,    |
| id y los campos clientId de PurchaseView/PaymentView identifican CuentaCredito.id; dni identifica |
| la persona. Esta adaptación evita consolidar monedas o condiciones de cuentas diferentes.         |
| El nombre mostrado siempre procede de Cliente; el negocio no modifica identidad personal.         |
| La tasa compensatoria se proyecta mensual, preservando su equivalencia; la moratoria se presenta  |
| anual. Las tasas históricas de Compra se congelan al confirmar.                                   |
| Los endpoints legados se conservan con autorización. Registrar compra a crédito delega en el      |
| mismo flujo financiero; el identificador del borrador se conserva y no se permite refinalizar.    |
| Registrar negocio sigue disponible en /registrarNegocio con NegocioDTO. No se inventó una         |
| pantalla administrativa que no existía en las ramas inspeccionadas.                               |
