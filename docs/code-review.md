# Code Review — Sistema de Gestión de Solicitudes Académicas

**Alcance:** comparación entre `estado-inicial` y la versión refactorizada.
**Autor de los cambios:** Daniel Alderete · **Revisor:** Tadeo Acosta
**Método:** revisión con checklist sobre el diff completo (`git diff estado-inicial HEAD`) y ejecución de los tests.

---

## 1. Checklist técnico

Leyenda: ✅ cumple · ⚠️ cumple parcialmente · ❌ no cumple

| # | Criterio | Inicial | Final | Evidencia |
|---|---|---|---|---|
| 1 | El sistema compila y todos los tests pasan | ✅ (23) | ✅ (28) | Ejecución de tests |
| 2 | Los métodos tienen una sola tarea y menos de ~30 líneas | ❌ | ⚠️ | `registrarSolicitud` pasó de 60 a 18 líneas; `Main.main` sigue largo |
| 3 | Los métodos tienen 3 parámetros o menos | ❌ | ⚠️ | `registrarSolicitud` pasó de 7 a 1; el constructor de `Solicitud` sigue con 8 |
| 4 | No hay código duplicado | ❌ | ⚠️ | Búsquedas unificadas (S08); el formato CSV sigue duplicado (S09) |
| 5 | Cada clase tiene una sola responsabilidad (SRP) | ❌ | ❌ | `SistemaSolicitudes` sigue mezclando operaciones y persistencia (S02) |
| 6 | Agregar un tipo no obliga a modificar lógica existente (OCP) | ❌ | ✅ | Enum `TipoSolicitud`: se agrega una línea |
| 7 | La lógica depende de abstracciones (DIP) | ❌ | ❌ | Sigue usando `FileWriter` directamente (S10) |
| 8 | Los valores válidos los controla el compilador | ❌ | ⚠️ | El tipo es enum; el estado y el área siguen siendo `String` |
| 9 | Ningún error se ignora en silencio | ❌ | ✅ | `fix(S01)`: los `catch` informan el error |
| 10 | Los recursos se cierran siempre | ❌ | ⚠️ | `try-with-resources` en `agregarAlArchivo`; `cargar` y `guardar` pendientes |
| 11 | Los nombres expresan la intención | ❌ | ⚠️ | Métodos nuevos con nombres claros; siguen `proc`, `lista2`, `x`, `flag` (S13) |
| 12 | La lógica no imprime en consola | ❌ | ❌ | 16 `System.out` en `SistemaSolicitudes` (S12); aumentaron en 2 al informar los errores |
| 13 | Las reglas de negocio se pueden testear en forma aislada | ❌ | ✅ | `Solicitud.estaVencida(hoy)` se prueba con fechas fijas |
| 14 | Atributos privados y colecciones protegidas | ❌ | ❌ | `Responsable` con campos públicos; `getLista()` expone la lista (S14) |
| 15 | Commits pequeños, con mensaje claro y un cambio por commit | — | ✅ | Un commit por refactoring, con prefijo `refactor` o `fix` |

**Resultado:** de 14 criterios aplicables al código, el estado inicial cumplía 1. La versión final cumple 5 y cumple parcialmente 6.

---

## 2. Observaciones y feedback

Las observaciones se redactan sobre el código, no sobre la persona, con el problema, su impacto y una propuesta concreta.

**O1 — Manejo de errores (corregida).**
"El `catch` vacío en `registrarSolicitud` hace que el sistema confirme el registro aunque no se haya guardado. Propongo informar el error y deshacer el alta en memoria." → Corregido en `fix(S01)`, verificado con el test `falloDeEscrituraSeInforma`.

**O2 — Plazos por tipo (corregida).**
"Los plazos están en un `if/else` lejos de los tipos, y cualquier tipo desconocido recibe 15 días sin que nadie lo haya decidido. Propongo un enum con el plazo en cada valor." → Corregido en `refactor(S07)`.

**O3 — Formato CSV duplicado (pendiente).**
"La línea del CSV se arma igual en `agregarAlArchivo` y en `guardar`. Si se agrega un campo en un lugar y no en el otro, el archivo queda inconsistente. Propongo un único método `aLineaCsv(Solicitud)`." → Pendiente: se resolverá al extraer el repositorio.

**O4 — God Class y dependencia del archivo (pendiente).**
"`SistemaSolicitudes` sigue validando, operando y persistiendo. Propongo extraer una interfaz `SolicitudRepository` con una implementación CSV, inyectada por el constructor." → Pendiente: mejora futura (S02, S10).

**O5 — Nombres (pendiente).**
"`proc(t, e, l)` obliga a leer el cuerpo para entender qué hace. Propongo `filtrar(tipo, estado, legajo)`." → Pendiente: es un Rename de bajo riesgo, porque los tests acceden mediante el auxiliar `filtrar`.

**O6 — Test con llamada directa (observación sobre los tests).**
"En `falloDeEscrituraSeInforma` se llama a `registrarSolicitud` sin pasar por el auxiliar `registrar(...)`, a diferencia del resto de los tests. Por eso tuvo que modificarse en el refactoring S04." → Aceptada como mejora menor de los tests.

---

## 3. Correcciones realizadas

| Commit | Corrección | Verificación |
|---|---|---|
| `refactor(S03)` | Extract Method | 23 tests en verde |
| `refactor(S04)` | Introduce Parameter Object | 23 tests en verde (se adaptó solo el auxiliar del test) |
| `refactor(S05)` | Move Method | 23 tests en verde, sin modificar tests |
| `refactor(S08)` | Eliminación de búsquedas duplicadas | 23 tests en verde, sin modificar tests |
| `refactor(S07)` | Enum `TipoSolicitud` | 23 tests en verde, sin modificar tests |
| `fix(S01)` | Manejo de errores | El test de caracterización falló a propósito y se actualizó; 23 en verde |

---

## 4. Criterios de aceptación

Un cambio se acepta si cumple **todos** estos criterios:

1. **Compila sin errores** y no agrega advertencias nuevas.
2. **Todos los tests pasan.**
3. **Un refactoring no modifica tests**, salvo los métodos auxiliares de adaptación a la API.
4. **Una corrección de comportamiento** (`fix`) se declara como tal y modifica explícitamente el test que la caracterizaba.
5. **El comportamiento externo se mantiene:** el reporte es idéntico (test GOLDEN MASTER) y el menú funciona igual.
6. **Un commit por cambio**, con mensaje que indique el tipo (`refactor`/`fix`) y el problema del diagnóstico (S01, S03...).
7. **El cambio reduce un problema documentado** en `diagnostico.md`.

**Resultado de la revisión:** todos los cambios cumplen los 7 criterios. **Aprobados.**

---

## 5. Pendientes (mejoras futuras)

- S02 / S10: extraer `SolicitudRepository` (SRP + DIP).
- S09: unificar el formato CSV.
- S12: quitar los `System.out` de la lógica.
- S13: renombrar `proc`, `lista2`, `x`, `flag`.
- S14: encapsular `Responsable` y proteger las listas.
- Enum para el estado, con reglas de transición (S15). Cambia el comportamiento, así que va como `fix` declarado.
- S16: endurecer la validación de email (cambia el comportamiento).
- S20: registro propio de estudiantes.