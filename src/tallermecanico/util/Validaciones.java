package tallermecanico.util;

import tallermecanico.excepciones.DatoInvalidoException;

public class Validaciones {

    public static void obligatorio(String valor, String campo) throws DatoInvalidoException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatoInvalidoException("El campo " + campo + " es obligatorio.");
        }
    }

    public static void longitudMaxima(String valor, int maximo, String campo) throws DatoInvalidoException {
        if (valor != null && valor.trim().length() > maximo) {
            throw new DatoInvalidoException("El campo " + campo + " no puede tener mas de " + maximo + " caracteres.");
        }
    }

    public static void soloLetras(String valor, String campo) throws DatoInvalidoException {
        obligatorio(valor, campo);
        String texto = valor.trim();
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (!Character.isLetter(c) && c != ' ' && c != '\'') {
                throw new DatoInvalidoException("El campo " + campo + " solo puede contener letras.");
            }
        }
    }

    public static void validarDpi(String valor) throws DatoInvalidoException {
        obligatorio(valor, "DPI");
        String texto = valor.trim();
        if (texto.length() != 13) {
            throw new DatoInvalidoException("El DPI debe tener exactamente 13 digitos.");
        }
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) {
                throw new DatoInvalidoException("El DPI solo puede contener numeros.");
            }
        }
    }

    public static void validarTelefono(String valor) throws DatoInvalidoException {
        obligatorio(valor, "telefono");
        String texto = valor.trim();
        if (texto.length() != 8) {
            throw new DatoInvalidoException("El telefono debe tener 8 digitos.");
        }
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) {
                throw new DatoInvalidoException("El telefono solo puede contener numeros.");
            }
        }
    }

    public static void validarCorreo(String valor) throws DatoInvalidoException {
        obligatorio(valor, "correo");
        String texto = valor.trim();
        int arroba = texto.indexOf("@");
        int punto = texto.lastIndexOf(".");
        if (arroba < 1 || punto < arroba + 2 || punto == texto.length() - 1) {
            throw new DatoInvalidoException("El correo electronico no tiene un formato valido.");
        }
        if (texto.contains(" ")) {
            throw new DatoInvalidoException("El correo electronico no puede contener espacios.");
        }
    }

    public static void validarPlaca(String valor) throws DatoInvalidoException {
        obligatorio(valor, "placa");
        String texto = valor.trim();
        if (texto.length() < 6 || texto.length() > 10) {
            throw new DatoInvalidoException("La placa debe tener entre 6 y 10 caracteres.");
        }
    }

    public static int enteroPositivo(String valor, String campo) throws DatoInvalidoException {
        obligatorio(valor, campo);
        int numero;
        try {
            numero = Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El campo " + campo + " debe ser un numero entero.");
        }
        if (numero < 0) {
            throw new DatoInvalidoException("El campo " + campo + " no puede ser negativo.");
        }
        return numero;
    }

    public static int enteroMayorACero(String valor, String campo) throws DatoInvalidoException {
        int numero = enteroPositivo(valor, campo);
        if (numero == 0) {
            throw new DatoInvalidoException("El campo " + campo + " debe ser mayor a cero.");
        }
        return numero;
    }

    public static double decimalPositivo(String valor, String campo) throws DatoInvalidoException {
        obligatorio(valor, campo);
        double numero;
        try {
            numero = Double.parseDouble(valor.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El campo " + campo + " debe ser un numero.");
        }
        if (numero < 0) {
            throw new DatoInvalidoException("El campo " + campo + " no puede ser negativo.");
        }
        return numero;
    }

    public static void rangoStock(int minimo, int maximo) throws DatoInvalidoException {
        if (minimo > maximo) {
            throw new DatoInvalidoException("El stock minimo no puede ser mayor que el stock maximo.");
        }
    }

    public static int validarAnio(String valor) throws DatoInvalidoException {
        int anio = enteroPositivo(valor, "anio");
        int limite = java.time.LocalDate.now().getYear() + 1;
        if (anio < 1950 || anio > limite) {
            throw new DatoInvalidoException("El anio del vehiculo debe estar entre 1950 y " + limite + ".");
        }
        return anio;
    }

    public static void validarCodigo(String valor, String campo) throws DatoInvalidoException {
        obligatorio(valor, campo);
        String texto = valor.trim();
        if (texto.length() > 15) {
            throw new DatoInvalidoException("El " + campo + " no puede tener mas de 15 caracteres.");
        }
        if (texto.contains(" ")) {
            throw new DatoInvalidoException("El " + campo + " no puede contener espacios.");
        }
    }

    public static java.time.LocalDate fechaOpcional(String valor, String campo) throws DatoInvalidoException {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(valor.trim(), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (java.time.format.DateTimeParseException e) {
            throw new DatoInvalidoException("La " + campo + " debe tener el formato dd/mm/aaaa, por ejemplo 05/10/2026.");
        }
    }

    public static java.time.LocalDate fechaObligatoria(String valor, String campo) throws DatoInvalidoException {
        obligatorio(valor, campo);
        return fechaOpcional(valor, campo);
    }

    public static void rangoFechas(java.time.LocalDate desde, java.time.LocalDate hasta) throws DatoInvalidoException {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new DatoInvalidoException("La fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    public static void seleccionoElemento(Object seleccion, String campo) throws DatoInvalidoException {
        if (seleccion == null) {
            throw new DatoInvalidoException("Debe seleccionar " + campo + ".");
        }
    }

    public static void seleccionoFila(int fila, String modulo) throws DatoInvalidoException {
        if (fila < 0) {
            throw new DatoInvalidoException("Debe seleccionar un registro de la tabla de " + modulo + ".");
        }
    }
}
