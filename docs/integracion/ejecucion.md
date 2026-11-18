# Ejecución

Requisitos: JDK 17 completo, Node compatible con Angular 22 y npm; PostgreSQL y cliente psql.
Maven Wrapper está en Cuentix. En este equipo se usó Node 26.13.0 y JDK temurin-17.0.21.

## Base de datos

Usar una base local de desarrollo y un usuario propio con permisos para el esquema.
Las migraciones son SQL versionado manual; no se ejecutan automáticamente.

- Base vacía: ejecutar V0, V1 y V2 en ese orden desde Cuentix/src/main/resources/db/migration.
- Base existente: respaldar, revisar duplicados de usuario.codigo (consulta en V2), ejecutar V1 y V2.
  No ejecutar V0 sobre una base existente. No se reconcilian duplicados automáticamente.
  Ejemplo: psql -h localhost -U <usuario> -d db_cuentix -v ON_ERROR_STOP=1 -f <archivo.sql>.
  Las migraciones PostgreSQL no se ejecutaron en este entorno. Validarlas primero sobre una copia.
  No existe Flyway instalado. Registrar las versiones aplicadas en el control de cambios de la base.
  Hibernate usa validate: no crea, elimina ni actualiza tablas durante el arranque.
  Los datos de prueba viven exclusivamente en H2 bajo src/test; V0 solo incorpora catálogos de referencia.

## Spring Boot

Desde Cuentix, PowerShell:

```powershell
$env:JAVA_HOME='C:
utaaljdk-17'
$env:DB_URL='jdbc:postgresql://localhost:5432/db_cuentix'
$env:DB_USERNAME='<usuario>'
$env:DB_PASSWORD='<contraseña local>'
.mvnw.cmd spring-boot:run
```

Backend: http://127.0.0.1:8080. No guardar valores reales en Git.
En HTTPS establecer SESSION_COOKIE_SECURE=true. Desplegar Angular y /api bajo el mismo origen;
el proxy de desarrollo no forma parte del build de producción. No se habilita CORS abierto.

## Angular

Desde Cuentix-front:

```powershell
npm.cmd ci --no-audit --no-fund
npm.cmd start
```

Abrir http://localhost:4200/auth/login. El proxy /api apunta al puerto 8080.
Registrar un cliente en /auth/register. Para un negocio nuevo usar POST /registrarNegocio
con el DTO NegocioDTO existente (RUC, contraseña, razón social, dirección, teléfono y giroId válido).
Los roles mantienen los IDs originales: 1 administrador, 2 cliente, 3 negocio.
El formulario de cuentas vincula un DNI ya registrado; no crea identidades ni contraseñas de terceros.
La identidad personal se edita desde el perfil del cliente, no desde el negocio.
El catálogo exige una unidad de medida configurada. Cuenta y cuotas se consultan por sesión.

## Pruebas

```powershell
# Desde Cuentix
.mvnw.cmd -B test
# Desde Cuentix-front
npm.cmd run build
npm.cmd test -- --watch=false
# Con ng serve activo y Playwright disponible:
node scripts/browser-smoke.cjs
```

Para instalar Playwright sin modificar el proyecto:
npm.cmd exec --yes --package=@playwright/test -- playwright --version.
Asignar PLAYWRIGHT_MODULE al directorio node_modules/playwright de esa instalación, o instalarlo
localmente con npm install --no-save --package-lock=false playwright. Usa Chrome ya instalado.
No se requieren credenciales reales para las pruebas: H2 y fixtures HTTP están aislados.
