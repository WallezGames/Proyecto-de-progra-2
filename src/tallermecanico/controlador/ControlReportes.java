package tallermecanico.controlador;

import java.io.File;
import java.time.LocalDate;
import tallermecanico.dao.ReporteDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Vehiculo;
import tallermecanico.reportes.GeneradorReportes;
import tallermecanico.reportes.ResultadoReporte;
import tallermecanico.util.Validaciones;

public class ControlReportes {

    private ReporteDAO dao;
    private GeneradorReportes generador;

    public ControlReportes() {
        dao = new ReporteDAO();
        generador = new GeneradorReportes();
    }

    public LocalDate[] periodo(String desde, String hasta) throws DatoInvalidoException {
        LocalDate inicio = Validaciones.fechaObligatoria(desde, "fecha inicial");
        LocalDate fin = Validaciones.fechaObligatoria(hasta, "fecha final");
        Validaciones.rangoFechas(inicio, fin);
        return new LocalDate[]{inicio, fin};
    }

    public double ordenesActivas() throws BaseDatosException {
        return dao.numero("SELECT COUNT(*) FROM orden_trabajo WHERE estado IN ('Pendiente', 'En proceso')");
    }

    public double ordenesEnEstado(String estado) throws BaseDatosException {
        return dao.numero("SELECT COUNT(*) FROM orden_trabajo WHERE estado = ?", estado);
    }

    public double ingresosDelMes() throws BaseDatosException {
        LocalDate hoy = LocalDate.now();
        return dao.numero("SELECT COALESCE(SUM(vt.total_servicios + vt.total_repuestos), 0) FROM orden_trabajo o "
                + "JOIN vista_total_orden vt ON vt.id_orden = o.id_orden "
                + "WHERE o.estado IN ('Terminado', 'Entregado') AND o.fecha_salida >= ?",
                hoy.withDayOfMonth(1).toString() + " 00:00:00");
    }

    public double ordenesCerradasDelMes() throws BaseDatosException {
        LocalDate hoy = LocalDate.now();
        return dao.numero("SELECT COUNT(*) FROM orden_trabajo WHERE estado IN ('Terminado', 'Entregado') "
                + "AND fecha_salida >= ?", hoy.withDayOfMonth(1).toString() + " 00:00:00");
    }

    public double vehiculosEnTaller() throws BaseDatosException {
        return dao.numero("SELECT COUNT(DISTINCT id_vehiculo) FROM orden_trabajo WHERE estado IN ('Pendiente', 'En proceso')");
    }

    public double totalVehiculos() throws BaseDatosException {
        return dao.numero("SELECT COUNT(*) FROM vehiculo WHERE estado = 1");
    }

    public double requerimientosEnEstado(String estado) throws BaseDatosException {
        return dao.numero("SELECT COUNT(*) FROM requerimiento WHERE estado = ?", estado);
    }

    public double productosBajoMinimo() throws BaseDatosException {
        return dao.numero("SELECT COUNT(*) FROM producto WHERE stock < stock_minimo");
    }

    public ResultadoReporte consulta(int numeroReporte, String desde, String hasta)
            throws DatoInvalidoException, BaseDatosException {
        if (numeroReporte == 4) {
            return dao.inventarioPorCategoria();
        }
        LocalDate[] p = periodo(desde, hasta);
        if (numeroReporte == 1) {
            return dao.ordenesPorTaller(p[0], p[1]);
        }
        if (numeroReporte == 2) {
            return dao.ingresosPorPeriodo(p[0], p[1]);
        }
        if (numeroReporte == 3) {
            return dao.productosMayorMovimiento(p[0], p[1], 10);
        }
        if (numeroReporte == 6) {
            return dao.requerimientosDetalle(p[0], p[1]);
        }
        return dao.bitacora(p[0], p[1]);
    }

    public File generarHtml(int numeroReporte, String desde, String hasta, Vehiculo vehiculo)
            throws DatoInvalidoException, BaseDatosException, Exception {
        File archivo;
        if (numeroReporte == 4) {
            archivo = generador.reporteInventario();
        } else if (numeroReporte == 5) {
            Validaciones.seleccionoElemento(vehiculo, "un vehiculo");
            archivo = generador.reporteHistorialVehiculo(vehiculo);
        } else {
            LocalDate[] p = periodo(desde, hasta);
            if (numeroReporte == 1) {
                archivo = generador.reporteOrdenes(p[0], p[1]);
            } else if (numeroReporte == 2) {
                archivo = generador.reporteIngresos(p[0], p[1]);
            } else if (numeroReporte == 3) {
                archivo = generador.reporteMayorMovimiento(p[0], p[1]);
            } else if (numeroReporte == 6) {
                archivo = generador.reporteRequerimientos(p[0], p[1]);
            } else {
                archivo = generador.reporteBitacora(p[0], p[1]);
            }
        }
        Auditoria.registrar("Reportes", "Generar", "Se genero el reporte " + archivo.getName());
        return archivo;
    }

    public void abrir(File archivo) throws Exception {
        generador.abrir(archivo);
    }
}
