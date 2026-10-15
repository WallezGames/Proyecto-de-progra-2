package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Taller;

public class TallerDAO extends BaseDAO implements CrudDAO<Taller> {

    private static final String CONSULTA = "SELECT t.*, e.nombre AS j_nombre, e.apellido AS j_apellido, e.codigo AS j_codigo, "
            + "(SELECT COUNT(*) FROM orden_trabajo o WHERE o.id_taller = t.id_taller "
            + "  AND o.estado IN ('Pendiente', 'En proceso')) AS ordenes_activas "
            + "FROM taller t LEFT JOIN empleado e ON e.id_empleado = t.id_jefe ";

    private EmpleadoDAO empleadoDAO;

    public TallerDAO() {
        empleadoDAO = new EmpleadoDAO();
    }

    @Override
    public int insertar(Taller taller) throws BaseDatosException {
        String sql = "INSERT INTO taller (nombre, descripcion, id_jefe) VALUES (?, ?, ?)";
        try (PreparedStatement ps = prepararConLlave(sql)) {
            ps.setString(1, taller.getNombreTaller());
            ps.setString(2, taller.getDescripcion());
            asignarJefe(ps, 3, taller.getJefeCargo());
            ps.executeUpdate();
            int id = idGenerado(ps);
            taller.setId(id);
            return id;
        } catch (SQLException e) {
            throw error("registrar el taller", e);
        }
    }

    @Override
    public void actualizar(Taller taller) throws BaseDatosException {
        String sql = "UPDATE taller SET nombre = ?, descripcion = ?, id_jefe = ?, estado = ? WHERE id_taller = ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, taller.getNombreTaller());
            ps.setString(2, taller.getDescripcion());
            asignarJefe(ps, 3, taller.getJefeCargo());
            ps.setBoolean(4, taller.isEstado());
            ps.setInt(5, taller.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("modificar el taller", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM taller WHERE id_taller = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar el taller", e);
        }
    }

    @Override
    public Taller buscarPorId(int id) throws BaseDatosException {
        ArrayList<Taller> lista = consultar(CONSULTA + "WHERE t.id_taller = " + id, null, 0, true);
        if (lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    @Override
    public ArrayList<Taller> listar() throws BaseDatosException {
        return consultar(CONSULTA + "ORDER BY t.id_taller", null, 0, true);
    }

    public ArrayList<Taller> listarActivos() throws BaseDatosException {
        return consultar(CONSULTA + "WHERE t.estado = 1 ORDER BY t.nombre", null, 0, false);
    }

    public ArrayList<Taller> buscar(String criterio, String texto) throws BaseDatosException {
        String condicion;
        int parametros = 1;
        if (criterio.equals("Nombre")) {
            condicion = "t.nombre LIKE ?";
        } else if (criterio.equals("Jefe")) {
            condicion = "CONCAT(e.nombre, ' ', e.apellido) LIKE ?";
        } else if (criterio.equals("Descripcion")) {
            condicion = "t.descripcion LIKE ?";
        } else {
            condicion = "(t.nombre LIKE ? OR t.descripcion LIKE ? OR CONCAT(e.nombre, ' ', e.apellido) LIKE ?)";
            parametros = 3;
        }
        return consultar(CONSULTA + "WHERE " + condicion + " ORDER BY t.id_taller", texto, parametros, true);
    }

    public boolean existeNombre(String nombre, int excluirId) throws BaseDatosException {
        String sql = "SELECT COUNT(*) FROM taller WHERE nombre = ? AND id_taller <> ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, nombre.trim());
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw error("verificar el taller", e);
        }
    }

    public String tallerDelJefe(int idJefe, int excluirTaller) throws BaseDatosException {
        String sql = "SELECT nombre FROM taller WHERE id_jefe = ? AND id_taller <> ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setInt(1, idJefe);
            ps.setInt(2, excluirTaller);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            throw error("verificar el jefe del taller", e);
        }
        return null;
    }

    public int contarRelacionados(int idTaller) throws BaseDatosException {
        String sql = "SELECT (SELECT COUNT(*) FROM orden_trabajo WHERE id_taller = ?) "
                + "+ (SELECT COUNT(*) FROM servicio WHERE id_taller = ?) "
                + "+ (SELECT COUNT(*) FROM empleado WHERE id_taller = ?)";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setInt(1, idTaller);
            ps.setInt(2, idTaller);
            ps.setInt(3, idTaller);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw error("verificar el taller", e);
        }
    }

    private void asignarJefe(PreparedStatement ps, int posicion, JefeTaller jefe) throws SQLException {
        if (jefe == null) {
            ps.setNull(posicion, Types.INTEGER);
        } else {
            ps.setInt(posicion, jefe.getIdEmpleado());
        }
    }

    private ArrayList<Taller> consultar(String sql, String texto, int parametros, boolean conMecanicos)
            throws BaseDatosException {
        ArrayList<Taller> lista = new ArrayList<>();
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
            throw error("consultar los talleres", e);
        }
        if (conMecanicos) {
            for (int i = 0; i < lista.size(); i++) {
                Taller t = lista.get(i);
                t.setMecanicos(empleadoDAO.listarMecanicosDeTaller(t.getId()));
            }
        }
        return lista;
    }

    private Taller mapear(ResultSet rs) throws SQLException {
        Taller t = new Taller();
        t.setId(rs.getInt("id_taller"));
        t.setNombreTaller(rs.getString("nombre"));
        t.setDescripcion(rs.getString("descripcion"));
        t.setEstado(rs.getBoolean("estado"));
        t.setTotalOrdenesActivas(rs.getInt("ordenes_activas"));
        int idJefe = rs.getInt("id_jefe");
        if (idJefe > 0) {
            JefeTaller jefe = new JefeTaller();
            jefe.setIdEmpleado(idJefe);
            jefe.setNombre(rs.getString("j_nombre"));
            jefe.setApellido(rs.getString("j_apellido"));
            jefe.setCodigoEmpleado(rs.getString("j_codigo"));
            t.setJefeCargo(jefe);
        }
        return t;
    }
}
