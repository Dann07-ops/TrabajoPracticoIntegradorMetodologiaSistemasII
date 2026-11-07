# Diagnóstico de calidad — Estado inicial

**Sistema:** Gestión de Solicitudes Académicas
**Versión analizada:** tag `estado-inicial` (commit `bbf661d`)
**Autor:** Daniel Alderete

Este documento cubre las Etapas 2 (calidad externa vs. interna) y 3 (deuda técnica y code smells) del TPI. Todas las ubicaciones se refieren a la versión del tag `estado-inicial`.

---

## 1. Método de análisis

El diagnóstico combinó tres fuentes:

1. **Revisión manual del código**, clase por clase, buscando los smells del catálogo de Fowler.
2. **Tests de caracterización** (`SistemaSolicitudesCaracterizacionTest`, 23 tests). Se escribieron *antes* de diagnosticar, para registrar el comportamiento actual. Varios hallazgos surgieron de ellos, como la validación débil del email (S16).
3. **Métricas simples** de tamaño: líneas por clase y por método, cantidad de parámetros y lugares que hay que tocar para hacer un cambio.

### Escalas utilizadas

| Criterio | Alto | Medio | Bajo |
|---|---|---|---|
| **Impacto** (costo de cambiar el código) | Un cambio habitual obliga a tocar varios lugares o entender toda la clase | El cambio es localizado pero confuso | Molestia de lectura, sin costo real de cambio |
| **Riesgo** (qué puede salir mal) | Pérdida o corrupción de datos, comportamiento incorrecto sin aviso | Errores probables al modificar el código | Riesgo muy bajo de defecto |

**Prioridad:**
- **Alta:** el riesgo es alto, o el impacto es alto y además bloquea otras mejoras.
- **Media:** el impacto o el riesgo es medio.
- **Baja:** impacto y riesgo bajos.

---

## 2. Calidad externa vs. calidad interna

### Calidad externa (lo que percibe el usuario)

| Aspecto | Resultado | Evidencia |
|---|---|---|
| Las 5 operaciones funcionan (registrar, cambiar estado, asignar, buscar/filtrar, reporte) | ✅ Correcto | 23 tests en verde; ejecución manual del menú |
| Valida los datos obligatorios | ✅ Correcto en general | Tests de la sección 1 |
| No se cae con entradas incorrectas (letras donde van números, opciones inexistentes) | ✅ Correcto | Ejecución manual del menú |
| Los datos persisten entre ejecuciones | ✅ Correcto en condiciones normales | Test `registrarPersisteEnElArchivo` |
| El reporte informa las solicitudes vencidas | ✅ Correcto | Test `GOLDEN MASTER` |

**Defectos externos latentes**, que el usuario no ve en el uso normal:

- Si el archivo no se puede escribir, el sistema informa "Solicitud registrada" pero el dato se pierde (S01).
- Acepta emails mal formados como `ana.perez@mailtest` (S16).
- Permite transiciones de estado sin sentido, como RECHAZADA → PENDIENTE (S15).

### Calidad interna (lo que sufre el equipo técnico)

Desde afuera, el sistema funciona. Por dentro, el costo de cambiarlo es alto. Dos ejemplos medidos:

| Cambio habitual | Lugares que hay que tocar |
|---|---|
| Agregar un tipo de solicitud nuevo (ej. `PASE_DE_CARRERA`) | 4 lugares en 3 archivos: validación (`SistemaSolicitudes` 55-56), plazos (`SistemaSolicitudes` 209-216), menú (`Main` 42) y comentario (`Solicitud` 9) |
| Agregar un campo nuevo al CSV | 3 lugares, y si se olvida uno el archivo queda inconsistente: escritura (84-89), reescritura (295-299) y lectura (266-276) |

**Métricas de tamaño:**

| Elemento | Tamaño |
|---|---|
| `SistemaSolicitudes` | 305 líneas, 6 responsabilidades distintas |
| `SistemaSolicitudes.registrarSolicitud` | 60 líneas, 7 parámetros |
| `SistemaSolicitudes.generarReporte` | 50 líneas |
| `Main.main` | 133 líneas |
| Constructor de `Solicitud` | 8 parámetros |

