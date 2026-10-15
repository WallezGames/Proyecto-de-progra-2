package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import tallermecanico.controlador.ControlOrdenes;
import tallermecanico.controlador.ControlReportes;
import tallermecanico.modelo.OrdenTrabajo;
import tallermecanico.modelo.Requerimiento;
import tallermecanico.util.Estilos;
import tallermecanico.util.Formato;

public class PanelResumen extends JPanel implements Refrescable {

    private ControlOrdenes controlOrdenes;
    private ControlReportes controlReportes;

    private JLabel lblOrdenesActivas;
    private JLabel lblDetalleOrdenes;
    private JLabel lblIngresos;
    private JLabel lblDetalleIngresos;
    private JLabel lblVehiculos;
    private JLabel lblDetalleVehiculos;
    private JLabel lblRequerimientos;
    private JLabel lblDetalleRequerimientos;

    private DefaultTableModel modelo;

    public PanelResumen() {
        controlOrdenes = new ControlOrdenes();
        controlReportes = new ControlReportes();
        iniciarComponentes();
        refrescar();
    }

    private void iniciarComponentes() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Estilos.FONDO);

        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 16, 0));
        tarjetas.setBackground(Estilos.FONDO);
        tarjetas.setPreferredSize(new Dimension(0, 120));

        lblOrdenesActivas = new JLabel("0");
        lblDetalleOrdenes = new JLabel("");
        lblIngresos = new JLabel("Q0.00");
        lblDetalleIngresos = new JLabel("");
        lblVehiculos = new JLabel("0");
        lblDetalleVehiculos = new JLabel("");
        lblRequerimientos = new JLabel("0");
        lblDetalleRequerimientos = new JLabel("");

        tarjetas.add(construirTarjeta("ORDENES ACTIVAS", lblOrdenesActivas, lblDetalleOrdenes));
        tarjetas.add(construirTarjeta("INGRESOS DEL MES", lblIngresos, lblDetalleIngresos));
        tarjetas.add(construirTarjeta("VEHICULOS EN TALLER", lblVehiculos, lblDetalleVehiculos));
        tarjetas.add(construirTarjeta("REQUERIMIENTOS PENDIENTES", lblRequerimientos, lblDetalleRequerimientos));

        JPanel abajo = new JPanel(new BorderLayout(0, 10));
        abajo.setBackground(Estilos.FONDO);
        JLabel titulo = new JLabel("Ordenes activas");
        titulo.setFont(Estilos.NEGRITA);
        titulo.setForeground(Estilos.TEXTO);
        abajo.add(titulo, BorderLayout.NORTH);

        String[] columnas = {"Orden", "Vehiculo", "Cliente", "Motivo", "Taller", "Mecanico", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        Estilos.tabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        Estilos.scroll(scroll);
        abajo.add(scroll, BorderLayout.CENTER);

        add(tarjetas, BorderLayout.NORTH);
        add(abajo, BorderLayout.CENTER);
    }

    private JPanel construirTarjeta(String encabezado, JLabel valor, JLabel detalle) {
        JPanel tarjeta = Estilos.tarjeta();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel(encabezado);
        titulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        titulo.setForeground(Estilos.TEXTO_SUAVE);
        valor.setFont(Estilos.NUMERO);
        valor.setForeground(Estilos.TEXTO);
        detalle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        detalle.setForeground(Estilos.TEXTO_SUAVE);
        tarjeta.add(titulo);
        tarjeta.add(javax.swing.Box.createVerticalStrut(8));
        tarjeta.add(valor);
        tarjeta.add(javax.swing.Box.createVerticalStrut(6));
        tarjeta.add(detalle);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return tarjeta;
    }

    @Override
    public final void refrescar() {
        try {
            int pendientes = (int) controlReportes.ordenesEnEstado(OrdenTrabajo.PENDIENTE);
            int enProceso = (int) controlReportes.ordenesEnEstado(OrdenTrabajo.EN_PROCESO);
            lblOrdenesActivas.setText(String.valueOf(pendientes + enProceso));
            lblDetalleOrdenes.setText(enProceso + " en proceso, " + pendientes + " pendientes");

            lblIngresos.setText(Formato.quetzales(controlReportes.ingresosDelMes()));
            lblDetalleIngresos.setText((int) controlReportes.ordenesCerradasDelMes() + " orden(es) cerradas este mes");

            lblVehiculos.setText(String.valueOf((int) controlReportes.vehiculosEnTaller()));
            lblDetalleVehiculos.setText("De " + (int) controlReportes.totalVehiculos() + " vehiculos activos");

            int solicitados = (int) controlReportes.requerimientosEnEstado(Requerimiento.SOLICITADO);
            int autorizados = (int) controlReportes.requerimientosEnEstado(Requerimiento.AUTORIZADO);
            int bajos = (int) controlReportes.productosBajoMinimo();
            lblRequerimientos.setText(String.valueOf(solicitados + autorizados));
            lblDetalleRequerimientos.setText(solicitados + " por autorizar, " + autorizados + " por procesar"
                    + (bajos > 0 ? "  |  " + bajos + " producto(s) bajo minimo" : ""));

            modelo.setRowCount(0);
            ArrayList<OrdenTrabajo> activas = controlOrdenes.listarActivas();
            for (int i = 0; i < activas.size(); i++) {
                OrdenTrabajo o = activas.get(i);
                modelo.addRow(new Object[]{o.getNumero(), o.getVehiculo().getDescripcion() + " (" + o.getVehiculo().getPlaca() + ")",
                    o.getVehiculo().getPropietario().obtenerNombreCompleto(), o.getDescripcion(),
                    o.getTaller().getNombreTaller(), o.nombresMecanicos(), o.getEstado()});
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }
}
