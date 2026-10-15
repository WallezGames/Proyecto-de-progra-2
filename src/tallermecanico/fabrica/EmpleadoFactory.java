package tallermecanico.fabrica;

import java.time.LocalDate;
import tallermecanico.modelo.Administrador;
import tallermecanico.modelo.Bodeguero;
import tallermecanico.modelo.Empleado;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.Recepcionista;

public class EmpleadoFactory {

    public static final String[] PUESTOS = {"Mecanico", "Jefe de Taller", "Bodeguero", "Recepcionista", "Administrador"};

    public static Empleado crear(String puesto) {
        if (puesto == null) {
            throw new IllegalArgumentException("El puesto no puede estar vacio.");
        }
        if (puesto.equals("Mecanico")) {
            return new Mecanico();
        }
        if (puesto.equals("Jefe de Taller")) {
            return new JefeTaller();
        }
        if (puesto.equals("Bodeguero")) {
            return new Bodeguero();
        }
        if (puesto.equals("Recepcionista")) {
            return new Recepcionista();
        }
        if (puesto.equals("Administrador")) {
            return new Administrador();
        }
        throw new IllegalArgumentException("El puesto " + puesto + " no existe en el sistema.");
    }

    public static Empleado crear(String puesto, String nombre, String apellido, String dpi, String telefono,
            String direccion, String correo, String codigo, LocalDate contratacion, String datoPuesto) {
        Empleado empleado = crear(puesto);
        empleado.setNombre(nombre);
        empleado.setApellido(apellido);
        empleado.setDpi(dpi);
        empleado.setTelefono(telefono);
        empleado.setDireccion(direccion);
        empleado.setCorreo(correo);
        empleado.setCodigoEmpleado(codigo);
        empleado.setFechaContratacion(contratacion);
        empleado.setDatoPuesto(datoPuesto);
        return empleado;
    }

    public static String nombreDelDato(String puesto) {
        if (puesto.equals("Mecanico")) {
            return "Especialidad";
        }
        if (puesto.equals("Jefe de Taller")) {
            return "Area del taller";
        }
        if (puesto.equals("Bodeguero")) {
            return "Area de trabajo";
        }
        if (puesto.equals("Recepcionista")) {
            return "Turno";
        }
        return "Nivel de acceso";
    }
}
