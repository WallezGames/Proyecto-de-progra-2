package tallermecanico.dao;

import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;

public interface CrudDAO<T> {

    int insertar(T objeto) throws BaseDatosException;

    void actualizar(T objeto) throws BaseDatosException;

    void eliminar(int id) throws BaseDatosException;

    T buscarPorId(int id) throws BaseDatosException;

    ArrayList<T> listar() throws BaseDatosException;
}
