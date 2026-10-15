package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.fabrica.EmpleadoFactory;
import tallermecanico.modelo.Empleado;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.Taller;
import tallermecanico.modelo.Usuario;

public class UsuarioDAO extends BaseDAO implements CrudDAO<Usuario> {

    private static final String CONSULTA = "SELECT u.id_usuario, u.username, u.contrasena, u.estado AS u_estado, "
            + "u.ultimo_acceso, e.id_empleado, e.codigo, e.nombre, e.apellido, e.id_taller, r.nombre AS rol_nombre "
            + "FROM usuario u JOIN empleado e ON e.id_empleado = u.id_empleado "
            + "JOIN rol r ON r.id_rol = e.id_rol ";

    @Override
    public int insertar(Usuario usuario) throws BaseDatosException {
        String sql = "INSERT INTO usuario (username, contrasena, id_empleado) VALUES (?, ?, ?)";
        try (PreparedStatement ps = prepararConLlave(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getContrasena());
            ps.setInt(3, usuario.getEmpleado().getIdEmpleado());
            ps.executeUpdate();
            int id = idGenerado(ps);
            usuario.setId(id);
            return id;
        } catch (SQLException e) {
            throw error("registrar el usuario", e);
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws BaseDatosException {
        String sql = "UPDATE usuario SET username = ?, id_empleado = ?, estado = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setInt(2, usuario.getEmpleado().getIdEmpleado());
            ps.setBoolean(3, usuario.isEstado());
            ps.setInt(4, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("modificar el usuario", e);
        }
    }

    public void cambiarContrasena(int idUsuario, String contrasenaEncriptada) throws BaseDatosException {
        try (PreparedStatement ps = preparar("UPDATE usuario SET contrasena = ? WHERE id_usuario = ?")) {
            ps.setString(1, contrasenaEncriptada);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("cambiar la contrasena", e);
        }
    }

    public void registrarAcceso(int idUsuario) throws BaseDatosException {
        try (PreparedStatement ps = preparar("UPDATE usuario SET ultimo_acceso = NOW() WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("registrar el acceso", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM usuario WHERE id_usuario = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar el usuario", e);
        }
    }

    @Override
    public Usuario buscarPorId(int id) throws BaseDatosException {
        ArrayList<Usuario> lista = consultar(CONSULTA + "WHERE u.id_usuario = " + id, null, 0);
        if (lista.isEmpty()) {
            return null;
        }
        return lista.get(0);
    }

    public Usuario buscarPorUsername(String username) throws BaseDatosException {
        try (PreparedStatement ps = preparar(CONSULTA + "WHERE u.username = ?")) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw error("consultar el usuario", e);
        }
        return null;
    }

    @Override
    public ArrayList<Usuario> listar() throws BaseDatosException {
        return consultar(CONSULTA + "ORDER BY u.id_usuario", null, 0);
    }

    public ArrayList<Usuario> buscar(String criterio, String texto) throws BaseDatosException {
        String condicion;
        int parametros = 1;
        if (criterio.equals("Usuario")) {
            condicion = "u.username LIKE ?";
        } else if (criterio.equals("Empleado")) {
            condicion = "CONCAT(e.nombre, ' ', e.apellido) LIKE ?";
        } else if (criterio.equals("Puesto")) {
            condicion = "r.nombre LIKE ?";
        } else {
            condicion = "(u.username LIKE ? OR CONCAT(e.nombre, ' ', e.apellido) LIKE ? OR r.nombre LIKE ?)";
            parametros = 3;
        }
        return consultar(CONSULTA + "WHERE " + condicion + " ORDER BY u.id_usuario", texto, parametros);
    }

    public boolean existeUsername(String username, int excluirId) throws BaseDatosException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE username = ? AND id_usuario <> ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, username.trim());
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw error("verificar el usuario", e);
        }
    }

    public String usuarioDeEmpleado(int idEmpleado, int excluirId) throws BaseDatosException {
        String sql = "SELECT username FROM usuario WHERE id_empleado = ? AND id_usuario <> ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setInt(1, idEmpleado);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            throw error("verificar el usuario", e);
        }
        return null;
    }

    public int contarAdministradoresActivos() throws BaseDatosException {
        String sql = "SELECT COUNT(*) FROM usuario u JOIN empleado e ON e.id_empleado = u.id_empleado "
                + "JOIN rol r ON r.id_rol = e.id_rol WHERE r.nombre = 'Administrador' AND u.estado = 1";
        try (PreparedStatement ps = preparar(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw error("contar los administradores", e);
        }
    }

    private ArrayList<Usuario> consultar(String sql, String texto, int parametros) throws BaseDatosException {
        ArrayList<Usuario> lista = new ArrayList<>();
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
            throw error("consultar los usuarios", e);
        }
        return lista;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Empleado empleado = EmpleadoFactory.crear(rs.getString("rol_nombre"));
        empleado.setIdEmpleado(rs.getInt("id_empleado"));
        empleado.setCodigoEmpleado(rs.getString("codigo"));
        empleado.setNombre(rs.getString("nombre"));
        empleado.setApellido(rs.getString("apellido"));
        int idTaller = rs.getInt("id_taller");
        if (empleado instanceof Mecanico && idTaller > 0) {
            Taller taller = new Taller();
            taller.setId(idTaller);
            ((Mecanico) empleado).setTaller(taller);
        }

        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("username"));
        u.setContrasenaEncriptada(rs.getString("contrasena"));
        u.setEstado(rs.getBoolean("u_estado"));
        u.setUltimoAcceso(aFechaHora(rs.getTimestamp("ultimo_acceso")));
        u.setEmpleado(empleado);
        return u;
    }
}
