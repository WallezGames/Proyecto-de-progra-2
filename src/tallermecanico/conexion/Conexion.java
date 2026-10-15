package tallermecanico.conexion;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    private static Conexion instancia;

    private Connection conexion;
    private String servidor;
    private String puerto;
    private String base;
    private String usuario;
    private String contrasena;

    private Conexion() {
        cargarConfiguracion();
    }

    public static Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    private void cargarConfiguracion() {
        servidor = "localhost";
        puerto = "3306";
        base = "taller_mecanico";
        usuario = "root";
        contrasena = "";

        File archivo = new File("config" + File.separator + "conexion.properties");
        if (!archivo.exists()) {
            return;
        }
        Properties propiedades = new Properties();
        try (FileInputStream entrada = new FileInputStream(archivo)) {
            propiedades.load(entrada);
            servidor = propiedades.getProperty("servidor", servidor).trim();
            puerto = propiedades.getProperty("puerto", puerto).trim();
            base = propiedades.getProperty("base", base).trim();
            usuario = propiedades.getProperty("usuario", usuario).trim();
            contrasena = propiedades.getProperty("contrasena", contrasena).trim();
        } catch (Exception e) {
            System.out.println("No se pudo leer config/conexion.properties, se usan los valores por defecto.");
        }
    }

    private String getUrl() {
        return "jdbc:mysql://" + servidor + ":" + puerto + "/" + base
                + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    }

    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed() || !conexion.isValid(3)) {
            conexion = DriverManager.getConnection(getUrl(), usuario, contrasena);
        }
        return conexion;
    }

    public void iniciarTransaccion() throws SQLException {
        getConexion().setAutoCommit(false);
    }

    public void confirmarTransaccion() throws SQLException {
        conexion.commit();
        conexion.setAutoCommit(true);
    }

    public void deshacerTransaccion() {
        try {
            if (conexion != null && !conexion.getAutoCommit()) {
                conexion.rollback();
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("No se pudo deshacer la transaccion: " + e.getMessage());
        }
    }

    public boolean probarConexion() {
        try {
            return getConexion().isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    public String getDescripcion() {
        return "MySQL en " + servidor + ":" + puerto + ", base de datos " + base + ", usuario " + usuario;
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.out.println("Error al cerrar la conexion: " + e.getMessage());
        }
        conexion = null;
    }
}
