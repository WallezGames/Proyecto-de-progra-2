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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import tallermecanico.controlador.ControlCatalogos;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Categoria;
import tallermecanico.util.Estilos;

public class PanelCategorias extends JPanel implements ActionListener, Refrescable {

    private ControlCatalogos control;
    private ArrayList<Categoria> listaActual;
    private Categoria seleccionada;

    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnEliminar;

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelCategorias() {
        control = new ControlCatalogos();
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
        formulario.add(Estilos.etiqueta("Nombre de la categoria"));
        formulario.add(txtNombre);
        formulario.add(Estilos.etiqueta("Descripcion"));
        formulario.add(txtDescripcion);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBackground(Estilos.FONDO);
        txtBuscar = Estilos.campo();
        txtBuscar.setPreferredSize(new java.awt.Dimension(260, 30));
        btnBuscar = Estilos.boton("Buscar", Estilos.NARANJA);
        btnBuscar.addActionListener(this);
        txtBuscar.addActionListener(this);
        busqueda.add(Estilos.etiqueta("Buscar por nombre o descripcion:"));
        busqueda.add(txtBuscar);
        busqueda.add(btnBuscar);

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setBackground(Estilos.FONDO);
        arriba.add(formulario, BorderLayout.CENTER);
        arriba.add(busqueda, BorderLayout.SOUTH);

        String[] columnas = {"ID", "Categoria", "Descripcion", "Productos"};
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
        btnEliminar = Estilos.boton("Eliminar", Estilos.ROJO);
        JButton[] lista = {btnNuevo, btnGuardar, btnModificar, btnEliminar};
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
                control.agregarCategoria(txtNombre.getText(), txtDescripcion.getText());
                Mensajes.exito(this, "La categoria se registro correctamente.", "Registro guardado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnModificar) {
                requiereSeleccion();
                control.modificarCategoria(seleccionada, txtNombre.getText(), txtDescripcion.getText());
                Mensajes.exito(this, "La categoria se actualizo correctamente.", "Registro modificado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEliminar) {
                requiereSeleccion();
                if (Mensajes.confirmar(this, "Desea eliminar la categoria " + seleccionada.getNombre() + "?",
                        "Confirmar eliminacion")) {
                    control.eliminarCategoria(seleccionada);
                    Mensajes.exito(this, "La categoria se elimino.", "Registro eliminado");
                    limpiar();
                    refrescar();
                }
            } else if (e.getSource() == btnBuscar || e.getSource() == txtBuscar) {
                cargarTabla(control.buscarCategorias(txtBuscar.getText()));
            } else if (e.getSource() == btnNuevo) {
                limpiar();
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void requiereSeleccion() throws DatoInvalidoException {
        if (seleccionada == null) {
            throw new DatoInvalidoException("Debe seleccionar una categoria de la tabla.");
        }
    }

    @Override
    public final void refrescar() {
        try {
            cargarTabla(control.listarCategorias());
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void cargarTabla(ArrayList<Categoria> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Categoria c = lista.get(i);
            modelo.addRow(new Object[]{c.getIdCategoria(), c.getNombre(), c.getDescripcion(), c.getTotalProductos()});
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        seleccionada = listaActual.get(fila);
        txtNombre.setText(seleccionada.getNombre());
        txtDescripcion.setText(seleccionada.getDescripcion() == null ? "" : seleccionada.getDescripcion());
    }

    private void limpiar() {
        seleccionada = null;
        txtNombre.setText("");
        txtDescripcion.setText("");
        tabla.clearSelection();
    }
}
