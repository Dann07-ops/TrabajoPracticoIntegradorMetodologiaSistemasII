package ar.edu.utn.tup.solicitudes;

/**
 * Tipos de solicitud admitidos por el sistema, cada uno con su plazo de respuesta.
 *
 * Replace Type Code with Enum (S07) y Replace Conditional with Polymorphism (S06):
 * reemplaza el String libre y el if/else de plazos que estaba en el calculo de vencimiento.
 * Para agregar un tipo nuevo alcanza con agregar una linea aca.
 */
public enum TipoSolicitud {

    CERTIFICADO(5),
    EQUIVALENCIA(30),
    REVISION_NOTA(10),
    BAJA(15);

    private final int diasDePlazo;

    TipoSolicitud(int diasDePlazo) {
        this.diasDePlazo = diasDePlazo;
    }

    public int getDiasDePlazo() {
        return diasDePlazo;
    }

    /**
     * Devuelve el tipo cuyo nombre coincide exactamente con el texto,
     * o null si no existe (distingue mayusculas y minusculas).
     */
    public static TipoSolicitud desdeTexto(String texto) {
        for (TipoSolicitud tipo : values()) {
            if (tipo.name().equals(texto)) {
                return tipo;
            }
        }
        return null;
    }
}