**Conclusión:** el sistema tiene buena calidad externa y mala calidad interna. Hoy cumple, pero cada cambio futuro es caro y riesgoso. Esa diferencia es la **deuda técnica**.

---

## 3. Deuda técnica

La deuda de este sistema es **deliberada**: el prototipo se construyó con los atajos típicos de un desarrollo apurado (todo en una clase, strings sueltos, errores ignorados) para poder diagnosticarlos y corregirlos.

Su "interés" se paga en cada cambio:

- **Rigidez:** una regla de negocio está repartida en varios lugares (los ejemplos de la sección 2).
- **Fragilidad:** los cambios rompen cosas en lugares no relacionados, como el formato duplicado del CSV.
- **Baja testeabilidad:** el reporte depende de la fecha del sistema y la lógica imprime en consola. Los tests tuvieron que usar fechas relativas para poder controlarlo.

---

## 4. Inventario de problemas

| ID | Problema | Ubicación principal | Impacto | Riesgo | Prioridad |
|---|---|---|---|---|---|
| S01 | Manejo de errores silencioso | `SistemaSolicitudes` 90, 283, 302 | Medio | **Alto** | **Alta** |
| S02 | God Class | `SistemaSolicitudes` (clase entera) | **Alto** | Medio | **Alta** |
| S03 | Long Method | `SistemaSolicitudes.registrarSolicitud` 36-95 | **Alto** | Medio | **Alta** |
| S04 | Long Parameter List | `registrarSolicitud` 36-37; `Solicitud` 16-18 | Medio | Medio | **Alta** |
| S05 | Feature Envy | `generarReporte` 207-225 | Medio | Medio | **Alta** |
| S06 | Condicional por tipo | `generarReporte` 209-220 | **Alto** | Medio | Media |
| S07 | Primitive Obsession | `Solicitud` 9, 10, 12; `Responsable` 7 | **Alto** | **Alto** | **Alta** |
| S08 | Código duplicado: búsquedas | `SistemaSolicitudes` 101-106, 125-130, 157-162, 136-140, 270-274 | Medio | Medio | Media |
| S09 | Código duplicado: formato CSV | `SistemaSolicitudes` 84-89 y 295-299 | Medio | **Alto** | **Alta** |
| S10 | Dependencia concreta de la persistencia | `SistemaSolicitudes` 83, 260, 289 | **Alto** | Bajo | Media |
| S11 | Números y textos mágicos | `SistemaSolicitudes` 21, 26-29, 69, 88, 210-219, 266-276 | Medio | Medio | Media |
| S12 | Mezcla de interfaz y lógica | `SistemaSolicitudes` (14 `System.out`) | Medio | Bajo | Media |
| S13 | Nombres poco descriptivos | `SistemaSolicitudes` 15-16, 101, 136, 169-172, 266 | Medio | Bajo | Media |
| S14 | Encapsulamiento roto | `Responsable` 5-7; `SistemaSolicitudes` 243-249 | Medio | Medio | Media |
| S15 | Modelo anémico / transiciones sin reglas | `Solicitud` 73 | Medio | Medio | Media |
| S16 | Validación insuficiente del email | `SistemaSolicitudes` 51 | Bajo | Medio | Media |
| S17 | Regla de negocio en la interfaz | `Main` 43, 67, 101, 103 | Medio | Medio | Media |
| S18 | Duplicación y método largo en el menú | `Main` 8-140 | Medio | Bajo | Baja |
| S19 | Dependencia oculta del reloj | `SistemaSolicitudes` 78, 221 | Medio | Bajo | Media |
| S20 | Datos del estudiante duplicados por solicitud | `solicitudes.csv`; `cargar()` 267 | Bajo | Medio | Baja |

---

## 5. Fichas de detalle

### S01 — Manejo de errores silencioso · Prioridad ALTA

**Ubicación:** `SistemaSolicitudes.registrarSolicitud` línea 90, `cargar()` línea 283, `guardar()` línea 302.

**Evidencia:**
```java
} catch (Exception ex) {
}
System.out.println("Solicitud registrada con id " + s.getId());
```
Además, los errores se comunican con códigos (`-1`, `false`, `null`) que quien llama puede ignorar sin que el compilador avise. Los `close()` están fuera de un *try-with-resources*: si falla la escritura, el archivo queda abierto.

