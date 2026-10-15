package tallermecanico.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public static String fecha(LocalDate valor) {
        if (valor == null) {
            return "";
        }
        return valor.format(FECHA);
    }

    public static String hora(LocalTime valor) {
        if (valor == null) {
            return "";
        }
        return valor.format(HORA);
    }

    public static LocalDate aFecha(String texto) {
        return LocalDate.parse(texto, FECHA);
    }

    public static String quetzales(double valor) {
        return String.format(java.util.Locale.US, "Q%,.2f", valor);
    }

    public static String decimal(double valor) {
        return String.format(java.util.Locale.US, "%.2f", valor);
    }
}
