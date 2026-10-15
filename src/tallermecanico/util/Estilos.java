package tallermecanico.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;

public class Estilos {

    public static final Color FONDO = new Color(18, 18, 18);
    public static final Color PANEL = new Color(28, 28, 28);
    public static final Color PANEL_CLARO = new Color(38, 38, 38);
    public static final Color BARRA = new Color(23, 23, 23);
    public static final Color BORDE = new Color(56, 56, 56);
    public static final Color NARANJA = new Color(232, 98, 44);
    public static final Color NARANJA_OSCURO = new Color(170, 66, 26);
    public static final Color TEXTO = new Color(236, 236, 236);
    public static final Color TEXTO_SUAVE = new Color(150, 150, 150);
    public static final Color VERDE = new Color(76, 175, 80);
    public static final Color ROJO = new Color(211, 76, 76);
    public static final Color AMARILLO = new Color(214, 168, 65);

    public static final Font TITULO = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font SUBTITULO = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font NEGRITA = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font NUMERO = new Font("Segoe UI", Font.BOLD, 26);

    public static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(TITULO);
        etiqueta.setForeground(TEXTO);
        return etiqueta;
    }

    public static JLabel subtitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(SUBTITULO);
        etiqueta.setForeground(TEXTO_SUAVE);
        return etiqueta;
    }

    public static JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(NORMAL);
        etiqueta.setForeground(TEXTO_SUAVE);
        return etiqueta;
    }

    public static JTextField campo() {
        JTextField campo = new JTextField();
        campo.setFont(NORMAL);
        campo.setBackground(PANEL_CLARO);
        campo.setForeground(TEXTO);
        campo.setCaretColor(NARANJA);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return campo;
    }

    public static void combo(JComboBox<?> combo) {
        combo.setFont(NORMAL);
        combo.setBackground(PANEL_CLARO);
        combo.setForeground(TEXTO);
        combo.setBorder(BorderFactory.createLineBorder(BORDE));
        combo.setPreferredSize(new Dimension(160, 30));
    }

    public static JButton boton(String texto, Color fondo) {
        JButton boton = new JButton(texto);
        boton.setFont(NEGRITA);
        boton.setBackground(fondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return boton;
    }

    public static JButton botonMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(NORMAL);
        boton.setBackground(BARRA);
        boton.setForeground(TEXTO_SUAVE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setHorizontalAlignment(JButton.LEFT);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setMaximumSize(new Dimension(230, 40));
        return boton;
    }

    public static void marcarMenuActivo(JButton boton, boolean activo) {
        if (activo) {
            boton.setBackground(PANEL_CLARO);
            boton.setForeground(NARANJA);
        } else {
            boton.setBackground(BARRA);
            boton.setForeground(TEXTO_SUAVE);
        }
    }

    public static JPanel panel() {
        JPanel panel = new JPanel();
        panel.setBackground(FONDO);
        return panel;
    }

    public static JPanel tarjeta() {
        JPanel panel = new JPanel();
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        return panel;
    }

    public static void tabla(JTable tabla) {
        tabla.setBackground(PANEL);
        tabla.setForeground(TEXTO);
        tabla.setFont(NORMAL);
        tabla.setRowHeight(28);
        tabla.setGridColor(BORDE);
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(NARANJA_OSCURO);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setBackground(PANEL_CLARO);
        encabezado.setForeground(TEXTO_SUAVE);
        encabezado.setFont(NEGRITA);
        encabezado.setReorderingAllowed(false);
        encabezado.setBorder(BorderFactory.createLineBorder(BORDE));
    }

    public static void scroll(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.getViewport().setBackground(PANEL);
        scroll.setBackground(FONDO);
    }
}