**Comprobado por el test:** `falloDeEscrituraSilencioso`. Con una ruta imposible, el método devuelve `1` (éxito) y el archivo no existe.

**Impacto:** medio. Cuando algo falla no hay rastro, así que diagnosticar un problema cuesta mucho.
**Riesgo:** alto. El usuario cree que guardó y pierde el dato al cerrar el programa. Un error en `cargar()` deja el sistema vacío sin explicar por qué.
**Prioridad: ALTA.** Es el único problema que hoy puede causar pérdida de datos.

**Mejora propuesta:** excepciones propias (por ejemplo `PersistenciaException`), *try-with-resources* y que la interfaz decida cómo mostrar el error.

---

### S02 — God Class · Prioridad ALTA

**Ubicación:** `SistemaSolicitudes`, 305 líneas.

**Evidencia:** la clase tiene al menos seis razones distintas para cambiar:

1. Reglas de validación (líneas 38-72).
2. Creación de solicitudes (74-79).
3. Lectura y escritura del CSV (253-305).
4. Búsquedas y filtros (156-187).
5. Cálculo de plazos y vencimientos (207-225).
6. Formato del reporte y mensajes al usuario (228-240 y los `System.out`).

**Impacto:** alto. Cualquier cambio obliga a entender una clase de 300 líneas, y casi todos los demás smells viven acá.
**Riesgo:** medio. Al modificar una responsabilidad es fácil romper otra.
**Prioridad: ALTA.**

**Principio afectado:** SRP (Responsabilidad Única).
**Mejora propuesta:** Extract Class. Separar un repositorio (persistencia), un validador, un servicio (operaciones) y un generador de reportes.

---

### S03 — Long Method · Prioridad ALTA

**Ubicación:** `SistemaSolicitudes.registrarSolicitud`, líneas 36-95 (60 líneas).

**Evidencia:** el método tiene tres bloques separados por comentarios: `// validar datos del estudiante`, `// crear la solicitud` y `// guardar en el archivo`. Cada comentario está nombrando un método que todavía no existe.

**Impacto:** alto. Para cambiar una sola regla de validación hay que leer los 60 renglones.
**Riesgo:** medio.
**Prioridad: ALTA.** Además es el punto de partida natural del refactoring.

**Refactoring:** **Extract Method** (obligatorio). Por ejemplo `validarEstudiante(...)`, `validarDatosSolicitud(...)` y `persistir(...)`.

---

### S04 — Long Parameter List · Prioridad ALTA

**Ubicación:** `registrarSolicitud` líneas 36-37 (7 parámetros) y el constructor de `Solicitud` líneas 16-18 (8 parámetros).

**Evidencia:**
```java
public int registrarSolicitud(String nombre, String apellido, String legajo, String email,
                              String tipo, int prioridad, String descripcion)
```
Cinco de los siete parámetros son `String`. Si alguien intercambia `legajo` y `email` al llamar al método, el compilador no lo detecta. Los cuatro primeros siempre viajan juntos: son los datos de un estudiante.

**Impacto:** medio. **Riesgo:** medio (confusiones al llamar).
**Prioridad: ALTA.** Es requisito de la consigna y simplifica S03.

**Refactoring:** **Introduce Parameter Object** (obligatorio). Por ejemplo un `NuevaSolicitud` o `DatosEstudiante` que agrupe los datos.

---

### S05 — Feature Envy · Prioridad ALTA

**Ubicación:** `SistemaSolicitudes.generarReporte`, líneas 207-225.

**Evidencia:** el cálculo de "¿está vencida?" usa `getTipo()`, `getPrioridad()`, `getFechaCreacion()` y `getEstado()` de `Solicitud`, y ningún dato propio de `SistemaSolicitudes`. El método "envidia" los datos de otra clase.

**Impacto:** medio. La regla de vencimiento no se puede reutilizar: si otra pantalla necesita saber si una solicitud venció, hay que copiar el bloque.
**Riesgo:** medio.
**Prioridad: ALTA.** Es requisito de la consigna.

**Refactoring:** **Move Method** (obligatorio). Mover el cálculo a `Solicitud`, como `estaVencida(LocalDate hoy)`. Recibir la fecha como parámetro resuelve, de paso, S19.

