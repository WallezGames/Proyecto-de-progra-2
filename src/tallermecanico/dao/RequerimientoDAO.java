package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import tallermecanico.conexion.Conexion;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.modelo.Bodeguero;
import tallermecanico.modelo.Categoria;
import tallermecanico.modelo.DetalleRequerimiento;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.OrdenTrabajo;
import tallermecanico.modelo.Producto;
import tallermecanico.modelo.Requerimiento;
import tallermecanico.modelo.Taller;
import tallermecanico.modelo.Vehiculo;

public class RequerimientoDAO extends BaseDAO implements CrudDAO<Requerimiento> {

    private static final String CONSULTA = "SELECT r.*, o.numero AS orden_numero, o.estado AS orden_estado, "
            + "o.id_taller, t.nombre AS taller_nombre, t.id_jefe, v.placa, "
            + "es.nombre AS s_nombre, es.apellido AS s_apellido, "
            + "ea.nombre AS a_nombre, ea.apellido AS a_apellido, "
            + "ep.nombre AS p_nombre, ep.apellido AS p_apellido, "
            + "(SELECT COALESCE(SUM(d.cantidad * d.precio_unitario), 0) FROM detalle_requerimiento d "
            + "  WHERE d.id_requerimiento = r.id_requerimiento) AS total "
            + "FROM requerimiento r "
            + "JOIN orden_trabajo o ON o.id_orden = r.id_orden "
            + "JOIN taller t ON t.id_taller = o.id_taller "
            + "JOIN vehiculo v ON v.id_vehiculo = o.id_vehiculo "
            + "JOIN empleado es ON es.id_empleado = r.id_solicitante "
            + "LEFT JOIN empleado ea ON ea.id_empleado = r.id_autoriza "
            + "LEFT JOIN empleado ep ON ep.id_empleado = r.id_procesa ";

    @Override
    public int insertar(Requerimiento requerimiento) throws BaseDatosException {
        Conexion bd = Conexion.getInstancia();
        try {
            bd.iniciarTransaccion();

            requerimiento.setNumero(siguienteNumero());
            String sql = "INSERT INTO requerimiento (numero, id_orden, id_solicitante, fecha_solicitud, estado, observacion) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            int id;
            try (PreparedStatement ps = prepararConLlave(sql)) {
                ps.setString(1, requerimiento.getNumero());
                ps.setInt(2, requerimiento.getOrdenTrabajo().getIdOrden());
                ps.setInt(3, requerimiento.getSolicitante().getIdEmpleado());
                ps.setTimestamp(4, aTimestamp(requerimiento.getFechaSolicitud()));
                ps.setString(5, Requerimiento.SOLICITADO);
                ps.setString(6, requerimiento.getObservacion());
                ps.executeUpdate();
                id = idGenerado(ps);
            }
            requerimiento.setIdRequerimiento(id);
            insertarDetalles(requerimiento);

            bd.confirmarTransaccion();
            return id;
        } catch (SQLException e) {
            bd.deshacerTransaccion();
            throw error("guardar el requerimiento", e);
        } catch (BaseDatosException e) {
            bd.deshacerTransaccion();
            throw e;
        }
    }

    @Override
    public void actualizar(Requerimiento requerimiento) throws BaseDatosException {
        Conexion bd = Conexion.getInstancia();
        try {
            bd.iniciarTransaccion();

            String sql = "UPDATE requerimiento SET observacion = ? WHERE id_requerimiento = ? AND estado = 'Solicitado'";
            int filas;
            try (PreparedStatement ps = preparar(sql)) {
                ps.setString(1, requerimiento.getObservacion());
                ps.setInt(2, requerimiento.getIdRequerimiento());
                filas = ps.executeUpdate();
            }
            if (filas == 0) {
                throw new BaseDatosException("El requerimiento ya no esta en estado Solicitado y no se puede modificar.");
            }
            try (PreparedStatement ps = preparar("DELETE FROM detalle_requerimiento WHERE id_requerimiento = ?")) {
                ps.setInt(1, requerimiento.getIdRequerimiento());
                ps.executeUpdate();
            }
            insertarDetalles(requerimiento);

            bd.confirmarTransaccion();
        } catch (SQLException e) {
            bd.deshacerTransaccion();
            throw error("modificar el requerimiento", e);
        } catch (BaseDatosException e) {
            bd.deshacerTransaccion();
            throw e;
        }
    }

