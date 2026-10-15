package tallermecanico.modelo;

import java.time.LocalDate;

public class Administrador extends Empleado {

    private String nivelAcceso;

    public Administrador() {
    }

    public Administrador(String nombre, String apellido, String dpi, String telefono, String direccion, String correo,
            String codigoEmpleado, LocalDate fechaContratacion, String nivelAcceso) {
        super(nombre, apellido, dpi, telefono, direccion, correo, codigoEmpleado, fechaContratacion);
        this.nivelAcceso = nivelAcceso;
    }

    @Override
    public String getPuesto() {
        return "Administrador";
    }

    @Override
    public String getDatoPuesto() {
        return nivelAcceso;
    }

    @Override
    public void setDatoPuesto(String dato) {
        this.nivelAcceso = dato;
    }

    public String getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(String nivelAcceso) {
        this.nivelAcceso = nivelAcceso;
    }

    public Usuario registrarUsuario(String username, String contrasena, Empleado empleado) {
        Usuario usuario = new Usuario(0, username, contrasena, empleado);
        return usuario;
    }

    public void administrarPermisos(Rol rol, String modulo, boolean permitir) {
        if (permitir) {
            rol.agregarPermiso(modulo);
        } else {
            rol.quitarPermiso(modulo);
        }
    }
}
