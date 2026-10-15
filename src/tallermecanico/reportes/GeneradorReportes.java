package tallermecanico.reportes;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import tallermecanico.controlador.Sesion;
import tallermecanico.dao.ReporteDAO;
import tallermecanico.modelo.Vehiculo;

public class GeneradorReportes {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ReporteDAO dao;

    public GeneradorReportes() {
        dao = new ReporteDAO();
    }

    public File reporteOrdenes(LocalDate desde, LocalDate hasta) throws Exception {
        StringBuilder html = new StringBuilder();
        encabezado(html, "Reporte de ordenes de trabajo por estado y taller", periodo(desde, hasta));

        ResultadoReporte porEstado = dao.ordenesPorEstado(desde, hasta);
        html.append("<h2>Resumen por estado</h2>");
        tabla(html, porEstado, new int[]{1, 2});

        html.append("<h2>Resumen por taller</h2>");
        tabla(html, dao.ordenesPorTaller(desde, hasta), new int[]{2, 3, 4});

        html.append("<h2>Detalle de ordenes</h2>");
        tabla(html, dao.detalleOrdenes(desde, hasta), new int[]{6, 7, 8});

        pie(html);
        return escribir("reporte_ordenes_" + desde + "_" + hasta + ".html", html.toString());
    }

    public File reporteIngresos(LocalDate desde, LocalDate hasta) throws Exception {
        StringBuilder html = new StringBuilder();
        encabezado(html, "Reporte de ingresos por periodo",
                "Ordenes terminadas o entregadas. " + periodo(desde, hasta));
        ResultadoReporte ingresos = dao.ingresosPorPeriodo(desde, hasta);
        html.append("<p>Se cerraron <b>").append((long) ingresos.sumarColumna(2)).append("</b> ordenes por un total de <b>")
                .append(String.format(java.util.Locale.US, "Q%,.2f", ingresos.sumarColumna(5))).append("</b>.</p>");
        tabla(html, ingresos, new int[]{2, 3, 4, 5});
        pie(html);
        return escribir("reporte_ingresos_" + desde + "_" + hasta + ".html", html.toString());
    }

    public File reporteMayorMovimiento(LocalDate desde, LocalDate hasta) throws Exception {
        StringBuilder html = new StringBuilder();
        encabezado(html, "Reporte de productos con mayor movimiento",
                "Los 10 productos con mas unidades despachadas por la bodega. " + periodo(desde, hasta));
        tabla(html, dao.productosMayorMovimiento(desde, hasta, 10), new int[]{3, 5});
        pie(html);
        return escribir("reporte_mayor_movimiento_" + desde + "_" + hasta + ".html", html.toString());
    }

    public File reporteInventario() throws Exception {
        StringBuilder html = new StringBuilder();
        encabezado(html, "Reporte de inventario por categoria y productos bajo minimo",
                "Existencias actuales de la bodega al " + LocalDate.now().format(FECHA));
        html.append("<h2>Inventario por categoria</h2>");
        tabla(html, dao.inventarioPorCategoria(), new int[]{1, 2, 3, 4});
        ResultadoReporte bajos = dao.productosBajoMinimo();
        html.append("<h2>Productos por debajo del stock minimo</h2>");
        html.append("<p>Se encontraron <b>").append(bajos.getFilas().size())
                .append("</b> producto(s) que necesitan reabastecerse. La cantidad sugerida completa el stock maximo.</p>");
        tabla(html, bajos, new int[]{7, 8});
        pie(html);
        return escribir("reporte_inventario_" + LocalDate.now() + ".html", html.toString());
    }

