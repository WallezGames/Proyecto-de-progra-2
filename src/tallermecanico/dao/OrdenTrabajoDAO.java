package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import tallermecanico.conexion.Conexion;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.fabrica.EmpleadoFactory;
import tallermecanico.modelo.Cliente;
import tallermecanico.modelo.DetalleOrden;
import tallermecanico.modelo.Empleado;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.OrdenTrabajo;
import tallermecanico.modelo.Servicio;
import tallermecanico.modelo.Taller;
import tallermecanico.modelo.Vehiculo;

public class OrdenTrabajoDAO extends BaseDAO implements CrudDAO<OrdenTrabajo> {

    private static final String CONSULTA = "SELECT o.*, v.placa, v.marca, v.modelo, v.anio, v.id_cliente, "
            + "c.nombre AS c_nombre, c.apellido AS c_apellido, c.telefono AS c_telefono, "
            + "t.nombre AS taller_nombre, t.id_jefe, er.nombre AS r_nombre, er.apellido AS r_apellido, rr.nombre AS r_rol, "
            + "vt.total_servicios, vt.total_repuestos "
            + "FROM orden_trabajo o "
            + "JOIN vehiculo v ON v.id_vehiculo = o.id_vehiculo "
            + "JOIN cliente c ON c.id_cliente = v.id_cliente "
            + "JOIN taller t ON t.id_taller = o.id_taller "
            + "JOIN empleado er ON er.id_empleado = o.id_registra "
            + "JOIN rol rr ON rr.id_rol = er.id_rol "
            + "JOIN vista_total_orden vt ON vt.id_orden = o.id_orden ";

    @Override
    public int insertar(OrdenTrabajo orden) throws BaseDatosException {
        Conexion bd = Conexion.getInstancia();
        try {
            bd.iniciarTransaccion();

            orden.setNumero(siguienteNumero());
            String sql = "INSERT INTO orden_trabajo (numero, id_vehiculo, id_taller, id_registra, fecha_ingreso, "
                    + "descripcion, trabajos_realizados, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            int idOrden;
            try (PreparedStatement ps = prepararConLlave(sql)) {
                ps.setString(1, orden.getNumero());
                ps.setInt(2, orden.getVehiculo().getIdVehiculo());
                ps.setInt(3, orden.getTaller().getId());
                ps.setInt(4, orden.getRegistradaPor().getIdEmpleado());
                ps.setTimestamp(5, aTimestamp(orden.getFechaIngreso()));
                ps.setString(6, orden.getDescripcion());
                ps.setString(7, orden.getTrabajosRealizados());
                ps.setString(8, orden.getEstado());
                ps.executeUpdate();
                idOrden = idGenerado(ps);
            }
            orden.setIdOrden(idOrden);

            insertarDetalles(orden);

            for (int i = 0; i < orden.getMecanicos().size(); i++) {
                insertarMecanico(idOrden, orden.getMecanicos().get(i).getIdEmpleado());
            }

            bd.confirmarTransaccion();
            return idOrden;
        } catch (SQLException e) {
            bd.deshacerTransaccion();
            throw error("guardar la orden de trabajo", e);
        } catch (BaseDatosException e) {
            bd.deshacerTransaccion();
            throw e;
        }
    }

    @Override
    public void actualizar(OrdenTrabajo orden) throws BaseDatosException {
        Conexion bd = Conexion.getInstancia();
        try {
            bd.iniciarTransaccion();

            String sql = "UPDATE orden_trabajo SET id_taller = ?, descripcion = ?, trabajos_realizados = ? WHERE id_orden = ?";
            try (PreparedStatement ps = preparar(sql)) {
                ps.setInt(1, orden.getTaller().getId());
                ps.setString(2, orden.getDescripcion());
                ps.setString(3, orden.getTrabajosRealizados());
                ps.setInt(4, orden.getIdOrden());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = preparar("DELETE FROM detalle_orden WHERE id_orden = ?")) {
                ps.setInt(1, orden.getIdOrden());
                ps.executeUpdate();
            }
            insertarDetalles(orden);

            bd.confirmarTransaccion();
        } catch (SQLException e) {
            bd.deshacerTransaccion();
            throw error("modificar la orden de trabajo", e);
        } catch (BaseDatosException e) {
            bd.deshacerTransaccion();
            throw e;
        }
    }

