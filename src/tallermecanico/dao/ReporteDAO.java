package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.reportes.ResultadoReporte;

public class ReporteDAO extends BaseDAO {

    private static final int T = ResultadoReporte.TEXTO;
    private static final int E = ResultadoReporte.ENTERO;
    private static final int M = ResultadoReporte.MONEDA;

    private static final String TOTAL_ORDEN = "(vt.total_servicios + vt.total_repuestos)";

    public double numero(String sql, Object... parametros) throws BaseDatosException {
        try (PreparedStatement ps = preparar(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw error("consultar el indicador", e);
        }
        return 0;
    }

    private ResultadoReporte consulta(String titulo, String sql, String[] columnas, int[] tipos, Object... parametros)
            throws BaseDatosException {
        ResultadoReporte resultado = new ResultadoReporte(titulo, columnas, tipos);
        try (PreparedStatement ps = preparar(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[columnas.length];
                    for (int c = 0; c < columnas.length; c++) {
                        if (tipos[c] == M) {
                            fila[c] = rs.getDouble(c + 1);
                        } else if (tipos[c] == E) {
                            fila[c] = rs.getLong(c + 1);
                        } else {
                            fila[c] = rs.getString(c + 1);
                        }
                    }
                    resultado.agregarFila(fila);
                }
            }
        } catch (SQLException e) {
            throw error("generar el reporte " + titulo, e);
        }
        return resultado;
    }

    private String inicio(LocalDate fecha) {
        return fecha.toString() + " 00:00:00";
    }

    private String fin(LocalDate fecha) {
        return fecha.toString() + " 23:59:59";
    }

    public ResultadoReporte ordenesPorEstado(LocalDate desde, LocalDate hasta) throws BaseDatosException {
        String sql = "SELECT o.estado, COUNT(*), SUM(" + TOTAL_ORDEN + ") "
                + "FROM orden_trabajo o JOIN vista_total_orden vt ON vt.id_orden = o.id_orden "
                + "WHERE o.fecha_ingreso BETWEEN ? AND ? "
                + "GROUP BY o.estado "
                + "ORDER BY FIELD(o.estado, 'Pendiente', 'En proceso', 'Terminado', 'Entregado', 'Cancelado')";
        return consulta("Ordenes por estado", sql, new String[]{"Estado", "Ordenes", "Monto"},
                new int[]{T, E, M}, inicio(desde), fin(hasta));
    }

    public ResultadoReporte ordenesPorTaller(LocalDate desde, LocalDate hasta) throws BaseDatosException {
        String sql = "SELECT t.nombre, CONCAT(COALESCE(e.nombre, 'Sin'), ' ', COALESCE(e.apellido, 'asignar')), "
                + "COUNT(o.id_orden), "
                + "SUM(CASE WHEN o.estado IN ('Pendiente', 'En proceso') THEN 1 ELSE 0 END), "
                + "COALESCE(SUM(" + TOTAL_ORDEN + "), 0) "
                + "FROM taller t LEFT JOIN empleado e ON e.id_empleado = t.id_jefe "
                + "LEFT JOIN orden_trabajo o ON o.id_taller = t.id_taller AND o.fecha_ingreso BETWEEN ? AND ? "
                + "LEFT JOIN vista_total_orden vt ON vt.id_orden = o.id_orden "
                + "GROUP BY t.id_taller, t.nombre, e.nombre, e.apellido ORDER BY t.id_taller";
        return consulta("Ordenes por taller", sql,
                new String[]{"Taller", "Jefe a cargo", "Ordenes", "Activas", "Monto"},
                new int[]{T, T, E, E, M}, inicio(desde), fin(hasta));
    }

    public ResultadoReporte detalleOrdenes(LocalDate desde, LocalDate hasta) throws BaseDatosException {
        String sql = "SELECT o.numero, DATE_FORMAT(o.fecha_ingreso, '%d/%m/%Y'), v.placa, "
                + "CONCAT(c.nombre, ' ', c.apellido), t.nombre, o.estado, vt.total_servicios, vt.total_repuestos, "
                + TOTAL_ORDEN + " "
                + "FROM orden_trabajo o JOIN vehiculo v ON v.id_vehiculo = o.id_vehiculo "
                + "JOIN cliente c ON c.id_cliente = v.id_cliente JOIN taller t ON t.id_taller = o.id_taller "
                + "JOIN vista_total_orden vt ON vt.id_orden = o.id_orden "
                + "WHERE o.fecha_ingreso BETWEEN ? AND ? ORDER BY o.fecha_ingreso";
        return consulta("Detalle de ordenes", sql,
                new String[]{"Orden", "Ingreso", "Placa", "Cliente", "Taller", "Estado", "Servicios", "Repuestos", "Total"},
                new int[]{T, T, T, T, T, T, M, M, M}, inicio(desde), fin(hasta));
    }

