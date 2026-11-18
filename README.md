# Cuentix

## Probar la integración con Docker

Desde la raíz del repositorio, con Docker Desktop en modo de contenedores Linux, ejecuta:

```powershell
docker compose up -d --build --wait
docker compose ps -a
```

Abre [Cuentix local](http://localhost:4200/auth/login). Nginx sirve Angular y reenvía las solicitudes de `/api/` a Spring. PostgreSQL conserva los datos en el volumen de Compose. El contenedor `demo` configura las cuentas iniciales y termina con código 0.

En una copia nueva del repositorio, copia `.env.example` a `.env` y asigna valores distintos a `DB_PASSWORD` y `CUENTIX_DEMO_PASSWORD` antes del primer arranque. En el equipo donde se preparó esta integración ya se generó el archivo local. Para consultar solo la clave de prueba:

```powershell
Select-String '^CUENTIX_DEMO_PASSWORD=' .env
```

| Perfil | Usuario | Prueba |
| --- | --- | --- |
| Administrador | `admin-demo` | Consultar o registrar tiendas |
| Gestor del comercio | `20123456789` | Vincular el DNI de prueba, crear un producto y registrar un crédito |
| Cliente | `12345678` | Consultar el acuerdo y las cuotas del crédito registrado por el gestor |

Los tres usuarios de prueba usan la clave inicial de `CUENTIX_DEMO_PASSWORD`. El alta inicial se ejecuta una vez. Para comprobar el recorrido completo, registra un crédito como gestor y luego abre el reporte del cliente en una ventana privada. Puedes recargar la página para comprobar la persistencia.

```powershell
docker compose logs --tail 80 backend frontend
docker compose down
```

`down` detiene la aplicación y conserva el volumen. Para volver a levantarla usa `docker compose up -d --wait`. Para incorporar cambios de código usa `docker compose up -d --build --wait`. El puerto local se configura con `CUENTIX_PORT`. PostgreSQL aplica las migraciones SQL al crear un volumen vacío. Para actualizar una base existente, aplica únicamente las migraciones pendientes según [el manual de ejecución](docs/integracion/ejecucion.md).

Integración de Manuel y Álvaro en integracion/alvaro-manuel.

- [Diagnóstico y matriz](docs/integracion/diagnostico.md)
- [Manual de ejecución y migraciones](docs/integracion/ejecucion.md)
- [Pruebas y límites](docs/integracion/pruebas.md)
- [Definición de cronogramas](docs/integracion/cronograma-persistente.md)
- [Resumen para Pull Request](docs/integracion/pull-request.md)
