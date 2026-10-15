package tallermecanico.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Cliente extends Persona {

    private int idCliente;
    private String nit;
    private LocalDateTime fechaRegistro;
    private boolean estado;
    private int totalVehiculos;
    private ArrayList<Vehiculo> vehiculos;

    public Cliente() {
        vehiculos = new ArrayList<>();
        estado = true;
    }

    public Cliente(String nombre, String apellido, String dpi, String telefono, String direccion, String correo, String nit) {
        super(nombre, apellido, dpi, telefono, direccion, correo);
        this.nit = nit;
        this.estado = true;
        this.vehiculos = new ArrayList<>();
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public int getTotalVehiculos() {
        return totalVehiculos;
    }

    public void setTotalVehiculos(int totalVehiculos) {
        this.totalVehiculos = totalVehiculos;
    }

    public ArrayList<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public void setVehiculos(ArrayList<Vehiculo> vehiculos) {
        this.vehiculos = vehiculos;
    }

    public void registrarVehiculo(Vehiculo vehiculo) {
        vehiculos.add(vehiculo);
        vehiculo.setPropietario(this);
    }

    public ArrayList<OrdenTrabajo> consultarHistorial() {
        ArrayList<OrdenTrabajo> historial = new ArrayList<>();
        for (int i = 0; i < vehiculos.size(); i++) {
            Vehiculo v = vehiculos.get(i);
            for (int j = 0; j < v.historialServicios().size(); j++) {
                historial.add(v.historialServicios().get(j));
            }
        }
        return historial;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cliente)) {
            return false;
        }
        Cliente otro = (Cliente) obj;
        return idCliente == otro.idCliente;
    }

    @Override
    public int hashCode() {
        return idCliente;
    }
}