    public File reporteHistorialVehiculo(Vehiculo vehiculo) throws Exception {
        StringBuilder html = new StringBuilder();
        encabezado(html, "Historial de servicios del vehiculo " + vehiculo.getPlaca(),
                "Todas las ordenes de trabajo registradas para el vehiculo");

        html.append("<table class='ficha'>");
        fila(html, "Placa", vehiculo.getPlaca());
        fila(html, "Vehiculo", vehiculo.getDescripcion());
        fila(html, "Color", vehiculo.getColor());
        fila(html, "Kilometraje", vehiculo.getKilometraje() + " km");
        fila(html, "Chasis", vehiculo.getChasis());
        fila(html, "Propietario", vehiculo.getPropietario().obtenerNombreCompleto());
        fila(html, "Telefono", vehiculo.getPropietario().getTelefono());
        html.append("</table>");

        ResultadoReporte ordenes = dao.ordenesDeVehiculo(vehiculo.getIdVehiculo());
        if (ordenes.estaVacio()) {
            html.append("<p><i>El vehiculo no tiene ordenes de trabajo registradas.</i></p>");
        }
        for (int i = 0; i < ordenes.getFilas().size(); i++) {
            Object[] o = ordenes.getFilas().get(i);
            int idOrden = ((Number) o[0]).intValue();
            html.append("<h2>").append(o[1]).append(" - ").append(o[7]).append("</h2>");
            html.append("<table class='ficha'>");
            fila(html, "Ingreso / salida", o[2] + " / " + o[3]);
            fila(html, "Taller", String.valueOf(o[4]));
            fila(html, "Motivo de ingreso", String.valueOf(o[5]));
            fila(html, "Trabajos realizados", String.valueOf(o[6]).isEmpty() ? "Sin registrar" : String.valueOf(o[6]));
            fila(html, "Total de la orden", ordenes.textoDe(i, 10));
            html.append("</table>");
            html.append("<p><b>Servicios</b></p>");
            tabla(html, dao.serviciosDeOrden(idOrden), new int[]{3});
            ResultadoReporte repuestos = dao.repuestosDeOrden(idOrden);
            if (!repuestos.estaVacio()) {
                html.append("<p><b>Repuestos solicitados a bodega</b> (solo los procesados se cobran)</p>");
                tabla(html, repuestos, new int[]{});
            }
        }
        if (!ordenes.estaVacio()) {
            html.append("<p><b>Total invertido en el vehiculo: ")
                    .append(String.format(java.util.Locale.US, "Q%,.2f", ordenes.sumarColumna(10))).append("</b></p>");
        }
        pie(html);
        return escribir("reporte_historial_" + vehiculo.getPlaca().replace("-", "") + ".html", html.toString());
    }

    public File reporteRequerimientos(LocalDate desde, LocalDate hasta) throws Exception {
        StringBuilder html = new StringBuilder();
        encabezado(html, "Reporte de requerimientos por taller y estado",
                "Flujo de solicitud, autorizacion y salida de bodega. " + periodo(desde, hasta));
        html.append("<h2>Resumen por estado</h2>");
        tabla(html, dao.requerimientosPorEstado(desde, hasta), new int[]{1, 2});
        html.append("<h2>Detalle por taller</h2>");
        tabla(html, dao.requerimientosDetalle(desde, hasta), new int[]{8});
        pie(html);
        return escribir("reporte_requerimientos_" + desde + "_" + hasta + ".html", html.toString());
    }

    public File reporteBitacora(LocalDate desde, LocalDate hasta) throws Exception {
        StringBuilder html = new StringBuilder();
        encabezado(html, "Bitacora de operaciones", "Registro de auditoria. " + periodo(desde, hasta));
        tabla(html, dao.bitacora(desde, hasta), null);
        pie(html);
        return escribir("reporte_bitacora_" + desde + "_" + hasta + ".html", html.toString());
    }

    private String periodo(LocalDate desde, LocalDate hasta) {
        return "Periodo del " + desde.format(FECHA) + " al " + hasta.format(FECHA) + ".";
    }

