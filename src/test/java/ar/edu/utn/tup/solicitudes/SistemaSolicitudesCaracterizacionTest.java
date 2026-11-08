package ar.edu.utn.tup.solicitudes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de caracterizacion del ESTADO INICIAL del sistema.
 *
 * Describen como se comporta el sistema HOY (incluso sus defectos),
 * para poder comprobar que el refactoring no altera el comportamiento externo.
 *
 * Todas las llamadas al sistema pasan por los metodos auxiliares del final
 * (registrar, filtrar, estadoDe, tipoDe, nombreResponsableDe). Si un refactoring
 * cambia la forma de la API, solo se adaptan esos metodos y no los tests.
 */
class SistemaSolicitudesCaracterizacionTest {

    @TempDir
    Path carpeta;

    private String archivo;
    private SistemaSolicitudes sistema;

    @BeforeEach
    void prepararSistemaVacio() {
        archivo = carpeta.resolve("solicitudes.csv").toString();
        sistema = new SistemaSolicitudes(archivo);
    }

    // =================================================================
    // 1. Registrar solicitud
    // =================================================================

    @Test
    @DisplayName("Registrar una solicitud valida devuelve id 1 y la deja PENDIENTE, sin responsable y con fecha de hoy")
    void registrarSolicitudValida() {
        int id = registrarValida();

        assertEquals(1, id);
        Solicitud s = sistema.buscarPorId(1);
        assertNotNull(s);
        assertEquals("CERTIFICADO", tipoDe(s));
        assertEquals("PENDIENTE", estadoDe(s));
        assertNull(s.getResponsable());
        assertEquals(LocalDate.now(), s.getFechaCreacion());
        assertEquals("90001", s.getEstudiante().getLegajo());
    }

    @Test
    @DisplayName("Los ids son consecutivos y continuan desde el mayor id guardado en el CSV")
    void idsConsecutivosDesdeElCsv() throws IOException {
        escribirCsv(linea(7, "90007", "PENDIENTE", 0, "CERTIFICADO", 2, 0));
        sistema = new SistemaSolicitudes(archivo);

        assertEquals(8, registrarValida());
        assertEquals(9, registrarValida());
    }

    @Test
    @DisplayName("Rechaza nombre vacio, email sin arroba y descripcion vacia (devuelve -1 y no agrega nada)")
    void rechazaDatosObligatoriosInvalidos() {
        assertEquals(-1, registrar("", "Perez", "90001", "ana.perez@mail.test", "CERTIFICADO", 2, "desc"));
        assertEquals(-1, registrar("Ana", "Perez", "90001", "ana.perez.mail.test", "CERTIFICADO", 2, "desc"));
        assertEquals(-1, registrar("Ana", "Perez", "90001", "ana.perez@mail.test", "CERTIFICADO", 2, "   "));
        assertEquals(0, sistema.getLista().size());
    }

    @Test
    @DisplayName("CARACTERIZA DEFECTO: la validacion de email es debil; acepta 'ana.perez@mailtest' (el punto esta antes del @)")
    void validacionDeEmailDebil() {
        assertEquals(1, registrar("Ana", "Perez", "90001", "ana.perez@mailtest", "CERTIFICADO", 2, "desc"));
    }

    @Test
    @DisplayName("CARACTERIZA: el tipo en minusculas se rechaza (la conversion a mayusculas esta en Main, no en la logica)")
    void tipoEnMinusculasSeRechaza() {
        assertEquals(-1, registrar("Ana", "Perez", "90001", "ana.perez@mail.test", "certificado", 2, "desc"));
    }

    @Test
    @DisplayName("La prioridad solo acepta 1, 2 o 3")
    void prioridadFueraDeRango() {
        assertEquals(-1, registrar("Ana", "Perez", "90001", "ana.perez@mail.test", "CERTIFICADO", 0, "desc"));
        assertEquals(-1, registrar("Ana", "Perez", "90001", "ana.perez@mail.test", "CERTIFICADO", 4, "desc"));
        assertEquals(1, registrar("Ana", "Perez", "90001", "ana.perez@mail.test", "CERTIFICADO", 3, "desc"));
    }

