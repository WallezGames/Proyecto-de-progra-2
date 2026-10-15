package tallermecanico.controlador;

import tallermecanico.dao.BitacoraDAO;

public class Auditoria {

    private static BitacoraDAO dao = new BitacoraDAO();

    public static void registrar(String modulo, String accion, String descripcion) {
        try {
            dao.registrar(Sesion.idUsuario(), modulo, accion, descripcion);
        } catch (Exception e) {
            System.out.println("No se pudo registrar en la bitacora: " + e.getMessage());
        }
    }
}
