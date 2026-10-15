package tallermecanico.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Bodeguero extends Empleado {

    private String areaTrabajo;

    public Bodeguero() {
    }

    public Bodeguero(String nombre, String apellido, String dpi, String telefono, String direccion, String correo,
            String codigoEmpleado, LocalDate fechaContratacion, String areaTrabajo) {
        super(nombre, apellido, dpi, telefono, direccion, correo, codigoEmpleado, fechaContratacion);
        this.areaTrabajo = areaTrabajo;
    }

    @Override
    public String getPuesto() {
        return "Bodeguero";
    }

    @Override
    public String getDatoPuesto() {
        return areaTrabajo;
    }

    @Override
    public void setDatoPuesto(String dato) {
        this.areaTrabajo = dato;
    }

    public String getAreaTrabajo() {
        return areaTrabajo;
    }

    public void setAreaTrabajo(String areaTrabajo) {
        this.areaTrabajo = areaTrabajo;
    }

    public void procesarRequerimiento(Requerimiento requerimiento) {
        requerimiento.generarSalida();
        requerimiento.setProcesadoPor(this);
        requerimiento.setFechaProceso(LocalDateTime.now());
    }

    public void cambiarEstadoRequerimiento(Requerimiento requerimiento, String nuevoEstado) {
        requerimiento.setEstado(nuevoEstado);
    }
}
