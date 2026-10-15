package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.modelo.Proveedor;

public class ProveedorDAO extends BaseDAO implements CrudDAO<Proveedor> {

    private static final String CONSULTA = "SELECT pr.*, "
            + "(SELECT COUNT(*) FROM producto p WHERE p.id_proveedor = pr.id_proveedor) AS total_productos "
            + "FROM proveedor pr ";

    @Override
    public int insertar(Proveedor proveedor) throws BaseDatosException {
        String sql = "INSERT INTO proveedor (nombre, nit, telefono, correo, direccion) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = prepararConLlave(sql)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getNit());
            ps.setString(3, proveedor.getTelefono());
            ps.setString(4, proveedor.getCorreo());
            ps.setString(5, proveedor.getDireccion());
            ps.executeUpdate();
            int id = idGenerado(ps);
            proveedor.setIdProveedor(id);
            return id;
        } catch (SQLException e) {
            throw error("registrar el proveedor", e);
        }
    }

    @Override
    public void actualizar(Proveedor proveedor) throws BaseDatosException {
        String sql = "UPDATE proveedor SET nombre = ?, nit = ?, telefono = ?, correo = ?, direccion = ?, estado = ? "
                + "WHERE id_proveedor = ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getNit());
            ps.setString(3, proveedor.getTelefono());
            ps.setString(4, proveedor.getCorreo());
            ps.setString(5, proveedor.getDireccion());
            ps.setBoolean(6, proveedor.isEstado());
            ps.setInt(7, proveedor.getIdProveedor());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("modificar el proveedor", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM proveedor WHERE id_proveedor = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar el proveedor", e);
        }
    }

    @Override
    public Proveedor buscarPorId(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar(CONSULTA + "WHERE pr.id_proveedor = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw error("consultar el proveedor", e);
        }
        return null;
    }

    @Override
    public ArrayList<Proveedor> listar() throws BaseDatosException {
        ArrayList<Proveedor> lista = new ArrayList<>();
        try (PreparedStatement ps = preparar(CONSULTA + "ORDER BY pr.nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw error("listar los proveedores", e);
        }
        return lista;
    }

    public ArrayList<Proveedor> listarActivos() throws BaseDatosException {
        ArrayList<Proveedor> lista = new ArrayList<>();
        try (PreparedStatement ps = preparar(CONSULTA + "WHERE pr.estado = 1 ORDER BY pr.nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw error("listar los proveedores", e);
        }
        return lista;
    }

    public ArrayList<Proveedor> buscar(String criterio, String texto) throws BaseDatosException {
        String condicion;
        int parametros = 1;
        if (criterio.equals("Nombre")) {
            condicion = "pr.nombre LIKE ?";
        } else if (criterio.equals("NIT")) {
            condicion = "pr.nit LIKE ?";
        } else if (criterio.equals("Telefono")) {
            condicion = "pr.telefono LIKE ?";
        } else {
            condicion = "(pr.nombre LIKE ? OR pr.nit LIKE ? OR pr.telefono LIKE ? OR pr.correo LIKE ?)";
            parametros = 4;
        }
        ArrayList<Proveedor> lista = new ArrayList<>();
        try (PreparedStatement ps = preparar(CONSULTA + "WHERE " + condicion + " ORDER BY pr.nombre")) {
            for (int i = 1; i <= parametros; i++) {
                ps.setString(i, patron(texto));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw error("buscar proveedores", e);
        }
        return lista;
    }

    public boolean existeNombre(String nombre, int excluirId) throws BaseDatosException {
        String sql = "SELECT COUNT(*) FROM proveedor WHERE nombre = ? AND id_proveedor <> ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, nombre.trim());
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw error("verificar el proveedor", e);
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdProveedor(rs.getInt("id_proveedor"));
        p.setNombre(rs.getString("nombre"));
        p.setNit(rs.getString("nit"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setDireccion(rs.getString("direccion"));
        p.setEstado(rs.getBoolean("estado"));
        p.setTotalProductos(rs.getInt("total_productos"));
        return p;
    }
}
