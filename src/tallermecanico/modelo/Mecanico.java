package tallermecanico.modelo;

import java.time.LocalDate;

public class Mecanico extends Empleado {

    private String especialidad;
    private Taller taller;

    public Mecanico() {
    }

    public Mecanico(String nombre, String apellido, String dpi, String telefono, String direccion, String correo,
            String codigoEmpleado, LocalDate fechaContratacion, String especialidad) {
        super(nombre, apellido, dpi, telefono, direccion, correo, codigoEmpleado, fechaContratacion);
        this.especialidad = especialidad;
    }

    @Override
    public String getPuesto() {
        return "Mecanico";
    }

    @Override
    public String getDatoPuesto() {
        return especialidad;
    }

    @Override
    public void setDatoPuesto(String dato) {
        this.especialidad = dato;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public Taller getTaller() {
        return taller;
    }

    public void setTaller(Taller taller) {
        this.taller = taller;
    }

    public void registrarAvance(OrdenTrabajo orden, String descripcion) {
        String actual = orden.getTrabajosRealizados();
        if (actual == null || actual.trim().isEmpty()) {
            orden.setTrabajosRealizados(descripcion);
        } else {
            orden.setTrabajosRealizados(actual + " | " + descripcion);
        }
    }

    public Requerimiento generarRequerimiento(OrdenTrabajo orden) {
        Requerimiento req = new Requerimiento();
        req.setOrdenTrabajo(orden);
        req.setSolicitante(this);
        req.setEstado(Requerimiento.SOLICITADO);
        return req;
    }
}
