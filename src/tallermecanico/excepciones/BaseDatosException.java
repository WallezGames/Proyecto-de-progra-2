package tallermecanico.excepciones;

public class BaseDatosException extends Exception {

    public BaseDatosException(String mensaje) {
        super(mensaje);
    }

    public BaseDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
