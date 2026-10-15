package tallermecanico.controlador;

import tallermecanico.modelo.Empleado;
import tallermecanico.modelo.Rol;
import tallermecanico.modelo.Usuario;

public class Sesion {

    private static Usuario usuarioActual;
    private static Rol rolActual;

    public static void iniciar(Usuario usuario, Rol rol) {
        usuarioActual = usuario;
        rolActual = rol;
    }

    public static void cerrar() {
        if (usuarioActual != null) {
            usuarioActual.cerrarSesion();
        }
        usuarioActual = null;
        rolActual = null;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static Empleado getEmpleado() {
        if (usuarioActual == null) {
            return null;
        }
        return usuarioActual.getEmpleado();
    }

    public static int idUsuario() {
        if (usuarioActual == null) {
            return 0;
        }
        return usuarioActual.getId();
    }

    public static String nombreUsuario() {
        if (usuarioActual == null) {
            return "Sin sesion";
        }
        return usuarioActual.getEmpleado().obtenerNombreCompleto();
    }

    public static String puesto() {
        if (usuarioActual == null) {
            return "";
        }
        return usuarioActual.getPuesto();
    }

    public static boolean esAdministrador() {
        return puesto().equals("Administrador");
    }

    public static boolean tienePermiso(String modulo) {
        if (rolActual == null) {
            return false;
        }
        return rolActual.tienePermiso(modulo);
    }

    public static void actualizarRol(Rol rol) {
        if (rolActual != null && rol.getIdRol() == rolActual.getIdRol()) {
            rolActual = rol;
        }
    }
}