    private void insertarDetalles(OrdenTrabajo orden) throws SQLException, BaseDatosException {
        String sql = "INSERT INTO detalle_orden (id_orden, id_servicio, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        for (int i = 0; i < orden.getDetalles().size(); i++) {
            DetalleOrden d = orden.getDetalles().get(i);
            try (PreparedStatement ps = preparar(sql)) {
                ps.setInt(1, orden.getIdOrden());
                ps.setInt(2, d.getServicio().getIdServicio());
                ps.setInt(3, d.getCantidad());
                ps.setDouble(4, d.getPrecioUnitario());
                ps.executeUpdate();
            }
        }
    }

    private void insertarMecanico(int idOrden, int idMecanico) throws SQLException, BaseDatosException {
        try (PreparedStatement ps = preparar("INSERT INTO orden_mecanico (id_orden, id_mecanico) VALUES (?, ?)")) {
            ps.setInt(1, idOrden);
            ps.setInt(2, idMecanico);
            ps.executeUpdate();
        }
    }

    public void agregarMecanico(int idOrden, int idMecanico) throws BaseDatosException {
        try {
            insertarMecanico(idOrden, idMecanico);
        } catch (SQLException e) {
            throw error("asignar el mecanico", e);
        }
    }

    public void quitarMecanico(int idOrden, int idMecanico) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM orden_mecanico WHERE id_orden = ? AND id_mecanico = ?")) {
            ps.setInt(1, idOrden);
            ps.setInt(2, idMecanico);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("quitar el mecanico", e);
        }
    }

    public void cambiarEstado(int idOrden, String estado, LocalDateTime fechaSalida) throws BaseDatosException {
        try (PreparedStatement ps = preparar("UPDATE orden_trabajo SET estado = ?, fecha_salida = ? WHERE id_orden = ?")) {
            ps.setString(1, estado);
            ps.setTimestamp(2, aTimestamp(fechaSalida));
            ps.setInt(3, idOrden);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("cambiar el estado de la orden", e);
        }
    }

    public void registrarAvance(int idOrden, String trabajos) throws BaseDatosException {
        try (PreparedStatement ps = preparar("UPDATE orden_trabajo SET trabajos_realizados = ? WHERE id_orden = ?")) {
            ps.setString(1, trabajos);
            ps.setInt(2, idOrden);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("registrar el avance", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM orden_trabajo WHERE id_orden = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar la orden de trabajo", e);
        }
    }

    @Override
    public OrdenTrabajo buscarPorId(int id) throws BaseDatosException {
        ArrayList<OrdenTrabajo> lista = consultar(CONSULTA + "WHERE o.id_orden = " + id, new ArrayList<String>());
        if (lista.isEmpty()) {
            return null;
        }
        OrdenTrabajo orden = lista.get(0);
        orden.setDetalles(detallesDe(id));
        return orden;
    }

    @Override
    public ArrayList<OrdenTrabajo> listar() throws BaseDatosException {
        return consultar(CONSULTA + "ORDER BY o.id_orden DESC", new ArrayList<String>());
    }

    public ArrayList<OrdenTrabajo> listarActivas() throws BaseDatosException {
        return consultar(CONSULTA + "WHERE o.estado IN ('Pendiente', 'En proceso') ORDER BY o.id_orden", new ArrayList<String>());
    }

    public ArrayList<OrdenTrabajo> filtrar(String estado, Taller taller, LocalDate desde, LocalDate hasta,
            String criterio, String texto) throws BaseDatosException {
        String condicion = "1 = 1";
        ArrayList<String> valores = new ArrayList<>();
        if (estado != null && !estado.equals("Todos")) {
            condicion = condicion + " AND o.estado = ?";
            valores.add(estado);
        }
        if (taller != null) {
            condicion = condicion + " AND o.id_taller = " + taller.getId();
        }
        if (desde != null) {
            condicion = condicion + " AND o.fecha_ingreso >= ?";
            valores.add(desde.toString() + " 00:00:00");
        }
        if (hasta != null) {
            condicion = condicion + " AND o.fecha_ingreso <= ?";
            valores.add(hasta.toString() + " 23:59:59");
        }
        if (texto != null && !texto.trim().isEmpty()) {
            if (criterio.equals("Numero")) {
                condicion = condicion + " AND o.numero LIKE ?";
                valores.add(patron(texto));
            } else if (criterio.equals("Placa")) {
                condicion = condicion + " AND v.placa LIKE ?";
                valores.add(patron(texto));
            } else if (criterio.equals("Cliente")) {
                condicion = condicion + " AND CONCAT(c.nombre, ' ', c.apellido) LIKE ?";
                valores.add(patron(texto));
            } else if (criterio.equals("Mecanico")) {
                condicion = condicion + " AND EXISTS (SELECT 1 FROM orden_mecanico om JOIN empleado em "
                        + "ON em.id_empleado = om.id_mecanico WHERE om.id_orden = o.id_orden "
                        + "AND CONCAT(em.nombre, ' ', em.apellido) LIKE ?)";
                valores.add(patron(texto));
            } else {
                condicion = condicion + " AND (o.numero LIKE ? OR v.placa LIKE ? OR o.descripcion LIKE ? "
                        + "OR CONCAT(c.nombre, ' ', c.apellido) LIKE ?)";
                for (int i = 0; i < 4; i++) {
                    valores.add(patron(texto));
                }
            }
        }
        return consultar(CONSULTA + "WHERE " + condicion + " ORDER BY o.id_orden DESC", valores);
    }

    public ArrayList<DetalleOrden> detallesDe(int idOrden) throws BaseDatosException {
        ArrayList<DetalleOrden> detalles = new ArrayList<>();
        String sql = "SELECT d.*, s.nombre, s.descripcion, s.precio, s.id_taller FROM detalle_orden d "
                + "JOIN servicio s ON s.id_servicio = d.id_servicio WHERE d.id_orden = ? ORDER BY d.id_detalle";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Servicio s = new Servicio();
                    s.setIdServicio(rs.getInt("id_servicio"));
                    s.setNombre(rs.getString("nombre"));
                    s.setDescripcion(rs.getString("descripcion"));
                    s.setPrecio(rs.getDouble("precio"));
                    DetalleOrden d = new DetalleOrden();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setServicio(s);
                    d.setCantidad(rs.getInt("cantidad"));
                    d.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    detalles.add(d);
                }
            }
        } catch (SQLException e) {
            throw error("consultar el detalle de la orden", e);
        }
        return detalles;
    }

    public String requerimientoPendiente(int idOrden) throws BaseDatosException {
        String sql = "SELECT numero FROM requerimiento WHERE id_orden = ? AND estado IN ('Solicitado', 'Autorizado') LIMIT 1";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            throw error("consultar los requerimientos de la orden", e);
        }
        return null;
    }

    public int contarRequerimientos(int idOrden) throws BaseDatosException {
        try (PreparedStatement ps = preparar("SELECT COUNT(*) FROM requerimiento WHERE id_orden = ?")) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw error("consultar los requerimientos de la orden", e);
        }
    }

    public int contarPorEstado(String estado) throws BaseDatosException {
        try (PreparedStatement ps = preparar("SELECT COUNT(*) FROM orden_trabajo WHERE estado = ?")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw error("contar las ordenes", e);
        }
    }

    private String siguienteNumero() throws SQLException, BaseDatosException {
        String prefijo = "OT-" + LocalDate.now().getYear() + "-";
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(numero, 9) AS UNSIGNED)), 0) + 1 FROM orden_trabajo WHERE numero LIKE ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, prefijo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return prefijo + String.format("%04d", rs.getInt(1));
            }
        }
    }

    private ArrayList<OrdenTrabajo> consultar(String sql, ArrayList<String> valores) throws BaseDatosException {
        ArrayList<OrdenTrabajo> lista = new ArrayList<>();
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
            throw error("consultar las ordenes de trabajo", e);
        }
        cargarMecanicos(lista);
        return lista;
    }

    private void cargarMecanicos(ArrayList<OrdenTrabajo> ordenes) throws BaseDatosException {
        if (ordenes.isEmpty()) {
            return;
        }
        String ids = "";
        for (int i = 0; i < ordenes.size(); i++) {
            if (i > 0) {
                ids = ids + ",";
            }
            ids = ids + ordenes.get(i).getIdOrden();
        }
        String sql = "SELECT om.id_orden, e.id_empleado, e.codigo, e.nombre, e.apellido, e.especialidad, e.id_taller "
                + "FROM orden_mecanico om JOIN empleado e ON e.id_empleado = om.id_mecanico "
                + "WHERE om.id_orden IN (" + ids + ") ORDER BY om.fecha_asignacion";
        try (PreparedStatement ps = preparar(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Mecanico m = new Mecanico();
                m.setIdEmpleado(rs.getInt("id_empleado"));
                m.setCodigoEmpleado(rs.getString("codigo"));
                m.setNombre(rs.getString("nombre"));
                m.setApellido(rs.getString("apellido"));
                m.setEspecialidad(rs.getString("especialidad"));
                Taller t = new Taller();
                t.setId(rs.getInt("id_taller"));
                m.setTaller(t);
                int idOrden = rs.getInt("id_orden");
                for (int i = 0; i < ordenes.size(); i++) {
                    if (ordenes.get(i).getIdOrden() == idOrden) {
                        ordenes.get(i).asignarMecanico(m);
                    }
                }
            }
        } catch (SQLException e) {
            throw error("consultar los mecanicos de las ordenes", e);
        }
    }

    private OrdenTrabajo mapear(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setNombre(rs.getString("c_nombre"));
        cliente.setApellido(rs.getString("c_apellido"));
        cliente.setTelefono(rs.getString("c_telefono"));

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setIdVehiculo(rs.getInt("id_vehiculo"));
        vehiculo.setPlaca(rs.getString("placa"));
        vehiculo.setMarca(rs.getString("marca"));
        vehiculo.setModelo(rs.getString("modelo"));
        vehiculo.setAnio(rs.getInt("anio"));
        vehiculo.setPropietario(cliente);

        Taller taller = new Taller();
        taller.setId(rs.getInt("id_taller"));
        taller.setNombreTaller(rs.getString("taller_nombre"));
        int idJefe = rs.getInt("id_jefe");
        if (idJefe > 0) {
            JefeTaller jefe = new JefeTaller();
            jefe.setIdEmpleado(idJefe);
            taller.setJefeCargo(jefe);
        }

        Empleado registra = EmpleadoFactory.crear(rs.getString("r_rol"));
        registra.setIdEmpleado(rs.getInt("id_registra"));
        registra.setNombre(rs.getString("r_nombre"));
        registra.setApellido(rs.getString("r_apellido"));

        OrdenTrabajo o = new OrdenTrabajo();
        o.setIdOrden(rs.getInt("id_orden"));
        o.setNumero(rs.getString("numero"));
        o.setVehiculo(vehiculo);
        o.setTaller(taller);
        o.setRegistradaPor(registra);
        o.setFechaIngreso(aFechaHora(rs.getTimestamp("fecha_ingreso")));
        o.setFechaSalida(aFechaHora(rs.getTimestamp("fecha_salida")));
        o.setDescripcion(rs.getString("descripcion"));
        o.setTrabajosRealizados(rs.getString("trabajos_realizados"));
        o.setEstado(rs.getString("estado"));
        o.setTotalServicios(rs.getDouble("total_servicios"));
        o.setTotalRepuestos(rs.getDouble("total_repuestos"));
        return o;
    }
}
