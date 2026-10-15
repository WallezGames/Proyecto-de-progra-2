package tallermecanico.modelo;

import java.time.LocalDateTime;
import tallermecanico.util.Seguridad;

public class Usuario {

    private int id;
    private String username;
    private String contrasena;
    private Empleado empleado;
    private boolean estado;
    private LocalDateTime ultimoAcceso;
    private boolean sesionActiva;

    public Usuario() {
        estado = true;
    }

    public Usuario(int id, String username, String contrasenaTextoPlano, Empleado empleado) {
        this.id = id;
        this.username = username;
        this.empleado = empleado;
        this.estado = true;
        this.sesionActiva = false;
        asignarContrasena(contrasenaTextoPlano);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasenaEncriptada(String contrasenaEncriptada) {
        this.contrasena = contrasenaEncriptada;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public boolean isSesionActiva() {
        return sesionActiva;
    }

    public String getPuesto() {
        if (empleado == null) {
            return "Sin puesto";
        }
        return empleado.getPuesto();
    }

    public final void asignarContrasena(String contrasenaTextoPlano) {
        this.contrasena = Seguridad.encriptar(contrasenaTextoPlano);
    }

    public boolean validarContrasena(String contrasenaTextoPlano) {
        return contrasena != null && contrasena.equals(Seguridad.encriptar(contrasenaTextoPlano));
    }

    public boolean iniciarSesion(String usuarioIngresado, String contrasenaIngresada) {
        if (!estado) {
            return false;
        }
        if (username.equalsIgnoreCase(usuarioIngresado) && validarContrasena(contrasenaIngresada)) {
            sesionActiva = true;
            return true;
        }
        return false;
    }

    public void cerrarSesion() {
        sesionActiva = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Usuario)) {
            return false;
        }
        Usuario otro = (Usuario) obj;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return username + " - " + getPuesto();
    }
}
