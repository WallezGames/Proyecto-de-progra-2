package tallermecanico.controlador;

import java.time.LocalDate;
import java.util.ArrayList;
import tallermecanico.dao.EmpleadoDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.fabrica.EmpleadoFactory;
import tallermecanico.modelo.Bodeguero;
import tallermecanico.modelo.Empleado;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Mecanico;
import tallermecanico.util.Validaciones;

public class ControlEmpleados {

    public static final String[] PUESTOS = EmpleadoFactory.PUESTOS;

    private EmpleadoDAO dao;

    public ControlEmpleados() {
        dao = new EmpleadoDAO();
    }

    public ArrayList<Empleado> listar() throws BaseDatosException {
        return dao.listar();
    }

    public ArrayList<Empleado> buscar(String criterio, String texto) throws BaseDatosException {
        if (texto == null || texto.trim().isEmpty()) {
            return dao.listar();
        }
        return dao.buscar(criterio, texto);
    }

    public ArrayList<Mecanico> listarMecanicos() throws BaseDatosException {
        return dao.listarMecanicos();
    }

    public ArrayList<Mecanico> listarMecanicosDeTaller(int idTaller) throws BaseDatosException {
        return dao.listarMecanicosDeTaller(idTaller);
    }

    public ArrayList<JefeTaller> listarJefes() throws BaseDatosException {
        return dao.listarJefes();
    }

    public ArrayList<Bodeguero> listarBodegueros() throws BaseDatosException {
        return dao.listarBodegueros();
    }

    public String datoSegunPuesto(String puesto) {
        return EmpleadoFactory.nombreDelDato(puesto);
    }

    private void validar(String nombre, String apellido, String dpi, String telefono, String direccion,
            String correo, String codigo, String dato, String puesto) throws DatoInvalidoException {
        Validaciones.soloLetras(nombre, "nombre");
        Validaciones.soloLetras(apellido, "apellido");
        Validaciones.validarDpi(dpi);
        Validaciones.validarTelefono(telefono);
        Validaciones.obligatorio(direccion, "direccion");
        Validaciones.longitudMaxima(direccion, 120, "direccion");
        Validaciones.validarCorreo(correo);
        Validaciones.validarCodigo(codigo, "codigo de empleado");
        Validaciones.longitudMaxima(codigo, 10, "codigo de empleado");
        Validaciones.obligatorio(dato, datoSegunPuesto(puesto).toLowerCase());
        Validaciones.longitudMaxima(dato, 60, datoSegunPuesto(puesto).toLowerCase());
    }

    public Empleado agregar(String puesto, String nombre, String apellido, String dpi, String telefono,
            String direccion, String correo, String codigo, String dato)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.obligatorio(puesto, "puesto");
        validar(nombre, apellido, dpi, telefono, direccion, correo, codigo, dato, puesto);

        if (dao.existeCodigo(codigo.trim().toUpperCase(), 0)) {
            throw new ReglaNegocioException("Ya existe un empleado con el codigo " + codigo.trim().toUpperCase() + ".");
        }
        if (dao.existeDpi(dpi, 0)) {
            throw new ReglaNegocioException("Ya existe un empleado registrado con el DPI " + dpi.trim() + ".");
        }

        Empleado empleado = EmpleadoFactory.crear(puesto, nombre.trim(), apellido.trim(), dpi.trim(), telefono.trim(),
                direccion.trim(), correo.trim(), codigo.trim().toUpperCase(), LocalDate.now(), dato.trim());
        dao.insertar(empleado);
        Auditoria.registrar("Empleados", "Alta", "Se registro al empleado " + empleado.getCodigoEmpleado()
                + " " + empleado.obtenerNombreCompleto() + " como " + puesto);
        return empleado;
    }

    public void modificar(Empleado empleado, String nombre, String apellido, String dpi, String telefono,
            String direccion, String correo, String codigo, String dato)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        validar(nombre, apellido, dpi, telefono, direccion, correo, codigo, dato, empleado.getPuesto());

        if (dao.existeCodigo(codigo.trim().toUpperCase(), empleado.getIdEmpleado())) {
            throw new ReglaNegocioException("Ya existe otro empleado con el codigo " + codigo.trim().toUpperCase() + ".");
        }
        if (dao.existeDpi(dpi, empleado.getIdEmpleado())) {
            throw new ReglaNegocioException("Ya existe otro empleado con el DPI " + dpi.trim() + ".");
        }

        empleado.setNombre(nombre.trim());
        empleado.setApellido(apellido.trim());
        empleado.setDpi(dpi.trim());
        empleado.setTelefono(telefono.trim());
        empleado.setDireccion(direccion.trim());
        empleado.setCorreo(correo.trim());
        empleado.setCodigoEmpleado(codigo.trim().toUpperCase());
        empleado.setDatoPuesto(dato.trim());
        dao.actualizar(empleado);
        Auditoria.registrar("Empleados", "Cambio", "Se modificaron los datos de " + empleado.getCodigoEmpleado());
    }

    public void eliminar(Empleado empleado) throws ReglaNegocioException, BaseDatosException {
        String usuario = dao.usuarioAsociado(empleado.getIdEmpleado());
        if (usuario != null) {
            throw new ReglaNegocioException("No se puede eliminar al empleado porque tiene el usuario "
                    + usuario + " asociado.");
        }
        String taller = dao.tallerACargo(empleado.getIdEmpleado());
        if (taller != null) {
            throw new ReglaNegocioException("No se puede eliminar al empleado porque es jefe del taller " + taller + ".");
        }
        String orden = dao.ordenActivaDeMecanico(empleado.getIdEmpleado());
        if (orden != null) {
            throw new ReglaNegocioException("No se puede eliminar al mecanico porque esta asignado a la orden "
                    + orden + ".");
        }
        if (dao.totalMovimientos(empleado.getIdEmpleado()) > 0) {
            throw new ReglaNegocioException("El empleado tiene ordenes o requerimientos en su historial y no se puede "
                    + "borrar. Puede inactivarlo en su lugar.");
        }
        dao.eliminar(empleado.getIdEmpleado());
        Auditoria.registrar("Empleados", "Baja", "Se elimino al empleado " + empleado.getCodigoEmpleado());
    }

    public void cambiarEstado(Empleado empleado) throws ReglaNegocioException, BaseDatosException {
        boolean activar = !empleado.isEstado();
        if (!activar) {
            String orden = dao.ordenActivaDeMecanico(empleado.getIdEmpleado());
            if (orden != null) {
                throw new ReglaNegocioException("No se puede inactivar al mecanico porque esta asignado a la orden "
                        + orden + ".");
            }
            if (Sesion.getEmpleado() != null && Sesion.getEmpleado().equals(empleado)) {
                throw new ReglaNegocioException("No puede inactivar al empleado con el que tiene la sesion iniciada.");
            }
        }
        dao.cambiarEstado(empleado.getIdEmpleado(), activar);
        empleado.setEstado(activar);
        Auditoria.registrar("Empleados", activar ? "Activar" : "Inactivar", "Empleado " + empleado.getCodigoEmpleado());
    }
}
