package tallermecanico.controlador;

import java.util.ArrayList;
import tallermecanico.dao.RolDAO;
import tallermecanico.dao.UsuarioDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.modelo.Empleado;
import tallermecanico.modelo.Rol;
import tallermecanico.modelo.Usuario;
import tallermecanico.util.Seguridad;
import tallermecanico.util.Validaciones;

public class ControlUsuarios {

    private UsuarioDAO dao;
    private RolDAO rolDAO;

    public ControlUsuarios() {
        dao = new UsuarioDAO();
        rolDAO = new RolDAO();
    }

    public ArrayList<Usuario> listar() throws BaseDatosException {
        return dao.listar();
    }

    public ArrayList<Usuario> buscar(String criterio, String texto) throws BaseDatosException {
        if (texto == null || texto.trim().isEmpty()) {
            return dao.listar();
        }
        return dao.buscar(criterio, texto);
    }

    public Usuario iniciarSesion(String username, String contrasena)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.obligatorio(username, "usuario");
        Validaciones.obligatorio(contrasena, "contrasena");

        Usuario usuario = dao.buscarPorUsername(username);
        if (usuario == null) {
            throw new ReglaNegocioException("El usuario o la contrasena son incorrectos.");
        }
        if (!usuario.isEstado()) {
            throw new ReglaNegocioException("El usuario " + usuario.getUsername()
                    + " esta inactivo. Comuniquese con el administrador.");
        }
        if (!usuario.iniciarSesion(username, contrasena)) {
            throw new ReglaNegocioException("El usuario o la contrasena son incorrectos.");
        }
        Rol rol = rolDAO.buscarPorNombre(usuario.getPuesto());
        if (rol == null) {
            throw new ReglaNegocioException("El puesto del usuario no tiene un rol configurado.");
        }
        Sesion.iniciar(usuario, rol);
        dao.registrarAcceso(usuario.getId());
        Auditoria.registrar("Usuarios", "Ingreso", "Inicio de sesion de " + usuario.getUsername());
        return usuario;
    }

    public void cerrarSesion() {
        if (Sesion.getUsuarioActual() != null) {
            Auditoria.registrar("Usuarios", "Salida", "Cierre de sesion de " + Sesion.getUsuarioActual().getUsername());
        }
        Sesion.cerrar();
    }

    private void validarUsername(String username) throws DatoInvalidoException {
        Validaciones.obligatorio(username, "usuario");
        if (username.trim().length() < 4) {
            throw new DatoInvalidoException("El usuario debe tener al menos 4 caracteres.");
        }
        if (username.trim().length() > 30) {
            throw new DatoInvalidoException("El usuario no puede tener mas de 30 caracteres.");
        }
        if (username.trim().contains(" ")) {
            throw new DatoInvalidoException("El usuario no puede contener espacios.");
        }
    }

    private void validarContrasena(String contrasena, String confirmacion) throws DatoInvalidoException {
        Validaciones.obligatorio(contrasena, "contrasena");
        if (contrasena.length() < 6) {
            throw new DatoInvalidoException("La contrasena debe tener al menos 6 caracteres.");
        }
        if (!contrasena.equals(confirmacion)) {
            throw new DatoInvalidoException("La contrasena y su confirmacion no coinciden.");
        }
    }

    public Usuario agregar(String username, String contrasena, String confirmacion, Empleado empleado)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(empleado, "un empleado");
        validarUsername(username);
        validarContrasena(contrasena, confirmacion);

        if (dao.existeUsername(username, 0)) {
            throw new ReglaNegocioException("Ya existe un usuario con el nombre " + username.trim() + ".");
        }
        String otro = dao.usuarioDeEmpleado(empleado.getIdEmpleado(), 0);
        if (otro != null) {
            throw new ReglaNegocioException("El empleado " + empleado.obtenerNombreCompleto()
                    + " ya tiene el usuario " + otro + " asignado.");
        }
        if (!empleado.isEstado()) {
            throw new ReglaNegocioException("No se puede crear un usuario para un empleado inactivo.");
        }

        Usuario usuario = new Usuario(0, username.trim(), contrasena, empleado);
        dao.insertar(usuario);
        Auditoria.registrar("Usuarios", "Alta", "Se creo el usuario " + usuario.getUsername()
                + " para " + empleado.obtenerNombreCompleto());
        return usuario;
    }

    public void modificar(Usuario usuario, String username, Empleado empleado)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(empleado, "un empleado");
        validarUsername(username);
        if (dao.existeUsername(username, usuario.getId())) {
            throw new ReglaNegocioException("Ya existe otro usuario con el nombre " + username.trim() + ".");
        }
        String otro = dao.usuarioDeEmpleado(empleado.getIdEmpleado(), usuario.getId());
        if (otro != null) {
            throw new ReglaNegocioException("El empleado seleccionado ya tiene el usuario " + otro + ".");
        }
        usuario.setUsername(username.trim());
        usuario.setEmpleado(empleado);
        dao.actualizar(usuario);
        Auditoria.registrar("Usuarios", "Cambio", "Se modifico el usuario " + usuario.getUsername());
    }

    public void cambiarContrasena(Usuario usuario, String contrasena, String confirmacion)
            throws DatoInvalidoException, BaseDatosException {

        Validaciones.seleccionoElemento(usuario, "un usuario");
        validarContrasena(contrasena, confirmacion);
        dao.cambiarContrasena(usuario.getId(), Seguridad.encriptar(contrasena));
        usuario.asignarContrasena(contrasena);
        Auditoria.registrar("Usuarios", "Contrasena", "Se cambio la contrasena del usuario " + usuario.getUsername());
    }

    public void cambiarEstado(Usuario usuario) throws ReglaNegocioException, BaseDatosException {
        boolean activar = !usuario.isEstado();
        if (!activar && Sesion.getUsuarioActual() != null && Sesion.getUsuarioActual().getId() == usuario.getId()) {
            throw new ReglaNegocioException("No puede desactivar el usuario con el que tiene la sesion iniciada.");
        }
        if (!activar && usuario.getPuesto().equals("Administrador") && dao.contarAdministradoresActivos() <= 1) {
            throw new ReglaNegocioException("No se puede desactivar al unico administrador activo del sistema.");
        }
        usuario.setEstado(activar);
        dao.actualizar(usuario);
        Auditoria.registrar("Usuarios", activar ? "Activar" : "Inactivar", "Usuario " + usuario.getUsername());
    }

    public void eliminar(Usuario usuario) throws ReglaNegocioException, BaseDatosException {
        if (Sesion.getUsuarioActual() != null && Sesion.getUsuarioActual().getId() == usuario.getId()) {
            throw new ReglaNegocioException("No puede eliminar el usuario con el que tiene la sesion iniciada.");
        }
        if (usuario.getPuesto().equals("Administrador") && usuario.isEstado() && dao.contarAdministradoresActivos() <= 1) {
            throw new ReglaNegocioException("No se puede eliminar al unico administrador activo del sistema.");
        }
        dao.eliminar(usuario.getId());
        Auditoria.registrar("Usuarios", "Baja", "Se elimino el usuario " + usuario.getUsername());
    }

    public ArrayList<Rol> listarRoles() throws BaseDatosException {
        return rolDAO.listar();
    }

    public void guardarPermisos(Rol rol, ArrayList<String> modulos)
            throws ReglaNegocioException, BaseDatosException {

        if (!Sesion.esAdministrador()) {
            throw new ReglaNegocioException("Solo el administrador puede modificar los permisos.");
        }
        if (rol.getNombre().equals("Administrador") && (!modulos.contains("Usuarios") || !modulos.contains("Resumen"))) {
            throw new ReglaNegocioException("El rol Administrador debe conservar los modulos Resumen y Usuarios "
                    + "para no perder el acceso al sistema.");
        }
        if (modulos.isEmpty()) {
            throw new ReglaNegocioException("El rol debe tener al menos un modulo permitido.");
        }
        rol.setPermisos(modulos);
        rolDAO.guardarPermisos(rol);
        Sesion.actualizarRol(rol);
        Auditoria.registrar("Usuarios", "Permisos", "Se actualizaron los permisos del rol " + rol.getNombre()
                + ": " + String.join(", ", modulos));
    }
}
