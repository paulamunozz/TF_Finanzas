# Conflictos de Git

Fetch de origin completado; árbol inicialmente limpio; nueva rama creada desde origin/manuel.
Merge controlado: git merge --no-commit --no-ff origin/alvaro.
Se detectaron exactamente cuatro conflictos:

1. app.config.ts: se conservaron los contratos de repositorio de Manuel y se añadió el interceptor de sesión.
   La configuración final usa HttpClientRepository y HttpPaymentRepository; mocks quedan para pruebas.
2. app.html: ambas ramas usaban router-outlet; se dejó una única salida.
3. app.routes.ts: se fusionaron rutas por rol con carga diferida, layouts de Manuel y cliente de Álvaro.
4. styles.css: conflicto modify/delete; se conserva SCSS como entrada global y estilos.css queda como
   archivo de compatibilidad sin reglas activas. No se pierde ninguna regla necesaria.
   Las páginas y servicios se movieron a features/client y core/services; no se seleccionó una rama
   completa para resolver conflictos. Se conservan rutas de negocio y alias de cronogramas.
   Se conserva el historial de ambas ramas mediante commit de merge. No se modifican las ramas
   main, manuel ni alvaro. No se publica la integración ni se fuerza el historial remoto.
