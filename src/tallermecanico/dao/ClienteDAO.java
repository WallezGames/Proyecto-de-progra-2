package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.modelo.Cliente;

public class ClienteDAO extends BaseDAO implements CrudDAO<Cliente> {

    private static final String CONSULTA = "SELECT c.*, "
            + "(SELECT COUNT(*) FROM vehiculo v WHERE v.id_cliente = c.id_cliente) AS total_vehiculos "
            + "FROM cliente c ";

    @Override
    public int insertar(Cliente cliente) throws BaseDatosException {
        String sql = "INSERT INTO cliente (nombre, apellido, dpi, nit, telefono, direccion, correo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = prepararConLlave(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getDpi());
            ps.setString(4, cliente.getNit());
            ps.setString(5, cliente.getTelefono());
            ps.setString(6, cliente.getDireccion());
            ps.setString(7, cliente.getCorreo());
            ps.executeUpdate();
            int id = idGenerado(ps);
            cliente.setIdCliente(id);
            return id;
        } catch (SQLException e) {
            throw error("registrar el cliente", e);
        }
    }

    @Override
    public void actualizar(Cliente cliente) throws BaseDatosException {
        String sql = "UPDATE cliente SET nombre = ?, apellido = ?, dpi = ?, nit = ?, telefono = ?, direccion = ?, "
                + "correo = ?, estado = ? WHERE id_cliente = ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getDpi());
            ps.setString(4, cliente.getNit());
            ps.setString(5, cliente.getTelefono());
            ps.setString(6, cliente.getDireccion());
            ps.setString(7, cliente.getCorreo());
            ps.setBoolean(8, cliente.isEstado());
            ps.setInt(9, cliente.getIdCliente());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("modificar el cliente", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM cliente WHERE id_cliente = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar el cliente", e);
        }
    }

    @Override
    public Cliente buscarPorId(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar(CONSULTA + "WHERE c.id_cliente = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw error("consultar el cliente", e);
        }
        return null;
    }

    @Override
    public ArrayList<Cliente> listar() throws BaseDatosException {
        ArrayList<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = preparar(CONSULTA + "ORDER BY c.apellido, c.nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw error("listar los clientes", e);
        }
        return lista;
    }

    public ArrayList<Cliente> listarActivos() throws BaseDatosException {
        ArrayList<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = preparar(CONSULTA + "WHERE c.estado = 1 ORDER BY c.apellido, c.nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw error("listar los clientes", e);
        }
        return lista;
    }

    public ArrayList<Cliente> buscar(String criterio, String texto) throws BaseDatosException {
        String condicion;
        int parametros = 1;
        if (criterio.equals("Nombre")) {
            condicion = "CONCAT(c.nombre, ' ', c.apellido) LIKE ?";
        } else if (criterio.equals("DPI")) {
            condicion = "c.dpi LIKE ?";
        } else if (criterio.equals("NIT")) {
            condicion = "c.nit LIKE ?";
        } else if (criterio.equals("Telefono")) {
            condicion = "c.telefono LIKE ?";
        } else if (criterio.equals("Correo")) {
            condicion = "c.correo LIKE ?";
        } else if (criterio.equals("Direccion")) {
            condicion = "c.direccion LIKE ?";
        } else {
            condicion = "(CONCAT(c.nombre, ' ', c.apellido) LIKE ? OR c.dpi LIKE ? OR c.nit LIKE ? "
                    + "OR c.telefono LIKE ? OR c.correo LIKE ? OR c.direccion LIKE ?)";
            parametros = 6;
        }
        ArrayList<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = preparar(CONSULTA + "WHERE " + condicion + " ORDER BY c.apellido, c.nombre")) {
            for (int i = 1; i <= parametros; i++) {
                ps.setString(i, patron(texto));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw error("buscar clientes", e);
        }
        return lista;
    }

    public boolean existeDpi(String dpi, int excluirId) throws BaseDatosException {
        String sql = "SELECT COUNT(*) FROM cliente WHERE dpi = ? AND id_cliente <> ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, dpi.trim());
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw error("verificar el DPI", e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setDpi(rs.getString("dpi"));
        c.setNit(rs.getString("nit"));
        c.setTelefono(rs.getString("telefono"));
        c.setDireccion(rs.getString("direccion"));
        c.setCorreo(rs.getString("correo"));
        c.setFechaRegistro(aFechaHora(rs.getTimestamp("fecha_registro")));
        c.setEstado(rs.getBoolean("estado"));
        c.setTotalVehiculos(rs.getInt("total_vehiculos"));
        return c;
    }
}
