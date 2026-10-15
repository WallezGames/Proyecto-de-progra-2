package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import tallermecanico.reportes.ResultadoReporte;
import tallermecanico.util.Estilos;

public class DlgTablaReporte extends JDialog {

    public DlgTablaReporte(Frame padre, ResultadoReporte resultado, String subtitulo) {
        super(padre, resultado.getTitulo(), true);
        getContentPane().setBackground(Estilos.FONDO);
        setLayout(new BorderLayout(0, 10));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Estilos.FONDO);
        cabecera.setBorder(BorderFactory.createEmptyBorder(14, 16, 0, 16));
        cabecera.add(Estilos.titulo(resultado.getTitulo()), BorderLayout.NORTH);
        cabecera.add(Estilos.subtitulo(subtitulo + "   |   " + resultado.getFilas().size() + " fila(s)"), BorderLayout.SOUTH);

        DefaultTableModel modelo = new DefaultTableModel(resultado.getColumnas(), 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (int f = 0; f < resultado.getFilas().size(); f++) {
            Object[] fila = new Object[resultado.getColumnas().length];
            for (int c = 0; c < fila.length; c++) {
                fila[c] = resultado.textoDe(f, c);
            }
            modelo.addRow(fila);
        }
        JTable tabla = new JTable(modelo);
        Estilos.tabla(tabla);
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c = 0; c < resultado.getTipos().length; c++) {
            if (resultado.getTipos()[c] != ResultadoReporte.TEXTO) {
                tabla.getColumnModel().getColumn(c).setCellRenderer(derecha);
            }
        }
        JScrollPane scroll = new JScrollPane(tabla);
        Estilos.scroll(scroll);
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(Estilos.FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        centro.add(scroll, BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pie.setBackground(Estilos.FONDO);
        for (int c = 0; c < resultado.getTipos().length; c++) {
            if (resultado.getTipos()[c] == ResultadoReporte.MONEDA) {
                JLabel total = new JLabel(resultado.getColumnas()[c] + ": " + String.format(java.util.Locale.US, "Q%,.2f",
                        resultado.sumarColumna(c)));
                total.setFont(Estilos.NEGRITA);
                total.setForeground(Estilos.NARANJA);
                pie.add(total);
            }
        }
        JButton cerrar = Estilos.boton("Cerrar", Estilos.PANEL_CLARO);
        cerrar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });
        pie.add(cerrar);

        add(cabecera, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
        setSize(1050, 520);
        setLocationRelativeTo(padre);
    }
}
