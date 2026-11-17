# Capturas del sistema en ejecución (17/11/2026)

Tomadas con el backend Java (ms-sri y ms-ant) y el frontend React corriendo en local, caché en Redis Cloud
y la ANT en modo simulado (la ANT real responde 502).

| Archivo | Qué muestra | Criterio de la rúbrica |
|---|---|---|
| `01-validacion-campos-obligatorios.jpg` | Validación en la interfaz cuando se envía el formulario vacío | Contrato API / usabilidad |
| `02-error-422-no-es-persona-natural.jpg` | RUC de sociedad → error 422 `NO_ES_PERSONA_NATURAL` con datos reales del SRI | Contrato API |
| `03-contribuyente-persona-natural.jpg` | Paso 1 correcto: datos del contribuyente desde el SRI | React + Java |
| `04-consulta-completa-servicio-externo.jpg` | Flujo completo: contribuyente, vehículo y puntos (origen: consultado ahora) | React + Java |
| `05-segunda-consulta-desde-cache.jpg` | La misma consulta sale de la caché vigente | Caché |
| `06-ant-caida-respaldo-con-aviso.jpg` | ANT caída (502): último dato guardado con aviso de dato desactualizado | Caché y resiliencia |
| `07-ant-lenta-timeout-8s-respaldo.jpg` | ANT lenta: se corta a los 8 s y se usa el respaldo | Caché y resiliencia |
| `08-pruebas-api-codigos-http.jpg` | 13 casos de prueba de la API con su código HTTP (200, 400, 404, 405, 422, 503) | Contrato API |
| `09-respuestas-json-errores-y-respaldo.jpg` | Cuerpos JSON de errores estructurados y del respaldo con `metadatos` | Contrato API / caché |
| `10-evidencia-ant-502-12h30.jpg` | Nueva prueba de la ANT antes de entregar: sigue en 502 | Falla del servicio externo |
