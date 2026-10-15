package tallermecanico.controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import tallermecanico.dao.EmpleadoDAO;
import tallermecanico.dao.OrdenTrabajoDAO;
import tallermecanico.dao.VehiculoDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.modelo.DetalleOrden;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.OrdenTrabajo;
import tallermecanico.modelo.Servicio;
import tallermecanico.modelo.Taller;
import tallermecanico.modelo.Vehiculo;
import tallermecanico.util.Validaciones;

public class ControlOrdenes {

    public static final String[] ESTADOS = {OrdenTrabajo.PENDIENTE, OrdenTrabajo.EN_PROCESO,
        OrdenTrabajo.TERMINADO, OrdenTrabajo.ENTREGADO, OrdenTrabajo.CANCELADO};

    private OrdenTrabajoDAO dao;
    private VehiculoDAO vehiculoDAO;
    private EmpleadoDAO empleadoDAO;

    public ControlOrdenes() {
        dao = new OrdenTrabajoDAO();
        vehiculoDAO = new VehiculoDAO();
        empleadoDAO = new EmpleadoDAO();
    }

    public ArrayList<OrdenTrabajo> listar() throws BaseDatosException {
        return dao.listar();
    }

    public ArrayList<OrdenTrabajo> listarActivas() throws BaseDatosException {
        return dao.listarActivas();
    }

    public ArrayList<OrdenTrabajo> filtrar(String estado, Taller taller, String desde, String hasta,
            String criterio, String texto) throws DatoInvalidoException, BaseDatosException {
        LocalDate fechaDesde = Validaciones.fechaOpcional(desde, "fecha inicial");
        LocalDate fechaHasta = Validaciones.fechaOpcional(hasta, "fecha final");
        Validaciones.rangoFechas(fechaDesde, fechaHasta);
        return dao.filtrar(estado, taller, fechaDesde, fechaHasta, criterio, texto);
    }

    public OrdenTrabajo cargarCompleta(OrdenTrabajo orden) throws BaseDatosException {
        OrdenTrabajo completa = dao.buscarPorId(orden.getIdOrden());
        if (completa != null) {
            completa.setMecanicos(orden.getMecanicos());
        }
        return completa;
    }

    public void agregarLinea(OrdenTrabajo orden, Servicio servicio, String cantidad)
            throws DatoInvalidoException, ReglaNegocioException {
        Validaciones.seleccionoElemento(servicio, "un servicio");
        int valor = Validaciones.enteroMayorACero(cantidad, "cantidad");
        if (valor > 20) {
            throw new DatoInvalidoException("La cantidad de un servicio no puede ser mayor a 20.");
        }
        if (orden.getIdOrden() > 0 && !orden.estaActiva()) {
            throw new ReglaNegocioException("No se pueden agregar servicios a una orden en estado " + orden.getEstado() + ".");
        }
        orden.agregarDetalle(servicio, valor);
    }

    public void quitarLinea(OrdenTrabajo orden, int posicion) throws DatoInvalidoException, ReglaNegocioException {
        Validaciones.seleccionoFila(posicion, "servicios de la orden");
        if (orden.getIdOrden() > 0 && !orden.estaActiva()) {
            throw new ReglaNegocioException("No se pueden quitar servicios de una orden en estado " + orden.getEstado() + ".");
        }
        orden.quitarDetalle(posicion);
    }

    public OrdenTrabajo guardar(OrdenTrabajo orden, Vehiculo vehiculo, Taller taller, String descripcion,
            Mecanico mecanicoInicial) throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(vehiculo, "un vehiculo");
        Validaciones.seleccionoElemento(taller, "un taller");
        Validaciones.obligatorio(descripcion, "motivo de ingreso");
        Validaciones.longitudMaxima(descripcion, 200, "motivo de ingreso");

        if (orden.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("La orden debe tener al menos un servicio en el detalle.");
        }
        for (int i = 0; i < orden.getDetalles().size(); i++) {
            Servicio s = orden.getDetalles().get(i).getServicio();
            if (s.getTaller() != null && s.getTaller().getId() > 0 && s.getTaller().getId() != taller.getId()) {
                throw new ReglaNegocioException("El servicio " + s.getNombre() + " no pertenece al taller "
                        + taller.getNombreTaller() + ". Quitelo del detalle o cambie el taller.");
            }
        }

