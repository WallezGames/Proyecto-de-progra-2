package tallermecanico.modelo;

public class Servicio {

    private int idServicio;
    private String nombre;
    private String descripcion;
    private Taller taller;
    private double precio;
    private boolean estado;

    public Servicio() {
        estado = true;
    }

    public Servicio(String nombre, String descripcion, Taller taller, double precio) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.taller = taller;
        this.precio = precio;
        this.estado = true;
    }

    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Taller getTaller() {
        return taller;
    }

    public void setTaller(Taller taller) {
        this.taller = taller;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Servicio)) {
            return false;
        }
        Servicio otro = (Servicio) obj;
        return idServicio == otro.idServicio;
    }

    @Override
    public int hashCode() {
        return idServicio;
    }

    @Override
    public String toString() {
        return nombre + " (Q" + String.format(java.util.Locale.US, "%.2f", precio) + ")";
    }
}
