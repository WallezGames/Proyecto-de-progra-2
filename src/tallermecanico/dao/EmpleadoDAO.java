package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.fabrica.EmpleadoFactory;
import tallermecanico.modelo.Bodeguero;
import tallermecanico.modelo.Empleado;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.Taller;

public class EmpleadoDAO extends BaseDAO implements CrudDAO<Empleado> {

    private static final String CONSULTA = "SELECT e.*, r.nombre AS rol_nombre, t.nombre AS taller_nombre "
            + "FROM empleado e JOIN rol r ON r.id_rol = e.id_rol "
            + "LEFT JOIN taller t ON t.id_taller = e.id_taller ";

    @Override
    public int insertar(Empleado empleado) throws BaseDatosException {
        String sql = "INSERT INTO empleado (codigo, nombre, apellido, dpi, telefono, direccion, correo, fecha_contratacion, "
                + "id_rol, especialidad, area_trabajo, turno, nivel_acceso, hora_entrada, hora_salida) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT id_rol FROM rol WHERE nombre = ?), ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = prepararConLlave(sql)) {
            ps.setString(1, empleado.getCodigoEmpleado());
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getApellido());
            ps.setString(4, empleado.getDpi());
            ps.setString(5, empleado.getTelefono());
            ps.setString(6, empleado.getDireccion());
            ps.setString(7, empleado.getCorreo());
            ps.setDate(8, aDate(empleado.getFechaContratacion()));
            ps.setString(9, empleado.getPuesto());
            asignarDatosPuesto(ps, 10, empleado);
            asignarHora(ps, 14, empleado.getEntrada());
            asignarHora(ps, 15, empleado.getSalida());
            ps.executeUpdate();
            int id = idGenerado(ps);
            empleado.setIdEmpleado(id);
            return id;
        } catch (SQLException e) {
            throw error("registrar el empleado", e);
        }
    }

    @Override
    public void actualizar(Empleado empleado) throws BaseDatosException {
        String sql = "UPDATE empleado SET codigo = ?, nombre = ?, apellido = ?, dpi = ?, telefono = ?, direccion = ?, "
                + "correo = ?, especialidad = ?, area_trabajo = ?, turno = ?, nivel_acceso = ?, estado = ? "
                + "WHERE id_empleado = ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, empleado.getCodigoEmpleado());
            ps.setString(2, empleado.getNombre());
            ps.setString(3, empleado.getApellido());
            ps.setString(4, empleado.getDpi());
            ps.setString(5, empleado.getTelefono());
            ps.setString(6, empleado.getDireccion());
            ps.setString(7, empleado.getCorreo());
            asignarDatosPuesto(ps, 8, empleado);
            ps.setBoolean(12, empleado.isEstado());
            ps.setInt(13, empleado.getIdEmpleado());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("modificar el empleado", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM empleado WHERE id_empleado = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar el empleado", e);
        }
    }

    public void cambiarEstado(int id, boolean activo) throws BaseDatosException {
        try (PreparedStatement ps = preparar("UPDATE empleado SET estado = ? WHERE id_empleado = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("cambiar el estado del empleado", e);
        }
    }

    public void asignarTaller(int idMecanico, int idTaller) throws BaseDatosException {
        try (PreparedStatement ps = preparar("UPDATE empleado SET id_taller = ? WHERE id_empleado = ?")) {
            if (idTaller > 0) {
                ps.setInt(1, idTaller);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, idMecanico);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("asignar el taller del mecanico", e);
        }
    }

    @Override
    public Empleado buscarPorId(int id) throws BaseDatosException {
        ArrayList<Empleado> lista = consultar(CONSULTA + "WHERE e.id_empleado = " + id, null, 0);
        if (lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    @Override
    public ArrayList<Empleado> listar() throws BaseDatosException {
        return consultar(CONSULTA + "ORDER BY e.codigo", null, 0);
    }

    public ArrayList<Empleado> listarPorPuesto(String puesto) throws BaseDatosException {
        ArrayList<Empleado> lista = new ArrayList<>();
        String sql = CONSULTA + "WHERE r.nombre = ? AND e.estado = 1 ORDER BY e.apellido, e.nombre";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, puesto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw error("listar los empleados", e);
        }
        return lista;
    }

    public ArrayList<Mecanico> listarMecanicos() throws BaseDatosException {
        ArrayList<Mecanico> mecanicos = new ArrayList<>();
        ArrayList<Empleado> lista = listarPorPuesto("Mecanico");
        for (int i = 0; i < lista.size(); i++) {
            mecanicos.add((Mecanico) lista.get(i));
        }
        return mecanicos;
    }

    public ArrayList<Mecanico> listarMecanicosDeTaller(int idTaller) throws BaseDatosException {
        ArrayList<Mecanico> mecanicos = new ArrayList<>();
        ArrayList<Empleado> lista = consultar(CONSULTA + "WHERE r.nombre = 'Mecanico' AND e.estado = 1 AND e.id_taller = "
                + idTaller + " ORDER BY e.apellido", null, 0);
        for (int i = 0; i < lista.size(); i++) {
            mecanicos.add((Mecanico) lista.get(i));
        }
        return mecanicos;
    }

    public ArrayList<JefeTaller> listarJefes() throws BaseDatosException {
        ArrayList<JefeTaller> jefes = new ArrayList<>();
        ArrayList<Empleado> lista = listarPorPuesto("Jefe de Taller");
        for (int i = 0; i < lista.size(); i++) {
            jefes.add((JefeTaller) lista.get(i));
        }
        return jefes;
    }

    public ArrayList<Bodeguero> listarBodegueros() throws BaseDatosException {
        ArrayList<Bodeguero> bodegueros = new ArrayList<>();
        ArrayList<Empleado> lista = listarPorPuesto("Bodeguero");
        for (int i = 0; i < lista.size(); i++) {
            bodegueros.add((Bodeguero) lista.get(i));
        }
        return bodegueros;
    }

    public ArrayList<Empleado> buscar(String criterio, String texto) throws BaseDatosException {
        String condicion;
        int parametros = 1;
        if (criterio.equals("Nombre")) {
            condicion = "CONCAT(e.nombre, ' ', e.apellido) LIKE ?";
        } else if (criterio.equals("Codigo")) {
            condicion = "e.codigo LIKE ?";
        } else if (criterio.equals("DPI")) {
            condicion = "e.dpi LIKE ?";
        } else if (criterio.equals("Puesto")) {
            condicion = "r.nombre LIKE ?";
        } else if (criterio.equals("Taller")) {
            condicion = "t.nombre LIKE ?";
        } else {
            condicion = "(CONCAT(e.nombre, ' ', e.apellido) LIKE ? OR e.codigo LIKE ? OR r.nombre LIKE ? OR t.nombre LIKE ?)";
            parametros = 4;
        }
        return consultar(CONSULTA + "WHERE " + condicion + " ORDER BY e.codigo", texto, parametros);
    }

    public boolean existeCodigo(String codigo, int excluirId) throws BaseDatosException {
        return existe("SELECT COUNT(*) FROM empleado WHERE codigo = ? AND id_empleado <> ?", codigo, excluirId);
    }

    public boolean existeDpi(String dpi, int excluirId) throws BaseDatosException {
        return existe("SELECT COUNT(*) FROM empleado WHERE dpi = ? AND id_empleado <> ?", dpi, excluirId);
    }

    public String usuarioAsociado(int idEmpleado) throws BaseDatosException {
        return primerTexto("SELECT username FROM usuario WHERE id_empleado = ?", idEmpleado);
    }

    public String tallerACargo(int idJefe) throws BaseDatosException {
        return primerTexto("SELECT nombre FROM taller WHERE id_jefe = ?", idJefe);
    }

    public String ordenActivaDeMecanico(int idMecanico) throws BaseDatosException {
        return primerTexto("SELECT o.numero FROM orden_mecanico om JOIN orden_trabajo o ON o.id_orden = om.id_orden "
                + "WHERE om.id_mecanico = ? AND o.estado IN ('Pendiente', 'En proceso') LIMIT 1", idMecanico);
    }

    public int totalMovimientos(int idEmpleado) throws BaseDatosException {
        String sql = "SELECT (SELECT COUNT(*) FROM orden_trabajo WHERE id_registra = ?) "
                + "+ (SELECT COUNT(*) FROM orden_mecanico WHERE id_mecanico = ?) "
                + "+ (SELECT COUNT(*) FROM requerimiento WHERE id_solicitante = ? OR id_autoriza = ? OR id_procesa = ?)";
        try (PreparedStatement ps = preparar(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setInt(i, idEmpleado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw error("verificar el historial del empleado", e);
        }
    }

    private String primerTexto(String sql, int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            throw error("consultar el empleado", e);
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
            throw error("verificar el empleado", e);
        }
    }

    private void asignarDatosPuesto(PreparedStatement ps, int inicio, Empleado empleado) throws SQLException {
        String puesto = empleado.getPuesto();
        String dato = empleado.getDatoPuesto();
        ps.setString(inicio, puesto.equals("Mecanico") ? dato : null);
        ps.setString(inicio + 1, puesto.equals("Jefe de Taller") || puesto.equals("Bodeguero") ? dato : null);
        ps.setString(inicio + 2, puesto.equals("Recepcionista") ? dato : null);
        ps.setString(inicio + 3, puesto.equals("Administrador") ? dato : null);
    }

    private void asignarHora(PreparedStatement ps, int posicion, java.time.LocalTime hora) throws SQLException {
        if (hora == null) {
            ps.setNull(posicion, Types.TIME);
        } else {
            ps.setTime(posicion, Time.valueOf(hora));
        }
    }

    private ArrayList<Empleado> consultar(String sql, String texto, int parametros) throws BaseDatosException {
        ArrayList<Empleado> lista = new ArrayList<>();
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
            throw error("consultar los empleados", e);
        }
        return lista;
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        String puesto = rs.getString("rol_nombre");
        Empleado e = EmpleadoFactory.crear(puesto);
        e.setIdEmpleado(rs.getInt("id_empleado"));
        e.setCodigoEmpleado(rs.getString("codigo"));
        e.setNombre(rs.getString("nombre"));
        e.setApellido(rs.getString("apellido"));
        e.setDpi(rs.getString("dpi"));
        e.setTelefono(rs.getString("telefono"));
        e.setDireccion(rs.getString("direccion"));
        e.setCorreo(rs.getString("correo"));
        e.setFechaContratacion(rs.getDate("fecha_contratacion").toLocalDate());
        e.setEstado(rs.getBoolean("estado"));
        if (rs.getTime("hora_entrada") != null) {
            e.setEntrada(rs.getTime("hora_entrada").toLocalTime());
        }
        if (rs.getTime("hora_salida") != null) {
            e.setSalida(rs.getTime("hora_salida").toLocalTime());
        }
        if (puesto.equals("Mecanico")) {
            e.setDatoPuesto(rs.getString("especialidad"));
            int idTaller = rs.getInt("id_taller");
            if (idTaller > 0) {
                Taller taller = new Taller();
                taller.setId(idTaller);
                taller.setNombreTaller(rs.getString("taller_nombre"));
                ((Mecanico) e).setTaller(taller);
            }
        } else if (puesto.equals("Jefe de Taller") || puesto.equals("Bodeguero")) {
            e.setDatoPuesto(rs.getString("area_trabajo"));
        } else if (puesto.equals("Recepcionista")) {
            e.setDatoPuesto(rs.getString("turno"));
        } else {
            e.setDatoPuesto(rs.getString("nivel_acceso"));
        }
        return e;
    }
}
