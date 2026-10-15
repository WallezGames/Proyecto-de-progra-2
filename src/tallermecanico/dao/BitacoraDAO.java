package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import tallermecanico.excepciones.BaseDatosException;

public class BitacoraDAO extends BaseDAO {

    public void registrar(int idUsuario, String modulo, String accion, String descripcion) throws BaseDatosException {
        String sql = "INSERT INTO bitacora (id_usuario, modulo, accion, descripcion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = preparar(sql)) {
            if (idUsuario > 0) {
                ps.setInt(1, idUsuario);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, modulo);
            ps.setString(3, accion);
            if (descripcion.length() > 255) {
                descripcion = descripcion.substring(0, 255);
            }
            ps.setString(4, descripcion);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("registrar la bitacora", e);
        }
    }
}
