package tallermecanico.controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import tallermecanico.dao.RequerimientoDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.modelo.Bodeguero;
import tallermecanico.modelo.DetalleRequerimiento;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.OrdenTrabajo;
import tallermecanico.modelo.Producto;
import tallermecanico.modelo.Requerimiento;
import tallermecanico.util.Validaciones;

public class ControlRequerimientos {

    public static final String[] ESTADOS = {"Todos", Requerimiento.SOLICITADO, Requerimiento.AUTORIZADO,
        Requerimiento.RECHAZADO, Requerimiento.PROCESADO};

    private RequerimientoDAO dao;

    public ControlRequerimientos() {
        dao = new RequerimientoDAO();
    }

    public ArrayList<Requerimiento> listar() throws BaseDatosException {
        return dao.listar();
    }

    public ArrayList<Requerimiento> filtrar(String estado, String desde, String hasta, String criterio, String texto)
            throws DatoInvalidoException, BaseDatosException {
        LocalDate fechaDesde = Validaciones.fechaOpcional(desde, "fecha inicial");
        LocalDate fechaHasta = Validaciones.fechaOpcional(hasta, "fecha final");
        Validaciones.rangoFechas(fechaDesde, fechaHasta);
        return dao.filtrar(estado, fechaDesde, fechaHasta, criterio, texto);
    }

    public Requerimiento cargarCompleto(Requerimiento requerimiento) throws BaseDatosException {
        return dao.buscarPorId(requerimiento.getIdRequerimiento());
    }

    public void agregarLinea(Requerimiento requerimiento, Producto producto, String cantidad)
            throws DatoInvalidoException, ReglaNegocioException {
        Validaciones.seleccionoElemento(producto, "un producto");
        int valor = Validaciones.enteroMayorACero(cantidad, "cantidad");
        if (!requerimiento.getEstado().equals(Requerimiento.SOLICITADO)) {
            throw new ReglaNegocioException("Solo se pueden agregar productos a un requerimiento en estado Solicitado.");
        }
        if (!producto.isEstado()) {
            throw new ReglaNegocioException("El producto " + producto.getNombre() + " esta inactivo.");
        }
        int total = requerimiento.cantidadDe(producto) + valor;
        if (total > producto.getStock()) {
            throw new ReglaNegocioException("No hay existencia suficiente de " + producto.getNombre()
                    + ". Disponible: " + producto.getStock() + ", solicitado: " + total + ".");
        }
        requerimiento.agregarDetalle(producto, valor);
    }

    public void quitarLinea(Requerimiento requerimiento, int posicion) throws DatoInvalidoException, ReglaNegocioException {
        Validaciones.seleccionoFila(posicion, "productos del requerimiento");
        if (!requerimiento.getEstado().equals(Requerimiento.SOLICITADO)) {
            throw new ReglaNegocioException("Solo se pueden quitar productos de un requerimiento en estado Solicitado.");
        }
        requerimiento.quitarDetalle(posicion);
    }

    public Requerimiento guardar(Requerimiento requerimiento, OrdenTrabajo orden, Mecanico solicitante,
            String observacion) throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.longitudMaxima(observacion, 200, "observacion");
        if (requerimiento.getDetalles().isEmpty()) {
            throw new ReglaNegocioException("El requerimiento debe tener al menos un producto en el detalle.");
        }

        if (requerimiento.getIdRequerimiento() == 0) {
            Validaciones.seleccionoElemento(orden, "una orden de trabajo activa");
            Validaciones.seleccionoElemento(solicitante, "el mecanico solicitante");
            if (!orden.estaActiva()) {
                throw new ReglaNegocioException("Solo se pueden generar requerimientos para ordenes activas.");
            }
            if (!orden.getMecanicos().contains(solicitante)) {
                throw new ReglaNegocioException("El mecanico " + solicitante.obtenerNombreCompleto()
                        + " no esta asignado a la orden " + orden.getNumero() + ".");
            }
            Requerimiento nuevo = solicitante.generarRequerimiento(orden);
            nuevo.setDetalles(requerimiento.getDetalles());
            nuevo.setObservacion(observacion == null ? null : observacion.trim());
            nuevo.setFechaSolicitud(LocalDateTime.now());
            dao.insertar(nuevo);
            Auditoria.registrar("Requerimientos", "Alta", "Se genero el requerimiento " + nuevo.getNumero()
                    + " para la orden " + orden.getNumero() + " con " + nuevo.getDetalles().size() + " producto(s)");
            return nuevo;
        }

