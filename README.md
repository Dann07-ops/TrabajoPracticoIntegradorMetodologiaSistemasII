# Sistema de Gestión de Solicitudes Académicas

Trabajo Práctico Integrador — **Metodología de Sistemas II**
Tecnicatura Universitaria en Programación a Distancia — UTN
Integrantes: **Daniel Alfredo Oscar Alderete** y **Tadeo Oscar Acosta**

## Descripción

Aplicación de consola que permite registrar solicitudes de estudiantes (certificados, equivalencias, revisiones de nota y bajas), clasificarlas por tipo, prioridad y estado, asignar responsables, consultarlas y generar un reporte simple con las solicitudes vencidas.

El objetivo del TPI no es la cantidad de funcionalidades, sino **mejorar la calidad interna** del sistema con evidencia. Para eso, la versión inicial se construyó con problemas de diseño intencionales, que después se diagnosticaron, se corrigieron mediante refactorings seguros y se verificaron con tests.

## Funcionalidades

1. Registrar una solicitud, con validación de datos.
2. Cambiar el estado de una solicitud.
3. Asignar un responsable. Una solicitud PENDIENTE pasa a EN_PROCESO.
4. Buscar y filtrar por tipo, estado y legajo.
5. Generar un reporte por estado, con las solicitudes vencidas según el plazo de cada tipo.
6. Persistencia en archivo CSV (`solicitudes.csv`).

## Tecnologías

- Java 17 (compilado con JDK 21)
- Maven
- JUnit 5
- IntelliJ IDEA
- Git y GitHub

## Instalación y ejecución

Requisitos: JDK 17 o superior y Maven (o IntelliJ IDEA, que trae Maven integrado).

```bash
git clone https://github.com/Dann07-ops/TrabajoPracticoIntegradorMetodologiaSistemasII.git
cd TrabajoPracticoIntegradorMetodologiaSistemasII
mvn test        # ejecuta los 28 tests
mvn compile     # compila el proyecto
```

Para usar el sistema, ejecutar la clase `ar.edu.utn.tup.solicitudes.Main` desde IntelliJ. El archivo `solicitudes.csv` incluido tiene 10 solicitudes con **datos sintéticos**.

## Estructura

```
src/main/java/ar/edu/utn/tup/solicitudes/
├── Main.java                 Menú de consola
├── SistemaSolicitudes.java   Operaciones y persistencia
├── Solicitud.java            Entidad central (incluye la regla de vencimiento)
├── Estudiante.java           Entidad
├── Responsable.java          Entidad
├── TipoSolicitud.java        Enum de tipos con su plazo
└── DatosSolicitud.java       Parameter Object para registrar
src/test/java/ar/edu/utn/tup/solicitudes/
├── SistemaSolicitudesCaracterizacionTest.java   23 tests de caracterización
└── SolicitudTest.java                           5 tests unitarios
docs/
├── diagnostico.md            Calidad externa/interna, deuda técnica y 20 problemas
├── code-review.md            Checklist de code review y criterios de aceptación
└── evidencias/               Capturas de pantalla
```

## Versiones (tags de Git)

| Tag | Contenido |
|---|---|
| `estado-inicial` | Sistema base, antes del diagnóstico |
| `etapa-diagnostico` | Tests de caracterización y diagnóstico |
| `etapa-refactoring` | Refactorings aplicados |
| `etapa-pruebas` | Tests nuevos sobre el código refactorizado |
| `etapa-docs` | Code review y documentación |
| `estado-refactorizado` | Versión final |

Para comparar el antes y el después: `git diff estado-inicial estado-refactorizado`.

## Mejoras aplicadas

| Commit | Cambio | Problema |
|---|---|---|
| `refactor(S03)` | Extract Method en `registrarSolicitud` | Long Method |
| `refactor(S04)` | Introduce Parameter Object `DatosSolicitud` | Long Parameter List |
| `refactor(S05)` | Move Method `estaVencida` a `Solicitud` | Feature Envy |
| `refactor(S08)` | Extract Method para búsquedas duplicadas | Código duplicado |
| `refactor(S07)` | Enum `TipoSolicitud` con plazos | Primitive Obsession y condicional por tipo (OCP) |
| `fix(S01)` | Informar errores de escritura y lectura | Errores silenciosos (corrección declarada) |

## Uso de IA

Se utilizó Claude (Anthropic) como apoyo y tutoría para diseñar el prototipo, proponer los tests, los refactorings y la documentación. Todo el contenido fue revisado, aplicado y verificado por los integrantes. El registro de interacciones se incluye en el informe y en el anexo ZIP.