    @Test
    @DisplayName("La descripcion no puede tener ';' ni superar los 200 caracteres")
    void descripcionInvalida() {
        assertEquals(-1, registrar("Ana", "Perez", "90001", "ana.perez@mail.test", "CERTIFICADO", 2, "uno;dos"));
        assertEquals(-1, registrar("Ana", "Perez", "90001", "ana.perez@mail.test", "CERTIFICADO", 2, "x".repeat(201)));
        assertEquals(1, registrar("Ana", "Perez", "90001", "ana.perez@mail.test", "CERTIFICADO", 2, "x".repeat(200)));
    }

    // =================================================================
    // 2. Persistencia en CSV
    // =================================================================

    @Test
    @DisplayName("Una solicitud registrada se recupera al crear un sistema nuevo sobre el mismo archivo")
    void registrarPersisteEnElArchivo() {
        registrarValida();

        SistemaSolicitudes recargado = new SistemaSolicitudes(archivo);

        assertEquals(1, recargado.getLista().size());
        Solicitud s = recargado.buscarPorId(1);
        assertEquals("Perez, Ana (90001)", s.getEstudiante().toString());
        assertEquals("CERTIFICADO", tipoDe(s));
        assertEquals(2, s.getPrioridad());
        assertEquals("Constancia de alumna regular", s.getDescripcion());
    }

    @Test
    @DisplayName("Los cambios de estado y de responsable tambien se persisten")
    void cambiosSePersisten() {
        registrarValida();
        registrarValida();
        sistema.asignarResponsable(1, 2);
        sistema.cambiarEstado(2, "RECHAZADA");

        SistemaSolicitudes recargado = new SistemaSolicitudes(archivo);

        assertEquals("EN_PROCESO", estadoDe(recargado.buscarPorId(1)));
        assertEquals("Martin Ruiz", nombreResponsableDe(recargado.buscarPorId(1)));
        assertEquals("RECHAZADA", estadoDe(recargado.buscarPorId(2)));
        assertNull(recargado.buscarPorId(2).getResponsable());
    }

    @Test
    @DisplayName("Si el archivo no existe, el sistema arranca vacio y sin errores")
    void archivoInexistente() {
        assertFalse(Files.exists(Path.of(archivo)));
        assertEquals(0, sistema.getLista().size());
    }

    @Test
    @DisplayName("CORREGIDO (S01): si no se puede escribir el archivo, informa el error y no registra la solicitud")
    void falloDeEscrituraSeInforma() {
        String rutaImposible = carpeta.resolve("no-existe").resolve("solicitudes.csv").toString();
        SistemaSolicitudes conRutaImposible = new SistemaSolicitudes(rutaImposible);

        int id = conRutaImposible.registrarSolicitud(
                new DatosSolicitud("Ana", "Perez", "90001", "ana.perez@mail.test", "CERTIFICADO", 2, "desc"));

        assertEquals(-1, id);                                 // informa el fallo
        assertEquals(0, conRutaImposible.getLista().size());  // no queda registrada en memoria
        assertFalse(Files.exists(Path.of(rutaImposible)));
    }

    // =================================================================
    // 3. Cambiar estado
    // =================================================================

    @Test
    @DisplayName("Cambiar a un estado valido devuelve true y actualiza la solicitud")
    void cambiarEstadoValido() {
        registrarValida();

        assertTrue(sistema.cambiarEstado(1, "RESUELTA"));
        assertEquals("RESUELTA", estadoDe(sistema.buscarPorId(1)));
    }

    @Test
    @DisplayName("Cambiar el estado de una solicitud inexistente devuelve false")
    void cambiarEstadoIdInexistente() {
        assertFalse(sistema.cambiarEstado(99, "RESUELTA"));
    }

