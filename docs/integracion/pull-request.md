# Integrar cliente y negocio sobre la arquitectura de Manuel

Las ramas tenían rutas y estilos incompatibles, pantallas cliente sin reportes HTTP y un módulo
de negocio conectado a mocks. La integración conserva layouts/features/tokens de Manuel,
adapta el cliente y conecta cuentas, catálogo, compras y cuotas a contratos Spring reales.
El registro de compra ahora guarda ítems, cronograma francés y deuda en una transacción.
La sesión del servidor protege recursos por rol y propietario; BCrypt cubre registros nuevos,
cambios de contraseña y migración al autenticar credenciales legadas.
Incluye migraciones manuales no destructivas, manual de ejecución y CI.
Validación: build Angular, 21 pruebas frontend, 12 backend (Spring/H2/HTTP) y Chrome a dos tamaños.
Antes de desplegar: validar migraciones y flujos contra una copia de PostgreSQL, revisar reglas
financieras documentadas y configurar secretos/origen HTTPS. No se recalculan datos históricos.
Rama local: integracion/alvaro-manuel. No se hizo push ni merge a main.
