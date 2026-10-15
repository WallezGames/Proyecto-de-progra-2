package tallermecanico.controlador;

import java.util.ArrayList;
import tallermecanico.dao.EmpleadoDAO;
import tallermecanico.dao.TallerDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.Taller;
import tallermecanico.util.Validaciones;

public class ControlTalleres {

    private TallerDAO dao;
    private EmpleadoDAO empleadoDAO;

    public ControlTalleres() {
        dao = new TallerDAO();
        empleadoDAO = new EmpleadoDAO();
    }

    public ArrayList<Taller> listar() throws BaseDatosException {
        return dao.listar();
    }

    public ArrayList<Taller> listarActivos() throws BaseDatosException {
        return dao.listarActivos();
    }

    public ArrayList<Taller> buscar(String criterio, String texto) throws BaseDatosException {
        if (texto == null || texto.trim().isEmpty()) {
            return dao.listar();
        }
        return dao.buscar(criterio, texto);
    }

    private void validarJefeLibre(JefeTaller jefe, int idTaller) throws ReglaNegocioException, BaseDatosException {
        if (jefe == null) {
            return;
        }
        String otro = dao.tallerDelJefe(jefe.getIdEmpleado(), idTaller);
        if (otro != null) {
            throw new ReglaNegocioException("El jefe " + jefe.obtenerNombreCompleto() + " ya tiene asignado el taller "
                    + otro + ". Cada taller tiene un unico jefe a cargo.");
        }
    }

    public Taller agregar(String nombre, String descripcion, JefeTaller jefe)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.obligatorio(nombre, "nombre del taller");
        Validaciones.longitudMaxima(nombre, 50, "nombre del taller");
        Validaciones.obligatorio(descripcion, "descripcion");
        Validaciones.longitudMaxima(descripcion, 150, "descripcion");

        if (dao.existeNombre(nombre, 0)) {
            throw new ReglaNegocioException("Ya existe un taller con el nombre " + nombre.trim() + ".");
        }
        validarJefeLibre(jefe, 0);

        Taller taller = new Taller(nombre.trim(), descripcion.trim());
        taller.setJefeCargo(jefe);
        dao.insertar(taller);
        Auditoria.registrar("Talleres", "Alta", "Se registro el taller " + taller.getNombreTaller());
        return taller;
    }

    public void modificar(Taller taller, String nombre, String descripcion, JefeTaller jefe)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.obligatorio(nombre, "nombre del taller");
        Validaciones.obligatorio(descripcion, "descripcion");
        Validaciones.longitudMaxima(descripcion, 150, "descripcion");

        if (dao.existeNombre(nombre, taller.getId())) {
            throw new ReglaNegocioException("Ya existe otro taller con el nombre " + nombre.trim() + ".");
        }
        validarJefeLibre(jefe, taller.getId());

        taller.setNombreTaller(nombre.trim());
        taller.setDescripcion(descripcion.trim());
        taller.setJefeCargo(jefe);
        dao.actualizar(taller);
        Auditoria.registrar("Talleres", "Cambio", "Se modifico el taller " + taller.getNombreTaller());
    }

    public void eliminar(Taller taller) throws ReglaNegocioException, BaseDatosException {
        if (taller.getTotalOrdenesActivas() > 0) {
            throw new ReglaNegocioException("No se puede eliminar el taller porque tiene "
                    + taller.getTotalOrdenesActivas() + " orden(es) activa(s).");
        }
        if (dao.contarRelacionados(taller.getId()) > 0) {
            throw new ReglaNegocioException("No se puede eliminar el taller porque tiene mecanicos, servicios u "
                    + "ordenes registradas. Puede inactivarlo en su lugar.");
        }
        dao.eliminar(taller.getId());
        Auditoria.registrar("Talleres", "Baja", "Se elimino el taller " + taller.getNombreTaller());
    }

    public void cambiarEstado(Taller taller) throws ReglaNegocioException, BaseDatosException {
        if (taller.isEstado() && taller.getTotalOrdenesActivas() > 0) {
            throw new ReglaNegocioException("No se puede inactivar un taller con ordenes activas.");
        }
        taller.setEstado(!taller.isEstado());
        dao.actualizar(taller);
        Auditoria.registrar("Talleres", taller.isEstado() ? "Activar" : "Inactivar", "Taller " + taller.getNombreTaller());
    }

    public void asignarMecanico(Taller taller, Mecanico mecanico)
            throws ReglaNegocioException, DatoInvalidoException, BaseDatosException {

        Validaciones.seleccionoElemento(taller, "un taller");
        Validaciones.seleccionoElemento(mecanico, "un mecanico");

        if (mecanico.getTaller() != null && mecanico.getTaller().getId() == taller.getId()) {
            throw new ReglaNegocioException("El mecanico ya esta asignado a este taller.");
        }
        if (mecanico.getTaller() != null) {
            throw new ReglaNegocioException("El mecanico " + mecanico.obtenerNombreCompleto()
                    + " ya pertenece al taller " + mecanico.getTaller().getNombreTaller()
                    + ". Primero debe quitarlo de ese taller.");
        }
        empleadoDAO.asignarTaller(mecanico.getIdEmpleado(), taller.getId());
        Auditoria.registrar("Talleres", "Asignar", "Se asigno a " + mecanico.obtenerNombreCompleto()
                + " al taller " + taller.getNombreTaller());
    }

    public void quitarMecanico(Taller taller, Mecanico mecanico)
            throws ReglaNegocioException, DatoInvalidoException, BaseDatosException {

        Validaciones.seleccionoElemento(taller, "un taller");
        Validaciones.seleccionoElemento(mecanico, "un mecanico");

        if (mecanico.getTaller() == null || mecanico.getTaller().getId() != taller.getId()) {
            throw new ReglaNegocioException("El mecanico no pertenece al taller " + taller.getNombreTaller() + ".");
        }
        String orden = empleadoDAO.ordenActivaDeMecanico(mecanico.getIdEmpleado());
        if (orden != null) {
            throw new ReglaNegocioException("No se puede quitar al mecanico porque esta asignado a la orden " + orden + ".");
        }
        empleadoDAO.asignarTaller(mecanico.getIdEmpleado(), 0);
        Auditoria.registrar("Talleres", "Quitar", "Se quito a " + mecanico.obtenerNombreCompleto()
                + " del taller " + taller.getNombreTaller());
    }
}
