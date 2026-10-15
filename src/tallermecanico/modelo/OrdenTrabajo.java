package tallermecanico.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class OrdenTrabajo {

    public static final String PENDIENTE = "Pendiente";
    public static final String EN_PROCESO = "En proceso";
    public static final String TERMINADO = "Terminado";
    public static final String ENTREGADO = "Entregado";
    public static final String CANCELADO = "Cancelado";

    private int idOrden;
    private String numero;
    private Vehiculo vehiculo;
    private Taller taller;
    private Empleado registradaPor;
    private ArrayList<Mecanico> mecanicos;
    private ArrayList<DetalleOrden> detalles;
    private ArrayList<Requerimiento> requerimientos;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaSalida;
    private String descripcion;
    private String trabajosRealizados;
    private String estado;
    private double totalServicios;
    private double totalRepuestos;

    public OrdenTrabajo() {
        mecanicos = new ArrayList<>();
        detalles = new ArrayList<>();
        requerimientos = new ArrayList<>();
        estado = PENDIENTE;
        trabajosRealizados = "";
    }

    public int getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(int idOrden) {
        this.idOrden = idOrden;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Taller getTaller() {
        return taller;
    }

    public void setTaller(Taller taller) {
        this.taller = taller;
    }

    public Empleado getRegistradaPor() {
        return registradaPor;
    }

    public void setRegistradaPor(Empleado registradaPor) {
        this.registradaPor = registradaPor;
    }

    public ArrayList<Mecanico> getMecanicos() {
        return mecanicos;
    }

    public void setMecanicos(ArrayList<Mecanico> mecanicos) {
        this.mecanicos = mecanicos;
    }

    public ArrayList<DetalleOrden> getDetalles() {
        return detalles;
    }

    public void setDetalles(ArrayList<DetalleOrden> detalles) {
        this.detalles = detalles;
    }

    public ArrayList<Requerimiento> getRequerimientos() {
        return requerimientos;
    }

    public void setRequerimientos(ArrayList<Requerimiento> requerimientos) {
        this.requerimientos = requerimientos;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTrabajosRealizados() {
        return trabajosRealizados;
    }

    public void setTrabajosRealizados(String trabajosRealizados) {
        this.trabajosRealizados = trabajosRealizados;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getTotalServicios() {
        return totalServicios;
    }

    public void setTotalServicios(double totalServicios) {
        this.totalServicios = totalServicios;
    }

    public double getTotalRepuestos() {
        return totalRepuestos;
    }

    public void setTotalRepuestos(double totalRepuestos) {
        this.totalRepuestos = totalRepuestos;
    }

    public double getTotal() {
        return totalServicios + totalRepuestos;
    }

    public void cambiarEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
        if (nuevoEstado.equals(TERMINADO)) {
            this.fechaSalida = LocalDateTime.now();
        }
    }

    public void asignarMecanico(Mecanico mecanico) {
        if (!mecanicos.contains(mecanico)) {
            mecanicos.add(mecanico);
        }
    }

    public void quitarMecanico(Mecanico mecanico) {
        mecanicos.remove(mecanico);
    }

    public void agregarDetalle(Servicio servicio, int cantidad) {
        for (int i = 0; i < detalles.size(); i++) {
            DetalleOrden d = detalles.get(i);
            if (d.getServicio().equals(servicio)) {
                d.setCantidad(d.getCantidad() + cantidad);
                totalServicios = calcularTotalServicios();
                return;
            }
        }
        detalles.add(new DetalleOrden(servicio, cantidad));
        totalServicios = calcularTotalServicios();
    }

    public void quitarDetalle(int posicion) {
        if (posicion >= 0 && posicion < detalles.size()) {
            detalles.remove(posicion);
        }
        totalServicios = calcularTotalServicios();
    }

    public double calcularTotalServicios() {
        double total = 0;
        for (int i = 0; i < detalles.size(); i++) {
            total = total + detalles.get(i).subtotal();
        }
        return total;
    }

    public String nombresMecanicos() {
        if (mecanicos.isEmpty()) {
            return "Sin asignar";
        }
        String texto = "";
        for (int i = 0; i < mecanicos.size(); i++) {
            if (i > 0) {
                texto = texto + ", ";
            }
            texto = texto + mecanicos.get(i).obtenerNombreCompleto();
        }
        return texto;
    }

    public boolean estaActiva() {
        return estado.equals(PENDIENTE) || estado.equals(EN_PROCESO);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OrdenTrabajo)) {
            return false;
        }
        OrdenTrabajo otra = (OrdenTrabajo) obj;
        return idOrden == otra.idOrden;
    }

    @Override
    public int hashCode() {
        return idOrden;
    }

    @Override
    public String toString() {
        if (vehiculo == null) {
            return numero;
        }
        return numero + " - " + vehiculo.getPlaca();
    }
}
