package tallermecanico.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Recepcionista extends Empleado {

    private String turno;

    public Recepcionista() {
    }

    public Recepcionista(String nombre, String apellido, String dpi, String telefono, String direccion, String correo,
            String codigoEmpleado, LocalDate fechaContratacion, String turno) {
        super(nombre, apellido, dpi, telefono, direccion, correo, codigoEmpleado, fechaContratacion);
        this.turno = turno;
    }

    @Override
    public String getPuesto() {
        return "Recepcionista";
    }

    @Override
    public String getDatoPuesto() {
        return turno;
    }

    @Override
    public void setDatoPuesto(String dato) {
        this.turno = dato;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public OrdenTrabajo generarOrdenTrabajo(Vehiculo vehiculo, Taller taller, String descripcion) {
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setVehiculo(vehiculo);
        orden.setTaller(taller);
        orden.setDescripcion(descripcion);
        orden.setFechaIngreso(LocalDateTime.now());
        orden.setRegistradaPor(this);
        return orden;
    }

    public void actualizarEstadoOrdenTrabajo(OrdenTrabajo orden, String nuevoEstado) {
        orden.cambiarEstado(nuevoEstado);
    }
}
