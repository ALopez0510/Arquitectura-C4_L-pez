# Prueba de usabilidad: Consulta vehicular

## 1. Objetivo

Comprobar si una persona sin conocimiento previo del sistema puede verificar un RUC, consultar su vehículo y
entender sus puntos de licencia, y sobre todo si **entiende el aviso** cuando la ANT no responde y el dato viene de la caché.

## 2. Participante y condiciones

| Campo | Valor |
|---|---|
| Participante | (nombre o código, p. ej. P1) |
| Perfil | (edad aproximada, ocupación, ¿conduce?, ¿ha usado la web del SRI o la ANT?) |
| Fecha y hora | |
| Lugar / dispositivo | (laptop, navegador, resolución) |
| Facilitador | Alexander López |
| Duración total | |

Condiciones: backend con `ANT_MODO=simulado` y estado **Disponible** al inicio. El facilitador no ayuda salvo que
el participante quede bloqueado más de 2 minutos (se registra como "asistido").

**Guion de inicio (leer en voz alta):** "Vamos a probar el sistema, no a usted. Piense en voz alta mientras lo usa:
diga lo que busca, lo que espera que pase y lo que le confunde. No hay respuestas incorrectas."

## 3. Tareas

| # | Tarea (lo que se le dice al participante) | Datos | Criterio de éxito |
|---|---|---|---|
| T1 | "Verifique si esta persona está registrada en el SRI." | Correo propio, RUC `1710034065001` | Ve la tarjeta del contribuyente con el nombre |
| T2 | "Ahora intente con este otro RUC y dígame qué pasó." | RUC `1790016919001` | Explica con sus palabras que no es persona natural |
| T3 | "Consulte el vehículo con esta placa." (dictada con guion) | `pbx-1234` | Ve marca JMC y modelo |
| T4 | "¿Cuántos puntos tiene la licencia y cuánto debe pagar?" | — | Dice los puntos y el total pendiente correctos |
| T5 | El facilitador pone la ANT en **Caída (HTTP 502)**. "Actualice los puntos. ¿Puede confiar en este dato? ¿Por qué?" | — | Pulsa Actualizar y explica que el dato es antiguo y desde cuándo |
| T6 | "Consulte otra placa: ABC12." | `ABC12` | Entiende el mensaje de formato y lo corrige o explica el error |

## 4. Registro por tarea

Éxito: **Sí** (sin ayuda) · **Parcial** (con dudas o un error recuperado) · **No** / **Asistido**.
Errores: acciones que no llevan a la meta (clic equivocado, dato mal escrito, volver atrás).
Facilidad (SEQ): 1 = muy difícil … 7 = muy fácil, preguntada al terminar cada tarea.

| Tarea | Éxito | Tiempo (s) | Errores | Facilidad (1-7) | Observaciones y frases textuales |
|---|---|---|---|---|---|
| T1 | | | | | |
| T2 | | | | | |
| T3 | | | | | |
| T4 | | | | | |
| T5 | | | | | |
| T6 | | | | | |

**Resumen:** tasa de éxito = tareas con "Sí" / 6 · tiempo total · errores totales · facilidad promedio.

## 5. Preguntas finales

1. ¿Qué fue lo más confuso?
2. Cuando apareció el aviso amarillo, ¿qué entendió?
3. ¿Cambiaría algo del orden de los pasos?
4. Del 1 al 10, ¿qué tan probable es que lo use para revisar sus puntos?

## 6. Hallazgos y mejoras

Una fila por problema observado. Cada mejora debe ser concreta (qué cambiar y dónde), no general.

| # | Hallazgo (qué pasó) | Evidencia (tarea, tiempo, frase) | Mejora concreta | Prioridad |
|---|---|---|---|---|
| 1 | | | | Alta / Media / Baja |
| 2 | | | | |
| 3 | | | | |

Ejemplos del nivel de detalle esperado (usar solo si se observan):
- *"No encontró el botón Actualizar (T5, 45 s buscando)"* → mover el botón junto al número de puntos y llamarlo "Volver a consultar la ANT".
- *"Pensó que 'Desde caché (vigente)' era un error (T4)"* → cambiar la etiqueta a "Guardado hace X min".

## 7. Evidencia

- Grabación de pantalla o fotos (con consentimiento del participante).
- Capturas de los mensajes que generaron dudas.
