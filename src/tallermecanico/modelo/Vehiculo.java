package tallermecanico.modelo;

import java.util.ArrayList;

public class Vehiculo {

    private int idVehiculo;
    private String placa;
    private String marca;
    private String modelo;
    private int anio;
    private String color;
    private int kilometraje;
    private String chasis;
    private String estado;
    private Cliente propietario;
    private int totalServicios;
    private ArrayList<OrdenTrabajo> historial;

    public Vehiculo() {
        historial = new ArrayList<>();
        estado = "Activo";
    }

    public Vehiculo(String placa, String marca, String modelo, int anio, String color, int kilometraje,
            String chasis, Cliente propietario) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.kilometraje = kilometraje;
        this.chasis = chasis;
        this.propietario = propietario;
        this.estado = "Activo";
        this.historial = new ArrayList<>();
    }

    public int getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(int idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getKilometraje() {
        return kilometraje;
    }

    public void setKilometraje(int kilometraje) {
        this.kilometraje = kilometraje;
    }

    public String getChasis() {
        return chasis;
    }

    public void setChasis(String chasis) {
        this.chasis = chasis;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Cliente getPropietario() {
        return propietario;
    }

    public void setPropietario(Cliente propietario) {
        this.propietario = propietario;
    }

    public int getTotalServicios() {
        return totalServicios;
    }

    public void setTotalServicios(int totalServicios) {
        this.totalServicios = totalServicios;
    }

    public ArrayList<OrdenTrabajo> historialServicios() {
        return historial;
    }

    public void agregarOrden(OrdenTrabajo orden) {
        historial.add(orden);
    }

    public String getDescripcion() {
        return marca + " " + modelo + " " + anio;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vehiculo)) {
            return false;
        }
        Vehiculo otro = (Vehiculo) obj;
        return idVehiculo == otro.idVehiculo;
    }

    @Override
    public int hashCode() {
        return idVehiculo;
    }

    @Override
    public String toString() {
        return placa + " - " + getDescripcion();
    }
}
