package ar.edu.utn.tup.solicitudes;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SistemaSolicitudes sistema = new SistemaSolicitudes();
        int op = -1;

        while (op != 0) {
            System.out.println();
            System.out.println("===== GESTION DE SOLICITUDES ACADEMICAS =====");
            System.out.println("1. Registrar solicitud");
            System.out.println("2. Cambiar estado");
            System.out.println("3. Asignar responsable");
            System.out.println("4. Buscar / filtrar solicitudes");
            System.out.println("5. Ver reporte");
            System.out.println("6. Listar todas");
            System.out.println("0. Salir");
            System.out.print("Opcion: ");

            try {
                op = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Opcion invalida");
                continue;
            }

            switch (op) {
                case 1:
                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();
                    System.out.print("Apellido: ");
                    String apellido = sc.nextLine();
                    System.out.print("Legajo: ");
                    String legajo = sc.nextLine();
                    System.out.print("Email: ");
                    String email = sc.nextLine();
                    System.out.print("Tipo (CERTIFICADO, EQUIVALENCIA, REVISION_NOTA, BAJA): ");
                    String tipo = sc.nextLine().trim().toUpperCase();
                    System.out.print("Prioridad (1=alta, 2=media, 3=baja): ");
                    int prioridad;
                    try {
                        prioridad = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException ex) {
                        System.out.println("La prioridad debe ser un numero");
                        break;
                    }
                    System.out.print("Descripcion: ");
                    String descripcion = sc.nextLine();
                    sistema.registrarSolicitud(new DatosSolicitud(nombre, apellido, legajo, email, tipo, prioridad, descripcion));
                    break;

                case 2:
                    System.out.print("Id de la solicitud: ");
                    int id;
                    try {
                        id = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException ex) {
                        System.out.println("El id debe ser un numero");
                        break;
                    }
                    System.out.print("Nuevo estado (PENDIENTE, EN_PROCESO, RESUELTA, RECHAZADA): ");
                    String estado = sc.nextLine().trim().toUpperCase();
                    if (sistema.cambiarEstado(id, estado)) {
                        System.out.println("Estado actualizado");
                    }
                    break;

                case 3:
                    System.out.print("Id de la solicitud: ");
                    int idSol;
                    try {
                        idSol = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException ex) {
                        System.out.println("El id debe ser un numero");
                        break;
                    }
                    for (Responsable r : sistema.getResponsables()) {
                        System.out.println("  " + r.id + ". " + r);
                    }
                    System.out.print("Id del responsable: ");
                    int idResp;
                    try {
                        idResp = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException ex) {
                        System.out.println("El id debe ser un numero");
                        break;
                    }
                    if (sistema.asignarResponsable(idSol, idResp)) {
                        System.out.println("Responsable asignado");
                    }
                    break;

                case 4:
                    System.out.println("(deje vacio para no filtrar por ese campo)");
                    System.out.print("Tipo: ");
                    String t = sc.nextLine().trim().toUpperCase();
                    System.out.print("Estado: ");
                    String e = sc.nextLine().trim().toUpperCase();
                    System.out.print("Legajo: ");
                    String l = sc.nextLine().trim();
                    List<Solicitud> res = sistema.proc(t, e, l);
                    if (res.isEmpty()) {
                        System.out.println("No se encontraron solicitudes");
                    }
                    for (Solicitud s : res) {
                        System.out.println("#" + s.getId() + " | " + s.getTipo() + " | P" + s.getPrioridad()
                                + " | " + s.getEstado() + " | " + s.getEstudiante()
                                + " | " + (s.getResponsable() == null ? "sin asignar" : s.getResponsable())
                                + " | " + s.getFechaCreacion());
                    }
                    break;

                case 5:
                    System.out.println(sistema.generarReporte());
                    break;

                case 6:
                    for (Solicitud s : sistema.getLista()) {
                        System.out.println("#" + s.getId() + " | " + s.getTipo() + " | P" + s.getPrioridad()
                                + " | " + s.getEstado() + " | " + s.getEstudiante()
                                + " | " + (s.getResponsable() == null ? "sin asignar" : s.getResponsable())
                                + " | " + s.getFechaCreacion());
                    }
                    break;

                case 0:
                    System.out.println("Hasta luego");
                    break;

                default:
                    System.out.println("Opcion invalida");
            }
        }
        sc.close();
    }
}