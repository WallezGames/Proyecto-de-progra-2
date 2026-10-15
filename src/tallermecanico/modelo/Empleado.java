package tallermecanico.modelo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public abstract class Empleado extends Persona {

    private int idEmpleado;
    private String codigoEmpleado;
    private LocalDate fechaContratacion;
    private boolean estado;
    private LocalTime entrada;
    private LocalTime salida;

    public Empleado() {
        estado = true;
    }

    public Empleado(String nombre, String apellido, String dpi, String telefono, String direccion, String correo,
            String codigoEmpleado, LocalDate fechaContratacion) {
        super(nombre, apellido, dpi, telefono, direccion, correo);
        this.codigoEmpleado = codigoEmpleado;
        this.fechaContratacion = fechaContratacion;
        this.estado = true;
    }

    public abstract String getPuesto();

    public abstract String getDatoPuesto();

    public abstract void setDatoPuesto(String dato);

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public LocalTime getEntrada() {
        return entrada;
    }

    public void setEntrada(LocalTime entrada) {
        this.entrada = entrada;
    }

    public LocalTime getSalida() {
        return salida;
    }

    public void setSalida(LocalTime salida) {
        this.salida = salida;
    }

    public double horasTrabajadas() {
        if (entrada == null || salida == null) {
            return 0;
        }
        if (salida.isBefore(entrada)) {
            return 0;
        }
        Duration jornada = Duration.between(entrada, salida);
        return jornada.toMinutes() / 60.0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Empleado)) {
            return false;
        }
        Empleado otro = (Empleado) obj;
        return idEmpleado == otro.idEmpleado;
    }

    @Override
    public int hashCode() {
        return idEmpleado;
    }

    @Override
    public String toString() {
        return obtenerNombreCompleto() + " (" + getPuesto() + ")";
    }
}
