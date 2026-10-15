package tallermecanico.controlador;

import java.util.ArrayList;
import tallermecanico.dao.ProductoDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.modelo.Categoria;
import tallermecanico.modelo.Producto;
import tallermecanico.modelo.Proveedor;
import tallermecanico.util.Validaciones;

public class ControlInventario {

    private ProductoDAO dao;

    public ControlInventario() {
        dao = new ProductoDAO();
    }

    public ArrayList<Producto> listar() throws BaseDatosException {
        return dao.listar();
    }

    public ArrayList<Producto> listarActivos() throws BaseDatosException {
        return dao.listarActivos();
    }

    public ArrayList<Producto> listarBajoMinimo() throws BaseDatosException {
        return dao.listarBajoMinimo();
    }

    public ArrayList<Producto> buscar(String criterio, String texto, Categoria categoria) throws BaseDatosException {
        return dao.buscar(criterio, texto, categoria);
    }

    public Producto agregar(String codigo, String nombre, String marca, Categoria categoria, Proveedor proveedor,
            String precioCompra, String precioVenta, String stock, String stockMin, String stockMax)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Producto producto = new Producto();
        llenar(producto, codigo, nombre, marca, categoria, proveedor, precioCompra, precioVenta, stock, stockMin, stockMax);

        if (dao.existeCodigo(producto.getCodigo(), 0)) {
            throw new ReglaNegocioException("Ya existe un producto con el codigo " + producto.getCodigo() + ".");
        }
        if (dao.existeNombre(producto.getNombre(), 0)) {
            throw new ReglaNegocioException("Ya existe un producto registrado con el nombre " + producto.getNombre() + ".");
        }

        dao.insertar(producto);
        Auditoria.registrar("Inventario", "Alta", "Se registro el producto " + producto.getCodigo() + " "
                + producto.getNombre() + " con " + producto.getStock() + " unidades");
        return producto;
    }

    public void modificar(Producto producto, String codigo, String nombre, String marca, Categoria categoria,
            Proveedor proveedor, String precioCompra, String precioVenta, String stock, String stockMin, String stockMax)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Producto copia = new Producto();
        llenar(copia, codigo, nombre, marca, categoria, proveedor, precioCompra, precioVenta, stock, stockMin, stockMax);

        if (dao.existeCodigo(copia.getCodigo(), producto.getIdProducto())) {
            throw new ReglaNegocioException("Ya existe otro producto con el codigo " + copia.getCodigo() + ".");
        }
        if (dao.existeNombre(copia.getNombre(), producto.getIdProducto())) {
            throw new ReglaNegocioException("Ya existe otro producto con el nombre " + copia.getNombre() + ".");
        }

        int stockAnterior = producto.getStock();
        producto.setCodigo(copia.getCodigo());
        producto.setNombre(copia.getNombre());
        producto.setMarca(copia.getMarca());
        producto.setCategoria(copia.getCategoria());
        producto.setProveedor(copia.getProveedor());
        producto.setPrecioCompra(copia.getPrecioCompra());
        producto.setPrecioVenta(copia.getPrecioVenta());
        producto.setStock(copia.getStock());
        producto.setStockMin(copia.getStockMin());
        producto.setStockMax(copia.getStockMax());
        dao.actualizar(producto);

        String detalle = "Se modifico el producto " + producto.getCodigo();
        if (stockAnterior != producto.getStock()) {
            detalle = detalle + ", ajuste de stock de " + stockAnterior + " a " + producto.getStock();
        }
        Auditoria.registrar("Inventario", "Cambio", detalle);
    }

    private void llenar(Producto producto, String codigo, String nombre, String marca, Categoria categoria,
            Proveedor proveedor, String precioCompra, String precioVenta, String stock, String stockMin, String stockMax)
            throws DatoInvalidoException, ReglaNegocioException {

        Validaciones.validarCodigo(codigo, "codigo del producto");
        Validaciones.obligatorio(nombre, "nombre del producto");
        Validaciones.longitudMaxima(nombre, 80, "nombre del producto");
        Validaciones.obligatorio(marca, "marca");
        Validaciones.seleccionoElemento(categoria, "una categoria");
        Validaciones.seleccionoElemento(proveedor, "un proveedor");
        double compra = Validaciones.decimalPositivo(precioCompra, "precio de compra");
        double venta = Validaciones.decimalPositivo(precioVenta, "precio de venta");
        int existencia = Validaciones.enteroPositivo(stock, "stock");
        int minimo = Validaciones.enteroPositivo(stockMin, "stock minimo");
        int maximo = Validaciones.enteroMayorACero(stockMax, "stock maximo");
        Validaciones.rangoStock(minimo, maximo);

        if (venta < compra) {
            throw new ReglaNegocioException("El precio de venta no puede ser menor que el precio de compra.");
        }
        if (existencia > maximo) {
            throw new ReglaNegocioException("El stock no puede ser mayor al stock maximo (" + maximo + ").");
        }

        producto.setCodigo(codigo.trim().toUpperCase());
        producto.setNombre(nombre.trim());
        producto.setMarca(marca.trim());
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setPrecioCompra(compra);
        producto.setPrecioVenta(venta);
        producto.setStock(existencia);
        producto.setStockMin(minimo);
        producto.setStockMax(maximo);
    }

    public void eliminar(Producto producto) throws ReglaNegocioException, BaseDatosException {
        String pendiente = dao.requerimientoPendiente(producto.getIdProducto());
        if (pendiente != null) {
            throw new ReglaNegocioException("No se puede eliminar el producto porque el requerimiento "
                    + pendiente + " esta pendiente de procesar.");
        }
        if (dao.vecesUtilizado(producto.getIdProducto()) > 0) {
            throw new ReglaNegocioException("El producto ya fue utilizado en requerimientos y no se puede borrar "
                    + "porque se perderia el historial. Puede inactivarlo en su lugar.");
        }
        dao.eliminar(producto.getIdProducto());
        Auditoria.registrar("Inventario", "Baja", "Se elimino el producto " + producto.getCodigo());
    }

    public void cambiarEstado(Producto producto) throws BaseDatosException {
        producto.setEstado(!producto.isEstado());
        dao.actualizar(producto);
        Auditoria.registrar("Inventario", producto.isEstado() ? "Activar" : "Inactivar", "Producto " + producto.getCodigo());
    }

    public void ingresarStock(Producto producto, String cantidad)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(producto, "un producto");
        int cantidadIngreso = Validaciones.enteroMayorACero(cantidad, "cantidad a ingresar");

        if (producto.getStock() + cantidadIngreso > producto.getStockMax()) {
            throw new ReglaNegocioException("El ingreso supera el stock maximo del producto. "
                    + "Stock actual: " + producto.getStock() + ", stock maximo: " + producto.getStockMax() + ".");
        }
        dao.ingresarStock(producto.getIdProducto(), cantidadIngreso);
        producto.aumentarStock(cantidadIngreso);
        Auditoria.registrar("Inventario", "Ingreso", "Ingreso de " + cantidadIngreso + " unidades de "
                + producto.getCodigo() + ", nuevo stock " + producto.getStock());
    }
}
