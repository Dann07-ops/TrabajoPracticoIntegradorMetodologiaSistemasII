package ar.edu.utn.tup.solicitudes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Solicitud {

    private int id;
    private Estudiante estudiante;
    private TipoSolicitud tipo;
    private int prioridad;    // 1 = alta, 2 = media, 3 = baja
    private String descripcion;
    private String estado;    // "PENDIENTE", "EN_PROCESO", "RESUELTA", "RECHAZADA"
    private Responsable responsable;
    private LocalDate fechaCreacion;

    public Solicitud(int id, Estudiante estudiante, TipoSolicitud tipo, int prioridad,
                     String descripcion, String estado, Responsable responsable,
                     LocalDate fechaCreacion) {
        this.id = id;
        this.estudiante = estudiante;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.descripcion = descripcion;
        this.estado = estado;
        this.responsable = responsable;
        this.fechaCreacion = fechaCreacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public TipoSolicitud getTipo() {
        return tipo;
    }

    public void setTipo(TipoSolicitud tipo) {
        this.tipo = tipo;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Responsable getResponsable() {
        return responsable;
    }

    public void setResponsable(Responsable responsable) {
        this.responsable = responsable;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean estaVencida(LocalDate hoy) {
        int dias = getTipo().getDiasDePlazo();
        if (getPrioridad() == 1) {
            dias = dias / 2;
        }
        long transcurridos = ChronoUnit.DAYS.between(getFechaCreacion(), hoy);
        return !getEstado().equals("RESUELTA") && !getEstado().equals("RECHAZADA")
                && transcurridos > dias;
    }
}