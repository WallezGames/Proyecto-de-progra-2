package tallermecanico.controlador;

import java.util.ArrayList;
import tallermecanico.dao.CategoriaDAO;
import tallermecanico.dao.ProveedorDAO;
import tallermecanico.dao.ServicioDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.modelo.Categoria;
import tallermecanico.modelo.Proveedor;
import tallermecanico.modelo.Servicio;
import tallermecanico.modelo.Taller;
import tallermecanico.util.Validaciones;

public class ControlCatalogos {

    private CategoriaDAO categoriaDAO;
    private ProveedorDAO proveedorDAO;
    private ServicioDAO servicioDAO;

    public ControlCatalogos() {
        categoriaDAO = new CategoriaDAO();
        proveedorDAO = new ProveedorDAO();
        servicioDAO = new ServicioDAO();
    }

    public ArrayList<Categoria> listarCategorias() throws BaseDatosException {
        return categoriaDAO.listar();
    }

    public ArrayList<Categoria> buscarCategorias(String texto) throws BaseDatosException {
        if (texto == null || texto.trim().isEmpty()) {
            return categoriaDAO.listar();
        }
        return categoriaDAO.buscar(texto);
    }

    public Categoria agregarCategoria(String nombre, String descripcion)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {
        Validaciones.obligatorio(nombre, "nombre de la categoria");
        Validaciones.longitudMaxima(nombre, 40, "nombre de la categoria");
        Validaciones.longitudMaxima(descripcion, 150, "descripcion");
        if (categoriaDAO.existeNombre(nombre, 0)) {
            throw new ReglaNegocioException("Ya existe la categoria " + nombre.trim() + ".");
        }
        Categoria categoria = new Categoria(nombre.trim(), descripcion.trim());
        categoriaDAO.insertar(categoria);
        Auditoria.registrar("Catalogos", "Alta", "Se registro la categoria " + categoria.getNombre());
        return categoria;
    }

    public void modificarCategoria(Categoria categoria, String nombre, String descripcion)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {
        Validaciones.obligatorio(nombre, "nombre de la categoria");
        Validaciones.longitudMaxima(nombre, 40, "nombre de la categoria");
        if (categoriaDAO.existeNombre(nombre, categoria.getIdCategoria())) {
            throw new ReglaNegocioException("Ya existe otra categoria con el nombre " + nombre.trim() + ".");
        }
        categoria.setNombre(nombre.trim());
        categoria.setDescripcion(descripcion.trim());
        categoriaDAO.actualizar(categoria);
        Auditoria.registrar("Catalogos", "Cambio", "Se modifico la categoria " + categoria.getNombre());
    }

    public void eliminarCategoria(Categoria categoria) throws ReglaNegocioException, BaseDatosException {
        if (categoria.getTotalProductos() > 0) {
            throw new ReglaNegocioException("No se puede eliminar la categoria porque tiene "
                    + categoria.getTotalProductos() + " producto(s).");
        }
        categoriaDAO.eliminar(categoria.getIdCategoria());
        Auditoria.registrar("Catalogos", "Baja", "Se elimino la categoria " + categoria.getNombre());
    }

    public ArrayList<Proveedor> listarProveedores() throws BaseDatosException {
        return proveedorDAO.listar();
    }

    public ArrayList<Proveedor> listarProveedoresActivos() throws BaseDatosException {
        return proveedorDAO.listarActivos();
    }

    public ArrayList<Proveedor> buscarProveedores(String criterio, String texto) throws BaseDatosException {
        if (texto == null || texto.trim().isEmpty()) {
            return proveedorDAO.listar();
        }
        return proveedorDAO.buscar(criterio, texto);
    }

    private void validarProveedor(String nombre, String nit, String telefono, String correo, String direccion)
            throws DatoInvalidoException {
        Validaciones.obligatorio(nombre, "nombre del proveedor");
        Validaciones.longitudMaxima(nombre, 80, "nombre del proveedor");
        Validaciones.obligatorio(nit, "NIT");
        Validaciones.longitudMaxima(nit, 15, "NIT");
        Validaciones.validarTelefono(telefono);
        if (correo != null && !correo.trim().isEmpty()) {
            Validaciones.validarCorreo(correo);
        }
        Validaciones.longitudMaxima(direccion, 120, "direccion");
    }

    public Proveedor agregarProveedor(String nombre, String nit, String telefono, String correo, String direccion)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {
        validarProveedor(nombre, nit, telefono, correo, direccion);
        if (proveedorDAO.existeNombre(nombre, 0)) {
            throw new ReglaNegocioException("Ya existe el proveedor " + nombre.trim() + ".");
        }
        Proveedor proveedor = new Proveedor(nombre.trim(), nit.trim(), telefono.trim(), correo.trim(), direccion.trim());
        proveedorDAO.insertar(proveedor);
        Auditoria.registrar("Catalogos", "Alta", "Se registro el proveedor " + proveedor.getNombre());
        return proveedor;
    }

