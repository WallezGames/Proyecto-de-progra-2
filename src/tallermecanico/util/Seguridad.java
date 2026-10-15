package tallermecanico.util;

import java.security.MessageDigest;

public class Seguridad {

    public static String encriptar(String texto) {
        if (texto == null) {
            texto = "";
        }
        try {
            MessageDigest algoritmo = MessageDigest.getInstance("SHA-256");
            byte[] resumen = algoritmo.digest(texto.getBytes("UTF-8"));
            StringBuilder salida = new StringBuilder();
            for (int i = 0; i < resumen.length; i++) {
                String parte = Integer.toHexString(0xff & resumen[i]);
                if (parte.length() == 1) {
                    salida.append("0");
                }
                salida.append(parte);
            }
            return salida.toString();
        } catch (Exception e) {
            return texto;
        }
    }
}
