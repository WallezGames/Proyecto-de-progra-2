package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.modelo.Categoria;
import tallermecanico.modelo.Producto;
import tallermecanico.modelo.Proveedor;

public class ProductoDAO extends BaseDAO implements CrudDAO<Producto> {

    private static final String CONSULTA = "SELECT p.*, c.nombre AS categoria_nombre, pr.nombre AS proveedor_nombre "
            + "FROM producto p JOIN categoria c ON c.id_categoria = p.id_categoria "
            + "JOIN proveedor pr ON pr.id_proveedor = p.id_proveedor ";

    @Override
    public int insertar(Producto producto) throws BaseDatosException {
        String sql = "INSERT INTO producto (codigo, nombre, marca, id_categoria, id_proveedor, precio_compra, precio_venta, "
                + "stock, stock_minimo, stock_maximo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = prepararConLlave(sql)) {
            asignarValores(ps, producto);
            ps.executeUpdate();
            int id = idGenerado(ps);
            producto.setIdProducto(id);
            return id;
        } catch (SQLException e) {
            throw error("registrar el producto", e);
        }
    }

    @Override
    public void actualizar(Producto producto) throws BaseDatosException {
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, marca = ?, id_categoria = ?, id_proveedor = ?, "
                + "precio_compra = ?, precio_venta = ?, stock = ?, stock_minimo = ?, stock_maximo = ?, estado = ? "
                + "WHERE id_producto = ?";
        try (PreparedStatement ps = preparar(sql)) {
            asignarValores(ps, producto);
            ps.setBoolean(11, producto.isEstado());
            ps.setInt(12, producto.getIdProducto());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("modificar el producto", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM producto WHERE id_producto = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar el producto", e);
        }
    }

    public void ingresarStock(int idProducto, int cantidad) throws BaseDatosException {
        try (PreparedStatement ps = preparar("UPDATE producto SET stock = stock + ? WHERE id_producto = ?")) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("ingresar el stock", e);
        }
    }

    @Override
    public Producto buscarPorId(int id) throws BaseDatosException {
        ArrayList<Producto> lista = consultar(CONSULTA + "WHERE p.id_producto = " + id, null, 0);
        if (lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    @Override
    public ArrayList<Producto> listar() throws BaseDatosException {
        return consultar(CONSULTA + "ORDER BY p.codigo", null, 0);
    }

    public ArrayList<Producto> listarActivos() throws BaseDatosException {
        return consultar(CONSULTA + "WHERE p.estado = 1 ORDER BY p.nombre", null, 0);
    }

    public ArrayList<Producto> listarBajoMinimo() throws BaseDatosException {
        return consultar(CONSULTA + "WHERE p.stock < p.stock_minimo ORDER BY p.codigo", null, 0);
    }

    public ArrayList<Producto> buscar(String criterio, String texto, Categoria categoria) throws BaseDatosException {
        String condicion;
        int parametros = 1;
        if (texto == null || texto.trim().isEmpty()) {
            condicion = "1 = 1";
            parametros = 0;
        } else if (criterio.equals("Codigo")) {
            condicion = "p.codigo LIKE ?";
        } else if (criterio.equals("Producto")) {
            condicion = "p.nombre LIKE ?";
        } else if (criterio.equals("Marca")) {
            condicion = "p.marca LIKE ?";
        } else if (criterio.equals("Proveedor")) {
            condicion = "pr.nombre LIKE ?";
        } else {
            condicion = "(p.codigo LIKE ? OR p.nombre LIKE ? OR p.marca LIKE ? OR pr.nombre LIKE ?)";
            parametros = 4;
        }
        if (categoria != null) {
            condicion = condicion + " AND p.id_categoria = " + categoria.getIdCategoria();
        }
        return consultar(CONSULTA + "WHERE " + condicion + " ORDER BY p.codigo", texto, parametros);
    }

    public boolean existeCodigo(String codigo, int excluirId) throws BaseDatosException {
        return existe("SELECT COUNT(*) FROM producto WHERE codigo = ? AND id_producto <> ?", codigo, excluirId);
    }

    public boolean existeNombre(String nombre, int excluirId) throws BaseDatosException {
        return existe("SELECT COUNT(*) FROM producto WHERE nombre = ? AND id_producto <> ?", nombre, excluirId);
    }

    public String requerimientoPendiente(int idProducto) throws BaseDatosException {
        String sql = "SELECT r.numero FROM detalle_requerimiento d JOIN requerimiento r ON r.id_requerimiento = d.id_requerimiento "
                + "WHERE d.id_producto = ? AND r.estado IN ('Solicitado', 'Autorizado') LIMIT 1";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            throw error("verificar el producto", e);
        }
        return null;
    }

    public int vecesUtilizado(int idProducto) throws BaseDatosException {
        try (PreparedStatement ps = preparar("SELECT COUNT(*) FROM detalle_requerimiento WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw error("verificar el producto", e);
        }
    }

    private boolean existe(String sql, String valor, int excluirId) throws BaseDatosException {
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, valor.trim());
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw error("verificar el producto", e);
        }
    }

    private void asignarValores(PreparedStatement ps, Producto producto) throws SQLException {
        ps.setString(1, producto.getCodigo());
        ps.setString(2, producto.getNombre());
        ps.setString(3, producto.getMarca());
        ps.setInt(4, producto.getCategoria().getIdCategoria());
        ps.setInt(5, producto.getProveedor().getIdProveedor());
        ps.setDouble(6, producto.getPrecioCompra());
        ps.setDouble(7, producto.getPrecioVenta());
        ps.setInt(8, producto.getStock());
        ps.setInt(9, producto.getStockMin());
        ps.setInt(10, producto.getStockMax());
    }

    private ArrayList<Producto> consultar(String sql, String texto, int parametros) throws BaseDatosException {
        ArrayList<Producto> lista = new ArrayList<>();
        try (PreparedStatement ps = preparar(sql)) {
            for (int i = 1; i <= parametros; i++) {
                ps.setString(i, patron(texto));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw error("consultar los productos", e);
        }
        return lista;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombre(rs.getString("categoria_nombre"));

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(rs.getInt("id_proveedor"));
        proveedor.setNombre(rs.getString("proveedor_nombre"));

        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setMarca(rs.getString("marca"));
        p.setCategoria(categoria);
        p.setProveedor(proveedor);
        p.setPrecioCompra(rs.getDouble("precio_compra"));
        p.setPrecioVenta(rs.getDouble("precio_venta"));
        p.setStock(rs.getInt("stock"));
        p.setStockMin(rs.getInt("stock_minimo"));
        p.setStockMax(rs.getInt("stock_maximo"));
        p.setEstado(rs.getBoolean("estado"));
        return p;
    }
}
