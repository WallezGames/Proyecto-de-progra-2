package tallermecanico.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JefeTaller extends Empleado {

    private String tallerArea;

    public JefeTaller() {
    }

    public JefeTaller(String nombre, String apellido, String dpi, String telefono, String direccion, String correo,
            String codigoEmpleado, LocalDate fechaContratacion, String tallerArea) {
        super(nombre, apellido, dpi, telefono, direccion, correo, codigoEmpleado, fechaContratacion);
        this.tallerArea = tallerArea;
    }

    @Override
    public String getPuesto() {
        return "Jefe de Taller";
    }

    @Override
    public String getDatoPuesto() {
        return tallerArea;
    }

    @Override
    public void setDatoPuesto(String dato) {
        this.tallerArea = dato;
    }

    public String getTallerArea() {
        return tallerArea;
    }

    public void setTallerArea(String tallerArea) {
        this.tallerArea = tallerArea;
    }

    public void asignarEmpleadoAUnidad(Taller taller, Mecanico mecanico) {
        if (!taller.getMecanicos().contains(mecanico)) {
            taller.getMecanicos().add(mecanico);
            mecanico.setTaller(taller);
        }
    }

    public void autorizarRequerimiento(Requerimiento requerimiento) {
        requerimiento.setEstado(Requerimiento.AUTORIZADO);
        requerimiento.setAutorizadoPor(this);
        requerimiento.setFechaAutorizacion(LocalDateTime.now());
    }

    public void rechazarRequerimiento(Requerimiento requerimiento) {
        requerimiento.setEstado(Requerimiento.RECHAZADO);
        requerimiento.setAutorizadoPor(this);
        requerimiento.setFechaAutorizacion(LocalDateTime.now());
    }
}
