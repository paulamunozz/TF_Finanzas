# Refactorización arquitectónica

## Diagnóstico
Angular 22.2 y TypeScript 6.0. El chequeo TypeScript inicial pasó. No hay configuración ESLint. Había vistas y estilos comprimidos dentro de componentes, adaptadores mezclados con servicios, rutas centralizadas y suscripción del formulario sin gestión de destrucción. `strict` y `strictTemplates` no estaban activados.

## Estructura final
```
app/
  core/
    config/          API_BASE_URL (contrato provisional)
    data-access/     adaptadores HTTP, mocks y catálogo de sesión
    models/          modelos canónicos y DTOs
    repositories/    contratos e injection tokens
    services/        coordinación, consultas y contexto de negocio
  features/
    auth/            rutas y páginas públicas
    customers/       formulario, listado, diálogo y rutas
    products/        gestión del catálogo
    purchases/       checkout y rutas
    payment-schedules/ cronogramas, cuota, pago e historial
    dashboard/       resumen financiero
  financial-engine/domain/ funciones financieras puras existentes
  layouts/           dashboard y autenticación
  shared/ui/         encabezado reutilizable
```

## Cambios
- Movidos 38 archivos con actualización de referencias relativas, incluyendo imports dinámicos y pruebas.
- Extraídos HTML y SCSS de componentes con vistas inline; formato consistente mediante Prettier.
- Conservadas URLs existentes y alias; clientes y compras tienen rutas lazy por feature.
- Activadas comprobaciones estrictas TypeScript y Angular sin supresiones.
- Formulario de cliente: controles de tasas tipados, eliminado casting redundante, estados de guardado/error con signals, prevención de envío duplicado y cierre de valueChanges con takeUntilDestroyed.
- Retirados imports Material que ya no se usaban tras adoptar controles nativos.
- Catálogo movido a CatalogStore; una definición CatalogProduct y persistencia de sesión fuera de la página.
- URL base API centralizada. Los endpoints siguen siendo propuestas de adaptador, no un backend disponible.
- Copia reaparecida en private/features/clients/client-list.ts reemplazada por una reexportación compatible; la implementación única vive en features/customers.

## Verificaciones
Ejecutar `npm.cmd exec tsc -- --noEmit -p tsconfig.app.json`, el equivalente para tsconfig.spec.json, `npm.cmd run build` y `npm.cmd test -- --watch=false`.
Las pruebas existentes cubren navegación y reglas financieras. No se cambiaron fórmulas ni expectativas para realizar la reorganización.

## Límites y próximos pasos
No se dispone de diagnósticos del editor ni navegador conectado para confirmar subrayados o regresiones visuales. ESLint no está instalado/configurado. La autenticación sigue incompleta. El checkout conserva sus datos de demostración y aún no registra compras persistentes ni genera cronogramas. La integración API debe confirmar contratos, errores, autorización y aislamiento en backend antes de cambiar proveedores. No se considera implementado lo que actualmente solo tiene presentación.
