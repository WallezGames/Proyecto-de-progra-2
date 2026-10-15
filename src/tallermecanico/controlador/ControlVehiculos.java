package tallermecanico.controlador;

import java.util.ArrayList;
import tallermecanico.dao.VehiculoDAO;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.modelo.Cliente;
import tallermecanico.modelo.Vehiculo;
import tallermecanico.util.Validaciones;

public class ControlVehiculos {

    private VehiculoDAO dao;

    public ControlVehiculos() {
        dao = new VehiculoDAO();
    }

    public ArrayList<Vehiculo> listar() throws BaseDatosException {
        return dao.listar();
    }

    public ArrayList<Vehiculo> listarActivos() throws BaseDatosException {
        return dao.listarActivos();
    }

    public ArrayList<Vehiculo> buscar(String criterio, String texto) throws BaseDatosException {
        if (texto == null || texto.trim().isEmpty()) {
            return dao.listar();
        }
        return dao.buscar(criterio, texto);
    }

    public Vehiculo agregar(String placa, String marca, String modelo, String anio, String color,
            String kilometraje, String chasis, Cliente propietario)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(propietario, "el cliente propietario");
        Validaciones.validarPlaca(placa);
        Validaciones.obligatorio(marca, "marca");
        Validaciones.obligatorio(modelo, "modelo");
        int valorAnio = Validaciones.validarAnio(anio);
        Validaciones.obligatorio(color, "color");
        Validaciones.obligatorio(chasis, "chasis");
        Validaciones.longitudMaxima(chasis, 30, "chasis");
        int km = Validaciones.enteroPositivo(kilometraje, "kilometraje");

        if (!propietario.isEstado()) {
            throw new ReglaNegocioException("El cliente " + propietario.obtenerNombreCompleto() + " esta inactivo.");
        }
        if (dao.existePlaca(placa.trim().toUpperCase(), 0)) {
            throw new ReglaNegocioException("Ya existe un vehiculo registrado con la placa " + placa.trim().toUpperCase() + ".");
        }
        if (dao.existeChasis(chasis, 0)) {
            throw new ReglaNegocioException("Ya existe un vehiculo registrado con el chasis " + chasis.trim() + ".");
        }

        Vehiculo vehiculo = new Vehiculo(placa.trim().toUpperCase(), marca.trim(), modelo.trim(), valorAnio,
                color.trim(), km, chasis.trim().toUpperCase(), propietario);
        dao.insertar(vehiculo);
        Auditoria.registrar("Vehiculos", "Alta", "Se registro el vehiculo " + vehiculo.getPlaca()
                + " de " + propietario.obtenerNombreCompleto());
        return vehiculo;
    }

    public void modificar(Vehiculo vehiculo, String placa, String marca, String modelo, String anio, String color,
            String kilometraje, String chasis, Cliente propietario)
            throws DatoInvalidoException, ReglaNegocioException, BaseDatosException {

        Validaciones.seleccionoElemento(propietario, "el cliente propietario");
        Validaciones.validarPlaca(placa);
        Validaciones.obligatorio(marca, "marca");
        Validaciones.obligatorio(modelo, "modelo");
        int valorAnio = Validaciones.validarAnio(anio);
        Validaciones.obligatorio(color, "color");
        Validaciones.obligatorio(chasis, "chasis");
        int km = Validaciones.enteroPositivo(kilometraje, "kilometraje");

        if (dao.existePlaca(placa.trim().toUpperCase(), vehiculo.getIdVehiculo())) {
            throw new ReglaNegocioException("Ya existe otro vehiculo con la placa " + placa.trim().toUpperCase() + ".");
        }
        if (dao.existeChasis(chasis, vehiculo.getIdVehiculo())) {
            throw new ReglaNegocioException("Ya existe otro vehiculo con el chasis " + chasis.trim() + ".");
        }
        if (km < vehiculo.getKilometraje()) {
            throw new ReglaNegocioException("El kilometraje no puede ser menor al registrado anteriormente ("
                    + vehiculo.getKilometraje() + " km).");
        }

        vehiculo.setPlaca(placa.trim().toUpperCase());
        vehiculo.setMarca(marca.trim());
        vehiculo.setModelo(modelo.trim());
        vehiculo.setAnio(valorAnio);
        vehiculo.setColor(color.trim());
        vehiculo.setKilometraje(km);
        vehiculo.setChasis(chasis.trim().toUpperCase());
        vehiculo.setPropietario(propietario);
        dao.actualizar(vehiculo);
        Auditoria.registrar("Vehiculos", "Cambio", "Se modifico el vehiculo " + vehiculo.getPlaca());
    }

    public void eliminar(Vehiculo vehiculo) throws ReglaNegocioException, BaseDatosException {
        String activa = dao.ordenActiva(vehiculo.getIdVehiculo());
        if (activa != null) {
            throw new ReglaNegocioException("No se puede eliminar el vehiculo " + vehiculo.getPlaca()
                    + " porque tiene la orden " + activa + " activa.");
        }
        Vehiculo actual = dao.buscarPorId(vehiculo.getIdVehiculo());
        if (actual != null && actual.getTotalServicios() > 0) {
            throw new ReglaNegocioException("El vehiculo " + vehiculo.getPlaca() + " tiene " + actual.getTotalServicios()
                    + " orden(es) en su historial y no se puede borrar. Puede inactivarlo en su lugar.");
        }
        dao.eliminar(vehiculo.getIdVehiculo());
        Auditoria.registrar("Vehiculos", "Baja", "Se elimino el vehiculo " + vehiculo.getPlaca());
    }

    public void cambiarEstado(Vehiculo vehiculo) throws ReglaNegocioException, BaseDatosException {
        boolean activar = vehiculo.getEstado().equals("Inactivo");
        if (!activar && dao.ordenActiva(vehiculo.getIdVehiculo()) != null) {
            throw new ReglaNegocioException("No se puede inactivar un vehiculo que esta en el taller.");
        }
        dao.cambiarEstado(vehiculo.getIdVehiculo(), activar);
        Auditoria.registrar("Vehiculos", activar ? "Activar" : "Inactivar", "Vehiculo " + vehiculo.getPlaca());
    }
}
