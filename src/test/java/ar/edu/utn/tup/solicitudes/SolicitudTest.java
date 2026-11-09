package ar.edu.utn.tup.solicitudes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios nuevos, posibles gracias al refactoring.
 *
 * Antes, la regla de vencimiento estaba dentro de generarReporte() y usaba LocalDate.now(),
 * por lo que solo podia probarse con fechas relativas al dia de ejecucion.
 * Despues del Move Method (S05) y del enum TipoSolicitud (S07), la regla vive en
 * Solicitud.estaVencida(hoy) y se prueba con FECHAS FIJAS, en forma determinista.
 */
class SolicitudTest {

    private static final LocalDate CREADA = LocalDate.of(2026, 11, 1);

    private Solicitud solicitud(TipoSolicitud tipo, int prioridad, String estado) {
        Estudiante estudiante = new Estudiante("90001", "Ana", "Perez", "ana.perez@mail.test");
        return new Solicitud(1, estudiante, tipo, prioridad, "desc", estado, null, CREADA);
    }

    @Test
    @DisplayName("Cada tipo conoce su plazo de respuesta")
    void plazosPorTipo() {
        assertEquals(5, TipoSolicitud.CERTIFICADO.getDiasDePlazo());
        assertEquals(30, TipoSolicitud.EQUIVALENCIA.getDiasDePlazo());
        assertEquals(10, TipoSolicitud.REVISION_NOTA.getDiasDePlazo());
        assertEquals(15, TipoSolicitud.BAJA.getDiasDePlazo());
    }

    @Test
    @DisplayName("desdeTexto acepta solo el nombre exacto del tipo")
    void conversionDesdeTexto() {
        assertEquals(TipoSolicitud.BAJA, TipoSolicitud.desdeTexto("BAJA"));
        assertNull(TipoSolicitud.desdeTexto("baja"));
        assertNull(TipoSolicitud.desdeTexto("OTRO"));
        assertNull(TipoSolicitud.desdeTexto(null));
    }

    @Test
    @DisplayName("Certificado (plazo 5): con 5 dias no vence, con 6 dias vence")
    void vencimientoEnElLimite() {
        Solicitud s = solicitud(TipoSolicitud.CERTIFICADO, 2, "PENDIENTE");

        assertFalse(s.estaVencida(LocalDate.of(2026, 11, 6)));
        assertTrue(s.estaVencida(LocalDate.of(2026, 11, 7)));
    }

    @Test
    @DisplayName("Prioridad alta reduce el plazo a la mitad con division entera (5 / 2 = 2)")
    void prioridadAltaReducePlazo() {
        Solicitud s = solicitud(TipoSolicitud.CERTIFICADO, 1, "PENDIENTE");

        assertFalse(s.estaVencida(LocalDate.of(2026, 11, 3)));
        assertTrue(s.estaVencida(LocalDate.of(2026, 11, 4)));
    }

    @Test
    @DisplayName("Una solicitud RESUELTA o RECHAZADA nunca vence")
    void cerradasNoVencen() {
        LocalDate unAnioDespues = LocalDate.of(2027, 11, 1);

        assertFalse(solicitud(TipoSolicitud.BAJA, 2, "RESUELTA").estaVencida(unAnioDespues));
        assertFalse(solicitud(TipoSolicitud.BAJA, 2, "RECHAZADA").estaVencida(unAnioDespues));
        assertTrue(solicitud(TipoSolicitud.BAJA, 2, "EN_PROCESO").estaVencida(unAnioDespues));
    }
}