    public void modificarProveedor(Proveedor proveedor, String nombre, String nit, String telefono, String correo,
            String direccion) throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {
        validarProveedor(nombre, nit, telefono, correo, direccion);
        if (proveedorDAO.existeNombre(nombre, proveedor.getIdProveedor())) {
            throw new ReglaNegocioException("Ya existe otro proveedor con el nombre " + nombre.trim() + ".");
        }
        proveedor.setNombre(nombre.trim());
        proveedor.setNit(nit.trim());
        proveedor.setTelefono(telefono.trim());
        proveedor.setCorreo(correo.trim());
        proveedor.setDireccion(direccion.trim());
        proveedorDAO.actualizar(proveedor);
        Auditoria.registrar("Catalogos", "Cambio", "Se modifico el proveedor " + proveedor.getNombre());
    }

    public void eliminarProveedor(Proveedor proveedor) throws ReglaNegocioException, BaseDatosException {
        if (proveedor.getTotalProductos() > 0) {
            throw new ReglaNegocioException("No se puede eliminar el proveedor porque abastece "
                    + proveedor.getTotalProductos() + " producto(s). Puede inactivarlo en su lugar.");
        }
        proveedorDAO.eliminar(proveedor.getIdProveedor());
        Auditoria.registrar("Catalogos", "Baja", "Se elimino el proveedor " + proveedor.getNombre());
    }

    public void cambiarEstadoProveedor(Proveedor proveedor) throws BaseDatosException {
        proveedor.setEstado(!proveedor.isEstado());
        proveedorDAO.actualizar(proveedor);
        Auditoria.registrar("Catalogos", proveedor.isEstado() ? "Activar" : "Inactivar", "Proveedor " + proveedor.getNombre());
    }

    public ArrayList<Servicio> listarServicios() throws BaseDatosException {
        return servicioDAO.listar();
    }

    public ArrayList<Servicio> listarServiciosDeTaller(Taller taller) throws BaseDatosException {
        if (taller == null) {
            return new ArrayList<>();
        }
        return servicioDAO.listarPorTaller(taller.getId());
    }

    public ArrayList<Servicio> buscarServicios(String criterio, String texto) throws BaseDatosException {
        if (texto == null || texto.trim().isEmpty()) {
            return servicioDAO.listar();
        }
        return servicioDAO.buscar(criterio, texto);
    }

    public Servicio agregarServicio(String nombre, String descripcion, Taller taller, String precio)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {
        Validaciones.obligatorio(nombre, "nombre del servicio");
        Validaciones.longitudMaxima(nombre, 80, "nombre del servicio");
        Validaciones.longitudMaxima(descripcion, 150, "descripcion");
        Validaciones.seleccionoElemento(taller, "el taller que presta el servicio");
        double valor = Validaciones.decimalPositivo(precio, "precio");
        if (valor <= 0) {
            throw new DatoInvalidoException("El precio del servicio debe ser mayor a cero.");
        }
        if (servicioDAO.existe(nombre, taller.getId(), 0)) {
            throw new ReglaNegocioException("El taller " + taller.getNombreTaller() + " ya tiene el servicio " + nombre.trim() + ".");
        }
        Servicio servicio = new Servicio(nombre.trim(), descripcion.trim(), taller, valor);
        servicioDAO.insertar(servicio);
        Auditoria.registrar("Catalogos", "Alta", "Se registro el servicio " + servicio.getNombre());
        return servicio;
    }

    public void modificarServicio(Servicio servicio, String nombre, String descripcion, Taller taller, String precio)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {
        Validaciones.obligatorio(nombre, "nombre del servicio");
        Validaciones.seleccionoElemento(taller, "el taller que presta el servicio");
        double valor = Validaciones.decimalPositivo(precio, "precio");
        if (valor <= 0) {
            throw new DatoInvalidoException("El precio del servicio debe ser mayor a cero.");
        }
        if (servicioDAO.existe(nombre, taller.getId(), servicio.getIdServicio())) {
            throw new ReglaNegocioException("El taller " + taller.getNombreTaller() + " ya tiene el servicio " + nombre.trim() + ".");
        }
        servicio.setNombre(nombre.trim());
        servicio.setDescripcion(descripcion.trim());
        servicio.setTaller(taller);
        servicio.setPrecio(valor);
        servicioDAO.actualizar(servicio);
        Auditoria.registrar("Catalogos", "Cambio", "Se modifico el servicio " + servicio.getNombre());
    }

    public void eliminarServicio(Servicio servicio) throws ReglaNegocioException, BaseDatosException {
        if (servicioDAO.vecesUtilizado(servicio.getIdServicio()) > 0) {
            throw new ReglaNegocioException("El servicio ya fue utilizado en ordenes de trabajo y no se puede borrar. "
                    + "Puede inactivarlo en su lugar.");
        }
        servicioDAO.eliminar(servicio.getIdServicio());
        Auditoria.registrar("Catalogos", "Baja", "Se elimino el servicio " + servicio.getNombre());
    }

    public void cambiarEstadoServicio(Servicio servicio) throws BaseDatosException {
        servicio.setEstado(!servicio.isEstado());
        servicioDAO.actualizar(servicio);
        Auditoria.registrar("Catalogos", servicio.isEstado() ? "Activar" : "Inactivar", "Servicio " + servicio.getNombre());
    }
}