        if (!requerimiento.getEstado().equals(Requerimiento.SOLICITADO)) {
            throw new ReglaNegocioException("Solo se puede modificar un requerimiento que aun no ha sido autorizado.");
        }
        requerimiento.setObservacion(observacion == null ? null : observacion.trim());
        dao.actualizar(requerimiento);
        Auditoria.registrar("Requerimientos", "Cambio", "Se modifico el detalle del requerimiento " + requerimiento.getNumero());
        return requerimiento;
    }

    private void validarJefe(Requerimiento requerimiento, JefeTaller jefe) throws ReglaNegocioException {
        JefeTaller aCargo = requerimiento.getOrdenTrabajo().getTaller().getJefeCargo();
        if (aCargo == null || aCargo.getIdEmpleado() != jefe.getIdEmpleado()) {
            throw new ReglaNegocioException("El jefe " + jefe.obtenerNombreCompleto()
                    + " no esta a cargo del taller " + requerimiento.nombreTaller()
                    + ", por lo tanto no puede autorizar ni rechazar este requerimiento.");
        }
    }

    public void autorizar(Requerimiento requerimiento, JefeTaller jefe)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(requerimiento, "un requerimiento");
        Validaciones.seleccionoElemento(jefe, "el jefe de taller que autoriza");
        if (!requerimiento.getEstado().equals(Requerimiento.SOLICITADO)) {
            throw new ReglaNegocioException("Solo se pueden autorizar requerimientos en estado Solicitado. "
                    + "El requerimiento " + requerimiento.getNumero() + " esta en estado " + requerimiento.getEstado() + ".");
        }
        validarJefe(requerimiento, jefe);
        jefe.autorizarRequerimiento(requerimiento);
        dao.autorizar(requerimiento);
        Auditoria.registrar("Requerimientos", "Autorizar", "Se autorizo el requerimiento " + requerimiento.getNumero());
    }

    public void rechazar(Requerimiento requerimiento, JefeTaller jefe, String motivo)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(requerimiento, "un requerimiento");
        Validaciones.seleccionoElemento(jefe, "el jefe de taller");
        Validaciones.obligatorio(motivo, "motivo del rechazo");
        if (!requerimiento.getEstado().equals(Requerimiento.SOLICITADO)) {
            throw new ReglaNegocioException("Solo se pueden rechazar requerimientos en estado Solicitado.");
        }
        validarJefe(requerimiento, jefe);
        jefe.rechazarRequerimiento(requerimiento);
        requerimiento.setObservacion(motivo.trim());
        dao.rechazar(requerimiento);
        Auditoria.registrar("Requerimientos", "Rechazar", "Se rechazo el requerimiento " + requerimiento.getNumero());
    }

    public Requerimiento procesar(Requerimiento seleccionado, Bodeguero bodeguero)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(seleccionado, "un requerimiento");
        Validaciones.seleccionoElemento(bodeguero, "el bodeguero que procesa");

        Requerimiento requerimiento = dao.buscarPorId(seleccionado.getIdRequerimiento());
        if (requerimiento == null) {
            throw new ReglaNegocioException("El requerimiento ya no existe en la base de datos.");
        }
        if (!requerimiento.isAutorizado()) {
            throw new ReglaNegocioException("El requerimiento " + requerimiento.getNumero()
                    + " no ha sido autorizado por el jefe de taller. "
                    + "Sin autorizacion no puede salir producto de la bodega.");
        }
        if (!requerimiento.getEstado().equals(Requerimiento.AUTORIZADO)) {
            throw new ReglaNegocioException("Solo se pueden procesar requerimientos en estado Autorizado. "
                    + "El requerimiento esta en estado " + requerimiento.getEstado() + ".");
        }
        for (int i = 0; i < requerimiento.getDetalles().size(); i++) {
            DetalleRequerimiento d = requerimiento.getDetalles().get(i);
            if (d.getCantidad() > d.getProducto().getStock()) {
                throw new ReglaNegocioException("No hay existencia suficiente de " + d.getProducto().getNombre()
                        + ". Se solicitaron " + d.getCantidad() + " y hay " + d.getProducto().getStock()
                        + ". No se proceso ningun producto.");
            }
        }

        bodeguero.procesarRequerimiento(requerimiento);
        dao.procesar(requerimiento);
        Auditoria.registrar("Requerimientos", "Procesar", "Salida de bodega del requerimiento "
                + requerimiento.getNumero() + " por " + bodeguero.obtenerNombreCompleto());
        return requerimiento;
    }

    public void eliminar(Requerimiento requerimiento) throws ReglaNegocioException, BaseDatosException {
        if (requerimiento.getEstado().equals(Requerimiento.PROCESADO)) {
            throw new ReglaNegocioException("No se puede eliminar un requerimiento ya procesado porque "
                    + "genero una salida de producto de la bodega.");
        }
        if (requerimiento.getEstado().equals(Requerimiento.AUTORIZADO)) {
            throw new ReglaNegocioException("No se puede eliminar un requerimiento autorizado. Debe procesarse.");
        }
        dao.eliminar(requerimiento.getIdRequerimiento());
        Auditoria.registrar("Requerimientos", "Baja", "Se elimino el requerimiento " + requerimiento.getNumero());
    }
}
