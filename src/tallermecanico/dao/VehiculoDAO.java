package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.modelo.Cliente;
import tallermecanico.modelo.Vehiculo;

public class VehiculoDAO extends BaseDAO implements CrudDAO<Vehiculo> {

    private static final String CONSULTA = "SELECT v.*, c.nombre AS c_nombre, c.apellido AS c_apellido, "
            + "c.dpi AS c_dpi, c.nit AS c_nit, c.telefono AS c_telefono, "
            + "(SELECT COUNT(*) FROM orden_trabajo o WHERE o.id_vehiculo = v.id_vehiculo) AS total_servicios, "
            + "(SELECT COUNT(*) FROM orden_trabajo o WHERE o.id_vehiculo = v.id_vehiculo "
            + "  AND o.estado IN ('Pendiente', 'En proceso')) AS ordenes_activas "
            + "FROM vehiculo v JOIN cliente c ON c.id_cliente = v.id_cliente ";

    @Override
    public int insertar(Vehiculo vehiculo) throws BaseDatosException {
        String sql = "INSERT INTO vehiculo (placa, marca, modelo, anio, color, kilometraje, chasis, id_cliente) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = prepararConLlave(sql)) {
            ps.setString(1, vehiculo.getPlaca());
            ps.setString(2, vehiculo.getMarca());
            ps.setString(3, vehiculo.getModelo());
            ps.setInt(4, vehiculo.getAnio());
            ps.setString(5, vehiculo.getColor());
            ps.setInt(6, vehiculo.getKilometraje());
            ps.setString(7, vehiculo.getChasis());
            ps.setInt(8, vehiculo.getPropietario().getIdCliente());
            ps.executeUpdate();
            int id = idGenerado(ps);
            vehiculo.setIdVehiculo(id);
            return id;
        } catch (SQLException e) {
            throw error("registrar el vehiculo", e);
        }
    }

    @Override
    public void actualizar(Vehiculo vehiculo) throws BaseDatosException {
        String sql = "UPDATE vehiculo SET placa = ?, marca = ?, modelo = ?, anio = ?, color = ?, kilometraje = ?, "
                + "chasis = ?, id_cliente = ? WHERE id_vehiculo = ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, vehiculo.getPlaca());
            ps.setString(2, vehiculo.getMarca());
            ps.setString(3, vehiculo.getModelo());
            ps.setInt(4, vehiculo.getAnio());
            ps.setString(5, vehiculo.getColor());
            ps.setInt(6, vehiculo.getKilometraje());
            ps.setString(7, vehiculo.getChasis());
            ps.setInt(8, vehiculo.getPropietario().getIdCliente());
            ps.setInt(9, vehiculo.getIdVehiculo());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("modificar el vehiculo", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM vehiculo WHERE id_vehiculo = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar el vehiculo", e);
        }
    }

    public void cambiarEstado(int id, boolean activo) throws BaseDatosException {
        try (PreparedStatement ps = preparar("UPDATE vehiculo SET estado = ? WHERE id_vehiculo = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("cambiar el estado del vehiculo", e);
        }
    }

    @Override
    public Vehiculo buscarPorId(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar(CONSULTA + "WHERE v.id_vehiculo = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw error("consultar el vehiculo", e);
        }
        return null;
    }

    @Override
    public ArrayList<Vehiculo> listar() throws BaseDatosException {
        return consultar(CONSULTA + "ORDER BY v.placa", null, 0);
    }

    public ArrayList<Vehiculo> listarActivos() throws BaseDatosException {
        return consultar(CONSULTA + "WHERE v.estado = 1 ORDER BY v.placa", null, 0);
    }

    public ArrayList<Vehiculo> buscar(String criterio, String texto) throws BaseDatosException {
        String condicion;
        int parametros = 1;
        if (criterio.equals("Placa")) {
            condicion = "v.placa LIKE ?";
        } else if (criterio.equals("Marca")) {
            condicion = "v.marca LIKE ?";
        } else if (criterio.equals("Modelo")) {
            condicion = "v.modelo LIKE ?";
        } else if (criterio.equals("Anio")) {
            condicion = "CAST(v.anio AS CHAR) LIKE ?";
        } else if (criterio.equals("Propietario")) {
            condicion = "CONCAT(c.nombre, ' ', c.apellido) LIKE ?";
        } else {
            condicion = "(v.placa LIKE ? OR v.marca LIKE ? OR v.modelo LIKE ? OR v.chasis LIKE ? "
                    + "OR CONCAT(c.nombre, ' ', c.apellido) LIKE ?)";
            parametros = 5;
        }
        return consultar(CONSULTA + "WHERE " + condicion + " ORDER BY v.placa", texto, parametros);
    }

    public boolean existePlaca(String placa, int excluirId) throws BaseDatosException {
        return existe("SELECT COUNT(*) FROM vehiculo WHERE placa = ? AND id_vehiculo <> ?", placa, excluirId);
    }

    public boolean existeChasis(String chasis, int excluirId) throws BaseDatosException {
        return existe("SELECT COUNT(*) FROM vehiculo WHERE chasis = ? AND id_vehiculo <> ?", chasis, excluirId);
    }

    public String ordenActiva(int idVehiculo) throws BaseDatosException {
        String sql = "SELECT numero FROM orden_trabajo WHERE id_vehiculo = ? AND estado IN ('Pendiente', 'En proceso') LIMIT 1";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            throw error("verificar las ordenes del vehiculo", e);
        }
        return null;
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
            throw error("verificar el vehiculo", e);
        }
    }

    private ArrayList<Vehiculo> consultar(String sql, String texto, int parametros) throws BaseDatosException {
        ArrayList<Vehiculo> lista = new ArrayList<>();
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
            throw error("consultar los vehiculos", e);
        }
        return lista;
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        Cliente propietario = new Cliente();
        propietario.setIdCliente(rs.getInt("id_cliente"));
        propietario.setNombre(rs.getString("c_nombre"));
        propietario.setApellido(rs.getString("c_apellido"));
        propietario.setDpi(rs.getString("c_dpi"));
        propietario.setNit(rs.getString("c_nit"));
        propietario.setTelefono(rs.getString("c_telefono"));

        Vehiculo v = new Vehiculo();
        v.setIdVehiculo(rs.getInt("id_vehiculo"));
        v.setPlaca(rs.getString("placa"));
        v.setMarca(rs.getString("marca"));
        v.setModelo(rs.getString("modelo"));
        v.setAnio(rs.getInt("anio"));
        v.setColor(rs.getString("color"));
        v.setKilometraje(rs.getInt("kilometraje"));
        v.setChasis(rs.getString("chasis"));
        v.setPropietario(propietario);
        v.setTotalServicios(rs.getInt("total_servicios"));
        if (!rs.getBoolean("estado")) {
            v.setEstado("Inactivo");
        } else if (rs.getInt("ordenes_activas") > 0) {
            v.setEstado("En taller");
        } else {
            v.setEstado("Activo");
        }
        return v;
    }
}
