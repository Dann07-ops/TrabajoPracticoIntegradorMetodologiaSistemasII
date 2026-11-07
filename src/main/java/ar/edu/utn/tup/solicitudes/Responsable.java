package ar.edu.utn.tup.solicitudes;

public class Responsable {

    public int id;
    public String nombre;
    public String area; // "ALUMNOS", "BEDELIA", "TESORERIA", "DIRECCION"

    public Responsable(int id, String nombre, String area) {
        this.id = id;
        this.nombre = nombre;
        this.area = area;
    }

    @Override
    public String toString() {
        return nombre + " - " + area;
    }
}