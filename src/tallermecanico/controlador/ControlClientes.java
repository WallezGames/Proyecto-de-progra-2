package tallermecanico.controlador;

import java.util.ArrayList;
import tallermecanico.dao.ClienteDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.modelo.Cliente;
import tallermecanico.util.Validaciones;

public class ControlClientes {

    private ClienteDAO dao;

    public ControlClientes() {
        dao = new ClienteDAO();
    }

    public ArrayList<Cliente> listar() throws BaseDatosException {
        return dao.listar();
    }

    public ArrayList<Cliente> listarActivos() throws BaseDatosException {
        return dao.listarActivos();
    }

    public ArrayList<Cliente> buscar(String criterio, String texto) throws BaseDatosException {
        if (texto == null || texto.trim().isEmpty()) {
            return dao.listar();
        }
        return dao.buscar(criterio, texto);
    }

    private void validar(String nombre, String apellido, String dpi, String telefono, String direccion,
            String correo, String nit) throws DatoInvalidoException {
        Validaciones.soloLetras(nombre, "nombre");
        Validaciones.longitudMaxima(nombre, 50, "nombre");
        Validaciones.soloLetras(apellido, "apellido");
        Validaciones.longitudMaxima(apellido, 50, "apellido");
        Validaciones.validarDpi(dpi);
        Validaciones.validarTelefono(telefono);
        Validaciones.obligatorio(direccion, "direccion");
        Validaciones.longitudMaxima(direccion, 120, "direccion");
        Validaciones.validarCorreo(correo);
        Validaciones.obligatorio(nit, "NIT");
        Validaciones.longitudMaxima(nit, 15, "NIT");
    }

    public Cliente agregar(String nombre, String apellido, String dpi, String telefono,
            String direccion, String correo, String nit)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        validar(nombre, apellido, dpi, telefono, direccion, correo, nit);
        if (dao.existeDpi(dpi, 0)) {
            throw new ReglaNegocioException("Ya existe un cliente registrado con el DPI " + dpi.trim() + ".");
        }

        Cliente cliente = new Cliente(nombre.trim(), apellido.trim(), dpi.trim(), telefono.trim(),
                direccion.trim(), correo.trim(), nit.trim());
        dao.insertar(cliente);
        Auditoria.registrar("Clientes", "Alta", "Se registro al cliente " + cliente.obtenerNombreCompleto());
        return cliente;
    }

    public void modificar(Cliente cliente, String nombre, String apellido, String dpi, String telefono,
            String direccion, String correo, String nit)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        validar(nombre, apellido, dpi, telefono, direccion, correo, nit);
        if (dao.existeDpi(dpi, cliente.getIdCliente())) {
            throw new ReglaNegocioException("Ya existe otro cliente registrado con el DPI " + dpi.trim() + ".");
        }

        cliente.setNombre(nombre.trim());
        cliente.setApellido(apellido.trim());
        cliente.setDpi(dpi.trim());
        cliente.setTelefono(telefono.trim());
        cliente.setDireccion(direccion.trim());
        cliente.setCorreo(correo.trim());
        cliente.setNit(nit.trim());
        dao.actualizar(cliente);
        Auditoria.registrar("Clientes", "Cambio", "Se modificaron los datos del cliente " + cliente.obtenerNombreCompleto());
    }

    public void eliminar(Cliente cliente) throws ReglaNegocioException, BaseDatosException {
        Cliente actual = dao.buscarPorId(cliente.getIdCliente());
        if (actual == null) {
            throw new ReglaNegocioException("El cliente ya no existe en la base de datos.");
        }
        if (actual.getTotalVehiculos() > 0) {
            throw new ReglaNegocioException("No se puede eliminar al cliente " + actual.obtenerNombreCompleto()
                    + " porque tiene " + actual.getTotalVehiculos() + " vehiculo(s) registrado(s). "
                    + "Puede inactivarlo en su lugar.");
        }
        dao.eliminar(cliente.getIdCliente());
        Auditoria.registrar("Clientes", "Baja", "Se elimino al cliente " + actual.obtenerNombreCompleto());
    }

    public void cambiarEstado(Cliente cliente) throws BaseDatosException {
        cliente.setEstado(!cliente.isEstado());
        dao.actualizar(cliente);
        Auditoria.registrar("Clientes", cliente.isEstado() ? "Activar" : "Inactivar",
                "Cliente " + cliente.obtenerNombreCompleto());
    }
}