    @Test
    @DisplayName("Un estado invalido devuelve false y no modifica la solicitud")
    void cambiarEstadoInvalido() {
        registrarValida();

        assertFalse(sistema.cambiarEstado(1, "ARCHIVADA"));
        assertEquals("PENDIENTE", estadoDe(sistema.buscarPorId(1)));
    }

    @Test
    @DisplayName("CARACTERIZA: se permite cualquier transicion, incluso de RECHAZADA a PENDIENTE")
    void transicionSinRestricciones() {
        registrarValida();
        sistema.cambiarEstado(1, "RECHAZADA");

        assertTrue(sistema.cambiarEstado(1, "PENDIENTE"));
        assertEquals("PENDIENTE", estadoDe(sistema.buscarPorId(1)));
    }

    // =================================================================
    // 4. Asignar responsable
    // =================================================================

    @Test
    @DisplayName("Asignar responsable a una solicitud PENDIENTE la pasa a EN_PROCESO")
    void asignarResponsablePasaAEnProceso() {
        registrarValida();

        assertTrue(sistema.asignarResponsable(1, 3));
        Solicitud s = sistema.buscarPorId(1);
        assertEquals("Sofia Diaz", nombreResponsableDe(s));
        assertEquals("EN_PROCESO", estadoDe(s));
    }

    @Test
    @DisplayName("Asignar responsable a una solicitud RESUELTA no cambia su estado")
    void asignarResponsableNoCambiaOtrosEstados() {
        registrarValida();
        sistema.cambiarEstado(1, "RESUELTA");

        assertTrue(sistema.asignarResponsable(1, 1));
        assertEquals("RESUELTA", estadoDe(sistema.buscarPorId(1)));
    }

    @Test
    @DisplayName("Un responsable o una solicitud inexistentes devuelven false y no asignan nada")
    void asignarConIdsInexistentes() {
        registrarValida();

        assertFalse(sistema.asignarResponsable(1, 99));
        assertFalse(sistema.asignarResponsable(99, 1));
        assertNull(sistema.buscarPorId(1).getResponsable());
        assertEquals("PENDIENTE", estadoDe(sistema.buscarPorId(1)));
    }

    // =================================================================
    // 5. Buscar y filtrar
    // =================================================================

    @Test
    @DisplayName("Buscar un id inexistente devuelve null")
    void buscarIdInexistente() {
        assertNull(sistema.buscarPorId(42));
    }

    @Test
    @DisplayName("Filtrar sin criterios (vacios o null) devuelve todas las solicitudes")
    void filtrarSinCriterios() throws IOException {
        cargarCasoDePrueba();

        assertEquals(4, filtrar("", "", "").size());
        assertEquals(4, filtrar(null, null, null).size());
    }

    @Test
    @DisplayName("Filtrar por tipo, y por estado y legajo combinados")
    void filtrarPorCriterios() throws IOException {
        cargarCasoDePrueba();

        assertEquals(2, filtrar("CERTIFICADO", "", "").size());
        List<Solicitud> res = filtrar("", "PENDIENTE", "90001");
        assertEquals(1, res.size());
        assertEquals(1, res.get(0).getId());
    }

    // =================================================================
    // 6. Reporte
    // =================================================================

    @Test
    @DisplayName("Plazos de vencimiento: estricto (>) y con division entera a la mitad para prioridad alta")
    void reglasDeVencimiento() throws IOException {
        escribirCsv(
                linea(1, "90001", "PENDIENTE", 0, "CERTIFICADO", 2, 6),  // plazo 5: 6 > 5 -> vencida
                linea(2, "90002", "PENDIENTE", 0, "CERTIFICADO", 2, 5),  // plazo 5: 5 > 5 es falso -> no vencida
                linea(3, "90003", "PENDIENTE", 0, "CERTIFICADO", 1, 3),  // plazo 5/2 = 2 (entera): 3 > 2 -> vencida
                linea(4, "90004", "RESUELTA", 1, "CERTIFICADO", 2, 90)   // resuelta: nunca vence
        );
        sistema = new SistemaSolicitudes(archivo);

        String reporte = sistema.generarReporte();

        assertTrue(reporte.contains("Vencidas: 2\n"));
        assertTrue(reporte.contains("  - #1 CERTIFICADO"));
        assertTrue(reporte.contains("  - #3 CERTIFICADO"));
        assertFalse(reporte.contains("  - #2 "));
        assertFalse(reporte.contains("  - #4 "));
    }

