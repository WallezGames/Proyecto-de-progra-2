package tallermecanico.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import tallermecanico.conexion.Conexion;
import tallermecanico.excepciones.BaseDatosException;

public abstract class BaseDAO {

    protected Connection conexion() throws BaseDatosException {
        try {
            return Conexion.getInstancia().getConexion();
        } catch (SQLException e) {
            throw new BaseDatosException("No se pudo conectar con la base de datos. Verifique que el servidor MySQL este iniciado.", e);
        }
    }

    protected BaseDatosException error(String accion, SQLException e) {
        int codigo = e.getErrorCode();
        if (codigo == 1062) {
            return new BaseDatosException("No se pudo " + accion + ": ya existe un registro con ese dato, que debe ser unico.", e);
        }
        if (codigo == 1451) {
            return new BaseDatosException("No se pudo " + accion + ": el registro tiene informacion relacionada en otras tablas.", e);
        }
        if (codigo == 1452) {
            return new BaseDatosException("No se pudo " + accion + ": el registro relacionado no existe.", e);
        }
        if (codigo == 3819 || codigo == 4025) {
            return new BaseDatosException("No se pudo " + accion + ": los datos no cumplen una restriccion de la base de datos.", e);
        }
        if (e.getSQLState() != null && e.getSQLState().startsWith("08")) {
            return new BaseDatosException("Se perdio la conexion con la base de datos.", e);
        }
        return new BaseDatosException("Error de base de datos al " + accion + ": " + e.getMessage(), e);
    }

    protected int idGenerado(PreparedStatement sentencia) throws SQLException {
        try (ResultSet llaves = sentencia.getGeneratedKeys()) {
            if (llaves.next()) {
                return llaves.getInt(1);
            }
        }
        return 0;
    }

    protected PreparedStatement preparar(String sql) throws BaseDatosException, SQLException {
        return conexion().prepareStatement(sql);
    }

    protected PreparedStatement prepararConLlave(String sql) throws BaseDatosException, SQLException {
        return conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    protected Timestamp aTimestamp(LocalDateTime fecha) {
        if (fecha == null) {
            return null;
        }
        return Timestamp.valueOf(fecha);
    }

    protected LocalDateTime aFechaHora(Timestamp valor) {
        if (valor == null) {
            return null;
        }
        return valor.toLocalDateTime();
    }

    protected java.sql.Date aDate(LocalDate fecha) {
        if (fecha == null) {
            return null;
        }
        return java.sql.Date.valueOf(fecha);
    }

    protected String patron(String texto) {
        return "%" + texto.trim() + "%";
    }
}
