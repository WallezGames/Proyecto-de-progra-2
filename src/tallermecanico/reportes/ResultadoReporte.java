package tallermecanico.reportes;

import java.util.ArrayList;

public class ResultadoReporte {

    public static final int TEXTO = 0;
    public static final int ENTERO = 1;
    public static final int MONEDA = 2;

    private String titulo;
    private String[] columnas;
    private int[] tipos;
    private ArrayList<Object[]> filas;

    public ResultadoReporte(String titulo, String[] columnas, int[] tipos) {
        this.titulo = titulo;
        this.columnas = columnas;
        this.tipos = tipos;
        this.filas = new ArrayList<>();
    }

    public String getTitulo() {
        return titulo;
    }

    public String[] getColumnas() {
        return columnas;
    }

    public int[] getTipos() {
        return tipos;
    }

    public ArrayList<Object[]> getFilas() {
        return filas;
    }

    public void agregarFila(Object[] fila) {
        filas.add(fila);
    }

    public boolean estaVacio() {
        return filas.isEmpty();
    }

    public double sumarColumna(int columna) {
        double suma = 0;
        for (int i = 0; i < filas.size(); i++) {
            Object valor = filas.get(i)[columna];
            if (valor instanceof Number) {
                suma = suma + ((Number) valor).doubleValue();
            }
        }
        return suma;
    }

    public String textoDe(int fila, int columna) {
        Object valor = filas.get(fila)[columna];
        if (valor == null) {
            return "";
        }
        if (tipos[columna] == MONEDA) {
            return String.format(java.util.Locale.US, "Q%,.2f", ((Number) valor).doubleValue());
        }
        if (tipos[columna] == ENTERO) {
            return String.valueOf(((Number) valor).longValue());
        }
        return valor.toString();
    }
}