---

### S06 — Condicional por tipo · Prioridad MEDIA

**Ubicación:** `generarReporte`, líneas 209-220.

**Evidencia:** una cadena `if / else if` asigna los días de plazo (5, 30, 10, 15) según el texto del tipo. El último `else` captura "cualquier otro tipo": un tipo nuevo recibiría 15 días sin que nadie lo decida.

**Impacto:** alto. Agregar un tipo obliga a modificar este método (ver la sección 2).
**Riesgo:** medio.
**Prioridad: MEDIA.** Se resuelve casi solo al corregir S07.

**Principio afectado:** OCP (Abierto/Cerrado).
**Mejora propuesta:** Replace Conditional with Polymorphism. Un `enum TipoSolicitud` donde cada valor conoce su plazo.

---

### S07 — Primitive Obsession · Prioridad ALTA

**Ubicación:** `Solicitud` líneas 9, 10 y 12; `Responsable` línea 7. Los mismos textos aparecen escritos a mano en `SistemaSolicitudes` 55-56, 111-112, 146, 197-205 y 222.

**Evidencia:**
```java
private String tipo;      // "CERTIFICADO", "EQUIVALENCIA", "REVISION_NOTA", "BAJA"
private int prioridad;    // 1 = alta, 2 = media, 3 = baja
private String estado;    // "PENDIENTE", "EN_PROCESO", "RESUELTA", "RECHAZADA"
```
Los comentarios explican reglas que el código no hace cumplir: `setPrioridad(47)` o `setEstado("Pendiente")` compilan sin problemas.

**Impacto:** alto. Los valores válidos están repartidos en muchos lugares.
**Riesgo:** alto. Un error de tipeo (`"PENDIETE"`) produce un comportamiento incorrecto sin ningún aviso.
**Prioridad: ALTA.**

**Mejora propuesta:** Replace Type Code with Enum: `TipoSolicitud`, `Estado`, `Prioridad` y `Area`.

---

### S08 — Código duplicado: búsquedas · Prioridad MEDIA

**Ubicación:** la búsqueda de solicitud por id está repetida en las líneas 101-106, 125-130 y 157-162. La búsqueda de responsable por id está en 136-140 y 270-274.

**Evidencia:** el mismo bucle `for` con `x = lista.get(i)` copiado tres veces. Además, el bucle no se detiene al encontrar el elemento.

**Impacto:** medio. **Riesgo:** medio. Si se corrige una copia, las otras quedan con el defecto.
**Prioridad: MEDIA.**

**Refactoring:** Extract Method. `cambiarEstado` y `asignarResponsable` pueden reutilizar `buscarPorId`.

---

### S09 — Código duplicado: formato CSV · Prioridad ALTA

**Ubicación:** `registrarSolicitud` líneas 84-89 y `guardar()` líneas 295-299.

**Evidencia:** la línea del CSV se arma con la misma concatenación en dos métodos. Hay una diferencia sutil: en `registrarSolicitud` el responsable se escribe como `0` fijo, y en `guardar()` se calcula.

**Impacto:** medio.
**Riesgo:** alto. Si se agrega un campo en un lugar y no en el otro, el archivo queda con líneas de distinto formato y `cargar()` falla, en silencio por S01.
**Prioridad: ALTA.**

**Refactoring:** Extract Method (`aLineaCsv(Solicitud)`) dentro del futuro repositorio.

---

### S10 — Dependencia concreta de la persistencia · Prioridad MEDIA

**Ubicación:** `SistemaSolicitudes` líneas 83, 260 y 289 (`FileWriter`, `BufferedReader`).

**Evidencia:** la lógica de negocio crea directamente los objetos que leen y escriben el archivo. No hay ninguna abstracción entre "qué se guarda" y "cómo se guarda".

**Impacto:** alto. Pasar de CSV a una base de datos obliga a reescribir la clase.
**Riesgo:** bajo.
**Prioridad: MEDIA.**

**Principio afectado:** DIP (Inversión de Dependencias).
**Mejora propuesta:** Extract Interface. Una interfaz `SolicitudRepository` con una implementación `CsvSolicitudRepository`, inyectada por el constructor.

