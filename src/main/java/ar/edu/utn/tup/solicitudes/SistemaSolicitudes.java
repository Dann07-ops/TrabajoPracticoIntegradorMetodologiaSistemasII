package ar.edu.utn.tup.solicitudes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SistemaSolicitudes {

    private List<Solicitud> lista = new ArrayList<>();
    private List<Responsable> lista2 = new ArrayList<>();
    private String archivo;
    private int ultimoId = 0;

    public SistemaSolicitudes() {
        this("solicitudes.csv");
    }

    public SistemaSolicitudes(String archivo) {
        this.archivo = archivo;
        lista2.add(new Responsable(1, "Laura Gomez", "ALUMNOS"));
        lista2.add(new Responsable(2, "Martin Ruiz", "BEDELIA"));
        lista2.add(new Responsable(3, "Sofia Diaz", "TESORERIA"));
        lista2.add(new Responsable(4, "Pablo Romero", "DIRECCION"));
        cargar();
    }

    // ---------------------------------------------------------------
    // Registrar una solicitud nueva
    // ---------------------------------------------------------------
    public int registrarSolicitud(DatosSolicitud datos) {
        if (!esEstudianteValido(datos)) {
            return -1;
        }
        if (!esSolicitudValida(datos)) {
            return -1;
        }

        Solicitud s = crearSolicitud(datos);
        agregarAlArchivo(s);

        System.out.println("Solicitud registrada con id " + s.getId());
        return s.getId();
    }

    private boolean esEstudianteValido(DatosSolicitud datos) {
        if (datos.nombre() == null || datos.nombre().trim().isEmpty()) {
            System.out.println("Error: el nombre es obligatorio");
            return false;
        }
        if (datos.apellido() == null || datos.apellido().trim().isEmpty()) {
            System.out.println("Error: el apellido es obligatorio");
            return false;
        }
        if (datos.legajo() == null || datos.legajo().trim().isEmpty()) {
            System.out.println("Error: el legajo es obligatorio");
            return false;
        }
        String email = datos.email();
        if (email == null || !email.contains("@") || !email.contains(".")) {
            System.out.println("Error: el email no es valido");
            return false;
        }
        return true;
    }

    private boolean esSolicitudValida(DatosSolicitud datos) {
        String tipo = datos.tipo();
        if (tipo == null || (!tipo.equals("CERTIFICADO") && !tipo.equals("EQUIVALENCIA")
                && !tipo.equals("REVISION_NOTA") && !tipo.equals("BAJA"))) {
            System.out.println("Error: tipo de solicitud invalido");
            return false;
        }
        if (datos.prioridad() < 1 || datos.prioridad() > 3) {
            System.out.println("Error: la prioridad debe ser 1, 2 o 3");
            return false;
        }
        String descripcion = datos.descripcion();
        if (descripcion == null || descripcion.trim().isEmpty()) {
            System.out.println("Error: la descripcion es obligatoria");
            return false;
        }
        if (descripcion.length() > 200 || descripcion.contains(";")) {
            System.out.println("Error: la descripcion es demasiado larga o contiene ';'");
            return false;
        }
        return true;
    }

    private Solicitud crearSolicitud(DatosSolicitud datos) {
        Estudiante e = new Estudiante(datos.legajo().trim(), datos.nombre().trim(),
                datos.apellido().trim(), datos.email().trim());
        ultimoId++;
        Solicitud s = new Solicitud(ultimoId, e, datos.tipo(), datos.prioridad(),
                datos.descripcion().trim(), "PENDIENTE", null, LocalDate.now());
        lista.add(s);
        return s;
    }

    private void agregarAlArchivo(Solicitud s) {
        try {
            FileWriter fw = new FileWriter(archivo, StandardCharsets.UTF_8, true);
            fw.write(s.getId() + ";" + s.getEstudiante().getLegajo() + ";"
                    + s.getEstudiante().getNombre() + ";" + s.getEstudiante().getApellido() + ";"
                    + s.getEstudiante().getEmail() + ";" + s.getTipo() + ";" + s.getPrioridad() + ";"
                    + s.getDescripcion() + ";" + s.getEstado() + ";" + 0 + ";"
                    + s.getFechaCreacion() + "\n");
            fw.close();
        } catch (Exception ex) {
        }
    }

    // ---------------------------------------------------------------
    // Cambiar el estado de una solicitud
    // ---------------------------------------------------------------
    public boolean cambiarEstado(int id, String nuevoEstado) {
        Solicitud x = null;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId() == id) {
                x = lista.get(i);
            }
        }
        if (x == null) {
            System.out.println("No existe la solicitud " + id);
            return false;
        }
        if (nuevoEstado == null || (!nuevoEstado.equals("PENDIENTE") && !nuevoEstado.equals("EN_PROCESO")
                && !nuevoEstado.equals("RESUELTA") && !nuevoEstado.equals("RECHAZADA"))) {
            System.out.println("Estado invalido");
            return false;
        }
        x.setEstado(nuevoEstado);
        guardar();
        return true;
    }

    // ---------------------------------------------------------------
    // Asignar un responsable a una solicitud
    // ---------------------------------------------------------------
    public boolean asignarResponsable(int idSolicitud, int idResponsable) {
        Solicitud x = null;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId() == idSolicitud) {
                x = lista.get(i);
            }
        }
        if (x == null) {
            System.out.println("No existe la solicitud " + idSolicitud);
            return false;
        }
        Responsable r = null;
        for (Responsable aux : lista2) {
            if (aux.id == idResponsable) {
                r = aux;
            }
        }
        if (r == null) {
            System.out.println("No existe el responsable " + idResponsable);
            return false;
        }
        x.setResponsable(r);
        if (x.getEstado().equals("PENDIENTE")) {
            x.setEstado("EN_PROCESO");
        }
        guardar();
        return true;
    }

    // ---------------------------------------------------------------
    // Buscar por id
    // ---------------------------------------------------------------
    public Solicitud buscarPorId(int id) {
        Solicitud x = null;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId() == id) {
                x = lista.get(i);
            }
        }
        return x;
    }

    // ---------------------------------------------------------------
    // Filtrar solicitudes (si un filtro viene vacio se ignora)
    // ---------------------------------------------------------------
    public List<Solicitud> proc(String t, String e, String l) {
        List<Solicitud> res = new ArrayList<>();
        for (Solicitud s : lista) {
            boolean flag = true;
            if (t != null && !t.isEmpty() && !s.getTipo().equals(t)) {
                flag = false;
            }
            if (e != null && !e.isEmpty() && !s.getEstado().equals(e)) {
                flag = false;
            }
            if (l != null && !l.isEmpty() && !s.getEstudiante().getLegajo().equals(l)) {
                flag = false;
            }
            if (flag) {
                res.add(s);
            }
        }
        return res;
    }

    // ---------------------------------------------------------------
    // Reporte basico
    // ---------------------------------------------------------------
    public String generarReporte() {
        int p = 0, ep = 0, r = 0, rc = 0;
        List<Solicitud> vencidas = new ArrayList<>();

        for (Solicitud s : lista) {
            if (s.getEstado().equals("PENDIENTE")) {
                p++;
            } else if (s.getEstado().equals("EN_PROCESO")) {
                ep++;
            } else if (s.getEstado().equals("RESUELTA")) {
                r++;
            } else if (s.getEstado().equals("RECHAZADA")) {
                rc++;
            }

            // calcular si esta vencida segun el tipo
            int dias;
            if (s.getTipo().equals("CERTIFICADO")) {
                dias = 5;
            } else if (s.getTipo().equals("EQUIVALENCIA")) {
                dias = 30;
            } else if (s.getTipo().equals("REVISION_NOTA")) {
                dias = 10;
            } else {
                dias = 15;
            }
            if (s.getPrioridad() == 1) {
                dias = dias / 2;
            }
            long transcurridos = ChronoUnit.DAYS.between(s.getFechaCreacion(), LocalDate.now());
            if (!s.getEstado().equals("RESUELTA") && !s.getEstado().equals("RECHAZADA")
                    && transcurridos > dias) {
                vencidas.add(s);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("===== REPORTE DE SOLICITUDES =====\n");
        sb.append("Total: ").append(lista.size()).append("\n");
        sb.append("Pendientes: ").append(p).append("\n");
        sb.append("En proceso: ").append(ep).append("\n");
        sb.append("Resueltas: ").append(r).append("\n");
        sb.append("Rechazadas: ").append(rc).append("\n");
        sb.append("Vencidas: ").append(vencidas.size()).append("\n");
        for (Solicitud s : vencidas) {
            sb.append("  - #").append(s.getId()).append(" ").append(s.getTipo())
                    .append(" | ").append(s.getEstudiante()).append("\n");
        }
        return sb.toString();
    }

    public List<Solicitud> getLista() {
        return lista;
    }

    public List<Responsable> getResponsables() {
        return lista2;
    }

    // ---------------------------------------------------------------
    // Persistencia en CSV
    // ---------------------------------------------------------------
    private void cargar() {
        try {
            File f = new File(archivo);
            if (!f.exists()) {
                return;
            }
            BufferedReader br = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8));
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                String[] c = linea.split(";");
                Estudiante e = new Estudiante(c[1], c[2], c[3], c[4]);
                Responsable r = null;
                int idR = Integer.parseInt(c[9]);
                for (Responsable aux : lista2) {
                    if (aux.id == idR) {
                        r = aux;
                    }
                }
                Solicitud s = new Solicitud(Integer.parseInt(c[0]), e, c[5], Integer.parseInt(c[6]),
                        c[7], c[8], r, LocalDate.parse(c[10]));
                lista.add(s);
                if (s.getId() > ultimoId) {
                    ultimoId = s.getId();
                }
            }
            br.close();
        } catch (Exception ex) {
        }
    }

    private void guardar() {
        try {
            FileWriter fw = new FileWriter(archivo, StandardCharsets.UTF_8, false);
            for (Solicitud s : lista) {
                int idR = 0;
                if (s.getResponsable() != null) {
                    idR = s.getResponsable().id;
                }
                fw.write(s.getId() + ";" + s.getEstudiante().getLegajo() + ";"
                        + s.getEstudiante().getNombre() + ";" + s.getEstudiante().getApellido() + ";"
                        + s.getEstudiante().getEmail() + ";" + s.getTipo() + ";" + s.getPrioridad() + ";"
                        + s.getDescripcion() + ";" + s.getEstado() + ";" + idR + ";"
                        + s.getFechaCreacion() + "\n");
            }
            fw.close();
        } catch (Exception ex) {
            System.out.println("Error al guardar");
        }
    }
}