        if (orden.getIdOrden() == 0) {
            if (vehiculo.getEstado().equals("Inactivo")) {
                throw new ReglaNegocioException("El vehiculo " + vehiculo.getPlaca() + " esta inactivo.");
            }
            String activa = vehiculoDAO.ordenActiva(vehiculo.getIdVehiculo());
            if (activa != null) {
                throw new ReglaNegocioException("El vehiculo " + vehiculo.getPlaca() + " ya tiene la orden "
                        + activa + " activa.");
            }
            if (mecanicoInicial != null) {
                validarMecanicoDelTaller(mecanicoInicial, taller);
                orden.asignarMecanico(mecanicoInicial);
            }
            orden.setVehiculo(vehiculo);
            orden.setTaller(taller);
            orden.setDescripcion(descripcion.trim());
            orden.setFechaIngreso(LocalDateTime.now());
            orden.setEstado(OrdenTrabajo.PENDIENTE);
            orden.setRegistradaPor(Sesion.getEmpleado());
            dao.insertar(orden);
            Auditoria.registrar("Ordenes", "Alta", "Se genero la orden " + orden.getNumero() + " para el vehiculo "
                    + vehiculo.getPlaca() + " con " + orden.getDetalles().size() + " servicio(s)");
        } else {
            if (!orden.estaActiva()) {
                throw new ReglaNegocioException("No se puede modificar una orden en estado " + orden.getEstado() + ".");
            }
            if (orden.getTaller().getId() != taller.getId() && !orden.getMecanicos().isEmpty()) {
                throw new ReglaNegocioException("No se puede cambiar el taller porque la orden ya tiene mecanicos asignados.");
            }
            orden.setTaller(taller);
            orden.setDescripcion(descripcion.trim());
            dao.actualizar(orden);
            Auditoria.registrar("Ordenes", "Cambio", "Se modifico la orden " + orden.getNumero()
                    + ", ahora con " + orden.getDetalles().size() + " servicio(s)");
        }
        return orden;
    }

    private void validarMecanicoDelTaller(Mecanico mecanico, Taller taller) throws ReglaNegocioException {
        if (mecanico.getTaller() == null || mecanico.getTaller().getId() != taller.getId()) {
            throw new ReglaNegocioException("El mecanico " + mecanico.obtenerNombreCompleto()
                    + " no pertenece al taller " + taller.getNombreTaller() + ".");
        }
    }

    public void asignarMecanico(OrdenTrabajo orden, Mecanico mecanico)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(orden, "una orden de trabajo guardada");
        Validaciones.seleccionoElemento(mecanico, "un mecanico");
        if (orden.getIdOrden() == 0) {
            throw new ReglaNegocioException("Primero debe guardar la orden.");
        }
        if (!orden.estaActiva()) {
            throw new ReglaNegocioException("Solo se pueden asignar mecanicos a ordenes pendientes o en proceso.");
        }
        validarMecanicoDelTaller(mecanico, orden.getTaller());
        if (orden.getMecanicos().contains(mecanico)) {
            throw new ReglaNegocioException("El mecanico ya esta asignado a esta orden.");
        }
        dao.agregarMecanico(orden.getIdOrden(), mecanico.getIdEmpleado());
        orden.asignarMecanico(mecanico);
        Auditoria.registrar("Ordenes", "Asignar", "Se asigno a " + mecanico.obtenerNombreCompleto()
                + " a la orden " + orden.getNumero());
    }

    public void quitarMecanico(OrdenTrabajo orden, Mecanico mecanico)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(orden, "una orden de trabajo guardada");
        Validaciones.seleccionoElemento(mecanico, "un mecanico");
        if (!orden.estaActiva()) {
            throw new ReglaNegocioException("Solo se pueden quitar mecanicos de ordenes activas.");
        }
        if (!orden.getMecanicos().contains(mecanico)) {
            throw new ReglaNegocioException("El mecanico no esta asignado a esta orden.");
        }
        if (orden.getEstado().equals(OrdenTrabajo.EN_PROCESO) && orden.getMecanicos().size() == 1) {
            throw new ReglaNegocioException("Una orden en proceso debe tener al menos un mecanico asignado.");
        }
        dao.quitarMecanico(orden.getIdOrden(), mecanico.getIdEmpleado());
        orden.quitarMecanico(mecanico);
        Auditoria.registrar("Ordenes", "Quitar", "Se quito a " + mecanico.obtenerNombreCompleto()
                + " de la orden " + orden.getNumero());
    }

    public void cambiarEstado(OrdenTrabajo orden, String nuevoEstado)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(orden, "una orden de trabajo");
        Validaciones.obligatorio(nuevoEstado, "estado");
        String actual = orden.getEstado();

        if (orden.getIdOrden() == 0) {
            throw new ReglaNegocioException("Primero debe guardar la orden.");
        }
        if (actual.equals(nuevoEstado)) {
            throw new ReglaNegocioException("La orden ya se encuentra en estado " + nuevoEstado + ".");
        }
        if (actual.equals(OrdenTrabajo.ENTREGADO) || actual.equals(OrdenTrabajo.CANCELADO)) {
            throw new ReglaNegocioException("La orden esta " + actual.toLowerCase() + " y ya no puede cambiar de estado.");
        }
        if (nuevoEstado.equals(OrdenTrabajo.PENDIENTE)) {
            throw new ReglaNegocioException("Una orden no puede regresar al estado Pendiente.");
        }
        if (nuevoEstado.equals(OrdenTrabajo.EN_PROCESO) && orden.getMecanicos().isEmpty()) {
            throw new ReglaNegocioException("No se puede poner la orden en proceso sin mecanicos asignados.");
        }
        if (nuevoEstado.equals(OrdenTrabajo.TERMINADO) || nuevoEstado.equals(OrdenTrabajo.CANCELADO)) {
            String pendiente = dao.requerimientoPendiente(orden.getIdOrden());
            if (pendiente != null) {
                throw new ReglaNegocioException("No se puede " + (nuevoEstado.equals(OrdenTrabajo.TERMINADO) ? "terminar" : "cancelar")
                        + " la orden porque el requerimiento " + pendiente + " todavia no se ha procesado ni rechazado.");
            }
        }
        if (nuevoEstado.equals(OrdenTrabajo.TERMINADO) && !actual.equals(OrdenTrabajo.EN_PROCESO)) {
            throw new ReglaNegocioException("Solo se puede terminar una orden que este en proceso.");
        }
        if (nuevoEstado.equals(OrdenTrabajo.ENTREGADO) && !actual.equals(OrdenTrabajo.TERMINADO)) {
            throw new ReglaNegocioException("Solo se puede entregar una orden que este terminada.");
        }

        orden.cambiarEstado(nuevoEstado);
        LocalDateTime salida = orden.getFechaSalida();
        if (nuevoEstado.equals(OrdenTrabajo.EN_PROCESO)) {
            salida = null;
        }
        if (nuevoEstado.equals(OrdenTrabajo.CANCELADO)) {
            salida = LocalDateTime.now();
        }
        dao.cambiarEstado(orden.getIdOrden(), nuevoEstado, salida);
        Auditoria.registrar("Ordenes", "Estado", "La orden " + orden.getNumero() + " paso de " + actual + " a " + nuevoEstado);
    }

    public void registrarAvance(OrdenTrabajo orden, Mecanico mecanico, String avance)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(orden, "una orden de trabajo");
        Validaciones.seleccionoElemento(mecanico, "el mecanico que registra el avance");
        Validaciones.obligatorio(avance, "avance");
        if (!orden.estaActiva()) {
            throw new ReglaNegocioException("Solo se registran avances en ordenes activas.");
        }
        if (!orden.getMecanicos().contains(mecanico)) {
            throw new ReglaNegocioException("El mecanico " + mecanico.obtenerNombreCompleto()
                    + " no esta asignado a esta orden.");
        }
        mecanico.registrarAvance(orden, avance.trim());
        dao.registrarAvance(orden.getIdOrden(), orden.getTrabajosRealizados());
        Auditoria.registrar("Ordenes", "Avance", "Avance en la orden " + orden.getNumero() + " por "
                + mecanico.obtenerNombreCompleto());
    }

    public void eliminar(OrdenTrabajo orden) throws ReglaNegocioException, BaseDatosException {
        if (!orden.getEstado().equals(OrdenTrabajo.PENDIENTE)) {
            throw new ReglaNegocioException("Solo se pueden eliminar ordenes en estado Pendiente. La orden "
                    + orden.getNumero() + " esta en estado " + orden.getEstado() + ". Puede cancelarla en su lugar.");
        }
        if (dao.contarRequerimientos(orden.getIdOrden()) > 0) {
            throw new ReglaNegocioException("No se puede eliminar la orden porque tiene requerimientos asociados.");
        }
        dao.eliminar(orden.getIdOrden());
        Auditoria.registrar("Ordenes", "Baja", "Se elimino la orden " + orden.getNumero());
    }

    public ArrayList<Mecanico> mecanicosDelTaller(Taller taller) throws BaseDatosException {
        if (taller == null) {
            return new ArrayList<>();
        }
        return empleadoDAO.listarMecanicosDeTaller(taller.getId());
    }

    public double totalDetalle(OrdenTrabajo orden) {
        double total = 0;
        for (int i = 0; i < orden.getDetalles().size(); i++) {
            DetalleOrden d = orden.getDetalles().get(i);
            total = total + d.subtotal();
        }
        return total;
    }
}
