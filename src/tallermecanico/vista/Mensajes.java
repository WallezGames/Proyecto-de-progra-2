package tallermecanico.vista;

import java.awt.Component;
import javax.swing.JOptionPane;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;

public class Mensajes {

    public static void exito(Component padre, String texto, String titulo) {
        JOptionPane.showMessageDialog(padre, texto, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(Component padre, String texto, String titulo) {
        JOptionPane.showMessageDialog(padre, texto, titulo, JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmar(Component padre, String texto, String titulo) {
        int respuesta = JOptionPane.showConfirmDialog(padre, texto, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return respuesta == JOptionPane.YES_OPTION;
    }

    public static void error(Component padre, Exception ex) {
        if (ex instanceof DatoInvalidoException) {
            JOptionPane.showMessageDialog(padre, ex.getMessage(), "Dato invalido", JOptionPane.WARNING_MESSAGE);
        } else if (ex instanceof ReglaNegocioException) {
            JOptionPane.showMessageDialog(padre, ex.getMessage(), "Regla de negocio", JOptionPane.ERROR_MESSAGE);
        } else if (ex instanceof BaseDatosException) {
            JOptionPane.showMessageDialog(padre, ex.getMessage(), "Base de datos", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(padre, "Ocurrio un error inesperado: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
