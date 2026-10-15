package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import tallermecanico.controlador.ControlReportes;
import tallermecanico.controlador.ControlVehiculos;
import tallermecanico.modelo.Vehiculo;
import tallermecanico.reportes.ResultadoReporte;
import tallermecanico.util.Estilos;
import tallermecanico.util.Formato;

public class PanelReportes extends JPanel implements ActionListener, Refrescable {

    private static final String[] TITULOS = {"",
        "Ordenes por estado y taller",
        "Ingresos por periodo",
        "Productos con mayor movimiento",
        "Inventario por categoria y bajo minimo",
        "Historial de servicios por vehiculo",
        "Requerimientos por taller y estado",
        "Bitacora de operaciones"};

    private static final String[] DESCRIPCIONES = {"",
        "Resumen por estado y por taller, con el detalle de cada orden del periodo.",
        "Mano de obra, repuestos y total de las ordenes cerradas, agrupado por mes y taller.",
        "Los 10 productos con mas unidades despachadas por la bodega en el periodo.",
        "Unidades y valor del inventario por categoria, y productos que hay que pedir.",
        "Ficha del vehiculo con todas sus ordenes, servicios y repuestos. Reporte detallado.",
        "Quien solicito, quien autorizo y quien proceso cada salida de bodega.",
        "Registro de auditoria de las operaciones realizadas por cada usuario."};

    private ControlReportes control;
    private ControlVehiculos controlVehiculos;

    private JTextField txtDesde;
    private JTextField txtHasta;
    private JComboBox<Vehiculo> cboVehiculo;
    private JButton[] btnTabla;
    private JButton[] btnHtml;
    private JTextArea txtBitacora;

    public PanelReportes() {
        control = new ControlReportes();
        controlVehiculos = new ControlVehiculos();
        btnTabla = new JButton[TITULOS.length];
        btnHtml = new JButton[TITULOS.length];
        iniciarComponentes();
        refrescar();
    }

    private void iniciarComponentes() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Estilos.FONDO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtros.setBackground(Estilos.FONDO);
        txtDesde = Estilos.campo();
        txtHasta = Estilos.campo();
        txtDesde.setPreferredSize(new Dimension(100, 30));
        txtHasta.setPreferredSize(new Dimension(100, 30));
        txtDesde.setText(Formato.fecha(LocalDate.now().minusMonths(3).withDayOfMonth(1)));
        txtHasta.setText(Formato.fecha(LocalDate.now()));
        cboVehiculo = new JComboBox<>();
        Estilos.combo(cboVehiculo);
        cboVehiculo.setPreferredSize(new Dimension(260, 30));
        filtros.add(Estilos.etiqueta("Periodo desde (dd/mm/aaaa):"));
        filtros.add(txtDesde);
        filtros.add(Estilos.etiqueta("hasta:"));
        filtros.add(txtHasta);
        filtros.add(Estilos.etiqueta("    Vehiculo para el historial:"));
        filtros.add(cboVehiculo);

        JPanel tarjetas = new JPanel(new GridLayout(2, 4, 12, 12));
        tarjetas.setBackground(Estilos.FONDO);
        for (int i = 1; i < TITULOS.length; i++) {
            tarjetas.add(construirTarjeta(i));
        }
        tarjetas.add(construirNota());