    public ResultadoReporte ingresosPorPeriodo(LocalDate desde, LocalDate hasta) throws BaseDatosException {
        String sql = "SELECT DATE_FORMAT(o.fecha_salida, '%Y-%m') AS mes, t.nombre, COUNT(*), "
                + "SUM(vt.total_servicios), SUM(vt.total_repuestos), SUM(" + TOTAL_ORDEN + ") "
                + "FROM orden_trabajo o JOIN taller t ON t.id_taller = o.id_taller "
                + "JOIN vista_total_orden vt ON vt.id_orden = o.id_orden "
                + "WHERE o.estado IN ('Terminado', 'Entregado') AND o.fecha_salida BETWEEN ? AND ? "
                + "GROUP BY mes, t.id_taller, t.nombre ORDER BY mes, t.nombre";
        return consulta("Ingresos por periodo", sql,
                new String[]{"Mes", "Taller", "Ordenes cerradas", "Mano de obra", "Repuestos", "Total"},
                new int[]{T, T, E, M, M, M}, inicio(desde), fin(hasta));
    }

    public ResultadoReporte productosMayorMovimiento(LocalDate desde, LocalDate hasta, int limite)
            throws BaseDatosException {
        String sql = "SELECT p.codigo, p.nombre, c.nombre, SUM(d.cantidad) AS unidades, "
                + "COUNT(DISTINCT r.id_requerimiento), SUM(d.cantidad * d.precio_unitario), p.stock "
                + "FROM detalle_requerimiento d "
                + "JOIN requerimiento r ON r.id_requerimiento = d.id_requerimiento "
                + "JOIN producto p ON p.id_producto = d.id_producto "
                + "JOIN categoria c ON c.id_categoria = p.id_categoria "
                + "WHERE r.estado = 'Procesado' AND r.fecha_proceso BETWEEN ? AND ? "
                + "GROUP BY p.id_producto, p.codigo, p.nombre, c.nombre, p.stock "
                + "ORDER BY unidades DESC, p.nombre LIMIT " + limite;
        return consulta("Productos con mayor movimiento", sql,
                new String[]{"Codigo", "Producto", "Categoria", "Unidades despachadas", "Requerimientos", "Monto", "Stock actual"},
                new int[]{T, T, T, E, E, M, E}, inicio(desde), fin(hasta));
    }

    public ResultadoReporte inventarioPorCategoria() throws BaseDatosException {
        String sql = "SELECT c.nombre, COUNT(p.id_producto), COALESCE(SUM(p.stock), 0), "
                + "COALESCE(SUM(p.stock * p.precio_compra), 0), "
                + "SUM(CASE WHEN p.stock < p.stock_minimo THEN 1 ELSE 0 END) "
                + "FROM categoria c LEFT JOIN producto p ON p.id_categoria = c.id_categoria "
                + "GROUP BY c.id_categoria, c.nombre ORDER BY c.nombre";
        return consulta("Inventario por categoria", sql,
                new String[]{"Categoria", "Productos", "Unidades en bodega", "Valor del inventario", "Bajo minimo"},
                new int[]{T, E, E, M, E});
    }

    public ResultadoReporte productosBajoMinimo() throws BaseDatosException {
        String sql = "SELECT p.codigo, p.nombre, c.nombre, pr.nombre, pr.telefono, p.stock, p.stock_minimo, "
                + "p.stock_maximo - p.stock, (p.stock_maximo - p.stock) * p.precio_compra "
                + "FROM producto p JOIN categoria c ON c.id_categoria = p.id_categoria "
                + "JOIN proveedor pr ON pr.id_proveedor = p.id_proveedor "
                + "WHERE p.stock < p.stock_minimo ORDER BY pr.nombre, p.nombre";
        return consulta("Productos bajo el stock minimo", sql,
                new String[]{"Codigo", "Producto", "Categoria", "Proveedor", "Telefono", "Stock", "Minimo",
                    "Sugerido a pedir", "Costo estimado"},
                new int[]{T, T, T, T, T, E, E, E, M});
    }

    public ResultadoReporte requerimientosPorEstado(LocalDate desde, LocalDate hasta) throws BaseDatosException {
        String sql = "SELECT r.estado, COUNT(DISTINCT r.id_requerimiento), COALESCE(SUM(d.cantidad * d.precio_unitario), 0) "
                + "FROM requerimiento r LEFT JOIN detalle_requerimiento d ON d.id_requerimiento = r.id_requerimiento "
                + "WHERE r.fecha_solicitud BETWEEN ? AND ? GROUP BY r.estado "
                + "ORDER BY FIELD(r.estado, 'Solicitado', 'Autorizado', 'Rechazado', 'Procesado')";
        return consulta("Requerimientos por estado", sql, new String[]{"Estado", "Requerimientos", "Valor"},
                new int[]{T, E, M}, inicio(desde), fin(hasta));
    }