---

### S11 — Números y textos mágicos · Prioridad MEDIA

**Ubicación y evidencia:**
- `"solicitudes.csv"` (línea 21).
- Responsables cargados a mano en el constructor (26-29).
- Largo máximo `200` (69).
- `0` como "sin responsable" (88).
- Plazos `5`, `30`, `10`, `15` y la división `/ 2` (210-219).
- Posiciones `c[0]` a `c[10]` del CSV (266-276).

**Impacto:** medio. Para cambiar un plazo hay que saber dónde buscarlo.
**Riesgo:** medio. `5 / 2` da `2` por división entera, una regla que nadie decidió explícitamente (comprobado en el test `reglasDeVencimiento`).
**Prioridad: MEDIA.**

**Refactoring:** Introduce Constant. Los plazos pasan al enum de S06/S07.

---

### S12 — Mezcla de interfaz y lógica · Prioridad MEDIA

**Ubicación:** 14 llamadas a `System.out.println` dentro de `SistemaSolicitudes`.

**Evidencia:** al ejecutar los tests, la consola se llena de mensajes como "Solicitud registrada con id 1" aunque nadie esté usando el menú (ver captura de la ejecución de tests).

**Impacto:** medio. La lógica no se puede reutilizar desde otra interfaz (web, API).
**Riesgo:** bajo.
**Prioridad: MEDIA.**

**Principio afectado:** SRP.
**Mejora propuesta:** la lógica informa mediante valores de retorno o excepciones, y `Main` decide qué mostrar.

---

### S13 — Nombres poco descriptivos · Prioridad MEDIA

**Ubicación y evidencia:**
- `lista` y `lista2` (15-16): `lista2` contiene responsables.
- `x` (101, 125, 157), `aux` (136, 270), `c` (266).
- `flag` (172).
- `proc(String t, String e, String l)` (169): ni el método ni los parámetros dicen qué hacen.

**Impacto:** medio en `proc`, que obliga a leer el cuerpo para saber qué hace; bajo en el resto.
**Riesgo:** bajo.
**Prioridad: MEDIA.**

**Refactoring:** Rename. Por ejemplo `proc` → `filtrar(tipo, estado, legajo)` y `lista2` → `responsables`.

---

### S14 — Encapsulamiento roto · Prioridad MEDIA

**Ubicación:** `Responsable` líneas 5-7 (atributos `public`); `SistemaSolicitudes.getLista()` y `getResponsables()` (243-249).

**Evidencia:** cualquier clase puede hacer `responsable.area = "cualquier cosa"`. Además, `getLista()` devuelve la lista interna: quien la recibe puede borrar o agregar solicitudes sin pasar por las validaciones ni guardar en el archivo.

**Impacto:** medio. **Riesgo:** medio.
**Prioridad: MEDIA.**

**Mejora propuesta:** atributos privados y devolver copias no modificables (`List.copyOf`).

---

### S15 — Modelo anémico / transiciones sin reglas · Prioridad MEDIA

**Ubicación:** `Solicitud` (clase entera); `setEstado` en la línea 73.

**Evidencia:** `Solicitud` solo guarda datos, sin ningún comportamiento. Cualquier transición de estado es válida. Comprobado por el test `transicionSinRestricciones`: una solicitud RECHAZADA puede volver a PENDIENTE.

**Impacto:** medio. **Riesgo:** medio.
**Prioridad: MEDIA.**

**Mejora propuesta:** darle comportamiento a `Solicitud` (S05). Las reglas de transición en el `enum Estado` quedan como mejora futura, porque cambiarían el comportamiento externo.

---

### S16 — Validación insuficiente del email · Prioridad MEDIA

**Ubicación:** `SistemaSolicitudes` línea 51.

**Evidencia:**
```java
if (email == null || !email.contains("@") || !email.contains(".")) {
```
Busca un punto en *cualquier* posición. `ana.perez@mailtest` pasa la validación porque el punto está antes de la arroba. **Este problema no fue sembrado intencionalmente: apareció al escribir los tests de caracterización** (test `validacionDeEmailDebil`).