    private void insertarDetalles(Requerimiento requerimiento) throws SQLException, BaseDatosException {
        String sql = "INSERT INTO detalle_requerimiento (id_requerimiento, id_producto, cantidad, precio_unitario) "
                + "VALUES (?, ?, ?, ?)";
        for (int i = 0; i < requerimiento.getDetalles().size(); i++) {
            DetalleRequerimiento d = requerimiento.getDetalles().get(i);
            try (PreparedStatement ps = preparar(sql)) {
                ps.setInt(1, requerimiento.getIdRequerimiento());
                ps.setInt(2, d.getProducto().getIdProducto());
                ps.setInt(3, d.getCantidad());
                ps.setDouble(4, d.getPrecioUnitario());
                ps.executeUpdate();
            }
        }
    }

    public void autorizar(Requerimiento requerimiento) throws BaseDatosException {
        cambiarAutorizacion(requerimiento, Requerimiento.AUTORIZADO);
    }

    public void rechazar(Requerimiento requerimiento) throws BaseDatosException {
        cambiarAutorizacion(requerimiento, Requerimiento.RECHAZADO);
    }

    private void cambiarAutorizacion(Requerimiento requerimiento, String nuevoEstado) throws BaseDatosException {
        String sql = "UPDATE requerimiento SET estado = ?, id_autoriza = ?, fecha_autorizacion = ?, observacion = ? "
                + "WHERE id_requerimiento = ? AND estado = 'Solicitado'";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, requerimiento.getAutorizadoPor().getIdEmpleado());
            ps.setTimestamp(3, aTimestamp(requerimiento.getFechaAutorizacion()));
            ps.setString(4, requerimiento.getObservacion());
            ps.setInt(5, requerimiento.getIdRequerimiento());
            if (ps.executeUpdate() == 0) {
                throw new BaseDatosException("El requerimiento ya fue atendido por otro usuario.");
            }
        } catch (SQLException e) {
            throw error("actualizar el requerimiento", e);
        }
    }

    public void procesar(Requerimiento requerimiento) throws BaseDatosException {
        Conexion bd = Conexion.getInstancia();
        try {
            bd.iniciarTransaccion();

            String descontar = "UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
            for (int i = 0; i < requerimiento.getDetalles().size(); i++) {
                DetalleRequerimiento d = requerimiento.getDetalles().get(i);
                try (PreparedStatement ps = preparar(descontar)) {
                    ps.setInt(1, d.getCantidad());
                    ps.setInt(2, d.getProducto().getIdProducto());
                    ps.setInt(3, d.getCantidad());
                    if (ps.executeUpdate() == 0) {
                        throw new BaseDatosException("No hay existencia suficiente de " + d.getProducto().getNombre()
                                + ". No se proceso ningun producto del requerimiento.");
                    }
                }
            }

            String sql = "UPDATE requerimiento SET estado = 'Procesado', id_procesa = ?, fecha_proceso = ? "
                    + "WHERE id_requerimiento = ? AND estado = 'Autorizado'";
            try (PreparedStatement ps = preparar(sql)) {
                ps.setInt(1, requerimiento.getProcesadoPor().getIdEmpleado());
                ps.setTimestamp(2, aTimestamp(requerimiento.getFechaProceso()));
                ps.setInt(3, requerimiento.getIdRequerimiento());
                if (ps.executeUpdate() == 0) {
                    throw new BaseDatosException("El requerimiento no esta autorizado. No se proceso ningun producto.");
                }
            }

            bd.confirmarTransaccion();
        } catch (SQLException e) {
            bd.deshacerTransaccion();
            throw error("procesar la salida de bodega", e);
        } catch (BaseDatosException e) {
            bd.deshacerTransaccion();
            throw e;
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM requerimiento WHERE id_requerimiento = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar el requerimiento", e);
        }
    }

    @Override
    public Requerimiento buscarPorId(int id) throws BaseDatosException {
        ArrayList<Requerimiento> lista = consultar(CONSULTA + "WHERE r.id_requerimiento = " + id, new ArrayList<String>());
        if (lista.isEmpty()) {
            return null;
        }
        Requerimiento r = lista.get(0);
        r.setDetalles(detallesDe(id));
        return r;
    }

    @Override
    public ArrayList<Requerimiento> listar() throws BaseDatosException {
        return consultar(CONSULTA + "ORDER BY r.id_requerimiento DESC", new ArrayList<String>());
    }

    public ArrayList<Requerimiento> filtrar(String estado, LocalDate desde, LocalDate hasta, String criterio, String texto)
            throws BaseDatosException {
        String condicion = "1 = 1";
        ArrayList<String> valores = new ArrayList<>();
        if (estado != null && !estado.equals("Todos")) {
            condicion = condicion + " AND r.estado = ?";
            valores.add(estado);
        }
        if (desde != null) {
            condicion = condicion + " AND r.fecha_solicitud >= ?";
            valores.add(desde.toString() + " 00:00:00");
        }
        if (hasta != null) {
            condicion = condicion + " AND r.fecha_solicitud <= ?";
            valores.add(hasta.toString() + " 23:59:59");
        }
        if (texto != null && !texto.trim().isEmpty()) {
            if (criterio.equals("Numero")) {
                condicion = condicion + " AND r.numero LIKE ?";
                valores.add(patron(texto));
            } else if (criterio.equals("Orden")) {
                condicion = condicion + " AND o.numero LIKE ?";
                valores.add(patron(texto));
            } else if (criterio.equals("Taller")) {
                condicion = condicion + " AND t.nombre LIKE ?";
                valores.add(patron(texto));
            } else if (criterio.equals("Solicitante")) {
                condicion = condicion + " AND CONCAT(es.nombre, ' ', es.apellido) LIKE ?";
                valores.add(patron(texto));
            } else if (criterio.equals("Producto")) {
                condicion = condicion + " AND EXISTS (SELECT 1 FROM detalle_requerimiento d JOIN producto p "
                        + "ON p.id_producto = d.id_producto WHERE d.id_requerimiento = r.id_requerimiento "
                        + "AND p.nombre LIKE ?)";
                valores.add(patron(texto));
            } else {
                condicion = condicion + " AND (r.numero LIKE ? OR o.numero LIKE ? OR t.nombre LIKE ? "
                        + "OR CONCAT(es.nombre, ' ', es.apellido) LIKE ?)";
                for (int i = 0; i < 4; i++) {
                    valores.add(patron(texto));
                }
            }
        }
        return consultar(CONSULTA + "WHERE " + condicion + " ORDER BY r.id_requerimiento DESC", valores);
    }

    public ArrayList<DetalleRequerimiento> detallesDe(int idRequerimiento) throws BaseDatosException {
        ArrayList<DetalleRequerimiento> detalles = new ArrayList<>();
        String sql = "SELECT d.*, p.codigo, p.nombre, p.stock, p.precio_venta, p.id_categoria, c.nombre AS categoria_nombre "
                + "FROM detalle_requerimiento d JOIN producto p ON p.id_producto = d.id_producto "
                + "JOIN categoria c ON c.id_categoria = p.id_categoria "
                + "WHERE d.id_requerimiento = ? ORDER BY d.id_detalle";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setInt(1, idRequerimiento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Categoria c = new Categoria();
                    c.setIdCategoria(rs.getInt("id_categoria"));
                    c.setNombre(rs.getString("categoria_nombre"));
                    Producto p = new Producto();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setCodigo(rs.getString("codigo"));
                    p.setNombre(rs.getString("nombre"));
                    p.setStock(rs.getInt("stock"));
                    p.setPrecioVenta(rs.getDouble("precio_venta"));
                    p.setCategoria(c);
                    DetalleRequerimiento d = new DetalleRequerimiento();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setProducto(p);
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    detalles.add(d);
                }
            }
        } catch (SQLException e) {
            throw error("consultar el detalle del requerimiento", e);
        }
        return detalles;
    }

    public int contarPorEstado(String estado) throws BaseDatosException {
        try (PreparedStatement ps = preparar("SELECT COUNT(*) FROM requerimiento WHERE estado = ?")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw error("contar los requerimientos", e);
        }
    }

    private String siguienteNumero() throws SQLException, BaseDatosException {
        String prefijo = "REQ-" + LocalDate.now().getYear() + "-";
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(numero, 10) AS UNSIGNED)), 0) + 1 FROM requerimiento WHERE numero LIKE ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, prefijo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return prefijo + String.format("%04d", rs.getInt(1));
            }
        }
    }

    private ArrayList<Requerimiento> consultar(String sql, ArrayList<String> valores) throws BaseDatosException {
        ArrayList<Requerimiento> lista = new ArrayList<>();
        try (PreparedStatement ps = preparar(sql)) {
            for (int i = 0; i < valores.size(); i++) {
                ps.setString(i + 1, valores.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw error("consultar los requerimientos", e);
        }
        return lista;
    }

    private Requerimiento mapear(ResultSet rs) throws SQLException {
        Taller taller = new Taller();
        taller.setId(rs.getInt("id_taller"));
        taller.setNombreTaller(rs.getString("taller_nombre"));
        int idJefe = rs.getInt("id_jefe");
        if (idJefe > 0) {
            JefeTaller jefeDelTaller = new JefeTaller();
            jefeDelTaller.setIdEmpleado(idJefe);
            taller.setJefeCargo(jefeDelTaller);
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(rs.getString("placa"));

        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setIdOrden(rs.getInt("id_orden"));
        orden.setNumero(rs.getString("orden_numero"));
        orden.setEstado(rs.getString("orden_estado"));
        orden.setTaller(taller);
        orden.setVehiculo(vehiculo);

        Mecanico solicitante = new Mecanico();
        solicitante.setIdEmpleado(rs.getInt("id_solicitante"));
        solicitante.setNombre(rs.getString("s_nombre"));
        solicitante.setApellido(rs.getString("s_apellido"));

        Requerimiento r = new Requerimiento();
        r.setIdRequerimiento(rs.getInt("id_requerimiento"));
        r.setNumero(rs.getString("numero"));
        r.setOrdenTrabajo(orden);
        r.setSolicitante(solicitante);
        r.setEstado(rs.getString("estado"));
        r.setObservacion(rs.getString("observacion"));
        r.setFechaSolicitud(aFechaHora(rs.getTimestamp("fecha_solicitud")));
        r.setFechaAutorizacion(aFechaHora(rs.getTimestamp("fecha_autorizacion")));
        r.setFechaProceso(aFechaHora(rs.getTimestamp("fecha_proceso")));
        r.setTotalCalculado(rs.getDouble("total"));

        int idAutoriza = rs.getInt("id_autoriza");
        if (idAutoriza > 0) {
            JefeTaller jefe = new JefeTaller();
            jefe.setIdEmpleado(idAutoriza);
            jefe.setNombre(rs.getString("a_nombre"));
            jefe.setApellido(rs.getString("a_apellido"));
            r.setAutorizadoPor(jefe);
        }
        int idProcesa = rs.getInt("id_procesa");
        if (idProcesa > 0) {
            Bodeguero bodeguero = new Bodeguero();
            bodeguero.setIdEmpleado(idProcesa);
            bodeguero.setNombre(rs.getString("p_nombre"));
            bodeguero.setApellido(rs.getString("p_apellido"));
            r.setProcesadoPor(bodeguero);
        }
        return r;
    }
}
