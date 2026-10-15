package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import tallermecanico.controlador.ControlCatalogos;
import tallermecanico.controlador.ControlTalleres;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Servicio;
import tallermecanico.modelo.Taller;
import tallermecanico.util.Estilos;
import tallermecanico.util.Formato;

public class PanelServicios extends JPanel implements ActionListener, Refrescable {

    private ControlCatalogos control;
    private ControlTalleres controlTalleres;
    private ArrayList<Servicio> listaActual;
    private Servicio seleccionado;

    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JComboBox<Taller> cboTaller;
    private JTextField txtPrecio;

    private JComboBox<String> cboCriterio;
    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnEstado;
    private JButton btnEliminar;

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelServicios() {
        control = new ControlCatalogos();
        controlTalleres = new ControlTalleres();
        listaActual = new ArrayList<>();
        iniciarComponentes();
        refrescar();
    }

    private void iniciarComponentes() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel formulario = new JPanel(new GridLayout(0, 4, 12, 8));
        formulario.setBackground(Estilos.PANEL);
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        txtNombre = Estilos.campo();
        txtDescripcion = Estilos.campo();
        cboTaller = new JComboBox<>();
        Estilos.combo(cboTaller);
        txtPrecio = Estilos.campo();
        formulario.add(Estilos.etiqueta("Nombre del servicio"));
        formulario.add(txtNombre);
        formulario.add(Estilos.etiqueta("Taller que lo presta"));
        formulario.add(cboTaller);
        formulario.add(Estilos.etiqueta("Descripcion"));
        formulario.add(txtDescripcion);
        formulario.add(Estilos.etiqueta("Precio (mano de obra Q)"));
        formulario.add(txtPrecio);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBackground(Estilos.FONDO);
        cboCriterio = new JComboBox<>(new String[]{"Todos", "Nombre", "Taller"});
        Estilos.combo(cboCriterio);
        txtBuscar = Estilos.campo();
        txtBuscar.setPreferredSize(new java.awt.Dimension(260, 30));
        btnBuscar = Estilos.boton("Buscar", Estilos.NARANJA);
        btnBuscar.addActionListener(this);
        txtBuscar.addActionListener(this);
        busqueda.add(Estilos.etiqueta("Buscar por:"));
        busqueda.add(cboCriterio);
        busqueda.add(txtBuscar);
        busqueda.add(btnBuscar);

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setBackground(Estilos.FONDO);
        arriba.add(formulario, BorderLayout.CENTER);
        arriba.add(busqueda, BorderLayout.SOUTH);

        String[] columnas = {"ID", "Servicio", "Descripcion", "Taller", "Precio", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        Estilos.tabla(tabla);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarFila();
            }
        });
        JScrollPane scroll = new JScrollPane(tabla);
        Estilos.scroll(scroll);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        botones.setBackground(Estilos.FONDO);
        btnNuevo = Estilos.boton("Nuevo", Estilos.PANEL_CLARO);
        btnGuardar = Estilos.boton("Guardar", Estilos.VERDE);
        btnModificar = Estilos.boton("Modificar", Estilos.NARANJA);
        btnEstado = Estilos.boton("Activar/Inactivar", Estilos.AMARILLO);
        btnEliminar = Estilos.boton("Eliminar", Estilos.ROJO);
        JButton[] lista = {btnNuevo, btnGuardar, btnModificar, btnEstado, btnEliminar};
        for (int i = 0; i < lista.length; i++) {
            lista[i].addActionListener(this);
            botones.add(lista[i]);
        }

        add(arriba, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == btnGuardar) {
                control.agregarServicio(txtNombre.getText(), txtDescripcion.getText(),
                        (Taller) cboTaller.getSelectedItem(), txtPrecio.getText());
                Mensajes.exito(this, "El servicio se registro correctamente.", "Registro guardado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnModificar) {
                requiereSeleccion();
                control.modificarServicio(seleccionado, txtNombre.getText(), txtDescripcion.getText(),
                        (Taller) cboTaller.getSelectedItem(), txtPrecio.getText());
                Mensajes.exito(this, "El servicio se actualizo correctamente.", "Registro modificado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEstado) {
                requiereSeleccion();
                control.cambiarEstadoServicio(seleccionado);
                Mensajes.exito(this, "El servicio quedo " + (seleccionado.isEstado() ? "activo." : "inactivo."),
                        "Estado actualizado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEliminar) {
                requiereSeleccion();
                if (Mensajes.confirmar(this, "Desea eliminar el servicio " + seleccionado.getNombre() + "?",
                        "Confirmar eliminacion")) {
                    control.eliminarServicio(seleccionado);
                    Mensajes.exito(this, "El servicio se elimino.", "Registro eliminado");
                    limpiar();
                    refrescar();
                }
            } else if (e.getSource() == btnBuscar || e.getSource() == txtBuscar) {
                cargarTabla(control.buscarServicios(cboCriterio.getSelectedItem().toString(), txtBuscar.getText()));
            } else if (e.getSource() == btnNuevo) {
                limpiar();
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void requiereSeleccion() throws DatoInvalidoException {
        if (seleccionado == null) {
            throw new DatoInvalidoException("Debe seleccionar un servicio de la tabla.");
        }
    }

    @Override
    public final void refrescar() {
        try {
            Object actual = cboTaller.getSelectedItem();
            cboTaller.removeAllItems();
            ArrayList<Taller> talleres = controlTalleres.listarActivos();
            for (int i = 0; i < talleres.size(); i++) {
                cboTaller.addItem(talleres.get(i));
            }
            if (actual != null) {
                cboTaller.setSelectedItem(actual);
            }
            cargarTabla(control.listarServicios());
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void cargarTabla(ArrayList<Servicio> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Servicio s = lista.get(i);
            modelo.addRow(new Object[]{s.getIdServicio(), s.getNombre(), s.getDescripcion(),
                s.getTaller().getNombreTaller(), Formato.quetzales(s.getPrecio()), s.isEstado() ? "Activo" : "Inactivo"});
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        seleccionado = listaActual.get(fila);
        txtNombre.setText(seleccionado.getNombre());
        txtDescripcion.setText(seleccionado.getDescripcion() == null ? "" : seleccionado.getDescripcion());
        cboTaller.setSelectedItem(seleccionado.getTaller());
        txtPrecio.setText(Formato.decimal(seleccionado.getPrecio()));
    }

    private void limpiar() {
        seleccionado = null;
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        tabla.clearSelection();
    }
}
