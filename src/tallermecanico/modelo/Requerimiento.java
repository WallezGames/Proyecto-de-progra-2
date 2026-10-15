package tallermecanico.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Requerimiento {

    public static final String SOLICITADO = "Solicitado";
    public static final String AUTORIZADO = "Autorizado";
    public static final String RECHAZADO = "Rechazado";
    public static final String PROCESADO = "Procesado";

    private int idRequerimiento;
    private String numero;
    private OrdenTrabajo ordenTrabajo;
    private Mecanico solicitante;
    private JefeTaller autorizadoPor;
    private Bodeguero procesadoPor;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaAutorizacion;
    private LocalDateTime fechaProceso;
    private String estado;
    private String observacion;
    private ArrayList<DetalleRequerimiento> detalles;
    private double totalCalculado;

    public Requerimiento() {
        estado = SOLICITADO;
        detalles = new ArrayList<>();
    }

    public int getIdRequerimiento() {
        return idRequerimiento;
    }

    public void setIdRequerimiento(int idRequerimiento) {
        this.idRequerimiento = idRequerimiento;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public OrdenTrabajo getOrdenTrabajo() {
        return ordenTrabajo;
    }

    public void setOrdenTrabajo(OrdenTrabajo ordenTrabajo) {
        this.ordenTrabajo = ordenTrabajo;
    }

    public Mecanico getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Mecanico solicitante) {
        this.solicitante = solicitante;
    }

    public JefeTaller getAutorizadoPor() {
        return autorizadoPor;
    }

    public void setAutorizadoPor(JefeTaller autorizadoPor) {
        this.autorizadoPor = autorizadoPor;
    }

    public Bodeguero getProcesadoPor() {
        return procesadoPor;
    }

    public void setProcesadoPor(Bodeguero procesadoPor) {
        this.procesadoPor = procesadoPor;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public LocalDateTime getFechaProceso() {
        return fechaProceso;
    }

    public void setFechaProceso(LocalDateTime fechaProceso) {
        this.fechaProceso = fechaProceso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public ArrayList<DetalleRequerimiento> getDetalles() {
        return detalles;
    }

    public void setDetalles(ArrayList<DetalleRequerimiento> detalles) {
        this.detalles = detalles;
    }

    public void setTotalCalculado(double totalCalculado) {
        this.totalCalculado = totalCalculado;
    }

    public boolean isAutorizado() {
        return autorizadoPor != null && (estado.equals(AUTORIZADO) || estado.equals(PROCESADO));
    }

    public void agregarDetalle(Producto producto, int cantidad) {
        for (int i = 0; i < detalles.size(); i++) {
            DetalleRequerimiento d = detalles.get(i);
            if (d.getProducto().equals(producto)) {
                d.setCantidad(d.getCantidad() + cantidad);
                return;
            }
        }
        detalles.add(new DetalleRequerimiento(producto, cantidad));
    }

    public void quitarDetalle(int posicion) {
        if (posicion >= 0 && posicion < detalles.size()) {
            detalles.remove(posicion);
        }
    }

    public int cantidadDe(Producto producto) {
        for (int i = 0; i < detalles.size(); i++) {
            if (detalles.get(i).getProducto().equals(producto)) {
                return detalles.get(i).getCantidad();
            }
        }
        return 0;
    }

    public void generarSalida() {
        for (int i = 0; i < detalles.size(); i++) {
            DetalleRequerimiento d = detalles.get(i);
            d.getProducto().descontarStock(d.getCantidad());
        }
        estado = PROCESADO;
    }

    public double total() {
        if (detalles.isEmpty()) {
            return totalCalculado;
        }
        double suma = 0;
        for (int i = 0; i < detalles.size(); i++) {
            suma = suma + detalles.get(i).subtotal();
        }
        return suma;
    }

    public String nombreTaller() {
        if (ordenTrabajo == null || ordenTrabajo.getTaller() == null) {
            return "Sin taller";
        }
        return ordenTrabajo.getTaller().getNombreTaller();
    }

    public String nombreAutorizadoPor() {
        if (autorizadoPor == null) {
            return "Pendiente";
        }
        return autorizadoPor.obtenerNombreCompleto();
    }

    public String nombreProcesadoPor() {
        if (procesadoPor == null) {
            return "Pendiente";
        }
        return procesadoPor.obtenerNombreCompleto();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Requerimiento)) {
            return false;
        }
        Requerimiento otro = (Requerimiento) obj;
        return idRequerimiento == otro.idRequerimiento;
    }

    @Override
    public int hashCode() {
        return idRequerimiento;
    }

    @Override
    public String toString() {
        return numero;
    }
}
