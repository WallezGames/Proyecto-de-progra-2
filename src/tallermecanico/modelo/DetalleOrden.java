package tallermecanico.modelo;

public class DetalleOrden {

    private int idDetalle;
    private Servicio servicio;
    private int cantidad;
    private double precioUnitario;

    public DetalleOrden() {
    }

    public DetalleOrden(Servicio servicio, int cantidad) {
        this.servicio = servicio;
        this.cantidad = cantidad;
        this.precioUnitario = servicio.getPrecio();
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double subtotal() {
        return cantidad * precioUnitario;
    }
}