    @Test
    @DisplayName("GOLDEN MASTER: el texto completo del reporte se mantiene identico")
    void reporteCompleto() throws IOException {
        cargarCasoDePrueba();

        String esperado =
                "===== REPORTE DE SOLICITUDES =====\n"
                        + "Total: 4\n"
                        + "Pendientes: 1\n"
                        + "En proceso: 1\n"
                        + "Resueltas: 1\n"
                        + "Rechazadas: 1\n"
                        + "Vencidas: 2\n"
                        + "  - #1 CERTIFICADO | Apellido90001, Nombre90001 (90001)\n"
                        + "  - #2 EQUIVALENCIA | Apellido90002, Nombre90002 (90002)\n";

        assertEquals(esperado, sistema.generarReporte());
    }

    // =================================================================
    // Datos de prueba
    // =================================================================

    /**
     * Caso fijo de 4 solicitudes, una por estado. Las fechas son RELATIVAS a hoy
     * porque generarReporte() usa LocalDate.now() internamente y no se puede controlar.
     */
    private void cargarCasoDePrueba() throws IOException {
        escribirCsv(
                linea(1, "90001", "PENDIENTE", 0, "CERTIFICADO", 2, 10),   // plazo 5  -> vencida
                linea(2, "90002", "EN_PROCESO", 2, "EQUIVALENCIA", 3, 40), // plazo 30 -> vencida
                linea(3, "90003", "RESUELTA", 1, "CERTIFICADO", 2, 60),    // resuelta -> no vence
                linea(4, "90001", "RECHAZADA", 4, "BAJA", 1, 60)           // rechazada -> no vence
        );
        sistema = new SistemaSolicitudes(archivo);
    }

    /** Arma una linea con el formato del CSV del sistema. */
    private String linea(int id, String legajo, String estado, int idResponsable,
                         String tipo, int prioridad, int diasAtras) {
        return id + ";" + legajo + ";Nombre" + legajo + ";Apellido" + legajo + ";"
                + "est" + legajo + "@mail.test;" + tipo + ";" + prioridad + ";desc;"
                + estado + ";" + idResponsable + ";" + LocalDate.now().minusDays(diasAtras);
    }

    private void escribirCsv(String... lineas) throws IOException {
        Files.writeString(Path.of(archivo), String.join("\n", lineas) + "\n", StandardCharsets.UTF_8);
    }

    // =================================================================
    // Puntos unicos de adaptacion a la API del sistema
    // =================================================================

    private int registrarValida() {
        return registrar("Ana", "Perez", "90001", "ana.perez@mail.test",
                "CERTIFICADO", 2, "Constancia de alumna regular");
    }

    private int registrar(String nombre, String apellido, String legajo, String email,
                          String tipo, int prioridad, String descripcion) {
        return sistema.registrarSolicitud(new DatosSolicitud(nombre, apellido, legajo, email, tipo, prioridad, descripcion));
    }

    private List<Solicitud> filtrar(String tipo, String estado, String legajo) {
        return sistema.proc(tipo, estado, legajo);
    }

    private String estadoDe(Solicitud s) {
        return String.valueOf(s.getEstado());
    }

    private String tipoDe(Solicitud s) {
        return String.valueOf(s.getTipo());
    }

    private String nombreResponsableDe(Solicitud s) {
        return s.getResponsable().nombre;
    }
}
