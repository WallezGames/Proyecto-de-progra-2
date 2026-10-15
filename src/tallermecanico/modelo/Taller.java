package tallermecanico.modelo;

import java.util.ArrayList;

public class Taller {

    private int id;
    private String nombreTaller;
    private String descripcion;
    private JefeTaller jefeCargo;
    private ArrayList<Mecanico> mecanicos;
    private int totalOrdenesActivas;
    private boolean estado;

    public Taller() {
        mecanicos = new ArrayList<>();
        estado = true;
    }

    public Taller(String nombreTaller, String descripcion) {
        this.nombreTaller = nombreTaller;
        this.descripcion = descripcion;
        this.mecanicos = new ArrayList<>();
        this.estado = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreTaller() {
        return nombreTaller;
    }

    public void setNombreTaller(String nombreTaller) {
        this.nombreTaller = nombreTaller;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public JefeTaller getJefeCargo() {
        return jefeCargo;
    }

    public void setJefeCargo(JefeTaller jefeCargo) {
        this.jefeCargo = jefeCargo;
    }

    public ArrayList<Mecanico> getMecanicos() {
        return mecanicos;
    }

    public void setMecanicos(ArrayList<Mecanico> mecanicos) {
        this.mecanicos = mecanicos;
    }

    public int getTotalOrdenesActivas() {
        return totalOrdenesActivas;
    }

    public void setTotalOrdenesActivas(int totalOrdenesActivas) {
        this.totalOrdenesActivas = totalOrdenesActivas;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void agregarMecanico(Mecanico mecanico) {
        if (!mecanicos.contains(mecanico)) {
            mecanicos.add(mecanico);
            mecanico.setTaller(this);
        }
    }

    public void quitarMecanico(Mecanico mecanico) {
        mecanicos.remove(mecanico);
        mecanico.setTaller(null);
    }

    public int cantidadMecanicos() {
        return mecanicos.size();
    }

    public String nombreJefe() {
        if (jefeCargo == null) {
            return "Sin asignar";
        }
        return jefeCargo.obtenerNombreCompleto();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Taller)) {
            return false;
        }
        Taller otro = (Taller) obj;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return nombreTaller;
    }
}