**Impacto:** bajo.
**Riesgo:** medio. Se guardan datos de contacto inválidos. La validación de entradas es además una defensa básica de seguridad.
**Prioridad: MEDIA.**

**Mejora propuesta:** extraer la validación a un método con nombre propio (`esEmailValido`). Endurecer la regla cambia el comportamiento externo, así que se hará como un cambio declarado y separado del refactoring.

---

### S17 — Regla de negocio en la interfaz · Prioridad MEDIA

**Ubicación:** `Main` líneas 43, 67, 101 y 103 (`toUpperCase()`).

**Evidencia:** la regla "el tipo y el estado no distinguen mayúsculas" la aplica el menú, no la lógica. Comprobado por el test `tipoEnMinusculasSeRechaza`: llamando directamente a la lógica, `"certificado"` se rechaza.

**Impacto:** medio. Cada nueva interfaz tiene que acordarse de repetir la regla.
**Riesgo:** medio.
**Prioridad: MEDIA.**

**Mejora propuesta:** que la conversión de texto a enum (S07) la haga el modelo, por ejemplo con un `TipoSolicitud.desdeTexto(...)`.

---

### S18 — Duplicación y método largo en el menú · Prioridad BAJA

**Ubicación:** `Main.main`, líneas 8-140 (133 líneas).

**Evidencia:**
- El bloque `try { Integer.parseInt(...) } catch` se repite 5 veces (líneas 26, 47, 61, 77 y 88).
- La impresión de una solicitud está duplicada (líneas 111 y 124).
- Las opciones del `switch` son números mágicos.

**Impacto:** medio. **Riesgo:** bajo.
**Prioridad: BAJA.** Afecta solo a la capa de presentación.

**Refactoring:** Extract Method (`leerEntero(mensaje)`, `imprimir(Solicitud)`).

---

### S19 — Dependencia oculta del reloj · Prioridad MEDIA

**Ubicación:** `SistemaSolicitudes` líneas 78 y 221 (`LocalDate.now()`).

**Evidencia:** el resultado del reporte cambia según el día en que se ejecuta, aunque los datos sean los mismos. Los tests de caracterización tuvieron que usar fechas relativas (`minusDays`) para poder controlarlo.

**Impacto:** medio (testeabilidad). **Riesgo:** bajo.
**Prioridad: MEDIA.**

**Mejora propuesta:** recibir la fecha como parámetro (ver S05) o inyectar un `java.time.Clock`.

---

### S20 — Datos del estudiante duplicados por solicitud · Prioridad BAJA

**Ubicación:** formato de `solicitudes.csv`; `cargar()` línea 267.

**Evidencia:** cada solicitud guarda nombre, apellido y email del estudiante. En el dataset de prueba, el legajo 90001 aparece en dos líneas. Nada impide que el mismo legajo tenga dos nombres distintos.

**Impacto:** bajo. **Riesgo:** medio (datos inconsistentes).
**Prioridad: BAJA.** Corregirlo exige cambiar el formato del archivo.

**Mejora propuesta:** queda como **mejora futura**: un registro propio de estudiantes, identificados por legajo.

---

## 6. Plan de corrección

| Orden | Problemas | Refactoring / técnica | Requisito de la consigna |
|---|---|---|---|
| 1 | S03 | Extract Method en `registrarSolicitud` | ✅ Extract Method |
| 2 | S04 | Introduce Parameter Object | ✅ Introduce Parameter Object |
| 3 | S05, S19 | Move Method: `estaVencida(hoy)` a `Solicitud` | ✅ Move Method |
| 4 | S07, S06, S11, S17 | Replace Type Code with Enum + polimorfismo de plazos | — |
| 5 | S08, S09, S13 | Extract Method + Rename | — |
| 6 | S02, S10, S01, S12 | Extract Class + Extract Interface (repositorio) + excepciones | — |
| 7 | S14 | Encapsulamiento | — |

**Fuera del alcance (mejoras futuras):** S15 (reglas de transición), S16 (endurecer la validación de email), S18 (menú) y S20 (registro de estudiantes). Las tres primeras cambian el comportamiento externo, así que no son refactorings puros. S20 requiere migrar el formato del archivo.

**Criterio de seguridad:** después de cada paso se ejecutan los 23 tests de caracterización y se hace un commit independiente.
