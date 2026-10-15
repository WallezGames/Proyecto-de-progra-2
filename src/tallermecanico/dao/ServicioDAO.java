package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.modelo.Servicio;
import tallermecanico.modelo.Taller;

public class ServicioDAO extends BaseDAO implements CrudDAO<Servicio> {

    private static final String CONSULTA = "SELECT s.*, t.nombre AS taller_nombre "
            + "FROM servicio s JOIN taller t ON t.id_taller = s.id_taller ";

    @Override
    public int insertar(Servicio servicio) throws BaseDatosException {
        String sql = "INSERT INTO servicio (nombre, descripcion, id_taller, precio) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = prepararConLlave(sql)) {
            ps.setString(1, servicio.getNombre());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getTaller().getId());
            ps.setDouble(4, servicio.getPrecio());
            ps.executeUpdate();
            int id = idGenerado(ps);
            servicio.setIdServicio(id);
            return id;
        } catch (SQLException e) {
            throw error("registrar el servicio", e);
        }
    }

    @Override
    public void actualizar(Servicio servicio) throws BaseDatosException {
        String sql = "UPDATE servicio SET nombre = ?, descripcion = ?, id_taller = ?, precio = ?, estado = ? "
                + "WHERE id_servicio = ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, servicio.getNombre());
            ps.setString(2, servicio.getDescripcion());
            ps.setInt(3, servicio.getTaller().getId());
            ps.setDouble(4, servicio.getPrecio());
            ps.setBoolean(5, servicio.isEstado());
            ps.setInt(6, servicio.getIdServicio());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("modificar el servicio", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM servicio WHERE id_servicio = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar el servicio", e);
        }
    }

    @Override
    public Servicio buscarPorId(int id) throws BaseDatosException {
        ArrayList<Servicio> lista = consultar(CONSULTA + "WHERE s.id_servicio = " + id, null, 0);
        if (lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    @Override
    public ArrayList<Servicio> listar() throws BaseDatosException {
        return consultar(CONSULTA + "ORDER BY t.id_taller, s.nombre", null, 0);
    }

    public ArrayList<Servicio> listarPorTaller(int idTaller) throws BaseDatosException {
        return consultar(CONSULTA + "WHERE s.estado = 1 AND s.id_taller = " + idTaller + " ORDER BY s.nombre", null, 0);
    }

    public ArrayList<Servicio> buscar(String criterio, String texto) throws BaseDatosException {
        String condicion;
        int parametros = 1;
        if (criterio.equals("Nombre")) {
            condicion = "s.nombre LIKE ?";
        } else if (criterio.equals("Taller")) {
            condicion = "t.nombre LIKE ?";
        } else {
            condicion = "(s.nombre LIKE ? OR s.descripcion LIKE ? OR t.nombre LIKE ?)";
            parametros = 3;
        }
        return consultar(CONSULTA + "WHERE " + condicion + " ORDER BY t.id_taller, s.nombre", texto, parametros);
    }

    public boolean existe(String nombre, int idTaller, int excluirId) throws BaseDatosException {
        String sql = "SELECT COUNT(*) FROM servicio WHERE nombre = ? AND id_taller = ? AND id_servicio <> ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, nombre.trim());
            ps.setInt(2, idTaller);
            ps.setInt(3, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw error("verificar el servicio", e);
        }
    }

    public int vecesUtilizado(int idServicio) throws BaseDatosException {
        try (PreparedStatement ps = preparar("SELECT COUNT(*) FROM detalle_orden WHERE id_servicio = ?")) {
            ps.setInt(1, idServicio);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw error("verificar el servicio", e);
        }
    }

    private ArrayList<Servicio> consultar(String sql, String texto, int parametros) throws BaseDatosException {
        ArrayList<Servicio> lista = new ArrayList<>();
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
            throw error("consultar los servicios", e);
        }
        return lista;
    }

    private Servicio mapear(ResultSet rs) throws SQLException {
        Taller taller = new Taller();
        taller.setId(rs.getInt("id_taller"));
        taller.setNombreTaller(rs.getString("taller_nombre"));

        Servicio s = new Servicio();
        s.setIdServicio(rs.getInt("id_servicio"));
        s.setNombre(rs.getString("nombre"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setTaller(taller);
        s.setPrecio(rs.getDouble("precio"));
        s.setEstado(rs.getBoolean("estado"));
        return s;
    }
}