        txtBitacora = new JTextArea(4, 20);
        txtBitacora.setEditable(false);
        txtBitacora.setBackground(Estilos.PANEL);
        txtBitacora.setForeground(Estilos.TEXTO_SUAVE);
        txtBitacora.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 12));
        txtBitacora.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        txtBitacora.setText("Los reportes se calculan con consultas a la base de datos. Los HTML se guardan en la "
                + "carpeta reportes del proyecto y se abren en el navegador.\n");
        JScrollPane scroll = new JScrollPane(txtBitacora);
        Estilos.scroll(scroll);

        add(filtros, BorderLayout.NORTH);
        add(tarjetas, BorderLayout.CENTER);
        add(scroll, BorderLayout.SOUTH);
    }

    private JPanel construirTarjeta(int numero) {
        JPanel tarjeta = Estilos.tarjeta();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));

        JLabel etiquetaNumero = new JLabel("REPORTE " + numero);
        etiquetaNumero.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        etiquetaNumero.setForeground(Estilos.NARANJA);
        JLabel etiquetaTitulo = new JLabel("<html>" + TITULOS[numero] + "</html>");
        etiquetaTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        etiquetaTitulo.setForeground(Estilos.TEXTO);

        JTextArea texto = new JTextArea(DESCRIPCIONES[numero]);
        texto.setWrapStyleWord(true);
        texto.setLineWrap(true);
        texto.setEditable(false);
        texto.setBackground(Estilos.PANEL);
        texto.setForeground(Estilos.TEXTO_SUAVE);
        texto.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        texto.setAlignmentX(JTextArea.LEFT_ALIGNMENT);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pie.setBackground(Estilos.PANEL);
        pie.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        pie.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        if (numero != 5) {
            btnTabla[numero] = Estilos.boton("Ver en tabla", Estilos.PANEL_CLARO);
            btnTabla[numero].addActionListener(this);
            pie.add(btnTabla[numero]);
        }
        btnHtml[numero] = Estilos.boton("Abrir HTML", Estilos.NARANJA);
        btnHtml[numero].addActionListener(this);
        pie.add(btnHtml[numero]);

        etiquetaNumero.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        etiquetaTitulo.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        tarjeta.add(etiquetaNumero);
        tarjeta.add(javax.swing.Box.createVerticalStrut(4));
        tarjeta.add(etiquetaTitulo);
        tarjeta.add(javax.swing.Box.createVerticalStrut(6));
        tarjeta.add(texto);
        tarjeta.add(javax.swing.Box.createVerticalGlue());
        tarjeta.add(pie);
        return tarjeta;
    }

    private JPanel construirNota() {
        JPanel nota = Estilos.tarjeta();
        nota.setLayout(new BorderLayout());
        JTextArea texto = new JTextArea("Ver en tabla muestra el resultado dentro del sistema en una tabla.\n\n"
                + "Abrir HTML genera un archivo con el reporte completo, listo para imprimir o entregar.\n\n"
                + "El periodo se aplica a todos los reportes excepto al inventario, que muestra las existencias actuales.");
        texto.setWrapStyleWord(true);
        texto.setLineWrap(true);
        texto.setEditable(false);
        texto.setBackground(Estilos.PANEL);
        texto.setForeground(Estilos.TEXTO_SUAVE);
        texto.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        nota.add(texto, BorderLayout.CENTER);
        return nota;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (int i = 1; i < TITULOS.length; i++) {
            if (e.getSource() == btnTabla[i] && btnTabla[i] != null) {
                verEnTabla(i);
                return;
            }
            if (e.getSource() == btnHtml[i]) {
                abrirHtml(i);
                return;
            }
        }
    }

    private void verEnTabla(int numero) {
        try {
            ResultadoReporte resultado = control.consulta(numero, txtDesde.getText(), txtHasta.getText());
            String subtitulo = numero == 4 ? "Existencias actuales"
                    : "Periodo del " + txtDesde.getText() + " al " + txtHasta.getText();
            DlgTablaReporte dialogo = new DlgTablaReporte((Frame) SwingUtilities.getWindowAncestor(this),
                    resultado, subtitulo);
            dialogo.setVisible(true);
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void abrirHtml(int numero) {
        try {
            File archivo = control.generarHtml(numero, txtDesde.getText(), txtHasta.getText(),
                    (Vehiculo) cboVehiculo.getSelectedItem());
            control.abrir(archivo);
            txtBitacora.append(archivo.getName() + "  ->  " + archivo.getAbsolutePath() + "\n");
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    @Override
    public final void refrescar() {
        try {
            Object actual = cboVehiculo.getSelectedItem();
            cboVehiculo.removeAllItems();
            ArrayList<Vehiculo> vehiculos = controlVehiculos.listar();
            for (int i = 0; i < vehiculos.size(); i++) {
                cboVehiculo.addItem(vehiculos.get(i));
            }
            if (actual != null) {
                cboVehiculo.setSelectedItem(actual);
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }
}
