package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tallermecanico.conexion.Conexion;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.modelo.Rol;

public class RolDAO extends BaseDAO {

    public ArrayList<Rol> listar() throws BaseDatosException {
        ArrayList<Rol> roles = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT * FROM rol ORDER BY id_rol");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rol rol = new Rol(rs.getInt("id_rol"), rs.getString("nombre"));
                rol.setDescripcion(rs.getString("descripcion"));
                roles.add(rol);
            }
        } catch (SQLException e) {
            throw error("listar los roles", e);
        }
        for (int i = 0; i < roles.size(); i++) {
            roles.get(i).setPermisos(permisosDe(roles.get(i).getIdRol()));
        }
        return roles;
    }

    public Rol buscarPorNombre(String nombre) throws BaseDatosException {
        Rol rol = null;
        try (PreparedStatement ps = preparar("SELECT * FROM rol WHERE nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rol = new Rol(rs.getInt("id_rol"), rs.getString("nombre"));
                    rol.setDescripcion(rs.getString("descripcion"));
                }
            }
        } catch (SQLException e) {
            throw error("consultar el rol", e);
        }
        if (rol != null) {
            rol.setPermisos(permisosDe(rol.getIdRol()));
        }
        return rol;
    }

    public ArrayList<String> permisosDe(int idRol) throws BaseDatosException {
        ArrayList<String> modulos = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT modulo FROM permiso WHERE id_rol = ?")) {
            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modulos.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw error("consultar los permisos", e);
        }
        return modulos;
    }

    public void guardarPermisos(Rol rol) throws BaseDatosException {
        Conexion bd = Conexion.getInstancia();
        try {
            bd.iniciarTransaccion();
            try (PreparedStatement ps = preparar("DELETE FROM permiso WHERE id_rol = ?")) {
                ps.setInt(1, rol.getIdRol());
                ps.executeUpdate();
            }
            for (int i = 0; i < rol.getPermisos().size(); i++) {
                try (PreparedStatement ps = preparar("INSERT INTO permiso (id_rol, modulo) VALUES (?, ?)")) {
                    ps.setInt(1, rol.getIdRol());
                    ps.setString(2, rol.getPermisos().get(i));
                    ps.executeUpdate();
                }
            }
            bd.confirmarTransaccion();
        } catch (SQLException e) {
            bd.deshacerTransaccion();
            throw error("guardar los permisos", e);
        } catch (BaseDatosException e) {
            bd.deshacerTransaccion();
            throw e;
        }
    }
}