    private void tabla(StringBuilder html, ResultadoReporte r, int[] columnasConTotal) {
        if (r.estaVacio()) {
            html.append("<p><i>No hay informacion para el periodo seleccionado.</i></p>");
            return;
        }
        html.append("<table><tr>");
        for (int c = 0; c < r.getColumnas().length; c++) {
            html.append("<th>").append(r.getColumnas()[c]).append("</th>");
        }
        html.append("</tr>");
        for (int f = 0; f < r.getFilas().size(); f++) {
            html.append("<tr>");
            for (int c = 0; c < r.getColumnas().length; c++) {
                String clase = r.getTipos()[c] == ResultadoReporte.TEXTO ? "" : " class='num'";
                html.append("<td").append(clase).append(">").append(escapar(r.textoDe(f, c))).append("</td>");
            }
            html.append("</tr>");
        }
        if (columnasConTotal != null && columnasConTotal.length > 0) {
            html.append("<tr class='total'>");
            for (int c = 0; c < r.getColumnas().length; c++) {
                boolean sumar = false;
                for (int k = 0; k < columnasConTotal.length; k++) {
                    if (columnasConTotal[k] == c) {
                        sumar = true;
                    }
                }
                if (c == 0) {
                    html.append("<td>TOTAL</td>");
                } else if (sumar) {
                    double suma = r.sumarColumna(c);
                    String texto = r.getTipos()[c] == ResultadoReporte.MONEDA ? String.format(java.util.Locale.US, "Q%,.2f", suma)
                            : String.valueOf((long) suma);
                    html.append("<td class='num'>").append(texto).append("</td>");
                } else {
                    html.append("<td></td>");
                }
            }
            html.append("</tr>");
        }
        html.append("</table>");
    }

    private void fila(StringBuilder html, String etiqueta, String valor) {
        html.append("<tr><th class='izq'>").append(etiqueta).append("</th><td>").append(escapar(valor)).append("</td></tr>");
    }

    private String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void encabezado(StringBuilder html, String titulo, String descripcion) {
        html.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>");
        html.append("<title>").append(titulo).append("</title>");
        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;font-size:12px;color:#000;margin:25px;}");
        html.append(".cab{border-bottom:1px solid #000;padding-bottom:8px;margin-bottom:16px;}");
        html.append(".cab h1{margin:0 0 4px 0;font-size:16px;}");
        html.append(".cab p{margin:0;font-size:11px;}");
        html.append("h2{font-size:14px;margin-top:22px;margin-bottom:8px;}");
        html.append("table{width:100%;border-collapse:collapse;margin-bottom:14px;font-size:11px;}");
        html.append("th{background:#d9d9d9;text-align:left;padding:5px 7px;border:1px solid #000;}");
        html.append("td{padding:5px 7px;border:1px solid #000;}");
        html.append(".num{text-align:right;}");
        html.append(".izq{width:180px;}");
        html.append(".ficha{width:auto;min-width:50%;}");
        html.append(".total td{font-weight:bold;}");
        html.append(".pie{margin-top:25px;border-top:1px solid #000;padding-top:8px;font-size:10px;}");
        html.append("</style></head><body>");
        html.append("<div class='cab'><h1>").append(titulo).append("</h1>");
        html.append("<p>").append(descripcion).append("</p></div>");
    }

    private void pie(StringBuilder html) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        html.append("<div class='pie'>Reporte generado el ").append(fecha);
        html.append(" por ").append(Sesion.nombreUsuario());
        if (!Sesion.puesto().isEmpty()) {
            html.append(" (").append(Sesion.puesto()).append(")");
        }
        html.append(". Datos obtenidos de la base de datos taller_mecanico.</div></body></html>");
    }

    private File escribir(String nombreArchivo, String contenido) throws Exception {
        File carpeta = new File(System.getProperty("user.dir"), "reportes");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        File archivo = new File(carpeta, nombreArchivo);
        try (PrintWriter salida = new PrintWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
            salida.print(contenido);
        }
        return archivo;
    }

    public void abrir(File archivo) throws Exception {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(archivo.toURI());
        }
    }
}