    public ResultadoReporte requerimientosDetalle(LocalDate desde, LocalDate hasta) throws BaseDatosException {
        String sql = "SELECT t.nombre, r.numero, o.numero, DATE_FORMAT(r.fecha_solicitud, '%d/%m/%Y'), "
                + "CONCAT(es.nombre, ' ', es.apellido), "
                + "COALESCE(CONCAT(ea.nombre, ' ', ea.apellido), 'Pendiente'), "
                + "COALESCE(CONCAT(ep.nombre, ' ', ep.apellido), 'Pendiente'), r.estado, "
                + "(SELECT COALESCE(SUM(d.cantidad * d.precio_unitario), 0) FROM detalle_requerimiento d "
                + " WHERE d.id_requerimiento = r.id_requerimiento) "
                + "FROM requerimiento r JOIN orden_trabajo o ON o.id_orden = r.id_orden "
                + "JOIN taller t ON t.id_taller = o.id_taller "
                + "JOIN empleado es ON es.id_empleado = r.id_solicitante "
                + "LEFT JOIN empleado ea ON ea.id_empleado = r.id_autoriza "
                + "LEFT JOIN empleado ep ON ep.id_empleado = r.id_procesa "
                + "WHERE r.fecha_solicitud BETWEEN ? AND ? ORDER BY t.id_taller, r.fecha_solicitud";
        return consulta("Requerimientos por taller", sql,
                new String[]{"Taller", "Requerimiento", "Orden", "Fecha", "Solicito", "Autorizo", "Proceso", "Estado", "Valor"},
                new int[]{T, T, T, T, T, T, T, T, M}, inicio(desde), fin(hasta));
    }

    public ResultadoReporte bitacora(LocalDate desde, LocalDate hasta) throws BaseDatosException {
        String sql = "SELECT DATE_FORMAT(b.fecha_hora, '%d/%m/%Y %H:%i'), COALESCE(u.username, 'Sistema'), "
                + "b.modulo, b.accion, b.descripcion "
                + "FROM bitacora b LEFT JOIN usuario u ON u.id_usuario = b.id_usuario "
                + "WHERE b.fecha_hora BETWEEN ? AND ? ORDER BY b.fecha_hora DESC, b.id_bitacora DESC";
        return consulta("Bitacora de operaciones", sql,
                new String[]{"Fecha y hora", "Usuario", "Modulo", "Accion", "Descripcion"},
                new int[]{T, T, T, T, T}, inicio(desde), fin(hasta));
    }

    public ResultadoReporte ordenesDeVehiculo(int idVehiculo) throws BaseDatosException {
        String sql = "SELECT o.id_orden, o.numero, DATE_FORMAT(o.fecha_ingreso, '%d/%m/%Y'), "
                + "COALESCE(DATE_FORMAT(o.fecha_salida, '%d/%m/%Y'), 'Pendiente'), t.nombre, o.descripcion, "
                + "COALESCE(o.trabajos_realizados, ''), o.estado, vt.total_servicios, vt.total_repuestos, " + TOTAL_ORDEN + " "
                + "FROM orden_trabajo o JOIN taller t ON t.id_taller = o.id_taller "
                + "JOIN vista_total_orden vt ON vt.id_orden = o.id_orden "
                + "WHERE o.id_vehiculo = ? ORDER BY o.fecha_ingreso";
        return consulta("Ordenes del vehiculo", sql,
                new String[]{"Id", "Orden", "Ingreso", "Salida", "Taller", "Motivo", "Trabajos", "Estado",
                    "Servicios", "Repuestos", "Total"},
                new int[]{E, T, T, T, T, T, T, T, M, M, M}, idVehiculo);
    }

    public ResultadoReporte serviciosDeOrden(int idOrden) throws BaseDatosException {
        String sql = "SELECT s.nombre, d.cantidad, d.precio_unitario, d.cantidad * d.precio_unitario "
                + "FROM detalle_orden d JOIN servicio s ON s.id_servicio = d.id_servicio "
                + "WHERE d.id_orden = ? ORDER BY d.id_detalle";
        return consulta("Servicios", sql, new String[]{"Servicio", "Cantidad", "Precio", "Subtotal"},
                new int[]{T, E, M, M}, idOrden);
    }

    public ResultadoReporte repuestosDeOrden(int idOrden) throws BaseDatosException {
        String sql = "SELECT r.numero, p.nombre, d.cantidad, d.precio_unitario, d.cantidad * d.precio_unitario, r.estado "
                + "FROM requerimiento r JOIN detalle_requerimiento d ON d.id_requerimiento = r.id_requerimiento "
                + "JOIN producto p ON p.id_producto = d.id_producto "
                + "WHERE r.id_orden = ? ORDER BY r.id_requerimiento, d.id_detalle";
        return consulta("Repuestos", sql,
                new String[]{"Requerimiento", "Producto", "Cantidad", "Precio", "Subtotal", "Estado"},
                new int[]{T, T, E, M, M, T}, idOrden);
    }
}
