# Riesgos y siguientes validaciones

1. Ejecutar V1/V2 en una copia de PostgreSQL y verificar arranque con validate; V0 solo en base vacía.
2. Revisar con el responsable académico las reglas explícitas de gracia, primera cuota, método
   francés, reserva de línea y mora antes de utilizar importes con personas reales.
3. Los campos históricos monetarios siguen Double por compatibilidad; planificar NUMERIC y
   conciliación mediante otra migración. Cálculos y redondeo usan BigDecimal donde corresponde.
4. Las credenciales históricas incluidas antes de esta integración siguen en el historial Git.
   Rotar esas credenciales; no se reescribe historia compartida. Los archivos actuales usan entorno.
5. Las contraseñas antiguas en claro migran a BCrypt al iniciar sesión correctamente. Para retirar
   todo dato heredado en claro sin esperar al login hace falta un plan de restablecimiento/migración.
6. No había pantallas administrativas completas ni registro visual de negocios en las ramas.
   Se conserva la API de negocios y el rol administrador; no se declara implementado un panel nuevo.
7. El administrador puede consultar/gestionar recursos globales autorizados; el contexto comercial
   de creación requiere una cuenta de negocio. Añadir selector explícito de negocio si se necesita.
8. Añadir paginación y consultas JPA dedicadas: algunos listados filtran propietarios en el servicio
   después de leer entidades. No se exponen al cliente recursos ajenos, pero no se midió rendimiento.
9. Probar concurrencia, conciliación de deuda histórica, recuperación de contraseña y snapshots
   de descripciones de productos. Los ítems existentes siguen relacionados con el catálogo actual.
10. Validar visualmente todos los estados y flujos con PostgreSQL real. Las capturas entregadas
    comprueban un subconjunto de pantallas; no certifican toda la aplicación.
