package tallermecanico.modelo;

import java.util.ArrayList;

public class Rol {

    public static final String[] MODULOS = {"Resumen", "Ordenes", "Clientes", "Vehiculos", "Empleados",
        "Talleres", "Inventario", "Requerimientos", "Catalogos", "Usuarios", "Reportes"};

    private int idRol;
    private String nombre;
    private String descripcion;
    private ArrayList<String> permisos;

    public Rol() {
        permisos = new ArrayList<>();
    }

    public Rol(int idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.permisos = new ArrayList<>();
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public ArrayList<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(ArrayList<String> permisos) {
        this.permisos = permisos;
    }

    public boolean tienePermiso(String modulo) {
        return permisos.contains(modulo);
    }

    public void agregarPermiso(String modulo) {
        if (!permisos.contains(modulo)) {
            permisos.add(modulo);
        }
    }

    public void quitarPermiso(String modulo) {
        permisos.remove(modulo);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Rol)) {
            return false;
        }
        Rol otro = (Rol) obj;
        return idRol == otro.idRol;
    }

    @Override
    public int hashCode() {
        return idRol;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
