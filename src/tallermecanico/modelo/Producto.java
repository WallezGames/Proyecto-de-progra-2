package tallermecanico.modelo;

public class Producto {

    private int idProducto;
    private String codigo;
    private String nombre;
    private String marca;
    private Categoria categoria;
    private Proveedor proveedor;
    private double precioCompra;
    private double precioVenta;
    private int stock;
    private int stockMin;
    private int stockMax;
    private boolean estado;

    public Producto() {
        estado = true;
    }

    public Producto(String codigo, String nombre, String marca, Categoria categoria, Proveedor proveedor,
            double precioCompra, double precioVenta, int stock, int stockMin, int stockMax) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.categoria = categoria;
        this.proveedor = proveedor;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stock = stock;
        this.stockMin = stockMin;
        this.stockMax = stockMax;
        this.estado = true;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMin() {
        return stockMin;
    }

    public void setStockMin(int stockMin) {
        this.stockMin = stockMin;
    }

    public int getStockMax() {
        return stockMax;
    }

    public void setStockMax(int stockMax) {
        this.stockMax = stockMax;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public boolean estaBajoMinimo() {
        return stock < stockMin;
    }

    public void descontarStock(int cantidad) {
        stock = stock - cantidad;
    }

    public void aumentarStock(int cantidad) {
        stock = stock + cantidad;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Producto)) {
            return false;
        }
        Producto otro = (Producto) obj;
        return idProducto == otro.idProducto;
    }

    @Override
    public int hashCode() {
        return idProducto;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
