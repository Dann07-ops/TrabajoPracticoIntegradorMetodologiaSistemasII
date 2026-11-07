package ar.edu.utn.tup.solicitudes;

/**
 * Datos necesarios para registrar una solicitud nueva.
 *
 * Introduce Parameter Object: agrupa los 7 datos que antes viajaban
 * como parametros sueltos en registrarSolicitud y crearSolicitud.
 */
public record DatosSolicitud(String nombre, String apellido, String legajo, String email,
                             String tipo, int prioridad, String descripcion) {
}