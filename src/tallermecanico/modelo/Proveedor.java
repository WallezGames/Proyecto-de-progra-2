package tallermecanico.modelo;

public class Proveedor {

    private int idProveedor;
    private String nombre;
    private String nit;
    private String telefono;
    private String correo;
    private String direccion;
    private boolean estado;
    private int totalProductos;

    public Proveedor() {
        estado = true;
    }

    public Proveedor(String nombre, String nit, String telefono, String correo, String direccion) {
        this.nombre = nombre;
        this.nit = nit;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.estado = true;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public int getTotalProductos() {
        return totalProductos;
    }

    public void setTotalProductos(int totalProductos) {
        this.totalProductos = totalProductos;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Proveedor)) {
            return false;
        }
        Proveedor otro = (Proveedor) obj;
        return idProveedor == otro.idProveedor;
    }

    @Override
    public int hashCode() {
        return idProveedor;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
