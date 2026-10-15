package tallermecanico;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import tallermecanico.conexion.Conexion;
import tallermecanico.vista.FrmLogin;

public class TallerMecanico {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("No se pudo aplicar la apariencia: " + e.getMessage());
        }

        Conexion conexion = Conexion.getInstancia();
        if (!conexion.probarConexion()) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar con la base de datos.\n\n"
                    + "Datos de conexion: " + conexion.getDescripcion() + "\n\n"
                    + "Verifique que:\n"
                    + "1. El servicio de MySQL este iniciado.\n"
                    + "2. Se haya ejecutado el script de la carpeta Base_de_Datos.\n"
                    + "3. El usuario y la contrasena de config/conexion.properties sean correctos.\n"
                    + "4. El archivo mysql-connector-j este agregado en las librerias del proyecto.",
                    "Sistema de Talleres - Error de conexion", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FrmLogin login = new FrmLogin();
                login.setVisible(true);
            }
        });
    }
}
